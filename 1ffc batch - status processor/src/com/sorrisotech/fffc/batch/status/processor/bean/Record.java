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

import java.util.List;

/**************************************************************************************************
 * Class which holds records for Records.
 * 
 * @author Rohit Singh
 * 
 */
public class Record {
	
	/**************************************************************************
	 * Ids of the user.
	 */
	private String m_szUserId;
	
	/**************************************************************************
	 * Flag denotes if the payment is disabled.
	 */
	private boolean m_bPaymentDisabled;
	
	/**************************************************************************
	 * Flag denotes if the payment disabledDQ.
	 */
	private boolean m_bPaymentDisabledDQ;
	
	/**************************************************************************
	 * Flag denotes if the ach is disabled.
	 */
	private boolean m_bAchDisabled;
	
	/**************************************************************************
	 * Flag denotes if the portal access is disabled.
	 */
	private boolean m_bPortalAcessDisabled;
	
	/**************************************************************************
	 * Flag denotes if the recurring payment is disabled.
	 */
	private boolean m_bRecurringPaymentDisabled;
	
	/**************************************************************************
	 * Flag denotes if the recurring payment is disabled until current.
	 */
	private boolean m_bRecurringPaymentDisabledUntilCurrent;
	
	/**************************************************************************
	 * Bill date of the status feed.
	 */
	private String m_szBillDate;
	
	/**************************************************************************
	 * The internal account number of the bill.
	 */
	private String m_szInternalAccount;
	
	/**************************************************************************
	 * The current amount due of the bill.
	 */
	private String m_szCurrentAmountDue;
	
	/**************************************************************************
	 * The bill amount.
	 */
	private String m_szMonthlyPaymentAmount;
	
	/**************************************************************************
	 * The payment group of the bill.
	 */
	private String m_szPaymentGroup;
	
	/**************************************************************************
	 * The status date of the bill.
	 */
	private String m_szStatusDate;
	
	/**************************************************************************
	 * The flag denotes if the account is current.
	 */
	private boolean m_bAccountCurrent;
	
	/**************************************************************************
	 * List of all scheduled payment records for user.
	 */
	private List<ScheduledPayment> m_cScheduledPayments;
	
	/**************************************************************************
	 * List of all recurring payment records for user.
	 */
	private List<RecurringPayment> m_cRecurringPayments;

	/**************************************************************************
	 * Gets the value of m_szUserId.
	 * 
	 * @return m_szUserId
	 */
	public String getUserId() {
		return m_szUserId;
	}

	/**************************************************************************
	 * Sets the value of m_szUserId.
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.m_szUserId = userId;
	}

	/**************************************************************************
	 * Gets the value of m_bPaymentDisabled.
	 * 
	 * @return m_bPaymentDisabled
	 */
	public boolean isPaymentDisabled() {
		return m_bPaymentDisabled;
	}

	/**************************************************************************
	 * Sets the value of m_bPaymentDisabled.
	 * 
	 * @param paymentDisabled
	 */
	public void setPaymentDisabled(boolean paymentDisabled) {
		this.m_bPaymentDisabled = paymentDisabled;
	}

	/**************************************************************************
	 * Gets the value of m_bAchDisabled.
	 * 
	 * @return m_bAchDisabled
	 */
	public boolean isAchDisabled() {
		return m_bAchDisabled;
	}

	/**************************************************************************
	 * Sets the value of m_bAchDisabled.
	 * 
	 * @param achDiabled
	 */
	public void setAchDisabled(boolean achDiabled) {
		this.m_bAchDisabled = achDiabled;
	}

	/**************************************************************************
	 * Gets the value of m_bPaymentDisabledDQ.
	 * 
	 * @return m_bPaymentDisabledDQ
	 */
	public boolean isPaymentDisabledDQ() {
		return m_bPaymentDisabledDQ;
	}

	/**
	 * Sets the value of m_bPaymentDisabledDQ.
	 * 
	 * @param paymentDisabledDQ
	 */
	public void setPaymentDisabledDQ(boolean paymentDisabledDQ) {
		this.m_bPaymentDisabledDQ = paymentDisabledDQ;
	}

	/**************************************************************************
	 * Gets the value of m_bPortalAcessDisabled.
	 * 
	 * @return m_bPortalAcessDisabled
	 */
	public boolean isPortalAcessDisabled() {
		return m_bPortalAcessDisabled;
	}

	/**************************************************************************
	 * Sets the value of m_bPortalAcessDisabled.
	 * 
	 * @param portalAcessDisabled
	 */
	public void setPortalAcessDisabled(boolean portalAcessDisabled) {
		this.m_bPortalAcessDisabled = portalAcessDisabled;
	}

	/**************************************************************************
	 * Gets the value of m_bRecurringPaymentDisabled.
	 * 
	 * @return m_bRecurringPaymentDisabled
	 */
	public boolean isRecurringPaymentDisabled() {
		return m_bRecurringPaymentDisabled;
	}

	/**************************************************************************
	 * Sets the value of m_bRecurringPaymentDisabled.
	 * 
	 * @param recurringPaymentDisabled
	 */
	public void setRecurringPaymentDisabled(boolean recurringPaymentDisabled) {
		this.m_bRecurringPaymentDisabled = recurringPaymentDisabled;
	}

