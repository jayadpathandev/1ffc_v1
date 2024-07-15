/**
 * 
 */
package com.sorrisotech.fffc.payment.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * Implements DAO to retrieve sum of payment history since a specific time for a specific
 * account in a payment group.
 * 
 * 
 * 
 * @author johnK
 * @since 2024-Jan-31
 * @version 2024-Jan-31 JAK First and hopefully last version
 * @version 2024-Jul-12 jak Updating to retrieve based on a timestamp intsead of 
 * 					a string.
 * 
 */
public class GetPaymentHistoryAmountDaoImpl implements IGetPaymentHistoryAmountDao {
	
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GetPaymentHistoryAmountDaoImpl.class);	  
	
	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static GetPaymentHistoryAmountDaoImpl mDao = null;
	static {	
		try {
			final ApplicationContext context = new ClassPathXmlApplicationContext("1ffcbalancehelper.xml");
			mDao = context.getBean("getHistoryForAccountSinceDateDao", GetPaymentHistoryAmountDaoImpl.class);
			((ClassPathXmlApplicationContext)context).close();
		}
		catch (Throwable e) {
			LOG.error("GetPaymentHistoryAmountDaoImpl:static -- Could not load 1ffcbalancehelper.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("GetPaymentHistoryAmountDaoImpl: static -- Could not load 1ffcbalancehelper.xml.");
	}
	
	/**********************************************************************************************
     * Query is the query to get the payment history sum
     */	
	@Autowired
	@Qualifier("getTotalScheduledPaymentBeforeDateSQL")  
	private String m_GetTotalScheduledPaymentBeforeDate;
	
	/**********************************************************************************************
     * Query is the query to get the total scheduled amount before date
     */	
	@Autowired
	@Qualifier("getHistoryForAccountSinceDateSQL")  
	private String m_GetPaymentHistoryAmount;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	/**
	 * Makes the query to get the sum of all payments since start time specified which 
	 * comes from the status feed. The process at 1st franklin is as follows:
	 * 
	 * We take the following items from payment history
	 * 
	 * @param cszPaymentGroup
	 * @param cszInternalAccount
	 * @param cszStartDate
	 * @return
	 */
	private BigDecimal getPaymentHistoryAmountForAccountInternal (String cszPaymentGroup, String cszInternalAccount,
			Timestamp ctStartTime) {
		List<BigDecimal> lResultList = null;
		BigDecimal lRval = BigDecimal.ZERO;
		try {
			
			// -- set parameter --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("PMT_GROUP", cszPaymentGroup);
			cParams.put("INT_ACCOUNT", cszInternalAccount);
			cParams.put("START_TIME", ctStartTime);
			
			// -- make query --
			lResultList = mJdbc.query(m_GetPaymentHistoryAmount, cParams, new PaymentHistoryAmtMapper());
			if (lResultList.size() == 1) {
				lRval = lResultList.get(0);
			} else {
				LOG.error("GetPaymentHistoryAmountDaoImpl:getPaymentHistoryAmountForAccountInternal -- bad result set, returning 0. num rows: {}", lResultList.size());
			}
		}
		catch (Exception e) {
			LOG.error("GetPaymentHistoryAmountDaoImpl:getAccountElementsForUser -- Exception thrown for query", e);
		}
			
		return lRval;
	
	}

	@Override
	public BigDecimal getPaymentHistoryAmountForAccount(String cszPaymentGroup, String cszInternalAccount,
			Timestamp ctStartTime) {
		BigDecimal lRval = mDao.getPaymentHistoryAmountForAccountInternal(cszPaymentGroup, cszInternalAccount, ctStartTime);
			
		return lRval;
	}

	@Override
	public BigDecimal getTotalScheduledPaymentBeforeBillDue(String cszUserId, String cszBillDueDate,
			String cszInternalAccountNumber) {
		return mDao.getTotalScheduledPaymentForAccount(cszUserId, cszBillDueDate, cszInternalAccountNumber);
	}
	
	private BigDecimal getTotalScheduledPaymentForAccount(String cszUserId, String cszBillDueDate,
			String cszInternalAccountNumber) {
		
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
        paramMap.addValue("userId", cszUserId);
        paramMap.addValue("internalAccountNumber", cszInternalAccountNumber);
        paramMap.addValue("compareDate", cszBillDueDate);
        String result = null;
        
        try {        	
        	result = mJdbc.queryForObject(m_GetTotalScheduledPaymentBeforeDate, paramMap, String.class);
        } catch (Exception e) {
			LOG.error("getTotalScheduledPaymentBeforeBillDue -- Exception thrown for query", e);
		}
        
		return new BigDecimal(result);
	}

}
