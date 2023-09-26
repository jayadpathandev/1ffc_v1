/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.batch.fffc.transaction.loader.beans;

import java.math.BigDecimal;

/******************************************************************************
 * Holds the information of the transaction details.
 *
 * @author Asrar Saloda
 *
 */
public class TransactionDetailsBean {
	
	/**************************************************************************
	 * The Online id.
	 */
	private final String onlineId;
	
	/**************************************************************************
	 * The transaction date
	 */
	private final Integer date;

	/**************************************************************************
	 * Account of user 
	 */
	private final String account;
	
	/**************************************************************************
	 * Type of transaction e.g. Payment/ Acct Update / Addr Change 
	 */
	private final String transactionType;

	/**************************************************************************
	 * The description of transaction.
	 */
	private final String description;
	
	/**************************************************************************
	 * The amount.
	 */
	private final BigDecimal amount;
	
	/**************************************************************************
	 * All argument constructor 
	 */
	public TransactionDetailsBean(
			String onlineId, 
			Integer date, 
			String account,
	        String transactionType, 
	        String description, 
	        BigDecimal amount
	        ) {
		super();
		this.onlineId = onlineId;
		this.date = date;
		this.account = account;
		this.transactionType = transactionType;
		this.description = description;
		this.amount = amount;
	}
	
	/**************************************************************************
	 * Getter for online_id
	 * 
	 * @return String
	 */
	public String getOnlineId() {
		return onlineId;
	}
	

	/**************************************************************************
	 * Getter for description
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**************************************************************************
	 * Getter for date
	 * 
	 * @return String
	 */
	public Integer getDate() {
		return date;
	}
	
	/**************************************************************************
	 * Getter for account
	 * 
	 * @return String
	 */
	public String getAccount() {
		return account;
	}
	
	/**************************************************************************
	 * Getter for transaction_type
	 * 
	 * @return String
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**************************************************************************
	 * Getter for amount.
	 * 
	 * @return BigDecimal
	 */
	public BigDecimal getAmount() {
		return amount;
	}
}
