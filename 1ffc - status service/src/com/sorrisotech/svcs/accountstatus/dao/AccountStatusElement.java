package com.sorrisotech.svcs.accountstatus.dao;

import java.math.BigDecimal;
import java.util.Calendar;

import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AcctStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AchEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.PayEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AutoPmtStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ContPrefsStats;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ViewAcct;

/**
 * Status information for a single account. Includes getters and setters for
 * the individual status items.
 * 
 * Primary Status Information includes:
 * 		AccountId -- internal account number
 * 		AcctStatus -- the status of that account
 * 		PayEnabled -- is payment enabled for this account, if not then reason
 * 		AchEnabled -- is ach enabled for this account, if not then reason
 * 		ViewAccount -- can the user view data on this account or even the portal
 * 
 * There's a bunch of other data available to use as well.	 	
 * 
 * @see EnumConst for more information on values
 * 
 * @author John A. Kowalonek
 * @since 25-Sep-2023
 * @version 01-Jan-2024 - Added additional status information
 * 
 */
public class AccountStatusElement {

	private		String			m_szStatusGroupId = "";
	private		String			m_szPaymentGroupId = "";
	private 	String			m_szCustomerId = "";
	private		String 	 		m_szAccountId = "";
	private		Boolean			m_bFinalBill = false;
	private 	Boolean 		m_bAcctClosed = false;
	private 	AcctStatus 		m_AcctStatus = AcctStatus.activeAccount;
	private 	PayEnabled 		m_PayEnabled = PayEnabled.enabled;
	private 	AchEnabled 		m_AchEnabled = AchEnabled.enabled;
	private 	ViewAcct		m_ViewAccount = ViewAcct.enabled;
	private 	BigDecimal		m_dOrigLoanAmt = null;
	private		BigDecimal		m_dMonthlyPayment = null;
	private		Integer			m_iTotalNumPayments = 0;
	private		Integer			m_iRemainingNumPayments = 0;
	private		Integer			m_iMostRecentUpdate = 0;
	private 	AutoPmtStatus 	m_AutoPmtStatus = AutoPmtStatus.eligible;
	private 	Boolean			m_bPortalEligible = true;
	private 	ContPrefsStats	m_ContPrefStatus = ContPrefsStats.enabled;
	private		BigDecimal		m_dMaxPaymentAmount = null;
	private 	BigDecimal		m_dCurrentAmtDue = null;
	private 	BigDecimal		m_dDebitConvenienceFee = null;
	private 	BigDecimal		m_dAccountBalance = null;
	
	/**
	 * Used by slf4J to log the contents of this object.
	 * 
	 */
	public String toString() {
		return ("Status Values: \n" +
					"    StatusGroupId - " + m_szStatusGroupId + "\n"+
					"    PaymentGroupId - " + m_szPaymentGroupId + "\n" +
					"    CustomerId - " + m_szCustomerId + "\n" +
					"    AccountId - " + m_szAccountId + "\n" +
					"    FinalBill - " + m_bFinalBill.toString() + "\n" +
					"    AcctClosed - " + m_bAcctClosed.toString() + "\n" +
					"    AcctStatus - " + m_AcctStatus.toString() + "\n" +
					"    PayEnabled - " + m_PayEnabled.toString() + "\n" +
					"    AchEnabled - " + m_AchEnabled.toString() + "\n" +
					"    ViewAccount - " + m_ViewAccount.toString() + "\n" +
					"    OrigLoanAmt - " + m_dOrigLoanAmt.toPlainString() + "\n" +
					"    MonthlyPayment - " + m_dMonthlyPayment.toPlainString() + "\n" +
					"    TotalNumPayments - " + m_iTotalNumPayments.toString() + "\n" +
					"    RemainingNumPayments - " + m_iRemainingNumPayments.toString() + "\n" +
					"    AutomaticPaymentStats - " + m_AutoPmtStatus.toString() + "\n" +
					"    PortalEligible - " + m_bPortalEligible.toString() + "\n" +
					"    ContactPrefsStats - " + m_ContPrefStatus.toString() + "\n" +
					"    MaxPaymentAcount - " + m_dMaxPaymentAmount.toString() + "\n" +
					"    CurrentAmtDue - " + m_dCurrentAmtDue.toString() + "\n" +
					"    DebitConvenienceFee - " + m_dDebitConvenienceFee.toString() + "\n" +
					"    MostRecentUpdate - " + m_iMostRecentUpdate.toString() + "\n");
	}
	
	public String getStatusGroupId() {
		return m_szStatusGroupId;
	}

	public void setStatusGroupId(final String cszStatusGroupId) {
		this.m_szStatusGroupId = cszStatusGroupId;
	}

	public String getPaymentGroupId() {
		return m_szPaymentGroupId;
	}

