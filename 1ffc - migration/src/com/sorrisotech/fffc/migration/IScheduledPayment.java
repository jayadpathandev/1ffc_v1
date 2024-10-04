package com.sorrisotech.fffc.migration;

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
	public void createScheduledPayment();
	
	/**
	 * Returns the customer Id value for this payment 
	 * 
	 * @return
	 */
	public String getCustomerId();

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
	 * Amount of this payment formatted for 
	 * use in JSON web service call.
	 * 
	 * @return
	 */
	public String getPayAmount();
	
	/**
	 * Date of this payment formatted
	 * as 
	 * 
	 * @return
	 */
	public String getPayDate();
	
	/**
	 * Gets the payment acount information from
	 * that will be used for this payment
	 * 
	 * @return
	 */
	public PmtAcct getPayAcct();

	/**
	 * Returns string suitable for use in logging.
	 * 
	 * @return
	 */
	public String getInfoAsString();
	
	/**
	 * 
	 * Sets the information on this payment after its been scheduled
	 * 
	 * @param cszPaymentId
	 * @param cszPaymentStatus
	 */
	public void setPayTransactionInfo(final String cszPaymentId, final String cszPaymentStatus);
	
	/**
	 * Returns payment identifier after its been set... otherwise null
	 * 
	 * @return
	 */
	public String getPaymentId();
	
	/**
	 * Returns payment status after its been set... otherwise null
	 * 
	 * @return
	 */
	public String getPaymentStatus();
	
	public String getOrgIdWhereRoutingNumberIsZero();

}