	/**************************************************************************
	 * Gets the value of m_bRecurringPaymentDisabledUntilCurrent.
	 * 
	 * @return m_bRecurringPaymentDisabledUntilCurrent
	 */
	public boolean isRecurringPaymentDisabledUntilCurrent() {
		return m_bRecurringPaymentDisabledUntilCurrent;
	}

	/**************************************************************************
	 * Sets the value of m_bRecurringPaymentDisabledUntilCurrent.
	 * 
	 * @param recurringPaymentDisabledUntilCurrent
	 */
	public void setRecurringPaymentDisabledUntilCurrent(boolean recurringPaymentDisabledUntilCurrent) {
		this.m_bRecurringPaymentDisabledUntilCurrent = recurringPaymentDisabledUntilCurrent;
	}

	/**************************************************************************
	 * Gets the value of m_szBillDate.
	 * 
	 * @return m_szBillDate
	 */
	public String getBillDate() {
		return m_szBillDate;
	}

	/**************************************************************************
	 * Sets the value of m_szBillDate.
	 * 
	 * @param billDate
	 */
	public void setBillDate(String billDate) {
		this.m_szBillDate = billDate;
	}

	/**************************************************************************
	 * Gets the value of m_szInternalAccount.
	 * 
	 * @return m_szInternalAccount
	 */
	public String getInternalAccount() {
		return m_szInternalAccount;
	}

	/**************************************************************************
	 * Sets the value of m_szInternalAccount.
	 * 
	 * @param internalAccount
	 */
	public void setInternalAccount(String internalAccount) {
		this.m_szInternalAccount = internalAccount;
	}

	/**************************************************************************
	 * Gets the value of m_szCurrentAmountDue.
	 * 
	 * @return m_szCurrentAmountDue
	 */
	public String getCurrentAmountDue() {
		return m_szCurrentAmountDue;
	}

	/**************************************************************************
	 * Sets the value of m_szCurrentAmountDue.
	 * 
	 * @param currentAmountDue
	 */
	public void setCurrentAmountDue(String currentAmountDue) {
		this.m_szCurrentAmountDue = currentAmountDue;
	}

	/**************************************************************************
	 * Gets the value of m_szMonthlyPaymentAnount.
	 * 
	 * @return m_szMonthlyPaymentAnount
	 */
	public String getMonthlyPaymentAmount() {
		return m_szMonthlyPaymentAmount;
	}

	/**************************************************************************
	 * Sets the value of m_szMonthlyPaymentAmount.
	 * 
	 * @param monthlyPaymentAmount
	 */
	public void setMonthlyPaymentAmount(String monthlyPaymentAmount) {
		this.m_szMonthlyPaymentAmount = monthlyPaymentAmount;
	}

	/**************************************************************************
	 * Gets the value of m_szPaymentGroup.
	 * 
	 * @return m_szPaymentGroup
	 */
	public String getPaymentGroup() {
		return m_szPaymentGroup;
	}

	/**************************************************************************
	 * Sets the value of m_szPaymentGroup.
	 * 
	 * @param paymentGroup
	 */
	public void setPaymentGroup(String paymentGroup) {
		this.m_szPaymentGroup = paymentGroup;
	}

	/**************************************************************************
	 * Gets the value of m_szStatusDate.
	 * 
	 * @return m_szStatusDate
	 */
	public String getStatusDate() {
		return m_szStatusDate;
	}

	/**************************************************************************
	 * Sets the value of m_szStatusDate.
	 * 
	 * @param statusDate
	 */
	public void setStatusDate(String statusDate) {
		this.m_szStatusDate = statusDate;
	}

	/**************************************************************************
	 * Gets the value of m_bAccountCurrent.
	 * 
	 * @return m_bAccountCurrent
	 */
	public boolean isAccountCurrent() {
		return m_bAccountCurrent;
	}

	/**************************************************************************
	 * Sets the value of m_bAccountCurrent.
	 * 
	 * @param bAccountCurrent
	 */
	public void setAccountCurrent(boolean bAccountCurrent) {
		this.m_bAccountCurrent = bAccountCurrent;
	}

	/**************************************************************************
	 * Gets the value of m_cScheduledPayments.
	 * 
	 * @return list of ScheduledPayment
	 */
	public List<ScheduledPayment> getScheduledPayments() {
		return m_cScheduledPayments;
	}

	/**************************************************************************
	 * Sets the value of m_cScheduledPayments.
	 * 
	 * @param scheduledPayments list of ScheduledPayment
	 */
	public void setScheduledPayments(List<ScheduledPayment> scheduledPayments) {
		this.m_cScheduledPayments = scheduledPayments;
	}

	/**************************************************************************
	 * Gets the value of m_cRecurringPayments.
	 * 
	 * @return list of RecurringPayment
	 */
	public List<RecurringPayment> getRecurringPayments() {
		return m_cRecurringPayments;
	}

	/**************************************************************************
	 * Sets the value of m_cRecurringPayments.
	 * 
	 * @param recurringPayments list of RecurringPayment
	 */
	public void setRecurringPayments(List<RecurringPayment> recurringPayments) {
		this.m_cRecurringPayments = recurringPayments;
	}

}
