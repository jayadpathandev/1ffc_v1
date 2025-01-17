/*
 * (c) Copyright 2025 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.fffc.account.report.bean;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/*****************************************************************************************
 * Maps rows from a SQL ResultSet to AccountRecord objects. Implements the
 * {@link RowMapper} interface to provide custom mapping logic.
 * 
 * @author Asrar Saloda
 */
public class AccountRecordMapper implements RowMapper<AccountRecord> {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountRecordMapper.class);
	
	/*************************************************************************************
	 * Maps a single row from the ResultSet to an {@link AccountRecord} object.
	 *
	 * @param rs     the ResultSet containing the data retrieved from the database.
	 * @param rowNum the number of the current row being processed.
	 * @return an {@link AccountRecord} object populated with data from the current
	 *         row.
	 * @throws RuntimeException if a {@link SQLException} occurs during the mapping
	 *                          process.
	 */
	@Override
	public AccountRecord mapRow(ResultSet rs, int rowNum) {
		var cAccountRecord = new AccountRecord();
		try {
			cAccountRecord.setLoanNum(rs.getString("loannum"));
			cAccountRecord.setRefNum(rs.getString("refnum"));
			cAccountRecord.setSourceFile(rs.getString("sourcefile"));
			cAccountRecord.setRecordType(rs.getString("recordtype"));
			cAccountRecord.setLatestRecordDate(rs.getTimestamp("latestrecorddate"));
			cAccountRecord.setMissing(rs.getString("missing"));
			
			LOG.debug("Successfully mapped row: {} to AccountRecord: {}", rowNum,
			        cAccountRecord.toString());
			return cAccountRecord;
		} catch (SQLException e) {
			LOG.error("Error mapping row: {} to AccountRecord: {}", rowNum, cAccountRecord);
			throw new RuntimeException("Failed to map row to AccountRecord", e);
		}
	}
}