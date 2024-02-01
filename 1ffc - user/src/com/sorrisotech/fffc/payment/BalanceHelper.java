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
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

/**
 * Class provides method to calculate current balance based on 
 *   combination of bill, status feed, and payments.
 *   
 * @author johnK
 * @since  2024-Jan-31
 * @version 2024-Jan-31 Hopefully works right away
 * 
 */
public class BalanceHelper implements IExternalReuse {

	private static final long serialVersionUID = 8771674348997401871L;

	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(BalanceHelper.class);
	
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
	public String getCurrentBalanceFormatted(IServiceLocator2 	locator, 
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
			m_cLog.error("getCurrentBalanceFormatted - An exception was thrown", e);
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
	 */
	public void getCurrentBalanceRaw(IServiceLocator2 	locator, 
									IUserData           data,
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszBillDate, 
									final String	 	cszBillBalance,
									final String 		cszStatusDate,
									final String	 	cszStatusBalance,
									IStringData			cReturnBalanceVal) {

		BigDecimal ldCurBalance = getCurrentBalanceInternal (
				cszPayGroup,
				cszIntAccount,
				cszBillDate, 
				cszBillBalance,
				cszStatusDate,
				cszStatusBalance);
		try {
			cReturnBalanceVal.putValue(ldCurBalance.toString());
		} catch (MargaritaDataException e) {
			m_cLog.error("GetCurrentBalanceRaw - An exception was thrown", e);
			e.printStackTrace();
		}
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
		
		// -- do the math --
		ldCurBalance = ldCurBalance.subtract(ldPayments);
		m_cLog.debug("getCurrentBalanceInternal amount to return: {}", ldCurBalance);
		
		ldRetVal = ldCurBalance;
		return ldRetVal;
	}
	
	/**************************************************************************
	 * This method returns a 2D array of payment due values. If a null string
	 * is passed in for any of the string we don't generate that drop down.
	 * 
	 * @param cData       The user data.
	 * @param cLocator    The service locator.
	 * @param szCurrent   The current balance.
	 * @param szStatement The statement balance.
	 * @param szMinimum   The minimum due.
	 * 
	 * @return A 2D array of payment due.
	 */
	public String[][] getPayAmountDropdown(IUserData cData, IServiceLocator2 cLocator, String szCurrentDisplay,
			String szStatementDisplay, String szMinimumDisplay) {
		
//		if (null != sCurrentDisplay)
		String szCurrentText = szCurrentDisplay + " - "
				+ I18n.translate(cLocator, cData, "paymentOneTime_sCurrentBalText");
		String szStatementText = szStatementDisplay + " - "
				+ I18n.translate(cLocator, cData, "paymentOneTime_sStatementBalText");
		String szMinimumText = szMinimumDisplay + " - "
				+ I18n.translate(cLocator, cData, "paymentOneTime_sMinimumText");
		String szOther = I18n.translate(cLocator, cData, "paymentOneTime_sOther");

		return new String[][] { { "current", szCurrentText }, { "statement", szStatementText },
				{ "minimum", szMinimumText }, { "other", szOther } };
	}
	
	/**
	 * returns the true minimum due based on the status feed and the calculated minimum due
	 * 
	 * @param locator
	 * @param data
	 * @param cszStatusMinimumPaymentAmt
	 * @param cszCurrentBalanceAmt
	 * @param oMinDueVariable
	 * @return
	 */
	public void getTrueMinimumDue (IServiceLocator2 		locator, 
											IUserData    	data,
											final String 	cszStatusMinimumPaymentAmt,
											final String 	cszCurrentBalanceAmt,
											IStringData		oMinDueVariable) {
		String lszRetValue = null;
		BigDecimal lbdStatusMinimumPaymentAmt = BigDecimal.ZERO;
		BigDecimal lbdCurrentBalanceAmt = BigDecimal.ZERO;
		BigDecimal lbdReturnTrueMinimumPaymentAmt = BigDecimal.ZERO;
		
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
			
			// -- convert to string --
			lbdReturnTrueMinimumPaymentAmt.setScale(2);
			lszRetValue = lbdReturnTrueMinimumPaymentAmt.toPlainString();
			
		} catch (NumberFormatException e) {
			m_cLog.debug("getTrueMinimumDue failed to convert input variable from string");
			return;
		}
		
		try {
			oMinDueVariable.putValue(lszRetValue);
		} catch (MargaritaDataException e) {
			m_cLog.error("GetCurrentBalanceRaw - An exception was thrown", e);
			e.printStackTrace();
		}
		return;
	}
							  

}
