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

import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.utils.DbConfig;

/**************************************************************************************************
 * Finds the public account numbers that have a new account ID.
 */
public class AccountNumReader 
	extends NamedParameterJdbcDaoSupport 
	implements ItemReader<String> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountNumReader.class);

	/**************************************************************************
	 * Used to access the DB_CONFIG table.
	 */
	private DbConfig mDbConfig = null;

	/**************************************************************************
	 * The key to read the date of the last file processed.
	 */
	private String mPreviousDateKey = null;
	
	/**************************************************************************
	 * Query to get the most recent id from the tm_account table.
	 */
	private String mCurrentIdSql = null;

	/**************************************************************************
	 * Query for new account numbers what have had their account ID change.
	 */
	private String mAccountSql = null;

	/**************************************************************************
	 * The list of accounts that still need to be processed.
	 */
	private LinkedList<String> mTodo = new LinkedList<String>();

	/**************************************************************************
	 * The ID of last record in TM_ACCOUNT that was processed.
	 */
	private Long mCurrent = null;
	
	/**************************************************************************
	 * Read the date of the last file processed.
	 */
	private Long from() {
		final String value = mDbConfig.getValue(mPreviousDateKey);
		
		if (value != null && value.isBlank() == false) {
			try {
				return Long.parseLong(value);
			} catch(Throwable e) {
				LOG.error("Could not parse key [" + mPreviousDateKey + "] with value [" + value + "]");
			}
		}
		
		return Long.valueOf(0);		
	}

	/**************************************************************************
	 * Read the date of the most recent bill file.
	 */
	private Long to() {
		final var db = getNamedParameterJdbcTemplate();
		
		try {
			return db.queryForObject(mCurrentIdSql, new HashMap<String, Object>(), Long.class);
		} catch(IncorrectResultSizeDataAccessException e) {
			if (e.getActualSize() != 0) {
				return Long.valueOf(0);
			}
			throw(e);
		}
	}
	
	/**************************************************************************
	 * Return a list of ORG_ID changes
	 */
	@Override
	public String read() throws Exception {
		
		if (mTodo.isEmpty()) {
			if (mCurrent != null) {
				if (!mDbConfig.setValue(mPreviousDateKey, mCurrent.toString())) {
					LOG.warn("Could not save the new date in DB_CONFIG.");
				};				
			}
			
			final HashMap<String, Object> params = new HashMap<String, Object>();
			
			mCurrent = to();
			
			params.put("previous", from());
			params.put("current", mCurrent);

			LOG.info(
				"Looking for reimported acccounts with an internal ID from [" + 
				params.get("previous") + "] and [" + params.get("current") + "]."
				);
			
			mTodo = new LinkedList<String>(
				this.getNamedParameterJdbcTemplate().queryForList(
					mAccountSql, params, String.class
					)
				);
		}
		
		String retval = null;
		
		if (mTodo.isEmpty() == false) {
			retval = mTodo.removeFirst();
			LOG.info("Processing Account ID change for ACCOUNT_NUMBER [" + retval + "]");
		} else {
			LOG.info("No more reimported accounts detected.");
		}	
		
		return retval;
	}

	/**************************************************************************
	 * Save the key to use in DB_CONFIG to load/save the previous ID value.
	 * 
	 * @param value  The key to use in DB_CONFIG.
	 */
	public void setPreviousIdKey(
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
	 * Save the SQL to query for the most ID in the TM_ACCOUNT table.
	 * 
	 * @param sql  The SQL to find the most recent ID in the TM_ACCOUNT table.
	 */
	public void setCurrentIdSql(
				final String sql
			) {
		this.mCurrentIdSql = sql.trim();
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
