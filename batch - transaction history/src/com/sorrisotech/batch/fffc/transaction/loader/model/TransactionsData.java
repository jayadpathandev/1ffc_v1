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

@XmlRootElement(name = "TransactionsData")
public class TransactionsData {
	
	protected TransactionsData.FileHeader fileHeader;
	
	protected TransactionsData.TransactionRecord transactionRecord;
	
	protected TransactionsData.Footer footer;
	
	public TransactionsData.FileHeader getFileHeader() {
		return fileHeader;
	}
	
	@XmlElement(required = true)
	public void setFileHeader(TransactionsData.FileHeader value) {
		this.fileHeader = value;
	}
	
	public TransactionsData.TransactionRecord getTransactionRecord() {
		return transactionRecord;
	}
	
	@XmlElement(required = true)
	public void setTransactionRecords(TransactionsData.TransactionRecord value) {
		this.transactionRecord = value;
	}
	
	public TransactionsData.Footer getFooter() {
		return footer;
	}
	
	@XmlElement(required = true)
	public void setFooter(TransactionsData.Footer value) {
		this.footer = value;
	}
	
	@XmlRootElement(name = "Footer")
	public static class Footer {
		
		protected int records;
		
		public int getRecords() {
			return records;
		}
		
		@XmlAttribute(required = true)
		public void setRecords(int records) {
			this.records = records;
		}
	}
	
	
		@XmlRootElement(name = "TransactionRecord")
		public static class TransactionRecord {
			
			protected TransactionsData.TransactionRecord.Invoice invoice;
			
			protected String onlineId;
			
			protected String date;
			
			public String getDate() {
				return date;
			}

			@XmlAttribute(name = "date")
			public void setDate(String date) {
				this.date = date;
			}

			protected String description;
			
			public TransactionsData.TransactionRecord.Invoice getInvoice() {
				if (invoice == null) {
					invoice = new TransactionsData.TransactionRecord.Invoice();
				}
				return this.invoice;
			}
			
			@XmlElement(name = "Invoice" , required = true)
			public void setInvoice(TransactionsData.TransactionRecord.Invoice invoice) {
				this.invoice = invoice;
			}

			public String getOnlineId() {
				return onlineId;
			}
			
			@XmlAttribute(name = "online_id")
			public void setOnlineId(String value) {
				this.onlineId = value;
			}
			
			public String getDescription() {
				return description;
			}
			
			@XmlAttribute(name = "description")
			public void setDescription(String value) {
				this.description = value;
			}
			
			@XmlRootElement(name = "Invoice")
			public static class Invoice {

				protected String pmtGroup;
				
				protected String account;
				
				protected BigDecimal amount;
				
				public String getAccount() {
					return account;
				}
				
				@XmlAttribute(name = "account")
				public void setAccount(String value) {
					this.account = value;
				}
				
				public BigDecimal getAmount() {
					return amount;
				}
				
				@XmlAttribute(name = "amount")
				public void setAmount(BigDecimal value) {
					this.amount = value;
				}
				
				public String getPmtGroup() {
					return pmtGroup;
				}
				
				@XmlAttribute(name = "pmt_group")
				public void setPmtGroup(String value) {
					this.pmtGroup = value;
				}
			}
		}
	
	@XmlRootElement(name = "FileHeader")
	public static class FileHeader {

		protected String group;
		
		public String getGroup() {
			return group;
		}
		
		@XmlAttribute(name = "group")
		public void setGroup(String value) {
			this.group = value;
		}
	}
}
