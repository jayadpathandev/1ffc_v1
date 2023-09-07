/* (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 40 Nagog Park
 * Acton, MA 01720
 * +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona 
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition", 
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network", 
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay", 
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active 
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.batch.fffc.transaction.loader.model;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * com.sorrisotech.batch.fffc.transaction.loader.model package.
 * <p>
 * An ObjectFactory allows you to programmatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 *@author Asrar Saloda
 */
@XmlRegistry
public class ObjectFactory {
	
	/**
	 * Create a new ObjectFactory that can be used to create new instances of schema
	 * derived classes for package: com.sorrisotech.batch.fffc.transaction.loader.model
	 *
	 */
	public ObjectFactory() {
	}
	
	/**
	 * Create an instance of {@link TransactionsData }
	 *
	 */
	public TransactionsData createTransactionData() {
		return new TransactionsData();
	}
	
	/**
	 * Create an instance of {@link TransactionsData.TransactionRecord }
	 *
	 */
	public TransactionsData.TransactionRecord createTransactionRecord() {
		return new TransactionsData.TransactionRecord();
	}
	
	/**
	 * Create an instance of {@link TransactionsData.FileHeader }
	 *
	 */
	public TransactionsData.FileHeader createTransactionsHeader() {
		return new TransactionsData.FileHeader();
	}
	
	/**
	 * Create an instance of {@link TransactionsData.Footer }
	 *
	 */
	public TransactionsData.Footer createTransactionsFooter() {
		return new TransactionsData.Footer();
	}
	
	/**
	 * Create an instance of
	 * {@link TransactionsData.TransactionRecord.Invoice }
	 *
	 */
	public TransactionsData.TransactionRecord.Invoice createTransactionRecordsRecordInvoice() {
		return new TransactionsData.TransactionRecord.Invoice();
	}
	
}
