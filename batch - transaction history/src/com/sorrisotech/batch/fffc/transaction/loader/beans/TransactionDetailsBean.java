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

import java.sql.Timestamp;

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
	 * The description of transaction.
	 */
	private final String description;
	
	/**
	 * The invoice of transactions.
	 */
	private TransactionDocumentDetailsBean cInvoices = new TransactionDocumentDetailsBean();
	
	/**************************************************************************
	 * The transaction time stamp.
	 */
	private final Timestamp mDateTime;
	
	/**************************************************************************
	 * Getter method for invoice
	 * 
	 * @return TransactionDocumentDetailsBean
	 */
	public TransactionDocumentDetailsBean getInvoices() {
		return cInvoices;
	}
	
	/**************************************************************************
	 * Setter method for invoice
	 * 
	 */
	public void setInvoices(TransactionDocumentDetailsBean lInvoices) {
		this.cInvoices = lInvoices;
	}
	
	/**************************************************************************
	 * Getter method for online id.
	 * 
	 * @return String
	 */
	public String getOnlineId() {
		return onlineId;
	}
	
	/**************************************************************************
	 * Getter method for description.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**************************************************************************
	 * Getter method for date and time.
	 * 
	 * @return Time stamp.
	 */
	public Timestamp getDateTime() {
		return mDateTime;
	}
	
	/**************************************************************************
	 * All argument constructor.
	 * 
	 */
	public TransactionDetailsBean(String onlineId, String description,
	        TransactionDocumentDetailsBean cInvoices, Timestamp mDateTime) {
		super();
		this.onlineId = onlineId;
		this.description = description;
		this.cInvoices = cInvoices;
		this.mDateTime = mDateTime;
	}
	
}
