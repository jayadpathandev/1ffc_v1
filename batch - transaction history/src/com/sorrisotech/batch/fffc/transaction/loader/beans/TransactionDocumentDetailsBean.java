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
 * Holds the information of the transaction invoice details.
 *
 * @author Asrar Saloda
 *
 */
public class TransactionDocumentDetailsBean {
	
	/**************************************************************************
	 * The payment group
	 */
	private String mPmtGroup;
	
	/**************************************************************************
	 * The account
	 */
	private String mAccount;
	
	/**************************************************************************
	 * The payment amount.
	 */
	private BigDecimal mAmount;
	
	/**************************************************************************
	 * Super class no argument constructor.
	 */
	public TransactionDocumentDetailsBean() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 * 
	 */
	public TransactionDocumentDetailsBean(String mPmtGroup, String mAccount, BigDecimal mAmount) {
		super();
		this.mPmtGroup = mPmtGroup;
		this.mAccount = mAccount;
		this.mAmount = mAmount;
	}
	
	/**************************************************************************
	 * Getter method for payment group.
	 * 
	 * @return String
	 */
	public String getPmtGroup() {
		return mPmtGroup;
	}
	
	/**************************************************************************
	 * Getter method for account.
	 * 
	 * @return String
	 */
	public String getAccount() {
		return mAccount;
	}
	
	/**************************************************************************
	 * Getter method for amount.
	 * 
	 * @return BigDecimal
	 */
	public BigDecimal getAmount() {
		return mAmount;
	}
}
