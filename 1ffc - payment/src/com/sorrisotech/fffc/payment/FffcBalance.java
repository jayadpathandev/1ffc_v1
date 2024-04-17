package com.sorrisotech.fffc.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @param cReturnBalanceVal
	 * @return String for current balance as nnnn.nn format
	 */
	public static String getCurrentBalanceRaw(
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszBillDate, 
									final String	 	cszBillBalance,
									final String 		cszStatusDate,
									final String	 	cszStatusBalance) {
		String lszRetValue = "";
		
		BigDecimal ldCurBalance = getCurrentBalance (
				cszPayGroup,
				cszIntAccount,
				cszBillDate, 
				cszBillBalance,
				cszStatusDate,
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
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	public static String isAccountCurrent (
			final String 		cszPayGroup,
			final String 		cszIntAccount,
			final String 		cszBillDate, 
			final String	 	cszBillBalance,
			final String 		cszStatusDate,
			final String	 	cszStatusBalance) {

		String lsRetVal = "true";
		BigDecimal ldCurBalance = getCurrentBalance (
			cszPayGroup,
			cszIntAccount,
			cszBillDate, 
			cszBillBalance,
			cszStatusDate,
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
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	public static BigDecimal getCurrentBalance (
										final String 		cszPayGroup,
										final String 		cszIntAccount,
										final String 		cszBillDate, 
										final String	 	cszBillBalance,
										final String 		cszStatusDate,
										final String	 	cszStatusBalance) {
		BigDecimal ldRetVal = BigDecimal.ZERO;
		BigDecimal ldCurBalance = BigDecimal.ZERO;
		BigDecimal ldPayments = BigDecimal.ZERO;
		String lszStartDate = null;	
		GetPaymentHistoryAmountDaoImpl loPmtHist = new GetPaymentHistoryAmountDaoImpl();
		LocalDate lBillDate = LocalDate.now();
		LocalDate lStatusDate = LocalDate.now();
		boolean bHasBillDate = false;
		boolean bHasStatusDate = false;
		
		// -- get the date in proper format --
		try {
			if ((null != cszBillDate) && (0 != cszBillDate.length()) && (!cszBillDate.equalsIgnoreCase("0"))) {
				lBillDate = LocalDate.parse(cszBillDate, DateTimeFormatter.BASIC_ISO_DATE);
				bHasBillDate = true;
			}
			if ((null != cszStatusDate) && (0 != cszStatusDate.length()) && (!cszStatusDate.equalsIgnoreCase("0"))) {
				lStatusDate = LocalDate.parse(cszStatusDate, DateTimeFormatter.BASIC_ISO_DATE);
				bHasStatusDate = true;
			}
		} catch (DateTimeParseException e) {
			m_cLog.error("getCurrentBalanceInternal....An exception was thrown", e);
			return ldRetVal;
		}
		
		// -- pick which date and balance to use --
		try {
			if (bHasBillDate &&  bHasStatusDate) { // -- has both, compare --
				if (lBillDate.isAfter(lStatusDate)) {
					// -- use bill data --
					lszStartDate = cszBillDate;
					ldCurBalance = new BigDecimal(cszBillBalance);
					m_cLog.debug("getCurrentBalanceInternal using Bill Data - Date: {}, starting balance {}", cszBillDate, cszBillBalance);
				}
				else {
					// -- use status data --
					lszStartDate = cszStatusDate;
					ldCurBalance = new BigDecimal(cszStatusBalance);
					m_cLog.debug("getCurrentBalanceInternal using Status Data - Date: {}, starting balance {}", cszStatusDate, cszStatusBalance);
				}
			} else if (bHasStatusDate) { // -- only status, use that --
				// -- use status data --
				lszStartDate = cszStatusDate;
				ldCurBalance = new BigDecimal(cszStatusBalance);
				m_cLog.debug("getCurrentBalanceInternal using Status Data - Date: {}, starting balance {}", cszStatusDate, cszStatusBalance);
			} else if (bHasBillDate) { // -- only bill, use that --
				// -- use bill data --
				lszStartDate = cszBillDate;
				ldCurBalance = new BigDecimal(cszBillBalance);
				m_cLog.debug("getCurrentBalanceInternal using Bill Data - Date: {}, starting balance {}", cszBillDate, cszBillBalance);
			} else {	// -- nothing.. we came up empty and send an error value back --
				// -- don't have a bill date, don't have a status date -- this is an error --
				m_cLog.error("getCurrentBalanceInternal no bill date or status date!");
				return ldRetVal;
			}
		} catch (NumberFormatException e) {
			m_cLog.debug("getCurrentBalanceInternal failed to convert balance from string");
			return ldRetVal;
		}
		
		// -- returns the total posted payments for an account since chosen date (inclusive) --
		ldPayments = loPmtHist.getPaymentHistoryAmountForAccount(cszPayGroup, cszIntAccount, lszStartDate);
		m_cLog.debug("getCurrentBalanceInternal payments posted: {}", ldPayments);
		
		if (null != ldPayments) {
			// -- do the math --
			ldCurBalance = ldCurBalance.subtract(ldPayments);
		}
		m_cLog.debug("getCurrentBalanceInternal amount to return: {}", ldCurBalance);
		
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
			m_cLog.debug("getTrueMinimumDueInternal failed to convert input variable from string");
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
	 * @param cszMaxDate
	 * @param cszFileMaxDue
	 * @return maximum payment allowed as nnnn.nn string
	 */
	public static String getTrueMaximumPayRaw(
										final String 		cszPayGroup,
										final String 		cszIntAccount,
										final String 		cszMaxDate, 
										final String 		cszFileMaxDue) {
		String lszRetValue = null;
		BigDecimal ldTrueMaxPaymentAmount = BigDecimal.ZERO;

		// -- gets the true maximum payment --
		ldTrueMaxPaymentAmount = getTrueMaximumPay(cszPayGroup, 
												   cszIntAccount,
												   cszMaxDate,
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
	 * @param cszMaxDate
	 * @param cszBillMaxDue
	 * @param cszFileMaxDue
	 * @return
	 */
	public static BigDecimal getTrueMaximumPay (
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszMaxDate, 
									final String 		cszFileMaxDue) {

		BigDecimal ldTrueMaxPayment = BigDecimal.ZERO;
		BigDecimal ldTempMaxPayment = BigDecimal.ZERO;
		BigDecimal ldPayments = BigDecimal.ZERO;
		String lszStartDate = null;	
		GetPaymentHistoryAmountDaoImpl loPmtHist = new GetPaymentHistoryAmountDaoImpl();

		// -- return 0 if we get bad input --
		if ((null == cszFileMaxDue) || (null == cszMaxDate) || 
			cszFileMaxDue.isEmpty() || cszMaxDate.isEmpty() ||
			cszFileMaxDue.isBlank() || cszMaxDate.isBlank()) {
			return ldTrueMaxPayment;
		}
		
		// - convert to big decimal -- 
		try {
			
			lszStartDate = cszMaxDate;
			ldTempMaxPayment = new BigDecimal(cszFileMaxDue);
			m_cLog.debug("getTrueMaximumPayInternal - Date: {}, starting balance {}", cszMaxDate, cszFileMaxDue);
			
		} catch (NumberFormatException e) {
			m_cLog.debug("getTrueMaximumPayInternal failed to convert balance from string");
			return ldTrueMaxPayment;
		}
		
		// -- returns the total posted payments for an account since chosen date (inclusive) --
		ldPayments = loPmtHist.getPaymentHistoryAmountForAccount(cszPayGroup, cszIntAccount, lszStartDate);
		m_cLog.debug("getTrueMaximumPayInternal payments posted: {}", ldPayments);
		
		if (null != ldPayments) {
			// -- do the math --
			ldTrueMaxPayment = ldTempMaxPayment.subtract(ldPayments);
		}
		else {
			ldTrueMaxPayment = ldTempMaxPayment;
		}
		m_cLog.debug("getTrueMaximumPayInternal amount to return: {}", ldTrueMaxPayment);
		
		return ldTrueMaxPayment;
		
	}

}
