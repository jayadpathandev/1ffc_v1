package com.sorrisotech.fffc.batch.reimport.account;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/******************************************************************************
 * Read the columns from a query and crates a TmRecord object.
 */
public class TmRecordMapper implements RowMapper<TmRecord> {

	/**************************************************************************
	 * Called for each row returned from the query, create a TmRecord object
	 * for each row.
	 * 
	 * @param set  The result set to extract the column values from.
	 * @param row  Not used.
	 */
	@Override
	public TmRecord mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final TmRecord retval = new TmRecord();
		
		retval.id        = set.getBigDecimal(1);
		retval.accountId = set.getString(2);
		retval.startDate = set.getBigDecimal(3);
		retval.companyId = set.getBigDecimal(4);
		retval.billGroup = set.getString(5);
		retval.orgId     = set.getString(6);
		
		// Sanity check that we don't have unexpected null values.
		if (retval.id == null) {
			throw new SQLException("Record does not have an ID.");
		}
		if (retval.accountId == null || retval.accountId.isBlank()) {
			throw new SQLException("Record [" + retval.id + "] does not have an ACCOUNT_NUMBER.");
		}
		if (retval.startDate == null) {
			throw new SQLException("Record [" + retval.id + "] does not have a START_DATE.");
		}		
		if (retval.billGroup == null || retval.billGroup.isBlank()) {
			throw new SQLException("Record [" + retval.id + "] does not have a BILL_GROUP.");
		}
		if (retval.orgId == null || retval.billGroup.isBlank()) {
			throw new SQLException("Record [" + retval.id + "] does not have an ORG_ID.");
		}

		return retval;
	}

}
