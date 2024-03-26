package com.sorrisotech.svcs.accountstatus.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class TmAccountsFromOrgIdDaoImpl implements ITmAccountsFromOrgIdDao {

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TmAccountsFromOrgIdDaoImpl.class);	  
	
	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static TmAccountsFromOrgIdDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("accountsFromOrgIdDao", TmAccountsFromOrgIdDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("TmAccountsFromOrgIdDaoImpl -- Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("TmAccountsFromOrgIdDaoImpl - Could not load 1ffcStatus.xml.");
	}
	
	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("accountNumbersFromOrgId")  
	private String m_GetAccountsFromTmAccountQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;
	
	private List<String> getTmAccountsListInternal( final String cszOrgId, final String cszStatusPayGroup) {
		List<String> lResultList = null;
		try {
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("org_id", cszOrgId );
			cParams.put("ignore_group", cszStatusPayGroup );
			
			// -- make query --
			lResultList = mJdbc.query(m_GetAccountsFromTmAccountQuery, cParams, new TmAccountsFromOrgIdMapper());
		}
		catch (Exception e) {
			LOG.error("getEligibleAccountsInternal -- Exception thrown for query", e);
		}
			
		return lResultList;

	}

	@Override
	public List<String> getTmAccountsList(final String cszOrgId, final String cszStatusPayGroup) {
		return mDao.getTmAccountsListInternal(cszOrgId, cszStatusPayGroup);
	}

}
