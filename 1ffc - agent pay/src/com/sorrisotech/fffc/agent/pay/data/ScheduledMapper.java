package com.sorrisotech.fffc.agent.pay.data;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.springframework.jdbc.core.RowMapper;

public class ScheduledMapper implements RowMapper<ScheduledBean> {

	@Override
	public ScheduledBean mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new ScheduledBean();
		
		retval.count = result.getBigDecimal(1);
		retval.total = result.getBigDecimal(2);
		
		final Date date = result.getDate(3);
		
		if (date != null) {
			final var f = new SimpleDateFormat("yyyy/MM/dd");
			
			retval.date = f.format(date);
		} 
		
		return retval;
	}

}
