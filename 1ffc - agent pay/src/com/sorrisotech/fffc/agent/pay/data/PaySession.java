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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class PaySession extends PaySessionData {

	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(PaySession.class);

	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired @Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	/***************************************************************************
	 * SQL to initialize the record in the database after it is created.
	 */
	@Autowired @Qualifier("pay.session.start")	
	private String mStartSessionSql = null;

	/***************************************************************************
	 * SQL to update the currently selected wallet information.
	 */
	@Autowired @Qualifier("pay.session.update.wallet")	
	private String mUpdateWalletSql = null;
	
	/***************************************************************************
	 * SQL to update the status of the pay session.
	 */
	@Autowired @Qualifier("pay.session.update.status")	
	private String mUpdateStatusSql = null;

	/***************************************************************************
	 * SQL to update the ACH enabled/disable status for the session.
	 */
	@Autowired @Qualifier("pay.session.update.ach")	
	private String mUpdateAchSql = null;

	/***************************************************************************
	 * SQL to update the auto pay exists state for the session.
	 */
	@Autowired @Qualifier("pay.session.update.auto.pay")	
	private String mUpdateAutoPaySql = null;
	
	// ************************************************************************
	public void start(
			final String userId,
			final String userName,
			final String customerId,
			final String accountId,
			final String accountNum,
			final String payGroup,
			final String invoice,
			final String companyId,
			final String autoPayExists
			) {
		this.mUserId        = userId;
		this.mUserName      = userName;
		this.mCustomerId    = customerId;
		this.mAccountId     = accountId;
		this.mAccountNum    = accountNum;
		this.mPayGroup      = payGroup;
		this.mInvoice       = invoice;
		this.mCompanyId     = companyId;
		this.mAutoPayExists = autoPayExists.equalsIgnoreCase("true");
		
		final var params = new HashMap<String, Object>();
		params.put("id",            mOnlineId);
		params.put("userId",        this.mUserId);
		params.put("userName",      this.mUserName);
		params.put("customerId",    this.mCustomerId);
		params.put("accountId",     this.mAccountId);
		params.put("accountNum",    this.mAccountNum);
		params.put("payGroup",      this.mPayGroup);
		params.put("invoice",       this.mInvoice);
		params.put("companyId"    , this.mCompanyId);
		params.put("autoPayExists", this.mAutoPayExists ? "y" : "n");
		
		final int updates = mJdbc.update(mStartSessionSql, params);
	
		if (updates == 0) {
			LOG.warn("FFFC_PAY_SESSION id " + mOnlineId + " is missing, could not update record.");
		}	
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
		
		final var params = new HashMap<String, Object>();
		params.put("id",        mOnlineId);
		params.put("name",      this.mWalletName);
		params.put("type",      this.mWalletType);
		params.put("account",   this.mWalletAccount);
		params.put("expiry",    this.mWalletExpiry);
		params.put("token",     this.mWalletToken);
		params.put("isDefault", this.mWalletDefault);
		
		final int updates = mJdbc.update(mUpdateWalletSql, params);
	
		if (updates == 0) {
			LOG.warn("FFFC_PAY_SESSION id " + mOnlineId + " is missing, could not update record.");
		}			
	}

	// ************************************************************************
	private void commit(
			final String sql,
			final String value
			) {
		final var params = new HashMap<String, Object>();
		params.put("id", mOnlineId);
		params.put("value", value);
		
		final int updates = mJdbc.update(sql, params);
	
		if (updates == 0) {
			LOG.warn("FFFC_PAY_SESSION id " + mOnlineId + " is missing, could not update record.");
		}		
	}
	
	// ************************************************************************
	public void setStatus(
				final PaySessionStatus status
			) {
		commit(mUpdateStatusSql, status.dbFormat());
		mStatus = status;
	}

	// ************************************************************************
	public void automaticPayment(
			final boolean value
			) {
		commit(mUpdateAutoPaySql, value ? "y" : "n");
		mAutoPayExists = value;
	}

	// ************************************************************************
	public void disableAch() {
		commit(mUpdateAchSql, "n");
		mAchEnabled = false;
	}
}
