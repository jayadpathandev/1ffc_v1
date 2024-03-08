package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountIdMapper implements RowMapper<AccountIdBean> {

	@Override
	public AccountIdBean mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new AccountIdBean();
		
		retval.id       = result.getBigDecimal(1);
		retval.account  = result.getString(2);
		retval.payGroup = result.getString(3);
		
		return retval; 
	}

}
