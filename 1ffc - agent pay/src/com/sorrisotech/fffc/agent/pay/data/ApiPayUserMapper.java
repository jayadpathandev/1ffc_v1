package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ApiPayUserMapper implements RowMapper<ApiPayUser> {

	@Override
	public ApiPayUser mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new ApiPayUser();
		
		retval.userId    = result.getBigDecimal(1);
		retval.username  = result.getString(2);
		retval.companyId = result.getBigDecimal(3);
		
		return retval; 
	}

}
