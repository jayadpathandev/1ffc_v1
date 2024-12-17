package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ProfCompanyAccount implements RowMapper<ProfCompanyAccount.Record> {
	public class Record {
		public BigDecimal companyid;
		public BigDecimal accountid;
		public BigDecimal startDate;
		public BigDecimal endDate;
	}

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.companyid = set.getBigDecimal(1);
		retval.accountid = set.getBigDecimal(2);
		retval.startDate = set.getBigDecimal(3);
		retval.endDate   = set.getBigDecimal(4);

		return retval;
	}

}
