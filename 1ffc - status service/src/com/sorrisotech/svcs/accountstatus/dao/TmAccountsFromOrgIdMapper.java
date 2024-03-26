package com.sorrisotech.svcs.accountstatus.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Returns the account_number from tm_accounts query
 * 
 * @author john kowalonek
 * @since 2024-Mar-24
 * @version 2024-Mar-24 jak First Version
 */
public class TmAccountsFromOrgIdMapper implements RowMapper<String> {

	@Override
	public String mapRow(ResultSet arg0, int arg1) throws SQLException {
		 return arg0.getString("account_number");
	}

}
