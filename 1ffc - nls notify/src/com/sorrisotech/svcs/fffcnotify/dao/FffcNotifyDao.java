/*
 * (c) Copyright 2017-2023 Sorriso Technologies, Inc(r), All Rights Reserved,
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
package com.sorrisotech.svcs.fffcnotify.dao;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**************************************************************************************************
 * This class handles all the DB queries for the FFFC Notifications.
 * 
 * @author Asrar Saloda.
 */
public class FffcNotifyDao {
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FffcNotifyDao.class);
	
	/**********************************************************************************************
	 * The singleton instance of this class.
	 */
	private static FffcNotifyDao singleton = null;
	
	/**********************************************************************************************
	 * Template class with a basic set of JDBC operations, allowing the use of named
	 * parameters rather than traditional '?' place holders.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate jdbc;
	
	/**********************************************************************************************
	 * Injecting the bean of SQL fetch userId based on attrName and attrValue.
	 */
	@Autowired
	@Qualifier("sqlUserIdFromAttrNameAndValue")
	private String sqlUserIdFromAttrNameAndValue;
	
	/**********************************************************************************************
	 * This method is calling sqlUserIdFromAttrNameAndValue query and returning the
	 * results. This method returns null if no record found.
	 * 
	 * @param szAttributeValue The value of attribute in auth_user_profile table.
	 * @param szAttributeName  The value of attribute in auth_user_profile table.
	 * 
	 * @return String userId
	 */
	public String queryUserId(final String szAttributeName, final String szAttributeValue) {
		
		String userId = null;
		
		final HashMap<String, Object> cParams = new HashMap<>(1);
		cParams.put("attrName", szAttributeName);
		cParams.put("attrValue", szAttributeValue);
		
		try {
			// --------------------------------------------------------------------------------------
			// Fetching userId based on attribute name and attribute value.
			userId = jdbc.queryForObject(sqlUserIdFromAttrNameAndValue, cParams, String.class);
		} catch (EmptyResultDataAccessException e) {
			LOG.warn("FffcNotifyDao.....queryUserId()...No record found for customerId: "
			        + szAttributeValue);
		} catch (DataAccessException e) {
			LOG.error("FffcNotifyDao.....queryUserId()...An exception was thrown: " + e, e);
		}
		
		return userId;
	}
	
	/**********************************************************************************************
	 * This method is reading DAO bean defined in NotificationsService.xml and
	 * assigns to singleton member variable if it is null.
	 * 
	 * @return Singleton instance of FffcNotifyDao class
	 */
	public static synchronized FffcNotifyDao get() {
		if (singleton == null) {
			ClassPathXmlApplicationContext context = null;
			
			try {
				context = new ClassPathXmlApplicationContext("FffcNotifyService.xml");
				singleton = context.getBean(FffcNotifyDao.class);
			} catch (Exception e) {
				LOG.error("Unable load FffcNotifyService.xml", e, e);
			} finally {
				if (context != null) {
					context.close();
				}
			}
		}
		return singleton;
	}
}
