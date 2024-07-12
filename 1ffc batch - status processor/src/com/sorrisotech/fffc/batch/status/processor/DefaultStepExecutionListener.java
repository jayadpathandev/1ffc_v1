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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

/**************************************************************************************************
 * The default step execution listener implementation, 
 * this will update the latest.udated.date value.
 * 
 * @author Rohit Singh
 * 
 */
public class DefaultStepExecutionListener extends NamedParameterJdbcDaoSupport implements StepExecutionListener {

	/**************************************************************************
	 * The default logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DefaultStepExecutionListener.class);

	/**************************************************************************
	 * this query will fetches the latest status update date.
	 */
	private String fetchLatestStatusUpdateSQL;

	/**************************************************************************
	 * this query will update or insert the latest status update date.
	 */
	private String upsertLatestStatusUpdateSQL;

	@Override
	public ExitStatus afterStep(StepExecution executionStatus) {
		
		if (executionStatus.getExitStatus().equals(ExitStatus.COMPLETED)) {
			try {
				List<String> updateDateTimeStamps = getNamedParameterJdbcTemplate()
						.queryForList(fetchLatestStatusUpdateSQL, new MapSqlParameterSource(), String.class);

				if (!updateDateTimeStamps.isEmpty()) {
					String timeStamp = updateDateTimeStamps.get(0);
					int rowsAffected = getNamedParameterJdbcTemplate().update(upsertLatestStatusUpdateSQL,
							new MapSqlParameterSource("latestUpdateTime", timeStamp));
					LOG.info("upsertLatestStatusUpdateSQL Rows Affected: {}", rowsAffected);
				}

			} catch (Exception e) {
				LOG.error("afterStep()...throws an Exception: {}", e);
			}

		}

		return ExitStatus.COMPLETED;
	}

	@Override
	public void beforeStep(StepExecution var1) {
		// TODO Auto-generated method stub

	}

	public void setFetchLatestStatusUpdateSQL(String fetchLatestStatusUpdateSQL) {
		this.fetchLatestStatusUpdateSQL = fetchLatestStatusUpdateSQL;
	}

	public void setUpsertLatestStatusUpdateSQL(String upsertLatestStatusUpdateSQL) {
		this.upsertLatestStatusUpdateSQL = upsertLatestStatusUpdateSQL;
	}
	
	
}