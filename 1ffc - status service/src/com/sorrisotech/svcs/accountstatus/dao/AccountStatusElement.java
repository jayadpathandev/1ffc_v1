package com.sorrisotech.svcs.accountstatus.dao;

import java.math.BigDecimal;

import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AcctStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AchEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.PayEnabled;
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
 * @version 09-Oct-2023 - moved out to DAO.
 * 
 */
public class AccountStatusElement {

	private		String		m_szStatusGroupId = "";
	private		String		m_szPaymentGroupId = "";
	private 	String		m_szCustomerId = "";
	private		String 	 	m_szAccountId = "";
	private		Boolean		m_bFinalBill = false;
	private 	Boolean 	m_bAcctClosed = false;
	private 	AcctStatus 	m_AcctStatus = AcctStatus.activeAccount;
	private 	PayEnabled 	m_PayEnabled = PayEnabled.enabled;
	private 	AchEnabled 	m_AchEnabled = AchEnabled.enabled;
	private 	ViewAcct	m_ViewAccount = ViewAcct.enabled;
	private 	BigDecimal	m_dOrigLoanAmt = null;
	private		BigDecimal	m_dMonthlyPayment = null;
	private		Integer		m_iTotalNumPayments = 0;
	private		Integer		m_iRemainingNumPayments = 0;
	private		Integer		m_iMostRecentUpdate = 0;
	
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
	
}
