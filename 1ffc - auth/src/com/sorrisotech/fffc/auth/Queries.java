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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sorrisotech.fffc.auth.queries.AccountTranslate;

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
     * Query to get the org_id given an account and payment group.
     */	
	@Autowired
	@Qualifier("translate1ffcAccount")  
	private String mTranslateAccount;	

	/***************************************************************************
	 * Given an account ID find the org_id.
	 */
	static public AccountTranslate.Data lookupOrgId(
				final BigDecimal accountId,
				final String     billGroup
			) {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		
		params.put("account_id", accountId);
		params.put("bill_group", billGroup);
		
		return mSingleton.mJdbc.queryForObject(
			mSingleton.mTranslateAccount, params, new AccountTranslate()
		);		
	}
}
