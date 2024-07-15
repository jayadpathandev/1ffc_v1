package com.sorrisotech.fffc.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.INumberData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

/**
 * Class provides method to calculate current balance based on 
 *   combination of bill, status feed, and payments.
 *   
 * @author johnK
 * @since  2024-Jan-31
 * @version 2024-Jan-31 jak Hopefully works right away
 * @version 2024-Feb-07 jak Added min/max and several minor defect fixes around that.
 * @version 2024-Feb-18 jak Added some checks around bill vs status comparison to prevent
 * 																exceptions should one be empty.
 * @version 2024-May-07 jak Forced max payment to zero is it was less than zero.
 * @version 2024-Jul-13 jak Remove bill as a current balance component and adjust calculations
 * 								based on the way current balance is received.
 * 
 */
public class BalanceHelper extends FffcBalance {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(BalanceHelper.class);

	/**
	 * Calculates the current balance based on information from the bill,
	 * the status feed, and payment history.
	 * 
	 * @param locator,
	 * @param data,
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	public static String getCurrentBalanceFormattedAsCurrency(
								    IServiceLocator2 	locator, 
									IUserData           data,
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszStatusTime,
									final String	 	cszStatusBalance ) {

		String lszRetVal = null;
		BigDecimal ldCurBalance = getCurrentBalance (
													cszPayGroup,
													cszIntAccount,
													cszStatusTime,
													cszStatusBalance);
		
		try {
			final LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
			
			lszRetVal = format.formatAmount(cszPayGroup, ldCurBalance);
			
		} catch (Exception e) {
			m_cLog.error("BalanceHelper:getCurrentBalanceFormattedAsCurrency - An exception was thrown", e);
		}

		return lszRetVal ;
	}
	
	/**
	 * returns the true minimum due based on the status feed and the calculated minimum due
	 * 
	 * @param locator
	 * @param data
	 * @param cszStatusMinimumPaymentAmt
	 * @param cszCurrentBalanceAmt
	 * @param oMinDueVariable
	 * @return minimum due as a number
	 */
	public static void getTrueMinimumDueNumber (
											final String 	cszStatusMinimumPaymentAmt,
											final String 	cszCurrentBalanceAmt,
											INumberData		oMinDueVariable) {
		String lszRetValue = null;

		lszRetValue = getTrueMinimumDueRaw (cszStatusMinimumPaymentAmt,
											cszCurrentBalanceAmt);
		if (lszRetValue == m_cszReturnForInvalid) {
			lszRetValue = "0.00";
		}
		try {
			oMinDueVariable.setValue(lszRetValue);
		} catch (MargaritaDataException e) {
			m_cLog.error("BalanceHelper:getTrueMinimumDueNumber - An exception was thrown", e);
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Returns the true minimum payment due (based on payments) formatted in localized
	 * currency
	 * 
	 * @param locator
	 * @param data
	 * @param cszPayGroup
	 * @param cszStatusMinimumPaymentAmt
	 * @param cszCurrentBalanceAmt
	 * @param oMinDueVariable
	 * @return minimum due formatted as a localized currency
	 */
	public static String getTrueMinimumDueFormattedAsCurrency (
											IServiceLocator2 	locator, 
											IUserData    		data,
											final String 		cszPayGroup,
											final String 		cszStatusMinimumPaymentAmt,
											final String 		cszCurrentBalanceAmt) {
		String lszRetValue = null;
		String lszInternalUnformatted = null;

		lszInternalUnformatted = getTrueMinimumDueRaw (cszStatusMinimumPaymentAmt,
				 cszCurrentBalanceAmt);
		
		if (lszInternalUnformatted == m_cszReturnForInvalid) {
			lszInternalUnformatted = "0.00";
		}
			
		if (null != lszInternalUnformatted) {
			try {
				final LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
				BigDecimal ldMinimumDue = new BigDecimal(lszInternalUnformatted);
				ldMinimumDue.setScale(2);
				lszRetValue = " " + format.formatAmount(cszPayGroup, ldMinimumDue);
				
			} catch (Exception e) {
				m_cLog.error("BalanceHelper:getTrueMinimumDueFormattedAsCurrency - An exception was thrown", e);
				e.printStackTrace();
			}
		}
		
		return lszRetValue;
	}
	
	/**
	 * Returns the maximum payment allowed based on billing information and payments formatted
	 * as a localized currency.
	 * 
	 * @param locator
	 * @param data
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszMaxTimeStamp
	 * @param cszFileMaxDue
	 * @return maximum payment allowed formatted as a localized currency
	 */
	public static String getTrueMaximumPayFormattedAsCurrency
										(	IServiceLocator2 	locator, 
											IUserData    		data,
											final String 		cszPayGroup,
											final String 		cszIntAccount,
											final String 		cszMaxTimeStamp, 
											final String 		cszFileMaxDue) {
		String lszRetValue = null;
		BigDecimal ldTrueMaxPaymentAmount = BigDecimal.ZERO;

		// -- gets the true maximum payment --
		ldTrueMaxPaymentAmount = getTrueMaximumPay(cszPayGroup, 
												   cszIntAccount,
												   cszMaxTimeStamp,
												   cszFileMaxDue);

		// -- If not greater than zero force return to be zero 
		//		this only happens if we've received bad status data --
		if (1 != ldTrueMaxPaymentAmount.compareTo(BigDecimal.ZERO)) {
			ldTrueMaxPaymentAmount = BigDecimal.ZERO;
		}

		// -- format it for return --
		try {
				LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
				ldTrueMaxPaymentAmount.setScale(2);
				lszRetValue = " " + format.formatAmount(cszPayGroup, ldTrueMaxPaymentAmount);
			} catch (MargaritaDataException e) {
				m_cLog.error("BalanceHelper:getTrueMaximumPayFormattedAsCurrency - An exception was thrown", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_cszReturnForInvalid;
		}
		
		return lszRetValue;
	}
	
	public String isValidConvenienceFee(String szConvenienceFee) {
		return BigDecimal.ZERO.compareTo(new BigDecimal(szConvenienceFee)) != 0 ? "true" : "false";
	}
	
	public String parseDateToOtherFormat (String date) {
		try {
			return DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(LocalDate.parse(date));
		} catch(DateTimeParseException e) {
			m_cLog.error("BalanceHelper:parseDateToOtherFormat() .. unable to parse the date, exception occurred {}", e);
			return null;
		}
	}
	
	public String getTotalSchedulePmtBeofreDueDate(String currentDue, String szUserId, String szDate, String internalAccountNumber) {
		BigDecimal totalScheduledAmount = getTotalScheduledPayment(szUserId, szDate, internalAccountNumber);
		BigDecimal remainingAmount = new BigDecimal(currentDue).subtract(totalScheduledAmount);
		return remainingAmount.setScale(2, RoundingMode.HALF_UP).toString();
	}
	
	public String isRemaingDue(String currentDue, String szUserId, String szDate, String internalAccountNumber) {
		BigDecimal totalScheduledAmount = getTotalScheduledPayment(szUserId, szDate, internalAccountNumber);
		BigDecimal remainingAmount = new BigDecimal(currentDue).subtract(totalScheduledAmount);
		
		if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
			return "true";
		} else {
			return "false";
		}
	}
	
	/**
	 * Creates grouping JSON string.
	 * 
	 * @param payDate
	 * @param token
	 * @param internalAccountNumber
	 * @param displayAccountNumber
	 * @param documentNumber
	 * @param userId
	 * @param amount
	 * @param surchargeAmount
	 * @param totalAmount
	 * @param account
	 * @param type
	 * @param nickName
	 * @param expiry
	 * @param paymentGroup
	 * @return json string
	 */
	public String createGrouingJson(
            final String payDate,
            final String token,
            final String internalAccountNumber,
            final String displayAccountNumber,
            final String documentNumber,
            final String userId,
            final String amount,
            final String surchargeAmount,
            final String totalAmount,
            final String account,
            final String type,
            final String nickName,
            final String expiry,
            final String paymentGroup) {

		 // Create the main payData JSON object
        JSONObject payData = new JSONObject();
        
        try {
            // Create paymentMethod JSON object
            JSONObject paymentMethod = new JSONObject();
            paymentMethod.put("token", token);
            paymentMethod.put("account", account);
            paymentMethod.put("type", type);
            paymentMethod.put("nickName", nickName);
            paymentMethod.put("expiry", expiry);

            // Create grouping JSON array
            JSONArray grouping = new JSONArray();
            JSONObject bill = new JSONObject();
            bill.put("internalAccountNumber", internalAccountNumber);
            bill.put("displayAccountNumber", displayAccountNumber);
            bill.put("documentNumber", documentNumber);
            bill.put("amount", amount);
            bill.put("paymentGroup", paymentGroup);
            bill.put("surcharge", surchargeAmount);
            bill.put("totalAmount", totalAmount);
            bill.put("interPayTransactionId", "N/A");

            grouping.put(bill);

            payData.put("payDate", payDate); // Assuming sBillDueDate is defined elsewhere
            payData.put("paymentGroup", paymentGroup);
            payData.put("payMethod", paymentMethod);
            payData.put("grouping", grouping);
            payData.put("autoScheduledConfirm", true);
            payData.put("userId", userId); // Adding userId parameter

        } catch (JSONException e) {
        	m_cLog.error("createGrouingJson() ... exception occurred {}", e);
        }
        
        return payData.toString();
    }
	
	/**
	 * Adds two amounts string as bigdecimal.
	 * 
	 * @param amount1 The first amount
	 * @param amount2 The second amount
	 * @return the sum of provided amounts
	 */
	public static String addAmount(String amount1, String amount2) {
		BigDecimal firstAmount = null;
		BigDecimal secondAmount = null;
		
		try {
			firstAmount = new BigDecimal(amount1).setScale(2, RoundingMode.HALF_UP);
		} catch(Exception e) {
			firstAmount = BigDecimal.ZERO;
		}
		
		try {
			secondAmount = new BigDecimal(amount2).setScale(2, RoundingMode.HALF_UP);
		} catch(Exception e) {
			secondAmount = BigDecimal.ZERO;
		}
		
		return firstAmount.add(secondAmount).toString();
		
	}
}
