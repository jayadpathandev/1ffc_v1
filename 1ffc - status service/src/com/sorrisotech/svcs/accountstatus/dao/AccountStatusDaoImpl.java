package com.sorrisotech.svcs.accountstatus.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * 	Retrieves the status of accounts associated with a user
 * 
 * @author John A. Kowalonek
 * @since 09-Oct-2023
 * @version 30-Oct-2023
 */
public class AccountStatusDaoImpl implements IAccountStatusDao {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountStatusDaoImpl.class);	  


	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static AccountStatusDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("userAccountsStatusDao", AccountStatusDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("Could not load 1ffcStatus.xml.");
	}
	
	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("getUserAccountsStatusSql")  
	private String m_getUserAccountsStatusQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	private List<AccountStatusElement> getAccountElementsInternal (final String cszUserId) {
		List<AccountStatusElement> lResultList = null;
		try {
			
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("user_id", cszUserId);
			
			// -- make query --
			lResultList = mJdbc.query(m_getUserAccountsStatusQuery, cParams, new AccountStatusElementMapper());
		}
		catch (Exception e) {
			LOG.error("getAccountElementsForUser -- Exception thrown for query", e);
		}
			
		return lResultList;
	
	}

	@Override
	public List<AccountStatusElement> getAccountElementsForUser (final String cszUserId) {
	
		return mDao.getAccountElementsInternal(cszUserId);
	}
}