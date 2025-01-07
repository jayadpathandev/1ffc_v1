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
package com.sorrisotech.fffc.batch.reimport.customer;

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
public class OrgIdChangeWriter 
	extends NamedParameterJdbcDaoSupport 
	implements ItemWriter<OrgIdChange> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(OrgIdChangeWriter.class);

	/**************************************************************************
	 * SQL to update a record in PROF_COMPANY_ORGID.
	 */
	private String mUpdateProfCompanyOrgid = null;

	/**************************************************************************
	 * SQL to delete a record in PROF_COMPANY_ACCOUNT.
	 */
	private String mDeleteProfCompanyAccount = null;
	
	/**************************************************************************
	 * SQL to update records in TM_ACCOUNT.
	 */
	private String mUpdateTmAccount = null;

	/**************************************************************************
	 * SQL to update records in TM_TAGS.
	 */
	private String mUpdateTmTags = null;

	/**************************************************************************
	 * SQL to check if a company exists even through we were not given one.
	 */
	private String mCheckIfCompanyExists = null;
	
	/**************************************************************************
	 * Class to perform a sanity check on the company.
	 */
	private Check mSanity = null;
	
	/**************************************************************************
	 * Class to report the database state
	 */
	private Reporter mReporter = null;
	
	/**************************************************************************
	 * Update PROF_COMPANY_ORGID to remove the old OrgIds and add in the new
	 * OrgId. This the OrgId did not change this method does not modify the
	 * database.
	 * 
	 * @param change  The change we are processing.
	 */
	private void updateProfCompanyOrgid(
			final OrgIdChange change
			) throws Exception {
		final var params = new HashMap<String, Object>();
		final var newId  = change.mNewOrgId;
		final var oldId  = change.mOldOrgId;
		
		params.put("newOrgId", newId);
		params.put("oldOrgId", oldId);
							
		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdateProfCompanyOrgid,
			params
			);
		
		if (updates > 1) {
			throw new Exception(
					"PROF_COMPANY_ORGID - Updated [" + updates + 
					"] records from ORG_ID [" + oldId + "] to [" +
					newId + "]. Expected to only update 1 record."
					);
		} else {
			LOG.info(
				"PROF_COMPANY_ORGID - Updated [" + updates + 
				"] records from ORG_ID [" + oldId + "] to [" +
				newId + "]."
				);
		}
	}

	/**************************************************************************
	 * Update PROF_COMPANY_ACCOUNT to remove the old account link.
	 * 
	 * @param change  The change we are processing.
	 */
	private void deleteProfCompanyAccount(
			final OrgIdChange change
			) throws Exception {
		final var params = new HashMap<String, Object>();
		
		params.put("companyId", change.mCompanyId);
							
		final var deletes = getNamedParameterJdbcTemplate().update(
			mDeleteProfCompanyAccount,
			params
			);
		
		if (deletes > 1) {
			throw new Exception(
					"PROF_COMPANY_ACCOUNT - Deleted [" + deletes + 
					"] records for COMPANY_ID [" + change.mCompanyId + 
					"]. Expected to only delete 1 record."
					);
		} else {
			LOG.info(
				"PROF_COMPANY_ACCOUNT - Deleted [" + deletes + 
				"] records for COMPANY_ID [" + change.mCompanyId + "]."
				);
		}
	}
	
	/**************************************************************************
	 * Update TM_ACCOUNT to remove the old accounts.
	 * 
	 * @param change  The change we are processing.
	 * 
	 * @return  True if everything is good, otherwise false.
	 */
	private void updateTmAccount(
			final OrgIdChange change
			) {
		final var params = new HashMap<String, Object>();
		final var newId  = change.mNewOrgId;
		final var oldId  = change.mOldOrgId;
		
		params.put("newOrgId", newId);
		params.put("oldOrgId", oldId);
							
		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdateTmAccount,
			params
			);
		LOG.info(
			"TM_ACCOUNT - Updated [" + updates + 
			"] records from ORG_ID [" + oldId + "] to [" +
			newId + "]."
			);
	}

	/**************************************************************************
	 * Update TM_TAGS to the new orgID.
	 * 
	 * @param change  The change we are processing.
	 * 
	 * @return  True if everything is good, otherwise false.
	 */
	private void updateTmTags(
			final OrgIdChange change
			) {
		final var params = new HashMap<String, Object>();
		final var oldId  = change.mOldOrgId;
		final var newId  = change.mNewOrgId;
		
		params.put("newOrgId", newId);
							
		final var updates = getNamedParameterJdbcTemplate().update(
			mUpdateTmTags,
			params
			);
		LOG.info(
			"TM_TAGS - Updated [" + updates + 
			"] records from ORG_ID [" + oldId + "] to [" +
			newId + "]."
			);
	}

	/**************************************************************************
	 * See if a company_id actually exists.
	 * 
	 * @param orgId  The OLD org id.
	 * 
	 * @return  The companyId if found.
	 */
	private BigDecimal checkForCompany(
			final String orgId
			) {
		final var params = new HashMap<String, Object>();
		params.put("oldOrgId", orgId);
		
		var list = this.getNamedParameterJdbcTemplate().queryForList(
				mCheckIfCompanyExists,
				params,
				BigDecimal.class
				);
		if (list.size() != 1) {
			return null;
		}
		return list.get(0);
	}
	
	/**************************************************************************
	 * Process the changes for a re-imported account.
	 * 
	 * @param changes  The list of changes. 
	 */
	@Override
	public void write(
			List<? extends OrgIdChange> changes
			) throws Exception {

		for (final var change : changes) {
			mReporter.beforeOrgChange(change.mCompanyId, change.mOldOrgId, change.mNewOrgId);
			
			try {
				if (change.mCompanyId != null) {
					updateProfCompanyOrgid(change);
					deleteProfCompanyAccount(change);
				} 
				updateTmAccount(change);
				updateTmTags(change);
				
				BigDecimal companyId = change.mCompanyId;
				
				if (companyId == null) {
					companyId = checkForCompany(change.mOldOrgId);					
				}
				mSanity.verifyCompany(
						change.mNewOrgId,
						companyId
						);
			} finally {
				mReporter.afterOrgChange(change.mCompanyId, change.mOldOrgId, change.mNewOrgId);
			}
		}

	}

	/**************************************************************************
	 * Set the SQL to update records into PROF_COMPANY_ORGID.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setUpdateProfCompanyOrgid(
			final String sql
			) {
		mUpdateProfCompanyOrgid = sql;
	}

	/**************************************************************************
	 * Set the SQL to delete records into PROF_COMPANY_ACCOUNT.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setDeleteProfCompanyAccount(
			final String sql
			) {
		mDeleteProfCompanyAccount = sql;
	}
	
	/**************************************************************************
	 * Set the SQL to update records from TM_ACCOUNT.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setUpdateTmAccount(
			final String sql
			) {
		mUpdateTmAccount = sql;
	}

	/**************************************************************************
	 * Set the SQL to update records from TM_TAGS.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setUpdateTmTags(
			final String sql
			) {
		mUpdateTmTags = sql;
	}

	/**************************************************************************
	 * Set the SQL to check if a company exists for the old org_id.
	 * 
	 * @param sql  The SQL command to save
	 */
	public void setCheckIfCompanyExists(
			final String sql
			) {
		mCheckIfCompanyExists = sql;
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
