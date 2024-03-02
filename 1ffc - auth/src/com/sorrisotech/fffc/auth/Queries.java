/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.fffc.auth;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Queries {
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Queries.class);	  

	/**************************************************************************
	 * Context for 1ffc_auth.xml.
	 */
	private static Queries mSingleton = null;
	
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffc_auth.xml");
			mSingleton = context.getBean("fffc_queries", Queries.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("Could not load 1ffc_auth.xml", e); 
		}
		
	    if (mSingleton == null) throw new RuntimeException("Could not load 1ffc.xml.");
	}

	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	/**********************************************************************************************
     * SQL to take an account ID and find the OrgId it belongs to.
     */	
	@Autowired
	@Qualifier("accountIdToOrgId")  
	private String mGetOrId;	

	/**********************************************************************************************
     * SQL to take an org ID and find all it's accounts.
     */	
	@Autowired
	@Qualifier("accountIdsFromOrgId")  
	private String mGetAccounts;	

	/**********************************************************************************************
     * SQL to take an accountId and find the existing user with that account.  The registration
     * status for the user must be "agentpay" or "pending".
     */	
	@Autowired
	@Qualifier("accountIdToUser")  
	private String mGetUser;	

	/**********************************************************************************************
     * SQL to take a user ID and find the company they belong to.
     */	
	@Autowired
	@Qualifier("userIdToCompanyId")  
	private String mGetCompanyId;	
	
	/***************************************************************************
	 * Given an account ID find the OrgId.
	 */
	static public String lookupOrgId(
				final BigDecimal accountId
			) {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("account_id", accountId);
		
		return mSingleton.mJdbc.queryForObject(mSingleton.mGetOrId, params, String.class);
	}

	/***************************************************************************
	 * Given an OrgId find all the account IDs belonging to it..
	 */
	static public List<BigDecimal> findAccounts(
				final String orgId,
				final String ignore
			) {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("org_id", orgId);
		params.put("ignore_group", ignore);
		
		return mSingleton.mJdbc.queryForList(mSingleton.mGetAccounts, params, BigDecimal.class);
	}
	
	/***************************************************************************
	 * Given an account ID find the user.
	 */
	static public BigDecimal lookupUser(
				final BigDecimal accountId
			) {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("account_id", accountId);
		
		BigDecimal retval = null;
		
		try {
			retval = mSingleton.mJdbc.queryForObject(mSingleton.mGetUser, params, BigDecimal.class); 
		} catch(EmptyResultDataAccessException e) {
		}
		return retval;
	}

	/***************************************************************************
	 * Given an account ID find the user.
	 */
	static public BigDecimal lookupCompany(
				final BigDecimal userId
			) {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("user_id", userId);
		
		BigDecimal retval = null;
		
		try {
			retval = mSingleton.mJdbc.queryForObject(mSingleton.mGetCompanyId, params, BigDecimal.class); 
		} catch(IncorrectResultSizeDataAccessException e) {
			if (e.getActualSize() > 0) {
				throw e;
			}
		}
		
		return retval;
	}
	
}
