package com.sorrisotech.fffc.payment;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.payment.dao.GetPaymentHistoryAmountDaoImpl;

/**
 * Class provides method to calculate current balance based on 
 *   combination of bill, status feed, and payments.
 *   
 * @author johnK
 * @since  2024-Jan-31
 * @version 2024-Jan-31 jak Hopefully works right away
 * @version 2024-Feb-07 jak Added min/max and several minor defect fixes around that.
 * @version 2024-Feb-18 jak Added some checks around bill vs status comparison to prevent
 * 								exceptions should one be empty.
 * @version 2024-Apr-17 jak	Added tests for "0" date which happens if you have no bills
 * 								and are looking for current balance.
 * @version 2024-May-07 jak Forced max payment to zero if it was less than zero.
 * @version 2024-May-07 jak	Always use status for current balance, use a precise time, and
 * 								update calculations appropriately.
 * @version 2024-Jul-13	jak	Updated for processing with time stamp instead of date
 * 
 */
public class FffcBalance {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(FffcBalance.class);
	
	protected static final String m_cszReturnForInvalid = "--";

	/**
	 * Returns the unformatted "number" version of the current balance for use in the 
	 * 	application.
	 * 
	 * @param locator
	 * @param data
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszStatusTimeStamp
	 * @param cszStatusBalance
	 * @param cReturnBalanceVal
	 * @return String for current balance as nnnn.nn format
	 */
	public static String getCurrentBalanceRaw(
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszStatusTimeStamp,
									final String	 	cszStatusBalance) {
		String lszRetValue = "";
		
		BigDecimal ldCurBalance = getCurrentBalance (
				cszPayGroup,
				cszIntAccount,
				cszStatusTimeStamp,
				cszStatusBalance);

		ldCurBalance.setScale(2);
		lszRetValue = ldCurBalance.toPlainString();
		
		return lszRetValue;
	}

	/**
	 * Returns "true" if account is current (i.e. balance <= zero) and "false" if not.
	 * 
	 * @param locator
	 * @param data
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszStatusTimeStamp
	 * @param cszStatusBalance
	 * @return
	 */
	public static String isAccountCurrent (
			final String 		cszPayGroup,
			final String 		cszIntAccount,
			final String 		cszStatusTimeStamp,
			final String	 	cszStatusBalance) {

		String lsRetVal = "true";
		BigDecimal ldCurBalance = getCurrentBalance (
			cszPayGroup,
			cszIntAccount,
			cszStatusTimeStamp,
			cszStatusBalance);
	
		if ( 1 == ldCurBalance.compareTo(BigDecimal.ZERO)) { // 1 means ldCurBalance > zero 
			 lsRetVal = "false";
		 }
		return lsRetVal;
}
	

	
	/**
	 * Publicl method to actually make the database call to get payments and calculate the 
	 * 	current balance.  Returns 0.00 as a floor if the amount calculated is less than 0.
	 * 								
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszStatusTimeStamp
	 * @param cszStatusBalance
	 * @return
	 */
	public static BigDecimal getCurrentBalance (
										final String 		cszPayGroup,
										final String 		cszIntAccount,
										final String 		cszStatusTimeStamp,
										final String	 	cszStatusBalance) {
		BigDecimal ldRetVal = BigDecimal.ZERO;
		BigDecimal ldCurBalance = BigDecimal.ZERO;
		BigDecimal ldPayments = BigDecimal.ZERO;
		Timestamp ltStartTime = null;	
		GetPaymentHistoryAmountDaoImpl loPmtHist = new GetPaymentHistoryAmountDaoImpl();

		// -- return 0 if we get bad input --
		if ((null == cszStatusBalance) || (null == cszStatusTimeStamp) || 
				cszStatusBalance.isEmpty() || cszStatusTimeStamp.isEmpty() ||
			cszStatusBalance.isBlank() || cszStatusTimeStamp.isBlank()) {
			return ldRetVal;
		}

		
		try {
				ltStartTime = new Timestamp(Long.parseUnsignedLong(cszStatusTimeStamp));
				ldCurBalance = new BigDecimal(cszStatusBalance);
				m_cLog.debug("FffcBalance:getCurrentBalanceInternal using Status Data - Date: {}, starting balance {}", cszStatusTimeStamp.toString(), cszStatusBalance);
		} catch (NumberFormatException e) {
			m_cLog.debug("FffcBalance:getCurrentBalanceInternal failed to convert timestamp or balance from string");
			return ldRetVal;
		}
		
		// -- returns the total posted payments for an account since chosen date (inclusive) --
		ldPayments = loPmtHist.getPaymentHistoryAmountForAccount(cszPayGroup, cszIntAccount, ltStartTime);
		m_cLog.debug("FffcBalance:getCurrentBalanceInternal payments posted: {}", ldPayments);
		
		if (null != ldPayments) {
			// -- do the math --
			ldCurBalance = ldCurBalance.subtract(ldPayments);
		}
		m_cLog.debug("FffcBalance:getCurrentBalanceInternal amount to return: {}", ldCurBalance);
		
		if (1 == ldCurBalance.compareTo(BigDecimal.ZERO)) {
			// -- only assign if > 0... otherwise we return the default
			// 		which is 0 --
			ldRetVal = ldCurBalance;
		}
		return ldRetVal;
	}
	
