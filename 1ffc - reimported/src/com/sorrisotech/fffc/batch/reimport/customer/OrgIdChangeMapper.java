package com.sorrisotech.fffc.batch.reimport.customer;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/******************************************************************************
 * This class expects a result set with two strings.  The first is the old
 * ORG_ID and the second is the new ORG_ID.
 */
public class OrgIdChangeMapper implements RowMapper<OrgIdChange> {

	/**************************************************************************
	 * Called for each row in the result set.  Creates and initializes an 
	 * OrgIdChange object to hold the data and returns it.
	 * 
	 * @param set  The ResultSet to get the data from.
	 * @param row  The row that is being red.
	 * 
	 * @return An OrgIdChange object with the old and new ORG_ID.
	 */
	@Override
	public OrgIdChange mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final OrgIdChange retval = new OrgIdChange();
		
		retval.mOldOrgId  = set.getString(1);
		retval.mNewOrgId  = set.getString(2);
		retval.mCompanyId = set.getBigDecimal(3);
		return retval;
	}

}
