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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.utils.DbConfig;

/**************************************************************************************************
 * Finds accounts that have a new ORG_ID for which a user account is already created.
 */
public class OrgIdChangeReader 
	extends NamedParameterJdbcDaoSupport 
	implements ItemReader<OrgIdChange> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(OrgIdChangeReader.class);

	/**************************************************************************
	 * Flag to determine if we are processing registered accounts or
	 * unregistered accounts.
	 */
	private boolean mRegistered = false;
	
	/**************************************************************************
	 * Used to access the DB_CONFIG table.
	 */
	private DbConfig mDbConfig = null;

	/**************************************************************************
	 * The key to read the date of the last file processed.
	 */
	private String mPreviousDateKey = null;
	
	/**************************************************************************
	 * Query to get the STATUS_UPDATE_DATE of the last bill file published.
	 */
	private String mCurrrentDateSql = null;

	/**************************************************************************
	 * Query for any ORG_IDs that have changed.
	 */
	private String mAccountSql = null;
	
	/**************************************************************************
	 * The list of changes that still need to be processed.
	 */
	private LinkedList<OrgIdChange> mTodo = new LinkedList<OrgIdChange>();

	/**************************************************************************
	 * The date of the last record in bill_file_meta_data.
	 */
	private Timestamp mCurrent = null;
	
	/**************************************************************************
	 * Read the date of the last file processed.
	 */
	private Timestamp from() {
		final String value = mDbConfig.getValue(mPreviousDateKey);
		
		if (value != null && value.isBlank() == false) {
			try {
				return new Timestamp(Long.parseLong(value));
			} catch(Throwable e) {
				LOG.error("Could not parse key [" + mPreviousDateKey + "] with value [" + value + "]");
			}
		}
		
		return new Timestamp(0);		
	}

	/**************************************************************************
	 * Read the date of the most recent bill file.
	 */
	private Timestamp to() {
		final var db = getNamedParameterJdbcTemplate();
		
		try {
			return db.queryForObject(mCurrrentDateSql, new HashMap<String, Object>(), Timestamp.class);
		} catch(IncorrectResultSizeDataAccessException e) {
			if (e.getActualSize() != 0) {
				return new Timestamp(0);
			}
			throw(e);
		}
	}
	
	/**************************************************************************
	 * Return a list of ORG_ID changes
	 */
	@Override
	public OrgIdChange read() throws Exception {
		if (mTodo.isEmpty()) {
			if (mCurrent != null) {
				if (!mDbConfig.setValue(mPreviousDateKey, String.valueOf(mCurrent.getTime()))) {
					LOG.warn("Could not save the new date in DB_CONFIG.");
				};
			}

			final HashMap<String, Object> params = new HashMap<String, Object>();
			final OrgIdChangeMapper       mapper = new OrgIdChangeMapper();
			
			mCurrent = to();
			
			params.put("previous", from());
			params.put("current", mCurrent);
			
			LOG.info(
				"Looking for reimported ORG_IDs for " +
				(mRegistered ? "registered" : "unregistered") +
				" accounts in data loaded between [" + 
				params.get("previous") + "] and [" + params.get("current") + "]."
				);

			mTodo = new LinkedList<OrgIdChange>(
				getNamedParameterJdbcTemplate().query(
					mAccountSql, params, mapper
					)
				);			
		}
		
		OrgIdChange retval = null;
		
		if (mTodo.isEmpty() == false) {
			retval = mTodo.removeFirst();

			LOG.info(
				"Processing change for " +
				(mRegistered ? "registered" : "unregistered") +
				" account(s) from ORG_ID [" + retval.mOldOrgId + 
				"] to [" + retval.mNewOrgId + "].");
		} else {
			LOG.info(
				"Finished processing reimported ORG_IDs for " +
				(mRegistered ? "registered" : "unregistered") +
				" accounts."
				);
		}	
		
		return retval;
	}

	/**************************************************************************
	 * Store a flag to indicate if we are processing registered or unregistered
	 * OrgIds
	 * 
	 * @param value  True if we are processing registered OrgIds.
	 */
	public void setRegistered(
				final boolean value
			) {
		this.mRegistered = value;
	}

	/**************************************************************************
	 * Save the key to use in DB_CONFIG to load/save the previous date value.
	 * 
	 * @param value  The key to use in DB_CONFIG.
	 */
	public void setPreviousDateKey(
				final String value
			) {
		this.mPreviousDateKey = value;
	}

	/**************************************************************************
	 * Save the DbConfig object to use to access the DB_CONFIG table.
	 * 
	 * @param value  The DbConfig object to use.
	 */
	public void setDbConfig(
				final DbConfig value
			) {
		this.mDbConfig = value;
	}

	/**************************************************************************
	 * Save the SQL to query for the most recent loaded bill date.
	 * 
	 * @param sql  The SQL to find the most recent loaded bill date.
	 */
	public void setCurrentDateSql(
				final String sql
			) {
		this.mCurrrentDateSql = sql.trim();
	}

	/**************************************************************************
	 * Save the SQL to query for ORG_ID changes for registered users.
	 * 
	 * @param sql  The SQL to find the ORG_ID changes for registered users.
	 */
	public void setAccountsSql(
				final String sql
			) {
		this.mAccountSql = sql.trim();
	}

}
