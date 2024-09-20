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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

/**************************************************************************************************
 * Process a list of Account Number whose account IDs have changed to collect:
 * 		Old 
 * 
 */
public class AccountNumProcessor 
	extends NamedParameterJdbcDaoSupport 
	implements ItemProcessor<String, AccountChange> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountNumProcessor.class);

	/**************************************************************************
	 * The payment group for bills.
	 */
	private String mBillGroup = null;

	/**************************************************************************
	 * The SQL to query for the TM_ACCOUNT records we are interested in.
	 */
	private String mTmRecordsSql = null;

	/**************************************************************************
	 * For an account figure out what changed and what needs to be updated.
	 * 
	 * @param account  The display account number for the account we are
	 *                 checking.
	 *                 
	 * @return  An AccountChange object if we can determine the changes 
	 *          otherwise null.
	 *          
	 * @throws Exception  Thrown is we could not find ANY records in the 
	 *                    TM_ACCOUNT table.
	 */
	public AccountChange process(
			final String account
			) throws Exception {
		//---------------------------------------------------------------------
		// Query for the records from TM_ACCOUNT.
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("account", account);
		
		final List<TmRecord> records = this.getNamedParameterJdbcTemplate().query(
				mTmRecordsSql,
				params,
				new TmRecordMapper()
				);
		
		if (records == null || records.isEmpty()) {
			throw new Exception("Could not find any records in TM_ACCOUNT.");
		}
		
		//---------------------------------------------------------------------
		// Parse the records from TM_ACCOUNT.
		final AccountChange retval = new AccountChange(account);
		
		for(final var rec : records) {
			final String error = retval.parseRecord(
				rec.billGroup.equals(mBillGroup), 
				rec
				);
			
			if (error != null) {
				LOG.error(
					"Skipping account: " + error
					);
				return null;				
			}
		}
		
		//---------------------------------------------------------------------
		// Validate that we actually got enough data.
		if (retval.getOldAccountIds().isEmpty()) {
			LOG.error(
				"Skipping account: No old account IDs found."
				);
			return null;							
		}
				
		return retval;	
	}
		
	/**************************************************************************
	 * Save the payment group that identifies bills in the bill table.
	 * 
	 * @param value  The bill group/payment group for bill records.
	 */
	public void setBillGroup(
			final String value
			) {
		this.mBillGroup = value;
	}

	/**************************************************************************
	 * Saves the SQL to query for the records in TM_ACCOUNT for the account
	 * we are interested in.
	 * 
	 * @param value  The SQL to query the database with.
	 */
	public void setTmRecordsSql(
			final String value
			) {
		this.mTmRecordsSql = value;
	}

}