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

/*****************************************************************************
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
	
	/***************************************************************************
	 * SQL query to insert transaction history to database (FFFC_TRANSACTIONS
	 * table).
	 * 
	 */
	private String insertHistorySql = null;
	
	/***************************************************************************
	 * SQL query to fetch online id of transaction history from 
	 * database (FFFC_TRANSACTIONS table).
	 * 
	 */
	private String queryOnlineIdSql = null;
	
	/***************************************************************************
	 * SQL query to delete  transaction history from database (FFFC_TRANSACTIONS) 
	 * table. 
	 * 
	 */
	private String deleteHistorySql = null;

	public void setDeleteHistory(String deleteHistorySql) {
		this.deleteHistorySql = deleteHistorySql;
	}

	public void setQueryOnlineId(String queryOnlineTransIdSql) {
		this.queryOnlineIdSql = queryOnlineTransIdSql;
	}
	
	public String getInsertHistory() {
		return insertHistorySql;
	}
	
	public void setInsertHistory(String insertHistory) {
		this.insertHistorySql = insertHistory;
	}
	
	/**************************************************************************
	 * Load the old online IDs.
	 * 
	 * @param beans  The list of transactions to get the old online IDs for.
	 */
	ArrayList<String> loadOldOnlineIds(
			final Collection<TransactionDetailsBean> beans
			) {
		final ArrayList<String> retval = new ArrayList<String>();
		
		for(final TransactionDetailsBean bean : beans) {
			final MapSqlParameterSource param = new MapSqlParameterSource();
			param.addValue("onlineId", bean.getOnlineId(), Types.VARCHAR);
			
			final List<String> ids = getNamedParameterJdbcTemplate().query(
				queryOnlineIdSql, 
				param, 
				new OldOnlineIdMapper()
			);
			
			if (ids.size() > 0) {
				retval.addAll(ids);
			}			
		}
		
		return retval;
	}
	
	/**************************************************************************
	 * Remove the old transactions.
	 * 
	 * @param beans  The list of transactions to clear
	 */
	private void clearOld(
			final Collection<TransactionDetailsBean> beans
			) {
		//---------------------------------------------------------------------
		// Delete the rows from fffc_transactions.
		ArrayList<String> oldIds = loadOldOnlineIds(beans);
		
		if (oldIds.size() > 0) {
			ArrayList<MapSqlParameterSource> grouping = new ArrayList<MapSqlParameterSource>(oldIds.size());
	
			for(final String onlineId : oldIds) {
				MapSqlParameterSource param = new MapSqlParameterSource();
				param.addValue("onlineId", onlineId, Types.VARCHAR);
				grouping.add(param);			
			}
			
			final MapSqlParameterSource[] data = grouping.toArray(new MapSqlParameterSource[0]);
			getNamedParameterJdbcTemplate().batchUpdate(deleteHistorySql, data);	
		}
	}
	
	/**************************************************************************
	 * Create the transaction history records.
	 * 
	 * @param beans The beans to get the values from.
	 */
	private void createTransactionHistory(final Collection<TransactionDetailsBean> beans) throws Exception{
		final int                              count  = beans.size();
		final ArrayList<MapSqlParameterSource> params = new ArrayList<MapSqlParameterSource>(count);
		
		for (final TransactionDetailsBean bean : beans) {
			
			MapSqlParameterSource param = new MapSqlParameterSource();
			
			param.addValue("onlineId", bean.getOnlineId(), Types.VARCHAR);
			
			param.addValue("date", bean.getDate(), Types.INTEGER);
			
			param.addValue("account", bean.getAccount(), Types.VARCHAR);
			
			//-------------------------------------------------------------------------------------------
			// As per discussion we are not currently enforcing any validation or enumeration for 
			// transaction types. We're storing them as it is.
			param.addValue("transaction_type",bean.getTransactionType(),Types.VARCHAR);
			
			param.addValue("description", bean.getDescription(), Types.VARCHAR);
			
			//--------------------------------------------------------------------------------------------
			// If we are getting amount as null or empty then storing amount as 'N/A' in fffc_transactions
			param.addValue("amount",bean.getAmount() , Types.DECIMAL);
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
			clearOld(actual);
			createTransactionHistory(actual);
		}
	}
}
