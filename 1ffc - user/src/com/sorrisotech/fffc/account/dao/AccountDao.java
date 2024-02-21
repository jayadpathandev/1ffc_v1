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

public class AccountDao {
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountDao.class);
	
	/**********************************************************************************************
	 * The singleton instance of this class.
	 */
	private static AccountDao singleton = null;
	
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
	@Qualifier("getDisplayAccountNumberSQL")
	private String getDisplayAccountNumberSQL;
	
	/**********************************************************************************************
	 * This method is calling sqlUserIdFromAttrNameAndValue query and returning the
	 * results. This method returns null if no record found.
	 * 
	 * @param szAttributeValue The value of attribute in auth_user_profile table.
	 * @param szInternalAccountNumber  The value of attribute in auth_user_profile table.
	 * 
	 * @return String userId
	 */
	public String queryDisplayAccountNumber(final String szInternalAccountNumber) {
		
		String displayAccountNumber = null;
		
		final HashMap<String, Object> cParams = new HashMap<>(1);
		cParams.put("INT_ACCOUNT", szInternalAccountNumber);
		
		try {
			// --------------------------------------------------------------------------------------
			// Fetching userId based on attribute name and attribute value.
			displayAccountNumber = jdbc.queryForObject(getDisplayAccountNumberSQL, cParams, String.class);
		} catch (EmptyResultDataAccessException e) {
			LOG.warn("AccountDao.....queryDisplayAccountNumber()...No record found for customerId: "
			        );
		} catch (DataAccessException e) {
			LOG.error("AccountDao.....queryDisplayAccountNumber()...An exception was thrown: " + e, e);
		}
		
		return displayAccountNumber;
	}
	
	/**********************************************************************************************
	 * This method is reading DAO bean defined in NotificationsService.xml and
	 * assigns to singleton member variable if it is null.
	 * 
	 * @return Singleton instance of FffcNotifyDao class
	 */
	public static synchronized AccountDao get() {
		if (singleton == null) {
			ClassPathXmlApplicationContext context = null;
			
			try {
				context = new ClassPathXmlApplicationContext("1ffcdisplayaccount.xml");
				singleton = context.getBean(AccountDao.class);
			} catch (Exception e) {
				LOG.error("Unable load 1ffcdisplayaccount.xml", e, e);
			} finally {
				if (context != null) {
					context.close();
				}
			}
		}
		return singleton;
	}
}