	public static String getTrueMinimumDueRaw ( 	
									final String 	cszStatusMinimumPaymentAmt,
									final String 	cszCurrentBalanceAmt) {
		String lszRetValue = null;
		BigDecimal lbdStatusMinimumPaymentAmt = BigDecimal.ZERO;
		BigDecimal lbdCurrentBalanceAmt = BigDecimal.ZERO;
		BigDecimal lbdReturnTrueMinimumPaymentAmt = BigDecimal.ZERO;
		
		// -- this could  be called with dummy data at first --
		if ((null == cszStatusMinimumPaymentAmt) || (null == cszCurrentBalanceAmt) ||
				cszStatusMinimumPaymentAmt.isEmpty() || cszCurrentBalanceAmt.isEmpty() ||
				cszStatusMinimumPaymentAmt.isBlank() || cszCurrentBalanceAmt.isBlank()) {
			lszRetValue = m_cszReturnForInvalid;
			return lszRetValue;
		}
			
		try {
			// -- convert from strings to BigDecimal
			lbdStatusMinimumPaymentAmt = new BigDecimal(cszStatusMinimumPaymentAmt);
			lbdCurrentBalanceAmt = new BigDecimal(cszCurrentBalanceAmt);
			
			// -- compare and act --
			if (1 == lbdStatusMinimumPaymentAmt.compareTo(lbdCurrentBalanceAmt))
				// -- minimum is greater than current balance so current balance becomes the true minimum --
				lbdReturnTrueMinimumPaymentAmt = lbdCurrentBalanceAmt;
			else
				// -- minimum is less than or equal to current balance so minimum is true minimum --
				lbdReturnTrueMinimumPaymentAmt = lbdStatusMinimumPaymentAmt;
			
			// -- minimum payment cannot be less than zero --
			if (-1 == lbdReturnTrueMinimumPaymentAmt.compareTo(BigDecimal.ZERO)) {
				lbdReturnTrueMinimumPaymentAmt = BigDecimal.ZERO;
			}
			// -- convert to string --
			lbdReturnTrueMinimumPaymentAmt.setScale(2);
			lszRetValue = lbdReturnTrueMinimumPaymentAmt.toPlainString();
			
		} catch (NumberFormatException e) {
			m_cLog.debug("FffcBalance:getTrueMinimumDueInternal -- failed to convert input variable from string");
			return lszRetValue;
		}
		
		return lszRetValue;
	}
		
