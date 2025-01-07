/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
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
package com.sorrisotech.fffc.batch.reimport.account;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.fffc.batch.reimport.report.Reporter;
import com.sorrisotech.fffc.batch.reimport.sanity.Check;

/**************************************************************************************************
 * Update all the changes required in the database to change the account ID for an account number.
 */
public class AccountNumWriter 
	extends NamedParameterJdbcDaoSupport 
	implements ItemWriter<AccountChange> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountNumWriter.class);

	/**************************************************************************
	 * SQL to insert a record in PROF_COMPANY_ORGID.
	 */
	private String mInsertProfCompanyOrgid = null;

	/**************************************************************************
	 * SQL to delete records in PROF_COMPANY_ORGID.
	 */
	private String mDeleteProfCompanyOrgid = null;
	
	/**************************************************************************
	 * SQL to delete records in PROF_COMPANY_ACCOUNT.
	 */
	private String mDeleteProfCompanyAccount = null;

	/**************************************************************************
	 * SQL to delete records in TM_ACCOUNT.
	 */
	private String mDeleteTmAccount = null;

	/**************************************************************************
	 * SQL to update the records in TM_ACCOUNT.
	 */
	private String mUpdateTmAccount = null;
	
	/**************************************************************************
	 * SQL to update records in PMT_GROUPING.
	 */
	private String mUpdatePmtGrouping = null;

	/**************************************************************************
	 * SQL to update records in PMT_AUTOMATIC_GROUPING.
	 */
	private String mUpdatePmtAutomaticGrouping = null;

	/**************************************************************************
	 * SQL to update records in PMT_AUTOMATIC_DOCUMENTS.
	 */
	private String mUpdatePmtAutomaticDocuments = null;

	/**************************************************************************
	 * SQL to update records in BILL.
	 */
	private String mUpdateBill = null;

	/**************************************************************************
	 * SQL to update records in FFFC_TRANSACTIONS.
	 */
	private String mUpdateFffcTransactions = null;
	
	/**************************************************************************
	 * Class to perform a sanity check on the company.
	 */
	private Check mSanity = null;
	
	/**************************************************************************
	 * Class to report the DB state before an after changes.
	 */
	private Reporter mReporter;
	
	/**************************************************************************
	 * Update PROF_COMPANY_ORGID to remove the old OrgIds and add in the new
	 * OrgId. This the OrgId did not change this method does not modify the
	 * database.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updateProfCompanyOrgid(
			final AccountChange change
			) throws Exception {
		final String     org     = change.getNewOrgId();
		final BigDecimal company = change.getCompanyId();
		
		if (change.getCompanyId() != null && change.getOldOrgIds().isEmpty() == false) {
			
			//-----------------------------------------------------------------
			// Add the new OrgId to the company.
			final var insert = new HashMap<String, Object>();
			
			insert.put("companyId", change.getCompanyId());
			insert.put("orgId", change.getNewOrgId());		
			
			final var inserts = getNamedParameterJdbcTemplate().update(
					mInsertProfCompanyOrgid,
					insert
					);
			
			if (inserts == 1) {
				LOG.info(
					"PROF_COMPANY_ORGID - Added ORGID [" + org + "] for COMPANYID [" + company + "]."
					);
			} else {
				throw new Exception(
					"PROF_COMPANY_ORGID - Failed to add ORGID [" + org +"] for COMPANYID [" + company + "]."
					);
			}
			
			//-----------------------------------------------------------------
			// Remove the old OrgId(s) from the company.
			final StringBuilder orgIds = new StringBuilder();
			
			for(final var orgId : change.getOldOrgIds()) {
				if (orgIds.isEmpty() == false) {
					orgIds.append(", ");
				}
				orgIds.append('\'');
				orgIds.append(orgId);
				orgIds.append('\'');
			}
			
			final var delete = new HashMap<String, Object>();
			
			delete.put("companyId", change.getCompanyId());
			
			final var deletes = getNamedParameterJdbcTemplate().update(
				mDeleteProfCompanyOrgid.replace("@VALUES@", orgIds.toString()),
				delete
				);
			LOG.info(
				"PROF_COMPANY_ORGID - Removed ORGIDs (" + orgIds + ") from COMPANYID [" + company + "]."
				);
			
			if (deletes != change.getOldOrgIds().size()) {
				LOG.warn(
					"PROF_COMPANY_ORGID - Number of rows deleted [" + deletes + 
					" does not match number of old OrgIds [" +
					change.getOldOrgIds().size() + "] " +
					"for COMPANYID [" + company + "]."
					);				
			}			
		} else if (change.getCompanyId() == null) {
			LOG.info(
				"PROF_COMPANY_ORGID - Unchanged, not registered."
				);			
		} else {
			LOG.info(
				"PROF_COMPANY_ORGID - Unchanged OrgID [" + org + "] did not change."
				);			
		}
	}
	
	/**************************************************************************
	 * Update PROF_COMPANY_ACCOUNT to remove the old accounts and add the new
	 * account. 
	 * 
	 * @param change  The change we are processing.
	 */
	private void updateProfCompanyAccount(
			final AccountChange change
			) throws Exception {
		final BigDecimal company = change.getCompanyId();

		if (change.getCompanyId() != null) {
			if (change.getTmRecsToDelete().isEmpty() == false) { 
				final StringBuilder accounts = new StringBuilder();
				
				for(final var recId : change.getTmRecsToDelete()) {
					if (accounts.isEmpty() == false) {
						accounts.append(", ");
					}
					accounts.append(recId.toPlainString());
				}
				
				final var delete = new HashMap<String, Object>();
				
				delete.put("companyId", change.getCompanyId());
				
				final var deletes = getNamedParameterJdbcTemplate().update(
					mDeleteProfCompanyAccount.replace("@VALUES@", accounts.toString()),
					delete
					);
				LOG.info(
					"PROF_COMPANY_ACCOUNT - Removed [" + deletes + "] old accounts from COMPANY_ID [" + company + "]."
					);
			} else {
				LOG.info(
					"PROF_COMPANY_ACCOUNT - Unchanged, no bills with new acocunt id."
					);
			}
		} else {
			LOG.info(
				"PROF_COMPANY_ACCOUNT - Unchanged, unregistered."
				);			
		}
	}

	/**************************************************************************
	 * Update TM_ACCOUNT to remove the old accounts.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updateTmAccount(
			final AccountChange change
			) throws Exception {
		// --------------------------------------------------------------------
		// Delete the old records
		final StringBuilder accounts = new StringBuilder();
		
		for(final var recId : change.getTmRecsToDelete()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append(recId.toPlainString());
		}
							
		final var deletes = getNamedParameterJdbcTemplate().update(
			mDeleteTmAccount.replace("@VALUES@", accounts.toString()),
			new HashMap<String, Object>()
			);
		LOG.info(
			"TM_ACCOUNT - Removed [" + deletes + "] old records."
			);
		
		// --------------------------------------------------------------------
		// Update the new record to be assigned to the company.
		if (change.getOldBillRecId() != null) {
			final var update = new HashMap<String, Object>();
			update.put("id", change.getOldBillRecId());
			update.put("companyId", change.getCompanyId());
			update.put("accountId", change.getNewAccountId());
			update.put("orgId", change.getNewOrgId());
			
			final var updates = getNamedParameterJdbcTemplate().update(
				mUpdateTmAccount,
				update
				);
			
			if (updates != 1) {
				throw new Exception(
					"TM_ACCOUNT - Failed to update TM_ACCOUNT record [" + update.get("id") + 
					"] with the correct COMPANY_ID [" + update.get("companyId") + 
					"], ACCOUNT_NUMBER [" + update.get("accountId") +
					"], and ORG_ID [" + update.get("orgId") + "]."
					);
			} else {
				LOG.info(
					"TM_ACCOUNT - Update old TM_ACCOUNT record [" + update.get("id") + 
					"] with the correct COMPANY_ID [" + update.get("companyId") + 
					"], ACCOUNT_NUMBER [" + update.get("accountId") +
					"], and ORG_ID [" + update.get("orgId") + "]."
					);
			}
		} else if (change.getNewBillRecId() != null) {
			LOG.info(
					"TM_ACCOUNT - Using new bill record, no changes."
					);			
		} else {
			LOG.info(
				"TM_ACCOUNT - No bill records loaded, no changes."
				);			
		}
	}

	/**************************************************************************
	 * Update BILL to change the old Account IDs to the new.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updateBill(
			final AccountChange change
			) {
		final StringBuilder accounts = new StringBuilder();
		
		for(final var account : change.getOldAccountIds()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append('\'');
			accounts.append(account);
			accounts.append('\'');
		}

		final var update = new HashMap<String, Object>();
		
		update.put("accountId", change.getNewAccountId());

		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdateBill.replace("@VALUES@", accounts.toString()),
			update
			);
		LOG.info(
			"BILL - Updated ACCOUNT to [" + change.getNewAccountId() + "] for [" +
			updates + "] rows." 
			);
	}
	
	/**************************************************************************
	 * Update PMT_GROUPING to change the old Account IDs to the new.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updatePmtGrouping(
			final AccountChange change
			) {
		final StringBuilder accounts = new StringBuilder();
		
		for(final var account : change.getOldAccountIds()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append('\'');
			accounts.append(account);
			accounts.append('\'');
		}

		final var update = new HashMap<String, Object>();
		
		update.put("accountId", change.getNewAccountId());
		update.put("accountNum", change.getAccountNum());

		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdatePmtGrouping.replace("@VALUES@", accounts.toString()),
			update
			);
		LOG.info(
			"PMT_GROUPING - Updated INTERNAL_ACCOUNT_NUMBER to [" +
			change.getNewAccountId() + "] for [" + updates + "] rows."
			);
	}

	/**************************************************************************
	 * Update PMT__AUTOMATIC_GROUPING to change the old Account IDs to the new.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updatePmtAutomaticGrouping(
			final AccountChange change
			) {
		final StringBuilder accounts = new StringBuilder();
		
		for(final var account : change.getOldAccountIds()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append('\'');
			accounts.append(account);
			accounts.append('\'');
		}

		final var update = new HashMap<String, Object>();
		
		update.put("accountId", change.getNewAccountId());
		update.put("accountNum", change.getAccountNum());

		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdatePmtAutomaticGrouping.replace("@VALUES@", accounts.toString()),
			update
			);
		LOG.info(
			"PMT_AUTOMATIC_GROUPING - Updated INTERNAL_ACCOUNT_NUMBER to [" +
			change.getNewAccountId() + "] for [" + updates + "] rows."
			);
	}

	/**************************************************************************
	 * Update PMT_AUTOMATIC_DOCUMENTS to change the old Account IDs to the new.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updatePmtAutomaticDocuments(
			final AccountChange change
			) {
		final StringBuilder accounts = new StringBuilder();
		
		for(final var account : change.getOldAccountIds()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append('\'');
			accounts.append(account);
			accounts.append('\'');
		}

		final var update = new HashMap<String, Object>();
		
		update.put("accountId", change.getNewAccountId());

		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdatePmtAutomaticDocuments.replace("@VALUES@", accounts.toString()),
			update
			);
		LOG.info(
			"PMT_AUTOMATIC_DOCUMENTS - Updated INTERNAL_ACCOUNT_NUMBER to [" +
			change.getNewAccountId() + "] for [" + updates + "] rows."
			);
	}

	/**************************************************************************
	 * Update FFFC_TRANSACTIONS to change the old Account IDs to the new.
	 * 
	 * @param change  The change we are processing.
	 * 
	 * @return  True if everything is good, otherwise false.
	 */
	private boolean updateFffcTransactions(
			final AccountChange change
			) {
		final StringBuilder accounts = new StringBuilder();
		
		for(final var account : change.getOldAccountIds()) {
			if (accounts.isEmpty() == false) {
				accounts.append(", ");
			}
			accounts.append('\'');
			accounts.append(account);
			accounts.append('\'');
		}

		final var update = new HashMap<String, Object>();
		
		update.put("accountId", change.getNewAccountId());

		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdateFffcTransactions.replace("@VALUES@", accounts.toString()),
			update
			);
		LOG.info(
			"FFFC_TRANSACTIONS - Updated ACCOUNT to [" +
			change.getNewAccountId() + "] for [" + updates + "] rows."
			);
		
		return true;		
	}	
	
	/**************************************************************************
	 * Process the changes for a re-imported account.
	 * 
	 * @param changes  The list of changes. 
	 */
	@Override
	public void write(
			List<? extends AccountChange> changes
			) throws Exception {

		for (final var change : changes) {
			mReporter.beforeAccountChange(
				change.getCompanyId(),
				change.getOldOrgIds(),
				change.getNewOrgId(),
				change.getAccountNum(),
				change.getOldAccountIds(),
				change.getNewAccountId()
			);
			
			try {
				updateProfCompanyOrgid(change);
				updateProfCompanyAccount(change);
				updateTmAccount(change);
				updateBill(change);
				updatePmtGrouping(change);
				updatePmtAutomaticGrouping(change);
				updatePmtAutomaticDocuments(change);
				updateFffcTransactions(change);
				
				var companyId = change.getCompanyId();						
				if (companyId != null) {
					mSanity.verifyCompany(change.getNewOrgId(),  companyId);
				}
				
			} finally {
				mReporter.afterAccountChange(
					change.getCompanyId(),
					change.getOldOrgIds(),
					change.getNewOrgId(),
					change.getAccountNum(),
					change.getOldAccountIds(),
					change.getNewAccountId()
				);
			}			
		}

	}

	/**************************************************************************
	 * Set the SQL to insert records into PROF_COMPANY_ORGID.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setInsertProfCompanyOrgid(
			final String sql
			) {
		mInsertProfCompanyOrgid = sql;
	}

	/**************************************************************************
	 * Set the SQL to delete records from PROF_COMPANY_ORGID.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setDeleteProfCompanyOrgid(
			final String sql
			) {
		mDeleteProfCompanyOrgid = sql;
	}
	
	/**************************************************************************
	 * Set the SQL to delete records from PROF_COMPANY_ACCOUNT.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setDeleteProfCompanyAccount(
			final String sql
			) {
		mDeleteProfCompanyAccount = sql;
	}

	/**************************************************************************
	 * Set the SQL to delete records from TM_ACCOUNT.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setDeleteTmAccount(
			final String sql
			) {
		mDeleteTmAccount = sql;
	}

	/**************************************************************************
	 * Set the SQL to update a record int TM_ACCOUNT.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setUpdateTmAccount(
			final String sql
			) {
		mUpdateTmAccount = sql;
	}
	
	/**************************************************************************
	 * Set the SQL to update records from PMT_GROUPING.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setUpdatePmtGrouping(
			final String sql
			) {
		mUpdatePmtGrouping = sql;
	}

	/**************************************************************************
	 * Set the SQL to update records from PMT_AUTOMATIC_GROUPING.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setUpdatePmtAutomaticGrouping(
			final String sql
			) {
		mUpdatePmtAutomaticGrouping = sql;
	}

	/**************************************************************************
	 * Set the SQL to update records from PMT_AUTOMATIC_DOCUMENTS.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setUpdatePmtAutomaticDocuments(
			final String sql
			) {
		mUpdatePmtAutomaticDocuments = sql;
	}

	/**************************************************************************
	 * Set the SQL to update records from BILL.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setUpdateBill(
			final String sql
			) {
		mUpdateBill = sql;
	}

	/**************************************************************************
	 * Set the SQL to update records from FFFC_TRANSACTIONS.
	 * 
	 * @param sql  The SQL command to save.
	 */
	public void setUpdateFffcTransactions(
			final String sql
			) {
		mUpdateFffcTransactions = sql;
	}
	
	/**************************************************************************
	 * Set the class to do a sanity check on the company.
	 * 
	 * @param sanity  The class to sanity check the company.
	 */
	public void setSanityCheck(
			final Check sanity
			) {
		mSanity = sanity;
	}	
	
	/**************************************************************************
	 * Set the class to report the database state.
	 * 
	 * @param report  The class to report the database state.
	 */
	public void setReporter(
			final Reporter report
			) {
		mReporter = report;
	}	
	
}
