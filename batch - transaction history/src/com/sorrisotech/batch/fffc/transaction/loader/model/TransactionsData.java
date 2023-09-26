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
package com.sorrisotech.batch.fffc.transaction.loader.model;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/******************************************************************************
 * Class used to hold java representation of XML content.
 *
 * @author Asrar Saloda
 *
 */
@XmlRootElement(name = "TransactionsData")
public class TransactionsData {
	
	protected TransactionsData.TransactionRecord transactionRecord;
	
	/*********************************************************************************
	 * Getter method for transaction record of transaction file
	 *
	 *@return TransactionRecord
	 */
	public TransactionsData.TransactionRecord getTransactionRecord() {
		return transactionRecord;
	}
	
	/*********************************************************************************
	 * Setter method for transaction record of transaction file
	 *
	 */
	@XmlElement(required = true)
	public void setTransactionRecords(TransactionsData.TransactionRecord transactionRecord) {
		this.transactionRecord = transactionRecord;
	}
	
	/*********************************************************************************
	 * Holding the XML root element (Transaction Record) of transaction history file
	 *
	 */
	@XmlRootElement(name = "TransactionRecord")
	public static class TransactionRecord {
		
		/******************************************************************************
		 * Online id of transaction.
		 *
		 */
		protected String onlineId;
		
		/******************************************************************************
		 * Date of transaction.
		 *
		 */
		protected String date;
		
		/******************************************************************************
		 * Account of user doing transaction.
		 *
		 */
		protected String account;
		
		/******************************************************************************
		 * Type of transaction.
		 *
		 */
		protected String transactionType;
		
		/******************************************************************************
		 * Descriptions of transaction.
		 *
		 */
		protected String description;
		
		/******************************************************************************
		 * The amount. (Optional)
		 *
		 */
		protected BigDecimal amount;
		
		/******************************************************************************
		 * Getter method for amount.
		 *
		 * @return BigDecimal
		 */
		public BigDecimal getAmount() {
			return amount;
		}
		
		/******************************************************************************
		 * Setter method for amount.
		 *
		 */
		@XmlAttribute(name = "amount")
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		/******************************************************************************
		 * Getter method for online_id of transaction.
		 *
		 * @return String
		 */
		public String getOnlineId() {
			return onlineId;
		}
		
		/******************************************************************************
		 * Setter method for online_id of transaction.
		 *
		 */
		@XmlAttribute(name = "online_id", required = true)
		public void setOnlineId(String onlineId) {
			this.onlineId = onlineId;
		}
		
		/******************************************************************************
		 * Getter method for date of transaction.
		 *
		 * @return String
		 */
		public String getDate() {
			return date;
		}
		
		/******************************************************************************
		 * Setter method for date of transaction.
		 *
		 */
		@XmlAttribute(name = "date", required = true)
		public void setDate(String date) {
			this.date = date;
		}
		
		/******************************************************************************
		 * Getter method for account of user doing transaction.
		 *
		 * @return String
		 */
		public String getAccount() {
			return account;
		}
		
		/******************************************************************************
		 * Setter method for account of user doing transaction.
		 *
		 */
		@XmlAttribute(name = "account", required = true)
		public void setAccount(String account) {
			this.account = account;
		}
		
		/******************************************************************************
		 * Getter method for type of transaction.
		 *
		 * @return String
		 */
		public String getTransactionType() {
			return transactionType;
		}
		
		/******************************************************************************
		 * Setter method for type of transaction.
		 *
		 */
		@XmlAttribute(name = "transaction_type", required = true)
		public void setTransactionType(String transactionType) {
			this.transactionType = transactionType;
		}
		
		/******************************************************************************
		 * Getter method for description related to transaction.
		 *
		 * @return String
		 */
		public String getDescription() {
			return description;
		}
		
		/******************************************************************************
		 * Setter method for description related to transaction.
		 *
		 */
		@XmlAttribute(name = "description", required = true)
		public void setDescription(String description) {
			this.description = description;
		}
	}
}
