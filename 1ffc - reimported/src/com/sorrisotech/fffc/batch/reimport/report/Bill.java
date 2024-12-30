package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class Bill implements RowMapper<Bill.Record> {
	public class Record {
		public BigDecimal billId;
		public String     account;
		public BigDecimal billDate;
		public String     invoiceNumber;
	}

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.billId        = set.getBigDecimal(1);
		retval.account       = set.getString(2);
		retval.billDate      = set.getBigDecimal(3);
		retval.invoiceNumber = set.getString(4);

		return retval;
	}

}
