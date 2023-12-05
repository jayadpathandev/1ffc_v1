/*
 * (c) Copyright 2017-2021 Sorriso Technologies, Inc(r), All Rights Reserved,
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

import java.math.BigDecimal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;



/**************************************************************************************************
 * This class handles all the DB queries for the profile services.
 * 
 * @author Asrar Saloda.
 */
public class FffcNotificationDao {
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FffcNotificationDao.class);
	
	/**********************************************************************************************
	 * The singleton instance of this class.
	 */
	private static FffcNotificationDao singleton = null;
	
	/**********************************************************************************************
	 * Template class with a basic set of JDBC operations, allowing the use of named
	 * parameters rather than traditional '?' place holders.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate jdbc;
	
	/**********************************************************************************************
	 * Injecting the bean of SQL orgId.
	 */
	@Autowired
	@Qualifier("sqlOrgId")
	private String sqlOrgId;
	
	private FffcNotificationDao() {
		
	}

	/**********************************************************************************************
	 * This method is calling sqlOrgId query and returning the results.
	 * 
	 * @return OrgId of user.
 	 */
	public String queryOrgId(String szUserId) {
		
		final HashMap<String, Object> cParams = new HashMap<>(1);
		cParams.put("userid", new BigDecimal(szUserId));
		
		String orgId = null;
		
		try {
			 orgId = jdbc.queryForObject(sqlOrgId, cParams, String.class);
		} catch (DataAccessException e) {
			LOG.error("FffcNotificationDao.....queryOrgId()...An exception was thrown: " + e, e);
		}
		
		return orgId;
	}
	
	/**********************************************************************************************
	 * This method is reading DAO bean defined in NotificationsService.xml and
	 * assigns to singleton member variable if it is null.
	 * 
	 * @return Singleton instance of NotificationsDao class
	 */
	public static synchronized FffcNotificationDao get() {
		if (singleton == null) {
			ClassPathXmlApplicationContext context = null;
			
			try {
				context = new ClassPathXmlApplicationContext("FffcNotification.xml");
				singleton = context.getBean("dao",FffcNotificationDao.class);
			} catch (Exception e) {
				LOG.error("Unable load FffcNotification.xml", e, e);
			} 
//			finally {
//				if (context != null) {
//					context.close();
//				}
//			}
		}
		return singleton;
	}
}
