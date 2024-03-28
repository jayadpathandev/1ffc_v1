/* (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
 * +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona 
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition", 
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network", 
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay", 
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active 
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.batch.status.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.fffc.batch.status.processor.bean.Record;
import com.sorrisotech.fffc.batch.status.processor.bean.RecurringPayment;
import com.sorrisotech.fffc.batch.status.processor.bean.ScheduledPayment;
import com.sorrisotech.svcs.payment.dao.PaymentAutomaticDao;
import com.sorrisotech.utils.Spring;

/**************************************************************************************************
 * Implementation class for ItemProcessor.
 * 
 * @author Rohit Singh
 * 
 */
public class Processor extends NamedParameterJdbcDaoSupport implements ItemProcessor<Record, Record> {
	
	/**************************************************************************
     * Development level logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    /**************************************************************************
     * Query for deleting the scheduled records.
     */
	private String m_szDeleteScheduled = null;
	
	/**************************************************************************
     * Config change value of pmt_history table for soft deleted records.
     */
	private String m_szConfigChange = null;
	
	/**************************************************************************
     * Query for getting the scheduled payment records by userId.
     */
	private String m_szGetScheduledInfoByUserId = null;
	
	/**************************************************************************
     * Query for getting upcoming recurring payment records by userId.
     */
	private String m_szGetRecurringInfoByUserId = null;
	
	/**************************************************************************
     * RowMapper implementation for ScheduledPayment.
     */
	private RowMapper<ScheduledPayment> m_cScheduledPaymentMapper = null;
	
	/**************************************************************************
     * RowMapper implementation for RecurringPayment.
     */
	private RowMapper<RecurringPayment> m_cRecurringPaymentMapper = null;
	
	/**************************************************************************
	 * Used by this class to make SQL calls.
	 */
	private static PaymentAutomaticDao m_cPaymentAutomaticDao = null;
	
	/**************************************************************************	 
     * Creates the spring object and gets the pmtAutomatic bean.    
     */
	public Processor() {	    		
		try {		    		
		    final ApplicationContext cContext = Spring.getPaymentQuery();
		    m_cPaymentAutomaticDao = cContext.getBean("pmtAutomatic", PaymentAutomaticDao.class);
		} catch (Exception e) {
			LOG.error("DeleteAutomaticPayment().....An exception was thrown", e);
		}
	}

	@Override
	public Record process(Record cUser) throws Exception {
		
		LOG.info("Processing user : {}", cUser.getUserId());
		
		List<ScheduledPayment> cScheduledPayments = getScheduledPaymentsForUser(cUser.getUserId());
		List<RecurringPayment> cRecurringPayments = getRecurringPaymentsForUser(cUser.getUserId());
		
		boolean bAllDeleted = false;
		
		List<String> cScheduledRecordsIds = new ArrayList<String>();
		List<BigDecimal> cRecurringRecordsIds = new ArrayList<BigDecimal>();
		
		if (cUser.isPortalAcessDisabled()
				|| (cUser.isPaymentDisabled() && !cUser.isPaymentDisabledDQ())
				|| (cUser.isPaymentDisabledDQ() && !cUser.isAccountCurrent())) {
			bAllDeleted = true;
			
			cScheduledRecordsIds = cScheduledPayments.stream()
					.map(val -> val.getId())
					.collect(Collectors.toList());
			
			cRecurringRecordsIds = cRecurringPayments.stream()
					.map(val -> val.getId())
					.collect(Collectors.toList());
			
		}
		
		if ( !bAllDeleted && cUser.isAchDisabled() ) {
			
			cScheduledRecordsIds = cScheduledPayments.stream()
					.filter(payment -> "bank".equals(payment.getSourceType()))
					.map(val -> val.getId())
					.collect(Collectors.toList());
			
			cRecurringRecordsIds = cRecurringPayments.stream()
					.filter(payment -> "bank".equals(payment.getSourceType()))
					.map(val -> val.getId())
					.collect(Collectors.toList());
		}
		
		if (!bAllDeleted 
				&& (cUser.isRecurringPaymentDisabled() 
						|| (cUser.isRecurringPaymentDisabledUntilCurrent() && !cUser.isAccountCurrent()))) {
			cRecurringRecordsIds = cRecurringPayments.stream()
					.map(val -> val.getId())
					.collect(Collectors.toList());
		}
		
		LOG.info("List of scheduled records : {}", cScheduledRecordsIds);
		
		LOG.info("List of recurring payment records : {}", cRecurringRecordsIds);
				
		final var recurringPaymentIds = cRecurringRecordsIds;
		cRecurringPayments.removeIf(payment -> !recurringPaymentIds.contains(payment.getId()));
		cUser.setRecurringPayments(cRecurringPayments);
		
		if (!cRecurringRecordsIds.isEmpty()) {
			int iRecurringRowsAffected = deleteRecurringPaymentsForUser(cRecurringRecordsIds);
			LOG.info("Number of recurring records deleted : {} for user : {}", iRecurringRowsAffected, cUser.getUserId());
		}
		
		final var schedulePayymentIds = cScheduledRecordsIds;
		cScheduledPayments.removeIf(payment -> !schedulePayymentIds.contains(payment.getId()));
		cUser.setScheduledPayments(cScheduledPayments);

		if (!cScheduledPayments.isEmpty()) {
			int iScheduledRowsAffected = deleteScheduledPaymentsForUser(cScheduledRecordsIds);
			LOG.info("Number of scheduled records deleted : {} for user : {}", iScheduledRowsAffected, cUser.getUserId());
		}
		
		
		LOG.info("Records deleted successfully for userId : {}", cUser.getUserId());
		
		return cUser;
	}

