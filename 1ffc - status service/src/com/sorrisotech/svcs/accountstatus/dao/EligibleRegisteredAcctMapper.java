package com.sorrisotech.svcs.accountstatus.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class EligibleRegisteredAcctMapper implements RowMapper<EligibleRegisteredAcctElement> {

	@Override
	public EligibleRegisteredAcctElement mapRow(ResultSet arg0, int arg1) throws SQLException {

		EligibleRegisteredAcctElement lRetVal = new EligibleRegisteredAcctElement();
		
		lRetVal.m_szInternalAccountId = arg0.getString("internalAccountId");
		lRetVal.m_szDisplayAccountId = arg0.getString("displayAccountId");

		return lRetVal;
	}

}
