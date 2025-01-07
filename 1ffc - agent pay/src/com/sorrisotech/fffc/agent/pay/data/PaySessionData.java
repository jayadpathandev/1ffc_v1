/*
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public abstract class PaySessionData {
	// ************************************************************************
	protected long             mOnlineId;
	protected Calendar         mExpiresAt;
	protected PaySessionStatus mStatus;
	protected String           mUserId;
	protected String           mUserName;
	protected String           mCompanyId;
	protected String           mCustomerId;
	protected String           mAccountId;
	protected String           mAccountNum;
	protected String           mInvoice;
	protected String           mPayGroup;
	protected String           mWalletName;
	protected String           mWalletType;
	protected String           mWalletAccount;
	protected String           mWalletExpiry;
	protected String           mWalletToken;
	protected String           mWalletDefault;
	protected boolean          mAutoPayExists;
	protected boolean          mSaveSource;
	protected boolean          mAchEnabled;

	// ************************************************************************
	private static String readString(
			final ResultSet set,
			final String    column
			) throws SQLException {
		final var retval = set.getString(column);
		
		if (retval == null) return "";
		
		return retval;		
	}

	// ************************************************************************
	private static boolean readBoolean(
			final ResultSet set,
			final String    column
			) throws SQLException {		
		final var value = set.getString(column);
		
		if (value == null) return false;
		
		return value.equalsIgnoreCase("y");		
	}
	
	// ************************************************************************
	public void init(
			final ResultSet set
			) throws SQLException {
		mOnlineId  = set.getLong("FPS_ONLINE_ID");
		mExpiresAt = Calendar.getInstance();
		mExpiresAt.setTime(set.getDate("FPS_EXPIRES_AT"));
		
		mStatus        = PaySessionStatus.lookup(set.getString("FPS_STATUS"));
		mUserId        = readString(set, "FPS_USER_ID");
		mUserName      = readString(set, "FPS_USER_NAME");
		mCompanyId     = readString(set, "FPS_COMPANY_ID");
		mCustomerId    = readString(set, "FPS_CUSTOMER_ID");
		mAccountId     = readString(set, "FPS_ACCOUNT_ID");
		mAccountNum    = readString(set, "FPS_ACCOUNT_NUM");
		mInvoice       = readString(set, "FPS_INVOICE");
		mPayGroup      = readString(set, "FPS_PAY_GROUP");
		mWalletName    = readString(set, "FPS_WALLET_NAME");
		mWalletType    = readString(set, "FPS_WALLET_TYPE");
		mWalletAccount = readString(set, "FPS_WALLET_ACCOUNT");
		mWalletExpiry  = readString(set, "FPS_WALLET_EXPIRY");
		mWalletToken   = readString(set, "FPS_WALLET_TOKEN");
		mWalletDefault = readString(set, "FPS_WALLET_DEFAULT");
		mAutoPayExists = readBoolean(set, "FPS_AUTO_PAY_EXISTS");
		mSaveSource    = readBoolean(set, "FPS_SAVE_SOURCE");
		mAchEnabled    = readBoolean(set, "FPS_ACH_ENABLED");
	}
			
	// ************************************************************************
	public long id() {
		return mOnlineId;
	}
	public String userId() {
		return mUserId;
	}
	public String username() {
		return mUserName;
	}
	public String companyId() {
		return mCompanyId;
	}
	public String customerId() {
		return mCustomerId;
	}
	public String accountId() {
		return mAccountId;
	}
	public String accountNumber() {
		return mAccountNum;
	}
	public String invoice() {
		return mInvoice;
	}
	public String payGroup() {
		return mPayGroup;
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
	public boolean automaticPayment() {
		return mAutoPayExists;
	}
	public boolean saveSource() {
		return mSaveSource;
	}	
	public boolean isAchEnabled() {
		return mAchEnabled;
	}
	
	//*************************************************************************
	abstract public void setStatus(
			PaySessionStatus status
			);
	
	private boolean isExpired() {
		return Calendar.getInstance().after(mExpiresAt) ||
				(PaySessionStatus.transactionIdExpired == mStatus);
	}

	public PaySessionStatus getStatus() {
		
		PaySessionStatus leStatus = mStatus;
		
		// -- check for expiration of this session --
		if (isExpired()) 
			leStatus = PaySessionStatus.transactionIdExpired;

		// -- if the transaction is complete we can mark this expired for
		//		the next status call --
		switch (mStatus) {
			case transactionComplete:
			case error:
			case transactionIdExpired:
				setStatus(PaySessionStatus.transactionIdExpired);
				break;
			default:
				break;
		}
		
		return leStatus;
	}	
}