	public void setPaymentGroupId(final String cszPaymentGroupId) {
		this.m_szPaymentGroupId = cszPaymentGroupId;
	}

	public String getCustomerId() {
		return m_szCustomerId;
	}

	public void setCustomerId(final String cszCustomerId) {
		this.m_szCustomerId = cszCustomerId;
	}

	public String getAccountId() {
		return m_szAccountId;
	}

	public void setAccountId(final String cszAccountId) {
		m_szAccountId = cszAccountId;
	}

	public Boolean getFinalBill() {
		return m_bFinalBill;
	}
	
	public void setFinalBill(final Boolean cbFinalBill) {
		m_bFinalBill = cbFinalBill;
	}
	
	public Boolean isAcctClosed() {
		return m_bAcctClosed;
	}

	public void setAcctClosed(final Boolean cbAcctClosed) {
		this.m_bAcctClosed = cbAcctClosed;
	}
	
	public AcctStatus getAcctStatus() {
		return m_AcctStatus;
	}
	
	public void setAcctStatus(final AcctStatus cAcctStatus) {
		m_AcctStatus = cAcctStatus;
	}
	
	public PayEnabled getPayEnabled() {
		return m_PayEnabled;
	}

	public void setPayEnabled(final PayEnabled cPayEnabled) {
		this.m_PayEnabled = cPayEnabled;
	}

	public AchEnabled getAchEnabled() {
		return m_AchEnabled;
	}

	public void setAchEnabled(final AchEnabled cAchEnabled) {
		this.m_AchEnabled = cAchEnabled;
	}

	public ViewAcct getViewAccount() {
		return m_ViewAccount;
	}

	public void setViewAccount(final ViewAcct cViewAccount) {
		this.m_ViewAccount = cViewAccount;
	}
	
	public BigDecimal getOrigLoanAmt() {
		return m_dOrigLoanAmt;
	}

	public void setOrigLoanAmt(final BigDecimal cdOrigLoanAmt) {
		this.m_dOrigLoanAmt = cdOrigLoanAmt;
	}

	public BigDecimal getMonthlyPayment() {
		return m_dMonthlyPayment;
	}

	public void setMonthlyPayment(final BigDecimal cdMonthlyPayment) {
		this.m_dMonthlyPayment = cdMonthlyPayment;
	}

	public Integer getTotalNumPayments() {
		return m_iTotalNumPayments;
	}

	public void setTotalNumPayments(final Integer ciTotalNumPayments) {
		this.m_iTotalNumPayments = ciTotalNumPayments;
	}

	public Integer getRemainingNumPayments() {
		return m_iRemainingNumPayments;
	}

	public void setRemainingNumPayments(final Integer ciRemainingNumPayments) {
		this.m_iRemainingNumPayments = ciRemainingNumPayments;
	}

	public Integer getMostRecentUpdate() {
		return m_iMostRecentUpdate;
	}

	public void setMostRecentUpdate(final Integer cMostRecentUpdate) {
		this.m_iMostRecentUpdate = cMostRecentUpdate;
	}
	
	public AutoPmtStatus getAutoPaymentStatus () {
		return m_AutoPmtStatus;
	}
	
	public void setAutoPaymentStatus(final AutoPmtStatus cAutoPayStatus) {
		this.m_AutoPmtStatus = cAutoPayStatus;
	}
	
	public Boolean getPortalEligible() {
		return m_bPortalEligible;
	}
	
	public void setPortalEligible(final Boolean cbPortalEligible) {
		m_bPortalEligible = cbPortalEligible;
	}
	
	public ContPrefsStats getContactPrefsStatus() {
		return m_ContPrefStatus;
	}
	
	public void setContactPrefsStatus(final ContPrefsStats cContactPrefsStats) {
		m_ContPrefStatus = cContactPrefsStats;
	}
	
	public BigDecimal getMaxPaymentAmount() {
		return m_dMaxPaymentAmount;
	}
	
	void setMaxPaymentAmount(final BigDecimal cMaxPaymentAmount) {
		m_dMaxPaymentAmount = cMaxPaymentAmount;
	}
	
	public BigDecimal getCurrentAmountDue() {
		return m_dCurrentAmtDue;
	}
	
	void setCurrentAmountDue(final BigDecimal cCurrAmountDue) {
		m_dCurrentAmtDue = cCurrAmountDue;
	}
	
	public BigDecimal getAccountBalance() {
		return m_dAccountBalance;
	}

	void setAccountBalance(final BigDecimal cAcctBalance) {
		m_dAccountBalance = cAcctBalance;
	}
	public BigDecimal getDebitConvenienceFee() {
		return m_dDebitConvenienceFee;
	}
	
	void setDebitConvenienceFee(final BigDecimal cDebitConvenienceFee) {
		m_dDebitConvenienceFee = cDebitConvenienceFee;
	}

}
