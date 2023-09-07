/*
 * (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.batch.fffc.transaction.loader.writer;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.batch.fffc.transaction.loader.beans.TransactionDetailsBean;

/**************************************************************************
 * 
 * Writes transaction details to the FFFC_TRANSACTIONS table.
 * 
 * @author Asrar Saloda
 *
 */
public class TransactionDetailsWriter extends NamedParameterJdbcDaoSupport
        implements ItemWriter<TransactionDetailsBean> {
	
	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TransactionDetailsWriter.class);
	
	private String insertHistorySql = null;
	
	public String getInsertHistory() {
		return insertHistorySql;
	}
	
	public void setInsertHistory(String insertHistory) {
		this.insertHistorySql = insertHistory;
	}
	
	/**************************************************************************
	 * Create the transaction history records.
	 * 
	 * @param beans The beans to get the values from.
	 */
	private void createTransactionHistory(final Collection<TransactionDetailsBean> beans) {
		final int                              count  = beans.size();
		final ArrayList<MapSqlParameterSource> params = new ArrayList<MapSqlParameterSource>(count);
		
		for (final TransactionDetailsBean bean : beans) {
			MapSqlParameterSource param = new MapSqlParameterSource();
			
			param.addValue("onlineId", bean.getOnlineId(), Types.VARCHAR);
			
			param.addValue("description", bean.getDescription(), Types.VARCHAR);
			
			param.addValue("created_date", bean.getDateTime(), Types.TIMESTAMP);
			
			if (null != bean.getInvoices()) {
				param.addValue("pmtGroup", bean.getInvoices().getPmtGroup(), Types.VARCHAR);
				param.addValue("account", bean.getInvoices().getAccount(), Types.VARCHAR);
				param.addValue("amount", bean.getInvoices().getAmount().toString(), Types.VARCHAR);
			}
			params.add(param);
		}
		final MapSqlParameterSource[] data = params.toArray(new MapSqlParameterSource[count]);
		getNamedParameterJdbcTemplate().batchUpdate(insertHistorySql, data);
	}
	
	/**************************************************************************
	 * Update the database with the list of transactions.
	 */
	public void write(List<? extends TransactionDetailsBean> beans) throws Exception {
		if (beans.size() > 0) {
			final HashMap<String, TransactionDetailsBean> todo = new HashMap<String, TransactionDetailsBean>();
			
			for (final TransactionDetailsBean bean : beans) {
				if (todo.containsKey(bean.getOnlineId())) {
					LOG.info("Record " + bean.getOnlineId() + " replaced by newer record.");
				}
				todo.put(bean.getOnlineId(), bean);
			}
			
			Collection<TransactionDetailsBean> actual = todo.values();
			
			LOG.info("Processing " + actual.size() + " records, ignoring "
			        + (beans.size() - actual.size()) + " old records.");
			
			createTransactionHistory(actual);
		}
	}
}
