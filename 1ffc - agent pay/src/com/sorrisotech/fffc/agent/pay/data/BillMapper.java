package com.sorrisotech.fffc.agent.pay.data;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class BillMapper implements RowMapper<BillBean> {
	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(BillMapper.class);

	//*************************************************************************
	@Override
	public BillBean mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new BillBean();
		
		retval.invoice = result.getString(1);
		
		final var date = result.getBigDecimal(2);
		
		if (date != null) {
			try {
				final var f = new SimpleDateFormat("yyyyMMdd");
				retval.dueDate = new Date(f.parse(date.toPlainString()).getTime());
			} catch (ParseException e) {
				LOG.error("Could not parse database date.", e);
			}
		}
		
		return retval; 
	}

}
