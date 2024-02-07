package com.sorrisotech.fffc.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.fffc.payment.dao.GetPaymentHistoryAmountDaoImpl;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.INumberData;
import com.sorrisotech.svcs.itfc.data.IStringData;
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
 * 
 */
public class BalanceHelper implements IExternalReuse {

	private static final long serialVersionUID = 8771674348997401871L;

	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(BalanceHelper.class);
	
	private final String m_cszReturnForInvalid = "--";
	@Override
	public int getReuse() {
		// -- we don't need more than one of these, they are just a calculator --
		return IExternalReuse.REUSE_USE_CASE_SINGLETON;
	}

	/**
	 * Calculates the current balance based on information from the bill,
	 * the status feed, and payment history.
	 * 
	 * @param locator,
	 * @param data,
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	public String getCurrentBalanceFormattedAsCurrency(
								    IServiceLocator2 	locator, 
									IUserData           data,
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszBillDate, 
									final String	 	cszBillBalance,
									final String 		cszStatusDate,
									final String	 	cszStatusBalance ) {

		String lszRetVal = null;
		BigDecimal ldCurBalance = getCurrentBalanceInternal (
														cszPayGroup,
														cszIntAccount,
														cszBillDate, 
														cszBillBalance,
														cszStatusDate,
														cszStatusBalance);
		
		try {
			final LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
			
			lszRetVal = format.formatAmount(cszPayGroup, ldCurBalance);
			
		} catch (Exception e) {
			m_cLog.error("getCurrentBalanceFormattedAsCurrency - An exception was thrown", e);
		}

		return lszRetVal ;
	}
	
	
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
	public String getCurrentBalanceRaw(IServiceLocator2 	locator, 
									IUserData           data,
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszBillDate, 
									final String	 	cszBillBalance,
									final String 		cszStatusDate,
									final String	 	cszStatusBalance) {
		String lszRetValue = "";
		
		BigDecimal ldCurBalance = getCurrentBalanceInternal (
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
	public String isAccountCurrent (IServiceLocator2 	locator, 
			IUserData           data,
			final String 		cszPayGroup,
			final String 		cszIntAccount,
			final String 		cszBillDate, 
			final String	 	cszBillBalance,
			final String 		cszStatusDate,
			final String	 	cszStatusBalance) {

		String lsRetVal = "true";
		BigDecimal ldCurBalance = getCurrentBalanceInternal (
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
	 * Internal method to actually make the database call to get payments and calculate the 
	 * 	current balance.
	 * 								
	 * @param cszPayGroup
	 * @param cszIntAccount
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	private BigDecimal getCurrentBalanceInternal (
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

		// -- get the date in proper format --
		try {
			lBillDate = LocalDate.parse(cszBillDate, DateTimeFormatter.BASIC_ISO_DATE);
			lStatusDate = LocalDate.parse(cszStatusDate, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (DateTimeParseException e) {
			m_cLog.error("getCurrentBalanceInternal....An exception was thrown", e);
			return ldRetVal;
		}
		
		// -- pick which date and balance to use --
		try {
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
		
		ldRetVal = ldCurBalance;
		return ldRetVal;
	}
	
	private String getTrueMinimumDueInternal ( 	final String 	cszStatusMinimumPaymentAmt,
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
	 * returns the true minimum due based on the status feed and the calculated minimum due
	 * 
	 * @param locator
	 * @param data
	 * @param cszStatusMinimumPaymentAmt
	 * @param cszCurrentBalanceAmt
	 * @param oMinDueVariable
	 * @return minnimum due as a string nnn.nn format
	 */
	public String getTrueMinimumDueRaw (IServiceLocator2 		locator, 
											IUserData    	data,
											final String 	cszStatusMinimumPaymentAmt,
											final String 	cszCurrentBalanceAmt ) {
		String lszRetValue = null;

		lszRetValue = getTrueMinimumDueInternal (cszStatusMinimumPaymentAmt,
												 cszCurrentBalanceAmt);
		
		return lszRetValue;
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
	public void getTrueMinimumDueNumber (IServiceLocator2 	locator, 
											IUserData    	data,
											final String 	cszStatusMinimumPaymentAmt,
											final String 	cszCurrentBalanceAmt,
											INumberData		oMinDueVariable) {
		String lszRetValue = null;

		lszRetValue = getTrueMinimumDueInternal (cszStatusMinimumPaymentAmt,
												 cszCurrentBalanceAmt);
		if (lszRetValue == m_cszReturnForInvalid) {
			lszRetValue = "0.00";
		}
		try {
			oMinDueVariable.setValue(lszRetValue);
		} catch (MargaritaDataException e) {
			m_cLog.error("GetCurrentBalanceRaw - An exception was thrown", e);
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
	public String getTrueMinimumDueFormattedAsCurrency (
											IServiceLocator2 	locator, 
											IUserData    		data,
											final String 		cszPayGroup,
											final String 		cszStatusMinimumPaymentAmt,
											final String 		cszCurrentBalanceAmt) {
		String lszRetValue = null;
		String lszInternalUnformatted = null;

		lszInternalUnformatted = getTrueMinimumDueInternal (cszStatusMinimumPaymentAmt,
				 cszCurrentBalanceAmt);
		
		if (lszInternalUnformatted == m_cszReturnForInvalid) {
			return lszInternalUnformatted;
		}
			
		if (null != lszInternalUnformatted) {
			try {
				final LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
				BigDecimal ldMinimumDue = new BigDecimal(lszInternalUnformatted);
				ldMinimumDue.setScale(2);
				lszRetValue = " " + format.formatAmount(cszPayGroup, ldMinimumDue);
				
			} catch (Exception e) {
				m_cLog.error("getTrueMinimumDueFormattedAsCurrency - An exception was thrown", e);
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
	 * @param cszMaxDate
	 * @param cszFileMaxDue
	 * @return maximum payment allowed formatted as a localized currency
	 */
	public String getTrueMaximumPayFormattedAsCurrency
										(	IServiceLocator2 	locator, 
											IUserData    		data,
											final String 		cszPayGroup,
											final String 		cszIntAccount,
											final String 		cszMaxDate, 
											final String 		cszFileMaxDue) {
		String lszRetValue = null;
		BigDecimal ldTrueMaxPaymentAmount = BigDecimal.ZERO;

		// -- gets the true maximum payment --
		ldTrueMaxPaymentAmount = getTrueMaximumPayInternal(cszPayGroup, 
														   cszIntAccount,
														   cszMaxDate,
														   cszFileMaxDue);
		if (ldTrueMaxPaymentAmount.equals(BigDecimal.ZERO)) {
			return m_cszReturnForInvalid;
		}

		// -- format it for return --
		if (!ldTrueMaxPaymentAmount.equals(BigDecimal.ZERO)) {
			try {
				LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
				ldTrueMaxPaymentAmount.setScale(2);
				lszRetValue = " " + format.formatAmount(cszPayGroup, ldTrueMaxPaymentAmount);
			} catch (MargaritaDataException e) {
				m_cLog.error("getTrueMaximumPayFormattedAsCurrency - An exception was thrown", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_cszReturnForInvalid;
			}
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
	public String getTrueMaximumPayRaw(	IServiceLocator2 	locator, 
											IUserData    		data,
											final String 		cszPayGroup,
											final String 		cszIntAccount,
											final String 		cszMaxDate, 
											final String 		cszFileMaxDue) {
		String lszRetValue = null;
		BigDecimal ldTrueMaxPaymentAmount = BigDecimal.ZERO;

		// -- gets the true maximum payment --
		ldTrueMaxPaymentAmount = getTrueMaximumPayInternal(cszPayGroup, 
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
	private BigDecimal getTrueMaximumPayInternal (
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
