package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountMapper implements RowMapper<AccountBean> {

	@Override
	public AccountBean mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new AccountBean();
		
		retval.externalNumber = result.getString(1);
		retval.payGroup       = result.getString(2);
		
		return retval; 
	}

}
