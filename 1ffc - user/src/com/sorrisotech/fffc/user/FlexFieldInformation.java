/* (c) Copyright 2017-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
 * +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona 
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition", 
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network", 
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay", 
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active 
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.user;

import java.math.BigDecimal;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.billutils.BillCache;
import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.persona.bill.api.IBillInfo;
import com.sorrisotech.persona.bill.api.exception.BillDbException;
import com.sorrisotech.persona.bill.api.exception.NoBillDataFoundException;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

/*********************************************************************************
* This class gets information from BillInfo and return the value in Flex field as
* an amount to be displayed.
* 
* @author Yvette Nguyen, updated by John Kowalonek
* @since 2023-Dec-01
* @version 2024-Feb-06 repair it so that it uses IExternalReuse and add method to get
* 			the value raw as it comes from the database. Finally refactor for  
* 			common internal function to get IBillInfo interface.
*/
public class FlexFieldInformation implements IExternalReuse{
	
	private static final long serialVersionUID = -2037931091421584381L;
	private static final Logger m_cLog = LoggerFactory.getLogger(FlexFieldInformation.class);
	
	/**
	 * Method to return a specified flex field as an amount.  Note that the Item number is passed in 
	 * as a 1 based flex field i.e. starting with flex1... flexn
	 * @param locator
	 * @param cUserData
	 * @param cszUserId
	 * @param cszBillingAccountNumber
	 * @param cszBillingPeriod
	 * @param cszPaymentGroup
	 * @param cszItemNumber
	 * @param cszErrorValue
	 * @return flex field formatted as a string with currency amount or cszErrorValue.
	 */
	public String getFlexFieldFormattedAsCurrency (
							IServiceLocator2 	locator,
							IUserData cUserData, 
							String cszUserId, 
							String cszBillingAccountNumber, 
							String cszBillingPeriod, 
							String cszPaymentGroup,
							String cszItemNumber,
							String cszErrorValue) {
		
 		String szResult = cszErrorValue;
		Integer iItemNumber = Integer.valueOf(cszItemNumber) -1;
		String lszInterimResult = null;
    	IBillInfo localBillInfo = getBillInfo(	cUserData,
    											cszUserId,
    											cszBillingAccountNumber,
    											cszBillingPeriod,
    											cszPaymentGroup);	

		if (localBillInfo != null) {
			try {
				LocalizedFormat format = new LocalizedFormat(locator, cUserData.getLocale());
				lszInterimResult = localBillInfo.getFlex()[iItemNumber];				
				BigDecimal ldResultAsNumber = new BigDecimal(lszInterimResult);
				ldResultAsNumber.setScale(2);
				szResult = format.formatAmount(cszPaymentGroup, ldResultAsNumber);
			} catch (MargaritaDataException e) {
				m_cLog.error("getFlexFieldAsAmount - Exception localizing number as amount value = {}, see stack trace", 
										lszInterimResult, e);
				e.printStackTrace();
			} catch (NumberFormatException e) {
				m_cLog.error("getFlexFieldAsAmount - Exception converting flex field to flex value = (), see stack trace", 
										lszInterimResult, e);
				e.printStackTrace();
			}
		} 
    	return szResult;
	}

	/**
	 * Returns flex field as string just as stored in the database.
	 * 
	 * @param locator
	 * @param cUserData
	 * @param cszUserId
	 * @param cszBillingAccountNumber
	 * @param cszBillingPeriod
	 * @param cszPaymentGroup
	 * @param cszItemNumber
	 * @param cszErrorValue
	 * @return flex field as a string or cszErrorValue if there is an error
	 */
	public String getFlexFieldRaw(
			final IServiceLocator2 	locator,
			final IUserData cUserData, 
			final String cszUserId, 
			final String cszBillingAccountNumber, 
			final String cszBillingPeriod, 
			final String cszPaymentGroup,
			final String cszItemNumber,
			final String cszErrorValue) {

		String szResult = cszErrorValue;
		Integer iItemNumber = Integer.valueOf(cszItemNumber) -1;
    	IBillInfo localBillInfo = getBillInfo(	cUserData,
    											cszUserId,
    											cszBillingAccountNumber,
    											cszBillingPeriod,
    											cszPaymentGroup);	

		if (localBillInfo != null) {
			szResult = localBillInfo.getFlex()[iItemNumber];				
		}

		return szResult;
	}
	
	/**
	 * returns the billing information for use internally
	 * 
	 * @param cUserData
	 * @param cszUserId
	 * @param cszBillingAccountNumber
	 * @param cszBillingPeriod
	 * @param cszPaymentGroup
	 * @return
	 */
	private IBillInfo getBillInfo (	final IUserData cUserData, 
								final String cszUserId, 
								final String cszBillingAccountNumber, 
								final String cszBillingPeriod, 
								final String cszPaymentGroup) {
		IBillInfo localBillInfo = null;
		try {
			localBillInfo = cUserData.getJavaObj(BillCache.class).getBillInfoFromCache(
						cszUserId, 
						cszBillingAccountNumber, 
						cszBillingPeriod, 
						cszPaymentGroup);
		} catch (NoBillDataFoundException e) {
			m_cLog.error("getFlexFieldAsAmount - NoBillDataFound exception, see stack trace", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			m_cLog.error("getFlexFieldAsAmount - ClassNotFound exception, BillCache, see stack trace", e);
			e.printStackTrace();
		} catch (BillDbException e) {
			m_cLog.error("getFlexFieldAsAmount - BillDb exception, see stack trace", e);
			e.printStackTrace();
		} catch (ParseException e) {
			m_cLog.error("getFlexFieldAsAmount - Parse exception, see stack trace", e);
			e.printStackTrace();
		}			
		
		return localBillInfo;
	}	
	
	@Override
	public int getReuse() {
		return IExternalReuse.REUSE_USECASE;
	}
}
