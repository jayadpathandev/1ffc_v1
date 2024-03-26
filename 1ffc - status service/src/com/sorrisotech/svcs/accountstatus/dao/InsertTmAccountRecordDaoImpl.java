package com.sorrisotech.svcs.accountstatus.dao;

import java.math.BigDecimal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class InsertTmAccountRecordDaoImpl implements IInsertTmAccountRecordDao {

	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InsertTmAccountRecordDaoImpl.class);	  

	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static InsertTmAccountRecordDaoImpl mDao = null;
	static {
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcStatus.xml");
			mDao = context.getBean("insertTmAccountRecord", InsertTmAccountRecordDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("InsertTmAccountRecordDaoImpl -- Could not load 1ffcStatus.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("InsertTmAccountRecordDaoImpl - Could not load 1ffcStatus.xml.");
	}

	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("tmInsertAccountDetailsSQL")  
	private String m_InsertTmAccountDetailsSQL;
	
	/***************************************************************************
	 * Query for getting a new sequence number
	 */
	@Autowired
	@Qualifier("accountSequenceSql")  
	private String m_accountSequenceSql;
	
	/***************************************************************************
	 * Spring object to query for object
	 */
	@Autowired
	private JdbcTemplate mSeqJdbc = null;
	
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	/**
	 * Retrieves a new sequence number and then inserts the record specified by oElement and the 
	 * payment group into tm_accouint
	 * 
	 * @param oElement
	 * @param cszPayGroup
	 * @return
	 */
	private boolean InsertTmAccountRecordInternal(AccountForRegistrationElement oElement, String cszPayGroup) {
		
		try {
			BigDecimal lIdValue = BigDecimal.ZERO;
			int iResultSet = 0;
			
			// -- get a new sequence number --
		   lIdValue = (BigDecimal) mSeqJdbc.queryForObject(
														m_accountSequenceSql, BigDecimal.class);
			
			// -- set parameters --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
	       cParams.put("idVal", lIdValue);
          cParams.put("accountNumVal", oElement.m_szInternalAccountId);
          cParams.put("accountDisplayVal", oElement.m_szDisplayAccountId);
          cParams.put("startDateVal", oElement.m_dStartDate);
          cParams.put("accountNameVal", oElement.m_szAccountName);
          cParams.put("payGroupVal", cszPayGroup);
          cParams.put("billerIdVal", oElement.m_iBillerId);
          cParams.put("orgIdVal", oElement.m_szOrgId); 
			
			// -- insert the record --
			iResultSet = mJdbc.update(m_InsertTmAccountDetailsSQL, cParams);
		}
		catch (Exception e) {
			LOG.error("InsertTmAccountRecord -- Exception thrown for insert internal account id {}", 
									oElement.m_szInternalAccountId, e);
			return false;
		}
		
		LOG.debug("InsertTmAccountRecord -- success inserting account for  internal account id {}", 
				oElement.m_szInternalAccountId);
		return true;
	}
	
	@Override
	public boolean InsertTmAccountRecord(AccountForRegistrationElement oElement, String cszPayGroup) {
		return mDao.InsertTmAccountRecordInternal(oElement, cszPayGroup);
	}
}
