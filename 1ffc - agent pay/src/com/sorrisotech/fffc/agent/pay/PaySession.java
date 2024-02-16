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
	private boolean        mSaveSource;
	private String         mWalletName;
	private String         mWalletType;
	private String         mWalletAccount;
	private String         mWalletExpiry;
	private String         mWalletToken = "";
	
	// ************************************************************************
	public PaySession(
				final long id
			) {
		mId        = id;
		mExpiresAt = Calendar.getInstance();
		mExpiresAt.add(Calendar.HOUR, 1);
	}
	
	// ************************************************************************
	public long id() {
		return mId;
	}
	public boolean isExpired() {
		return Calendar.getInstance().after(mExpiresAt);
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
			final String token
			) {
		mWalletName    = name;
		mWalletType    = type;
		mWalletAccount = account;
		mWalletExpiry  = expiry;
		mWalletToken   = token;
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
}