	/**************************************************************************
	 * Sets the value of m_szDeleteScheduled.
	 * @param szDeleteScheduled
	 */
	public void setDeleteScheduled(String szDeleteScheduled) {
		this.m_szDeleteScheduled = szDeleteScheduled;
	}

	/**************************************************************************
	 * Sets the value of m_szConfigChange.
	 * @param szConfigChange
	 */
	public void setConfigChange(String szConfigChange) {
		this.m_szConfigChange = szConfigChange;
	}

	/**************************************************************************
	 * Sets the value of m_szGetScheduledInfoByUserId.
	 * @param szGetScheduledInfoByUserId
	 */
	public void setGetScheduledInfoByUserId(String szGetScheduledInfoByUserId) {
		this.m_szGetScheduledInfoByUserId = szGetScheduledInfoByUserId;
	}

	/**************************************************************************
	 * Sets the value of m_szGetRecurringInfoByUserId.
	 * @param szGetRecurringInfoByUserId
	 */
	public void setGetRecurringInfoByUserId(String szGetRecurringInfoByUserId) {
		this.m_szGetRecurringInfoByUserId = szGetRecurringInfoByUserId;
	}

	/**************************************************************************
	 * Sets the value of m_cScheduledPaymentMapper.
	 * @param cScheduledPaymentMapper
	 */
	public void setScheduledPaymentMapper(RowMapper<ScheduledPayment> cScheduledPaymentMapper) {
		this.m_cScheduledPaymentMapper = cScheduledPaymentMapper;
	}

	/**************************************************************************
	 * Sets the value of m_cRecurringPaymentMapper.
	 * @param cRecurringPaymentMapper
	 */
	public void setRecurringPaymentMapper(RowMapper<RecurringPayment> cRecurringPaymentMapper) {
		this.m_cRecurringPaymentMapper = cRecurringPaymentMapper;
	}
	
	/**************************************************************************
	 * Gets all scheduled payment records by userId.
	 * 
	 * @param userId
	 * @return list of {@link ScheduledPayment}
	 */
	private List<ScheduledPayment> getScheduledPaymentsForUser(String userId) {
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("userId", userId);
		return getNamedParameterJdbcTemplate().query(m_szGetScheduledInfoByUserId, param, m_cScheduledPaymentMapper);
	}
	
	/**************************************************************************
	 * Gets all recurring payment records by userId.
	 * 
	 * @param userId
	 * @return list of {@link RecurringPayment}
	 */
	private List<RecurringPayment> getRecurringPaymentsForUser(String userId) {
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("userId", userId);
		return getNamedParameterJdbcTemplate().query(m_szGetRecurringInfoByUserId, param, m_cRecurringPaymentMapper);
	}
	
	/**************************************************************************
	 * Deletes scheduled payments records.
	 * 
	 * @param ids list of scheduled payment ids
	 * @return rows affected
	 */
	private int deleteScheduledPaymentsForUser(List<String> ids) {
		// Handling the SQL exception is the list is empty
		if (ids.isEmpty()) ids.add("");
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("ids", ids);
		return getNamedParameterJdbcTemplate().update(m_szDeleteScheduled, param);
	}
	
	/**************************************************************************
	 * Deletes recurring payments records.
	 * 
	 * @param ids list recurring payment ids
	 * @return rows affected
	 */
	private int deleteRecurringPaymentsForUser(List<BigDecimal> ids) {
		// Handling the SQL exception is the list is empty
		int count = 0;
		
		for (BigDecimal cAutomaticId : ids) {
			String szSoftDeletePmtAutomaticStatus = m_cPaymentAutomaticDao
			        .softDeletePmtAutomatic(cAutomaticId);
			
			if (szSoftDeletePmtAutomaticStatus.equals("success")) {
				String szSoftDeletePmtAutomaticHistory = m_cPaymentAutomaticDao
				        .softDeletePmtAutomaticHistory(cAutomaticId, m_szConfigChange);
				
				if (szSoftDeletePmtAutomaticHistory.equals("success")) {
					count++;
				} else {
					LOG.error("Failed to delete automatic payament history for ID: {}", cAutomaticId);
				}
			} else {
				LOG.error("Failed to delete automatic payament for ID: {}", cAutomaticId);
			}
		}
		return count;
	}

}
