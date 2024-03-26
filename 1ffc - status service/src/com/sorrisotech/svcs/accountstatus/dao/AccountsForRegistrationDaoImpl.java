package com.sorrisotech.svcs.accountstatus.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AccountsForRegistrationDaoImpl implements IAccountsForRegistrationDao {

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccountsForRegistrationDaoImpl.class);	  

	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static AccountsForRegistrationDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("accountsForRegistrationDao", AccountsForRegistrationDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("AccountsForRegistrationDaoImpl -- Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("AccountsForRegistrationDaoImpl - Could not load 1ffcStatus.xml.");
	}
	
	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("getAccountsForRegistrationSql")  
	private String m_GetAccountsForRegistrationQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	private List<AccountForRegistrationElement> getAccountsForRegistrationInternal (final String cszPmtGroup, final String cszBaseAcct) {
		List<AccountForRegistrationElement> lResultList = null;
		try {
			String pmtGroup = cszPmtGroup;
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("pmtGroup", pmtGroup );
			cParams.put("pmtGroup1", pmtGroup );
			cParams.put("internalAccountNumber", cszBaseAcct);
			
			// -- make query --
			lResultList = mJdbc.query(m_GetAccountsForRegistrationQuery, cParams, new AccountForRegistrationMapper());
		}
		catch (Exception e) {
			LOG.error("getEligibleAccountsInternal -- Exception thrown for query", e);
		}
			
		return lResultList;
	
	}
	
	@Override
	public List<AccountForRegistrationElement> getAccountsForRegistration(final String cszPmtGroup, final String cszBaseAcct) {

		return mDao.getAccountsForRegistrationInternal(cszPmtGroup, cszBaseAcct);
	}

}
