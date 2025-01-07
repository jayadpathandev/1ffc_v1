package com.sorrisotech.fffc.agent.pay;

import java.util.Calendar;

public class PaySession {
	// ************************************************************************
	private final long     mId;
	private final Calendar mExpiresAt;
	private String         mUserId;
	private String         mUserName;
	private String         mCompanyId;
	private String         mCustomerId;
	private String         mAccountId;
	private String         mAccountNumber;
	private String         mInvoice;
	private String         mPayGroup;
	private boolean        mAutomaticPayment = false;
	private boolean        mSaveSource;
	private String         mWalletName;
	private String         mWalletType;
	private String         mWalletAccount;
	private String         mWalletExpiry;
	private String         mWalletToken = "";
	private boolean        mAchEnabled = true;
	private String         mWalletDefault;
	private String         mPayTransactionType;
	
	public  enum		   PayStatus {
								created,
								started,
								error,
								pmtAccountChosen,
								oneTimePmtInProgress,
								automaticPmtRuleInProgress,
								transactionComplete,
								transactionIdExpired,
								transactionIdNotFound
								}
	private PayStatus		meSessStatus;
	
	// ************************************************************************
	public PaySession(
				final long id
			) {
		mId        = id;
		mExpiresAt = Calendar.getInstance();
		mExpiresAt.add(Calendar.HOUR, 1);
		// -- status starts with transactionIdExpired --
		meSessStatus = PayStatus.created;
	}
	
	// ************************************************************************
	public long id() {
		return mId;
	}
	public boolean isExpired() {
		
		// -- expiration by time or status --
		return Calendar.getInstance().after(mExpiresAt) ||
				(PayStatus.transactionIdExpired == meSessStatus);
	}

	// ************************************************************************
	public void customerId(
			final String value
			) {
		mCustomerId = value;
	}
	public String customerId() {
		return mCustomerId;
	}

	// ************************************************************************
	public void accountId(
			final String value
			) {
		mAccountId = value;
	}
	public String accountId() {
		return mAccountId;
	}
		
	// ************************************************************************
	public void invoice(
			final String value
			) {
		mInvoice = value;
	}
	public String invoice() {
		return mInvoice;
	}
	
	// ************************************************************************
	public void userId(
			final String value
			) {
		mUserId = value;
	}
	public String userId() {
		return mUserId;
	}

	// ************************************************************************
	public void username(
			final String value
			) {
		mUserName = value;
	}
	public String username() {
		return mUserName;
	}

	// ************************************************************************
	public void companyId(
			final String value
			) {
		mCompanyId = value;
	}
	public String companyId() {
		return mCompanyId;
	}

	// ************************************************************************
	public void accountNumber(
			final String value
			) {
		mAccountNumber = value;
	}
	public String accountNumber() {
		return mAccountNumber;
	}

	// ************************************************************************
	public void payGroup(
			final String value
			) {
		mPayGroup = value;
	}
	public String payGroup() {
		return mPayGroup;
	}

	// ************************************************************************
	public void automaticPayment(
			final boolean value
			) {
		mAutomaticPayment = value;
	}
	public boolean automaticPayment() {
		return mAutomaticPayment;
	}
	
	// ************************************************************************
	public void saveSource(
			final boolean value
			) {
		mSaveSource = value;
	}
	public boolean saveSource() {
		return mSaveSource;
	}	
	
	// ************************************************************************
	public void wallet(
			final String name,
			final String type,
			final String account,
			final String expiry,
			final String token,
			final String isDefault 
			) {
		mWalletName    = name;
		mWalletType    = type;
		mWalletAccount = account;
		mWalletExpiry  = expiry;
		mWalletToken   = token;
		mWalletDefault = isDefault;
	}
	
	public String walletName() {
		return mWalletName;
	}
	public String walletType() {
		return mWalletType;
	}
	public String walletAccount() {
		return mWalletAccount;
	}
	public String walletExpiry() {
		return mWalletExpiry;
	}
	public String walletToken() {
		return mWalletToken;
	}
	
	public String walletDefault() {
		return mWalletDefault;
	}
	
	/**
	 * Returns status for this session... checks to see if its expired
	 * 		first and expires it if we are at one of the end statuses.
	 * 
	 * @return status value
	 */
	public PayStatus getStatus() {
		
		PayStatus leStatus = meSessStatus;
		// -- check for expiration of this session --
		if (isExpired()) 
			leStatus = PayStatus.transactionIdExpired;

		// -- if the transaction is complete we can mark this expired for
		//		the next status call --
		switch (meSessStatus) {
			case transactionComplete:
			case error:
			case transactionIdExpired:
				meSessStatus = PayStatus.transactionIdExpired;
				break;
			default:
				break;
		}
		return leStatus;
	}
	
	/** 
	 * Sets the status for this session from the api level
	 * 
	 * @param ceStatus new status setting
	 */
	public void setStatus(final PayStatus ceStatus) {
		
		meSessStatus = ceStatus;
	}
	
	// ************************************************************************
	public void disableAch() {
		mAchEnabled = false;
	}
	public boolean isAchEnabled() {
		return mAchEnabled;
	}
	
	// ************************************************************************
	public void payTransactionType(final String value) {
		mPayTransactionType = value;
	}
	public String payTransactionType() {
		return mPayTransactionType;
	}
}
