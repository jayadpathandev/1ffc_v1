package com.sorrisotech.svcs.accountstatus.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Returns the eligible account for this row
 * 
 * @author john kowalonek
 * @since  2024-Mar-22
 * @version 2024-Mar-22 jak First version
 * @version 2024-Apr-01 jak added eligibility to results
 * 
 */
public class AcctFromExtAcctMapper implements RowMapper<AcctFromExtAcctElement> {

	@Override
	public AcctFromExtAcctElement mapRow(ResultSet arg0, int arg1) throws SQLException {
		AcctFromExtAcctElement lRetVal = new AcctFromExtAcctElement();
		
		lRetVal.m_szInternalAccountId = arg0.getString("internalAcctId");
		lRetVal.m_szCustomerId = arg0.getString("customerId");

		return lRetVal;
	}

}
