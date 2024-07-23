package com.sorrisotech.svcs.accountstatus.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AcctFromExtAcctDaoImpl implements IAcctFromExtAcctDao {

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AcctFromExtAcctDaoImpl.class);	  

	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static AcctFromExtAcctDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("getAcctFromExtAcctDao", AcctFromExtAcctDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("AcctFromExtAcctDaoImpl -- Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("AcctFromExtAcctDaoImpl - Could not load 1ffcStatus.xml.");
	}
	
	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("getInternalAccountInfoSql")  
	private String m_GetInternalAccountInfoQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	private AcctFromExtAcctElement getAcctInfoFromExtAccountInternal (final String cszPaymentGroup , final String cszExtAcct) {
		List<AcctFromExtAcctElement> lResultList = null;
		AcctFromExtAcctElement lResult = null;
		try {
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("billPaymentGroup", cszPaymentGroup );
			cParams.put("externalAccount", cszExtAcct);
			
			// -- make query --  
			lResultList = mJdbc.query(m_GetInternalAccountInfoQuery, cParams, new AcctFromExtAcctMapper());
			// -- should only be one item --
			if (null != lResultList) {
				lResult = lResultList.get(0);
			}
		}
		catch (Exception e) {
			LOG.error("AcctFromExtAcctDaoImpl:externalAccountgetEligibleAccountsInternal -- Exception thrown for query", e);
		}
			
		return lResult;
	
	}
	

	@Override
	public AcctFromExtAcctElement getAcctInfoFromExtAccount(String cszPmtGroup, String cszExtAcct) {
		return mDao.getAcctInfoFromExtAccountInternal(cszPmtGroup, cszExtAcct);
	}

}
