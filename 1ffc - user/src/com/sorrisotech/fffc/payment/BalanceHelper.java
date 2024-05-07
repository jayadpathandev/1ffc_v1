package com.sorrisotech.fffc.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
	 * @param cszBillDate
	 * @param cszBillBalance
	 * @param cszStatusDate
	 * @param cszStatusBalance
	 * @return
	 */
	public static String getCurrentBalanceFormattedAsCurrency(
								    IServiceLocator2 	locator, 
									IUserData           data,
									final String 		cszPayGroup,
									final String 		cszIntAccount,
									final String 		cszBillDate, 
									final String	 	cszBillBalance,
									final String 		cszStatusDate,
									final String	 	cszStatusBalance ) {

		String lszRetVal = null;
		BigDecimal ldCurBalance = getCurrentBalance (
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
			m_cLog.error("getTrueMinimumDueNumber - An exception was thrown", e);
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
	public static String getTrueMaximumPayFormattedAsCurrency
										(	IServiceLocator2 	locator, 
											IUserData    		data,
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
				m_cLog.error("getTrueMaximumPayFormattedAsCurrency - An exception was thrown", e);
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
			m_cLog.error("parseDateToOtherFormat() .. unable to parse the date, exception occurred {}", e);
			return null;
		}
	}
}
