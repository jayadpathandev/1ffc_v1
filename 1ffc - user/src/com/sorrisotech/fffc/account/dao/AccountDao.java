/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
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
package com.sorrisotech.fffc.account.dao;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/******************************************************************************
 * Account DAO class.
 * 
 * @author Asrar Saloda
 */
public class AccountDao {
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountDao.class);
	
	/**********************************************************************************************
	 * The singleton instance of this class.
	 */
	private static AccountDao m_cSingleton = null;
	
	/**********************************************************************************************
	 * Template class with a basic set of JDBC operations, allowing the use of named
	 * parameters rather than traditional '?' place holders.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate jdbc;
	
	/**********************************************************************************************
	 * Injecting the bean of SQL fetch display account number based on internal
	 * account number.
	 */
	@Autowired
	@Qualifier("getDisplayAccountNumberSQL")
	private String getDisplayAccountNumberSQL;
	
	/**********************************************************************************************
	 * This method is calling getDisplayAccountNumberSQL query and returning the
	 * results. This method returns null if no record found.
	 * 
	 * @param szInternalAccountNumber Internal account number.
	 * 
	 * @return String Display account number.
	 */
	public String queryDisplayAccountNumber(final String szInternalAccountNumber) {
		
		String displayAccountNumber = null;
		
		final HashMap<String, Object> cParams = new HashMap<>(1);
		cParams.put("INT_ACCOUNT", szInternalAccountNumber);
		
		try {
			// --------------------------------------------------------------------------------------
			// Fetching display account number based on internal account number.
			displayAccountNumber = jdbc.queryForObject(getDisplayAccountNumberSQL, cParams,
			        String.class);
		} catch (EmptyResultDataAccessException e) {
			LOG.warn(
			        "AccountDao.....queryDisplayAccountNumber()...No record found for internalAccount: ");
		} catch (DataAccessException e) {
			LOG.error("AccountDao.....queryDisplayAccountNumber()...An exception was thrown: " + e,
			        e);
		}
		
		return displayAccountNumber;
	}
	
	/**********************************************************************************************
	 * This method is reading DAO bean defined in 1ffcdisplayaccount.xml and assigns
	 * to singleton member variable if it is null.
	 * 
	 * @return Singleton instance of AccountDao class
	 */
	public static synchronized AccountDao get() {
		if (m_cSingleton == null) {
			ClassPathXmlApplicationContext context = null;
			
			try {
				context = new ClassPathXmlApplicationContext("1ffcdisplayaccount.xml");
				m_cSingleton = context.getBean(AccountDao.class);
			} catch (Exception e) {
				LOG.error("Unable load 1ffcdisplayaccount.xml", e, e);
			} finally {
				if (context != null) {
					context.close();
				}
			}
		}
		return m_cSingleton;
	}
}