	/**
	 * Returns the maximum payment allowed based on billing info and payments. It is returned
	 * as a string of the format nnnn.nn that's suitable for edit fields.
	 * 
	 * @param locator
	 * @param data
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param csMaxTimeStamp
	 * @param cszFileMaxDue
	 * @return maximum payment allowed as nnnn.nn string
	 */
	public static String getTrueMaximumPayRaw(
										final String 		cszPayGroup,
										final String 		cszIntAccount,
										final String 		csMaxTimeStamp, 
										final String 		cszFileMaxDue) {
		String lszRetValue = null;
		BigDecimal ldTrueMaxPaymentAmount = BigDecimal.ZERO;

		// -- gets the true maximum payment --
		ldTrueMaxPaymentAmount = getTrueMaximumPay(cszPayGroup, 
												   cszIntAccount,
												   csMaxTimeStamp,
												   cszFileMaxDue);

		if (ldTrueMaxPaymentAmount.equals(BigDecimal.ZERO)) {
			return m_cszReturnForInvalid;
		}
		// -- convert it to string --
		ldTrueMaxPaymentAmount.setScale(2);
		lszRetValue = ldTrueMaxPaymentAmount.toPlainString();

		return lszRetValue;
		
	}
	
	/**
	 * Returns the true maximum payment allowed based on billing and payment information. Calculates
	 * maximum as most recent maximum less the amount paid since that date.
	 * 
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszMaxTimeStamp
	 * @param cszBillMaxDue
	 * @param cszFileMaxDue
	 * @return
	 */
	public static BigDecimal getTrueMaximumPay (
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszMaxTimeStamp, 
									final String 		cszFileMaxDue) {

		BigDecimal ldTrueMaxPayment = BigDecimal.ZERO;
		BigDecimal ldTempMaxPayment = BigDecimal.ZERO;
		BigDecimal ldPayments = BigDecimal.ZERO;
		Timestamp ltStartTime = null;	
		GetPaymentHistoryAmountDaoImpl loPmtHist = new GetPaymentHistoryAmountDaoImpl();

		// -- return 0 if we get bad input --
		if ((null == cszFileMaxDue) || (null == cszMaxTimeStamp) || 
			cszFileMaxDue.isEmpty() || cszMaxTimeStamp.isEmpty() ||
			cszFileMaxDue.isBlank() || cszMaxTimeStamp.isBlank()) {
			return ldTrueMaxPayment;
		}
		
		// - convert to timestamp and big decimal -- 
		try {
			
			ltStartTime = new Timestamp(Long.parseUnsignedLong(cszMaxTimeStamp));
			ldTempMaxPayment = new BigDecimal(cszFileMaxDue);
			m_cLog.debug("getTrueMaximumPayInternal - Date: {}, starting balance {}", cszMaxTimeStamp, cszFileMaxDue);
			
		} catch (NumberFormatException e) {
			m_cLog.debug("getTrueMaximumPayInternal failed to convert balance from string");
			return ldTrueMaxPayment;
		}
		
		// -- returns the total posted payments for an account since chosen date (inclusive) --
		ldPayments = loPmtHist.getPaymentHistoryAmountForAccount(cszPayGroup, cszIntAccount, ltStartTime);
		m_cLog.debug("FffcBalance:getTrueMaximumPayInternal payments posted: {}", ldPayments);
		
		if (null != ldPayments) {
			// -- do the math --
			ldTrueMaxPayment = ldTempMaxPayment.subtract(ldPayments);
		}
		else {
			ldTrueMaxPayment = ldTempMaxPayment;
		}
		
		// -- if less than or equal to zero for to zero
		//		only happens when we get bad status information --
		if (1 != ldTrueMaxPayment.compareTo(BigDecimal.ZERO)) {
			ldTrueMaxPayment = BigDecimal.ZERO;
		}
		
		m_cLog.debug("FffcBalance:getTrueMaximumPayInternal amount to return: {}", ldTrueMaxPayment);
		
		return ldTrueMaxPayment;
		
	}
	
	public static BigDecimal getTotalScheduledPayment(String szUserId, String szDate, String internalAccountNumber) {
		GetPaymentHistoryAmountDaoImpl loPmtHist = new GetPaymentHistoryAmountDaoImpl();
		return loPmtHist.getTotalScheduledPaymentBeforeBillDue(szUserId, szDate, internalAccountNumber);
	}

}
