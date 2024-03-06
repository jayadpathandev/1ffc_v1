/* (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
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
package com.sorrisotech.fffc.batch.status.processor.bean;

import java.math.BigDecimal;

/**************************************************************************************************
 * Class which holds records for recurring payments.
 * 
 * @author Rohit Singh
 * 
 */
public class RecurringPayment {
	
	/**************************************************************************
	 * Recurring payment Id
	 */
	private BigDecimal m_cId;
	
	/**************************************************************************
	 * Recurring payment account number
	 */
	private String m_szAccounutNumber;
	
	/**************************************************************************
	 * Recurring payment source name
	 */
	private String m_szWalletNickName;
	
	/**************************************************************************
	 * Recurring payment source type
	 */
	private String m_szSourceType;
	
	/**************************************************************************
	 * Internal account number for which the payment is scheduled
	 */
	private String m_szInternalAccount;
	
	/**************************************************************************
	 * Payment group.
	 */
	private String m_szPayGroup;

	/**************************************************************************
	 * Gets the value of m_cId
	 * 
	 * @return
	 */
	public BigDecimal getId() {
		return m_cId;
	}

	/**************************************************************************
	 * Sets the value of m_cId
	 * 
	 * @param id
	 */
	public void setId(BigDecimal id) {
		this.m_cId = id;
	}

	/**************************************************************************
	 * Gets the value of m_szAccounutNumber
	 * 
	 * @return
	 */
	public String getAccounutNumber() {
		return m_szAccounutNumber;
	}

	/**************************************************************************
	 * Sets the value of m_szAccounutNumber
	 * 
	 * @param accounutNumber
	 */
	public void setAccounutNumber(String accounutNumber) {
		this.m_szAccounutNumber = accounutNumber;
	}
	
	/**************************************************************************
	 * Gets the value of m_szWalletNickName
	 * 
	 * @return
	 */
	public String getWalletNickName() {
		return m_szWalletNickName;
	}

	/**************************************************************************
	 * Sets the value of m_szWalletNickName
	 * 
	 * @param walletNickName
	 */
	public void setWalletNickName(String walletNickName) {
		this.m_szWalletNickName = walletNickName;
	}

	/**************************************************************************
	 * Gets the value of m_szSourceType
	 * 
	 * @return
	 */
	public String getSourceType() {
		return m_szSourceType;
	}

	/**************************************************************************
	 * Sets the value of m_szSourceType
	 * 
	 * @param sourceType
	 */
	public void setSourceType(String sourceType) {
		this.m_szSourceType = sourceType;
	}
	
	/**************************************************************************
	 * Getter method for internal account number.
	 * @return the internal account number
	 */
	public String getInternalAccount() {
		return m_szInternalAccount;
	}

	/**************************************************************************
	 * Setter method for internal account number.
	 * @param internalAccount The internal account number to set.
	 */
	public void setInternalAccount(String internalAccount) {
		this.m_szInternalAccount = internalAccount;
	}

	/**************************************************************************
	 * Getter method for payment group.
	 * @return the payGroup
	 */
	public String getPayGroup() {
		return m_szPayGroup;
	}

	/**************************************************************************
	 * @param payGroup the payGroup to set
	 */
	public void setPayGroup(String payGroup) {
		this.m_szPayGroup = payGroup;
	}
}
