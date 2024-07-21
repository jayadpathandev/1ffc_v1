package com.sorrisotech.fffc.migration;

/**
 * Interface used to create an automatic payment rule
 * 
 * @author johnk
 * @since 2024-Jul-17
 * @version 2024-Jul-18 -- filled out details
 * 
 */
public interface IAutomaticPaymentRule {

	/**
	 * Creates a scheduled payment by calling the application
	 * web service endpoint.
	 * 
	 * @return
	 */
	public WebSvcReturnCode createAutomaticPaymentRule();

	
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
	public String getPayAmount();
	
	/**
	 * Get the day of the month when
	 * paymentd based on this rule will be made.
	 * 
	 * @return
	 */
	public String getPayDay();
	
	/**
	 * Returns string suitable for use in logging.
	 * 
	 * @return
	 */
	public String getInfoAsString();

}
