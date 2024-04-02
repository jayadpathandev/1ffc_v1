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
public class AccountForRegistrationMapper implements RowMapper<AccountForRegistrationElement> {

	@Override
	public AccountForRegistrationElement mapRow(ResultSet arg0, int arg1) throws SQLException {
		AccountForRegistrationElement lRetVal = new AccountForRegistrationElement();
		
		lRetVal.m_szInternalAccountId = arg0.getString("internalAccountId");
		lRetVal.m_szOrgId = arg0.getString("orgId");
		lRetVal.m_szDisplayAccountId = arg0.getString("displayAccountId");
		lRetVal.m_szAccountName = arg0.getString("accountName");
		lRetVal.m_iBillerId = arg0.getInt("billerId");
		lRetVal.m_dStartDate = arg0.getBigDecimal("startDate");
		lRetVal.m_bIsEligibleForPortal = Boolean.valueOf(arg0.getString("eligible"));

		return lRetVal;
	}

}
