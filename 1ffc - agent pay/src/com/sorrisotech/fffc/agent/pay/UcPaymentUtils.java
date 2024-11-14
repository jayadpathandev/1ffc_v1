/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

public class UcPaymentUtils {
	
	// ************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(UcPaymentUtils.class);
	
	// ************************************************************************
	final static ApiPayDao mDao = Fffc.apiPay();
	
	// *************************************************************************
	// The pay date.
	private Calendar mDate = null;
	
	// *************************************************************************
	// The amount.
	private BigDecimal mAmount = null;
	
	/************************************************************************************************
	 * This method can be used to fetch userId using customerId.
	 * 
	 * @param szCustomerId The customerId
	 * 
	 * @return The userId in string format.
	 * 
	 */
	public static String getUserIdFromCustomerId(
	        final IServiceLocator2 services,
	        final String szCustomerId) {
		
		String szUserId = "";
		
		try {
			
			LOG.debug(
			        "UcPaymentUtils:getUserIdFromCustomerId() ..... entered method for customer id: {}",
			        szCustomerId);
			
			// ************************************************************************************
			// Checking for existence of user.
			final var user = mDao.user(szCustomerId);
			
			// ************************************************************************************
			// If user not null returning userId.
			if (null != user) {
				szUserId = user.userId.toString();
			}
			
		} catch (Exception e) {
			szUserId = "";
			LOG.error(
			        "UcPaymentUtils:getUserIdFromCustomerId() ..... failed to check existance of user for customer id: {}",
			        szCustomerId, e, e);
		}
		return szUserId;
	}
	
	/************************************************************************************************
	 * This method can be used to validate the payAmount and payDate.
	 * 
	 * @param date   The pay date.
	 * @param amount The pay amount.
	 * 
	 * @return The status of validation.
	 */
	public String validateDateAndAmount(
	        final IUserData data,
	        final String date,
	        final String amount) {
		
		final var today = Calendar.getInstance();
		
		if (date.equalsIgnoreCase("today")) {
			return "invalid_date";
		} else {
			final var format = new SimpleDateFormat("yyyy/MM/dd");
			try {
				mDate = Calendar.getInstance();
				mDate.setTime(format.parse(date));
			} catch (ParseException e) {
				LOG.error("Could not parse date [" + date + "].");
				return "invalid_date";
			}
		}
		// ---------------------------------------------------------------------
		final var toLong   = new SimpleDateFormat("yyyyMMdd");
		final var todayNum = Long.parseLong(toLong.format(today.getTime()));
		final var payNum   = Long.parseLong(toLong.format(mDate.getTime()));
		
		if (payNum <= todayNum) {
			return "invalid_date";
		}
		
		try {
			mAmount = new BigDecimal(amount);
		} catch (NumberFormatException e) {
			LOG.error("Could not parse amount [" + amount + "].");
			return "invalid_amount";
		}
		
		if (mAmount.compareTo(BigDecimal.ZERO) <= 0 || mAmount.scale() > 2) {
			LOG.error("Invalid amount amount [" + amount + "].");
			return "invalid_amount";
		}
		
		return "success";
	}
	
	public void payDate(final IStringData value) throws MargaritaDataException {
		if (mDate == null)
			throw new RuntimeException("No date set.");
		final var f = new SimpleDateFormat("yyyy-MM-dd");
		value.putValue(f.format(mDate.getTime()));
	}
	
	public void amount(final IStringData value) throws MargaritaDataException {
		if (mAmount == null)
			throw new RuntimeException("No amount set.");
		value.putValue(mAmount.toString());
	}
	
	/**************************************************************************
	 * Checks if the wallet Expire before payment
	 * 
	 * @param szSourceExpiry
	 * @return "true", "false" or "error"
	 */
	public String willWalletExpireBeforePayDate(final String szSourceExpiry) {
		String szResult = "false";
		
		if (szSourceExpiry.isEmpty()) return szResult;
		
		try {
			if (szSourceExpiry != null && !szSourceExpiry.isEmpty()) {
				SimpleDateFormat cFormat = new SimpleDateFormat("MM/yyyy");
				cFormat.setLenient(false); // for strict date formatting
				
				Date cExpDate = cFormat.parse(szSourceExpiry);
				Calendar cExpCal = Calendar.getInstance();
				cExpCal.setTime(cExpDate);
				cExpCal.set(Calendar.DAY_OF_MONTH, cExpCal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Set to the end of the month

				Calendar cPayDateCal = this.mDate;
				
				// Wallet expires before the payment date
				if (cExpCal.before(cPayDateCal)) {
	                szResult = "true"; 
	            }
				
			}
		} catch (Exception e) {
			LOG.error("UcPaymentUtils.....willWalletExpireBeforePayDate()...An exception was thrown", e);
			szResult = "error";
		}
		
		return szResult;
	}
}
