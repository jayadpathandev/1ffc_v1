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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.billutils.BillCache;
import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.persona.bill.api.IBillInfo;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;

/*********************************************************************************
* This class gets information from BillInfo and return the value in Flex field as
* an amount to be displayed.
* 
* @author Yvette Nguyen, updated by John Kowalonek
* @version 2023-Dec-01
*/
public class FlexFieldInformation {
	
	private static final Logger m_cLog = LoggerFactory.getLogger(FlexFieldInformation.class);
	
	IBillInfo cBillInfo = null;	
	
	/**
	 * Method to return a specified flex field as an amount.  Note that the Item number is passed in 
	 * as a 1 based flex field i.e. starting with flex1... flexn
	 * @param locator
	 * @param cUserData
	 * @param szUserId
	 * @param szBillingAccountNumber
	 * @param szBillingPeriod
	 * @param szPaymentGroup
	 * @param ciItemNumber
	 * @param cszErrorValue
	 * @return flex field formatted as a string with currency amount or cszErrorValue.
	 */
	public String getFlexFieldAsAmount(
							IServiceLocator2 	locator,
							IUserData cUserData, 
							String szUserId, 
							String szBillingAccountNumber, 
							String szBillingPeriod, 
							String szPaymentGroup,
							String cszItemNumber,
							String cszErrorValue) {
		
    	String szResult = cszErrorValue;
    	
    	try {
        	Integer iItemNumber = Integer.valueOf(cszItemNumber) -1;
    		
    		if (cBillInfo == null)
				cBillInfo = cUserData.getJavaObj(BillCache.class).getBillInfoFromCache(
						szUserId, 
						szBillingAccountNumber, 
						szBillingPeriod, 
						szPaymentGroup);			

			if (cBillInfo != null) {
				final LocalizedFormat format = new LocalizedFormat(locator, cUserData.getLocale());
				String sxFlexFieldReturned = cBillInfo.getFlex()[iItemNumber];				
				
				if(sxFlexFieldReturned!= null && !sxFlexFieldReturned.isEmpty()) {
					return(format.formatAmount(szPaymentGroup, new BigDecimal(sxFlexFieldReturned)));
				}
			}
			
		} catch (Throwable e) {
			m_cLog.debug("getFlexFieldAsAmount: error in retrieval or conversion see trace", e);
		}
    	
    	return szResult;
				
	}
}
