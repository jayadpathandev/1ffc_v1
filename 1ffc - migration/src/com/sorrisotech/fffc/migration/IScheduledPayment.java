package com.sorrisotech.fffc.migration;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Scheduled payment information used when calling makeOneTimePaymentForAgent.
 * 
 * @author johnk
 * @since  2024-Jul-17
 * @version 2024-Jul-18 finished definition
 */
public interface IScheduledPayment {

	/**
	 * Creates a scheduled payment by calling the application
	 * web service endpoint.
	 * 
	 * @return
	 */
	public WebSvcReturnCode createScheduledPayment();

	/**
	 * Display account name (loan id for 1st Franklin).
	 * 
	 * @return
	 */
	public String getDisplayAccount();
	
	/**
	 * Internal account name
	 * 
	 * @return
	 */
	public String getInternalAccount();
	
	/**
	 * Amount to pay every month
	 * 
	 * @return
	 */
	public BigDecimal getPayAmount();
	
	/**
	 * Day to pay every month
	 * 
	 * @return
	 */
	public Calendar getPayDate();
	
	
	/**
	 * Returns string suitable for use in logging.
	 * 
	 * @return
	 */
	public String getInfoAsString();
	
}
