/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
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
package com.sorrisotech.batch.fffc.transaction.purge;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

/******************************************************************************
 * Tasklet that cleans up the fffc_transactions table.
 */
public class ExecuteTasklet implements InitializingBean, Tasklet {
	
	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExecuteTasklet.class);
	
	/**************************************************************************
	 * Points to the Jdbc template.
	 */
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	/**************************************************************************
	 * The SQL to delete a single row form the fffc_transactions TABLE.
	 */
	private String deleteRowSql;
	
	/**************************************************************************
	 * The SQL to get the "current" date.
	 */
	private String dateSql;
	
	/**************************************************************************
	 * The SQL to purge the meta data table.
	 */
	private String purgeSql;
	
	/**************************************************************************
	 * The SQL to perform the purge.
	 */
	private String deleteSql;
	
	/**************************************************************************
	 * DaysToKeep is the number of days to keep in the transaction history logs.
	 */
	private int daysToKeep;
	
	/**************************************************************************
	 * Points to the transaction manager
	 */
	private PlatformTransactionManager mTxManager;
	
	/***************************************************************************
	 * Checks if all required properties are set
	 * 
	 * @throws BatchException
	 */
	public void afterPropertiesSet() {
		Assert.notNull(dateSql, "The Date SQL cannot be null.");
		Assert.notNull(purgeSql, "The purge SQL cannot be null.");
		Assert.notNull(mTxManager, "The transaction manager cannot be null.");
	}
	
	/***************************************************************************
	 * Calls <code>executeSql()</code> method. An exception thrown if errors
	 * encountered during execution.
	 * 
	 * @param cStepConstribution Not used.
	 * @param cChunkContext      Not Used.
	 * 
	 * @return The status of the tasklet.
	 * 
	 * @throws Exception Thrown if there is an error
	 */
	public RepeatStatus execute(
	        final StepContribution cStepConstribution,
	        final ChunkContext cChunkContext) throws Exception {
		executeSql();
		return RepeatStatus.FINISHED;
	}
	
	/**************************************************************************
	 * Uses the Spring transaction manager to manage all the specified sql
	 * operations within the same database transaction.
	 *
	 * @throws Exception Thrown if there is an error.
	 */
	protected void executeSql() throws Exception {
		final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		final TransactionStatus status = mTxManager.getTransaction(def);
		
		try {
			final SimpleDateFormat FORMAT  = new SimpleDateFormat("yyyyMMdd");
			final SimpleDateFormat DISPLAY = new SimpleDateFormat("MMM dd, yyyy");
			final String           szDate  = jdbcTemplate.queryForObject(dateSql,
			        new HashMap<String, Object>(), String.class);
			final Calendar         cDate   = Calendar.getInstance();
			
			cDate.clear();
			cDate.setTime(FORMAT.parse(szDate));
			cDate.add(Calendar.DAY_OF_MONTH, -daysToKeep);
			
			LOG.debug("SQL: " + deleteSql);
			
			deleteSql = deleteSql.replace(":date", FORMAT.format(cDate.getTime()));
			
			List<String> rows = jdbcTemplate.query(deleteSql, new Extractor());
			
			while (rows.size() > 0) {
				
				List<MapSqlParameterSource> lTransactions = new ArrayList<MapSqlParameterSource>();
				
				for (final String id : rows) {
					MapSqlParameterSource cParams = new MapSqlParameterSource();
					cParams.addValue("online_id", id, Types.VARCHAR);
					lTransactions.add(cParams);
				}
				
				LOG.debug("Removing " + rows.size() + " old records form fffc_transactions.");
				jdbcTemplate.batchUpdate(deleteRowSql,
				        lTransactions.toArray(new MapSqlParameterSource[rows.size()]));
				
				rows = jdbcTemplate.query(deleteSql, new Extractor());
			}
			
			LOG.info("Removing fffc transactions files older than "
			        + DISPLAY.format(cDate.getTime()));
			LOG.debug("SQL: " + purgeSql);
			
			final String sql = purgeSql.replace(":date", FORMAT.format(cDate.getTime()));
			
			jdbcTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
				public Boolean doInPreparedStatement(final PreparedStatement ps)
				        throws SQLException {
					return ps.execute();
				}
			});
			
		} catch (DataAccessException cDataException) {
			LOG.error("Database Exception while executing sql: " + cDataException.getMessage(),
			        cDataException);
			mTxManager.rollback(status);
			throw cDataException;
		} catch (Throwable th) {
			LOG.error("Database Exception while executing sql: " + th.getMessage(), th);
			mTxManager.rollback(status);
			throw th;
		}
		
	}
	
	/**************************************************************************
	 * SetDeleteRowSql is called by spring to provide the SQL we need to delete a
	 * single row from the database.
	 * 
	 * @param sql The SQL to execute.
	 */
	public void setDeleteRowSql(final String sql) {
		deleteRowSql = sql;
	}
	
	/**************************************************************************
	 * SetDaysToKeep is called by spring to pass in the number of days of
	 * transactions history logs to keep.
	 * 
	 * @param days The number of days to keep.
	 */
	public void setDaysToKeep(final int days) {
		daysToKeep = days;
	}
	
	/**************************************************************************
	 * SetDateSql is called by spring to provide the SQL we need to exeucte to get
	 * the current date.
	 * 
	 * @param sql The SQL to execute.
	 */
	public void setDateSql(final String sql) {
		dateSql = sql;
	}
	
	/**************************************************************************
	 * SetJdbcTemplate sets the JDBC template to use for the database queries.
	 * 
	 * @param template The JDBC template to use.
	 */
	public void setJdbcTemplate(final NamedParameterJdbcTemplate template) {
		jdbcTemplate = template;
	}
	
	/**************************************************************************
	 * Returns the transaction manager.
	 * 
	 * @param cTxManager
	 */
	public void setTxManager(final PlatformTransactionManager cTxManager) {
		this.mTxManager = cTxManager;
	}
	
	/**************************************************************************
	 * SetPurgeSql sets the purge SQL we need to execute.
	 * 
	 * @param sql The list of SQL commands we need to execute.
	 */
	public void setPurgeSql(final String sql) {
		purgeSql = sql;
	}
	
	/**************************************************************************
	 * SetDeleteSql sets the purge SQL we need to execute.
	 * 
	 * @param sql The SQL command we need to execute.
	 */
	public void setDeleteSql(final String sql) {
		deleteSql = sql;
	}
}
