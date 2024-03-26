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
 * Returns list of accounts registered to a user that are eligible for online access
 * 
 * @author john Kowalonek
 * @since 2024-Mar-24
 * @version 2024-Mar-24 jak First version
 */
public class EligibleRegisteredAcctsDaoImpl implements IEligibleRegisteredAccountsDao {

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EligibleRegisteredAcctsDaoImpl.class);	  

	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static EligibleRegisteredAcctsDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("getEligibleRegisteredAcctsDao", EligibleRegisteredAcctsDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("EligibleRegisteredAccountsDaoImpl -- Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("EligibleRegisteredAccountsDaoImpl - Could not load 1ffcStatus.xml.");
	}

	/***************************************************************************
     * Query is the query to get the list eligible registered accounts for the user
     */	
	@Autowired
	@Qualifier("getEligibleRegisteredAccountsForUserSql")  
	private String m_GetEligibleRegisteredAccountsForUserQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	private List<EligibleRegisteredAcctElement> getEligibleRegisteredAcctsInternal (final String cszUserId, final String cszStatusPayGroup) {
		List<EligibleRegisteredAcctElement> lResultList = null;
		try {
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("userId", cszUserId );
			cParams.put("statusGroup", cszStatusPayGroup );
			
			// -- make query --
			lResultList = mJdbc.query(m_GetEligibleRegisteredAccountsForUserQuery, cParams, new EligibleRegisteredAcctMapper());
		}
		catch (Exception e) {
			LOG.error("getEligibleRegisteredAcctsInternal -- Exception thrown for query", e);
		}
			
		return lResultList;
	
	}
	
	@Override
	public List<EligibleRegisteredAcctElement> getEligibleRegisteredAccts(final String cszUserId, final String cszStatusPayGroup) {

		return mDao.getEligibleRegisteredAcctsInternal(cszUserId, cszStatusPayGroup);
	}

}
