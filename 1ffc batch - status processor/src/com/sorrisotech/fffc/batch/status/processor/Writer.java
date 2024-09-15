/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
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
import org.springframework.batch.item.ItemWriter;
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
 * Implementation of a No operation ItemWriter.
 * 
 * @author Rohit Singh
 * 
 * @eversion 2024-Jul-15 jak  Updated so recurring payment disabled will not delete a 
 * 			scheduled payment.
 * 
 */
public class Writer extends NamedParameterJdbcDaoSupport implements ItemWriter<List<Record>> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Writer.class);

	/**************************************************************************
	 * Query for deleting the scheduled records.
	 */
	private String m_szDeleteScheduled = null;

	/**************************************************************************
	 * Query for getting the scheduled payment records by userId.
	 */
	private String m_szGetScheduledInfoByInternalAccNumber = null;

	/**************************************************************************
	 * Query for getting upcoming recurring payment records by userId.
	 */
	private String m_szGetRecurringInfoByInternalAccNumber = null;
	
	/**************************************************************************
	 * The pay type for automatic payment.
	 */
	private String m_szAutomaticPmtInitiated = null;
	
	/**************************************************************************
	 * The pay type for scheduled payment.
	 */
	private String m_szScheduledPmtInitiated = null;
	
	/**************************************************************************
	 * The SQL for inserting payment history record.
	 */
	private String m_szInsertPmtHistory = null;

	/**************************************************************************
	 * RowMapper implementation for ScheduledPayment.
	 */
	private RowMapper<ScheduledPayment> m_cScheduledPaymentMapper = null;

	/**************************************************************************
	 * RowMapper implementation for RecurringPayment.
	 */
	private RowMapper<RecurringPayment> m_cRecurringPaymentMapper = null;
	
	/**************************************************************************
     * Config change value of pmt_history table for soft deleted records.
     */
	private String m_szConfigChange = null;

	/**************************************************************************
	 * Used by this class to make SQL calls.
	 */
	private PaymentAutomaticDao m_cPaymentAutomaticDao = null;

	public Writer() {
		try {
			final ApplicationContext cContext = Spring.getPaymentQuery();
			m_cPaymentAutomaticDao = cContext.getBean("pmtAutomatic", PaymentAutomaticDao.class);
		} catch (Exception e) {
			LOG.error("DeleteAutomaticPayment().....An exception was thrown", e);
		}
	}

	@Override
	public void write(List<? extends List<Record>> cListOfRecordList) throws Exception {

		for (List<Record> recordList : cListOfRecordList) {
			// Iterate over the inner list
			for (Record entry : recordList) {
				// Process each record

				LOG.info("Processing internal account number : {}", entry.getInternalAccNumber());
				

				List<ScheduledPayment> cScheduledPayments = getScheduledPaymentsForUser(
						entry.getInternalAccNumber()
				);
				
				List<RecurringPayment> cRecurringPayments = getRecurringPaymentsForUser(
						entry.getInternalAccNumber()
				);

				List<String> cScheduledRecordsIds = new ArrayList<String>();
				List<BigDecimal> cRecurringRecordsIds = new ArrayList<BigDecimal>();
				
				String sReasons = "";
				if (entry.isAchDisabled()) {
					
					cRecurringRecordsIds = cRecurringPayments.stream()
							.filter(payment -> "bank".equals(payment.getSourceType()))
							.map(val -> val.getId())
							.collect(Collectors.toList());
					
					cScheduledRecordsIds = cScheduledPayments.stream()
							.filter(payment -> "bank".equals(payment.getSourceType()))
							.map(val -> val.getId())
							.collect(Collectors.toList());
					sReasons = sReasons.concat("ach disabled | ");
				}

				if (entry.isPaymentDisabled() || entry.isRecurringPaymentDisabled()) {

					cRecurringRecordsIds = cRecurringPayments.stream()
							.map(val -> val.getId())
							.collect(Collectors.toList());
					sReasons = sReasons.concat("payment or recurring payment disabled | ");
				}

				if (entry.isPaymentDisabled()) {
					cScheduledRecordsIds = cScheduledPayments.stream()
						.map(val -> val.getId())
						.collect(Collectors.toList());
					sReasons = sReasons.concat("payment disabled");
				}

				final var recurringPaymentIds = cRecurringRecordsIds;
				cRecurringPayments.removeIf(payment -> !recurringPaymentIds.contains(payment.getId()));
				entry.setRecurringPayments(cRecurringPayments);
				
				
				LOG.info(
						"Recurring payments Ids that are going to be deleted for account : {}, ids : {}, reasons {}", 
						entry.getInternalAccNumber(), 
						recurringPaymentIds,
						sReasons
						
				);
				
				if (!cRecurringRecordsIds.isEmpty()) {
					int iRecurringRowsAffected = deleteRecurringPaymentsForUser(cRecurringRecordsIds);
					LOG.info("Number of recurring records deleted : {}", iRecurringRowsAffected);
				}

				final var schedulePaymentIds = cScheduledRecordsIds;
				cScheduledPayments.removeIf(payment -> !schedulePaymentIds.contains(payment.getId()));
				entry.setScheduledPayments(cScheduledPayments);

				LOG.info(
						"Scheduled payments Ids that are going to be deleted for account : {}, ids : {}, reasons: {}", 
						entry.getInternalAccNumber(), 
						schedulePaymentIds,
						sReasons
				);
				
				if (!cScheduledPayments.isEmpty()) {
					int iScheduledRowsAffected = deleteScheduledPaymentsForUser(cScheduledRecordsIds);
					LOG.info("Number of schedule records deleted : {}", iScheduledRowsAffected);

					int totalRowsAffected = cScheduledPayments.stream()
							.map(val -> insertFailedPmtHistory(val))
							.reduce(0, Integer::sum);
					LOG.info("Total records added to pmt_history table : {}", totalRowsAffected);
				}
				
				LOG.info(
						"Records deleted successfully for internal account Number : {}", 
						entry.getInternalAccNumber()
				);
			}
		}

	}

	/**************************************************************************
	 * Sets the value of m_szDeleteScheduled.
	 * 
	 * @param szDeleteScheduled
	 */
	public void setDeleteScheduled(String szDeleteScheduled) {
		this.m_szDeleteScheduled = szDeleteScheduled;
	}

	/**************************************************************************
	 * Sets the value of m_szGetScheduledInfoByUserId.
	 * 
	 * @param szGetScheduledInfoByUserId
	 */
	public void setGetScheduledInfoByInternalAccNumber(String szGetScheduledInfoByInternalAccNumber) {
		this.m_szGetScheduledInfoByInternalAccNumber = szGetScheduledInfoByInternalAccNumber;
	}

	/**************************************************************************
	 * Sets the value of m_szGetRecurringInfoByUserId.
	 * 
	 * @param szGetRecurringInfoByUserId
	 */
	public void setGetRecurringInfoByInternalAccNumber(String szGetRecurringInfoByInternalAccNumber) {
		this.m_szGetRecurringInfoByInternalAccNumber = szGetRecurringInfoByInternalAccNumber;
	}

	/**************************************************************************
	 * Sets the value of m_cScheduledPaymentMapper.
	 * 
	 * @param cScheduledPaymentMapper
	 */
	public void setScheduledPaymentMapper(RowMapper<ScheduledPayment> cScheduledPaymentMapper) {
		this.m_cScheduledPaymentMapper = cScheduledPaymentMapper;
	}

	/**************************************************************************
	 * Sets the value of m_cRecurringPaymentMapper.
	 * 
	 * @param cRecurringPaymentMapper
	 */
	public void setRecurringPaymentMapper(RowMapper<RecurringPayment> cRecurringPaymentMapper) {
		this.m_cRecurringPaymentMapper = cRecurringPaymentMapper;
	}

	/**************************************************************************
	 * Sets the value of m_szConfigChange.
	 * 
	 * @param szConfigChange
	 */
	public void setConfigChange(String szConfigChange) {
		this.m_szConfigChange = szConfigChange;
	}

	/**************************************************************************
	 * Sets the value of m_szAutomaticPmtInitiated.
	 * 
	 * @param szAutomaticPmtInitiated
	 */
	public void setAutomaticPmtInitiated(String szAutomaticPmtInitiated) {
		this.m_szAutomaticPmtInitiated = szAutomaticPmtInitiated;
	}
	
	/**************************************************************************
	 * Sets the value of m_szScheduledPmtInitiated.
	 * 
	 * @param szScheduledPmtInitiated
	 */
	public void setScheduledPmtInitiated(String szScheduledPmtInitiated) {
		this.m_szScheduledPmtInitiated = szScheduledPmtInitiated;
	}

	/**************************************************************************
	 * Sets the value of m_szInsertPmtHistory.
	 * 
	 * @param szInsertPmtHistory
	 */
	public void setInsertPmtHistory(String szInsertPmtHistory) {
		this.m_szInsertPmtHistory = szInsertPmtHistory;
	}

	/**************************************************************************
	 * Gets all scheduled payment records by userId.
	 * 
	 * @param userId
	 * @return list of {@link ScheduledPayment}
	 */
	private List<ScheduledPayment> getScheduledPaymentsForUser(String userId) {
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("accNumber", userId);
		return getNamedParameterJdbcTemplate().query(m_szGetScheduledInfoByInternalAccNumber, param,
				m_cScheduledPaymentMapper);
	}

	/**************************************************************************
	 * Gets all recurring payment records by userId.
	 * 
	 * @param userId
	 * @return list of {@link RecurringPayment}
	 */
	private List<RecurringPayment> getRecurringPaymentsForUser(String userId) {
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("accNumber", userId);
		return getNamedParameterJdbcTemplate().query(m_szGetRecurringInfoByInternalAccNumber, param,
				m_cRecurringPaymentMapper);
	}

	/**************************************************************************
	 * Deletes scheduled payments records.
	 * 
	 * @param ids list of scheduled payment ids
	 * @return rows affected
	 */
	private int deleteScheduledPaymentsForUser(List<String> ids) {
		// Handling the SQL exception is the list is empty
		if (ids.isEmpty())
			ids.add("");
		MapSqlParameterSource param = new MapSqlParameterSource().addValue("ids", ids);
		return getNamedParameterJdbcTemplate().update(m_szDeleteScheduled, param);
	}
	
	/**************************************************************************
	 * Inserts the scheduled payment as failed transaction into pmt_history table.
	 * 
	 * @param cScheduledPayment
	 * @return rows affected
	 */
	private int insertFailedPmtHistory(ScheduledPayment cScheduledPayment) {
		
		MapSqlParameterSource cParams  = new MapSqlParameterSource();
		
		cParams.addValue("transaction_id",cScheduledPayment.getId()); 
		cParams.addValue("online_trans_id",cScheduledPayment.getId()); 
		
		String payFromAcc = cScheduledPayment.getWalletNickName() + "|" 
							+ cScheduledPayment.getSourceType() + "|" 
							+ cScheduledPayment.getWalletNum();
		
		cParams.addValue("pay_from_account", payFromAcc); 
		cParams.addValue("pay_amt",cScheduledPayment.getAmount()); 
		cParams.addValue("user_id",cScheduledPayment.getUserId());
		cParams.addValue("pay_surcharge",cScheduledPayment.getSurcharge());
		cParams.addValue("pay_total_amt",cScheduledPayment.getTotalAmount());
		
		if ("automatic".equalsIgnoreCase(cScheduledPayment.getPayType()))
			cParams.addValue("flex1", this.m_szAutomaticPmtInitiated);
		else if ("onetime".equalsIgnoreCase(cScheduledPayment.getPayType()))
			cParams.addValue("flex1", this.m_szScheduledPmtInitiated);
		else
			cParams.addValue("flex1", null);
		
		LOG.debug("Adding entry to 'pmt_history' table : {}", cParams);
		
		int rowsAffected = getNamedParameterJdbcTemplate().update(m_szInsertPmtHistory, cParams);
		
		LOG.debug("Row affected: {}", cParams);
		
		return rowsAffected;
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
