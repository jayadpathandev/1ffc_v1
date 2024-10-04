package com.sorrisotech.fffc.migration;

import java.math.BigDecimal;
import java.time.LocalDate;

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
	public void createAutomaticPaymentRule();

	/**
	 * Removes an existing automatic payment rule for this 
	 * account if one exists and the payment info if that is selected
	 */
	public void removeOldAutomaticPaymentInfo();
	
	/**
	 * Returns customerId associatd with this payment rule
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
	 * Get the day of the month when
	 * paymentd based on this rule will be made.
	 * 
	 * @return
	 */
	public String getPayDay();
	
	/**
	 * Gets the payment acount information from
	 * that will be used for this rule
	 * 
	 * @return
	 */
	public PmtAcct getPayAcct();
	
	
	/**
	 * Gets the extra payment amount or 0 if there is none
	 * 
	 * @return
	 */
	public BigDecimal getExtraPayment();
	
	/**
	 * Gets the start date for this automatic payment rule
	 * 
	 * @return
	 */
	public LocalDate getStartDate();
	
	/**
	 * Returns string suitable for use in logging.
	 * 
	 * @return
	 */
	public String getInfoAsString();
	
	public String getOrgIdWhereRoutingNumberIsZero();

}
