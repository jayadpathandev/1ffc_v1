package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TmAccount implements RowMapper<TmAccount.Record> {
	public class Record {
		public String     accountNumber;
		public String     accountNumberDisplay;
		public BigDecimal startDate;
		public BigDecimal endDate;
		public String     accountName;
		public BigDecimal companyId;
		public String     orgId;
		
	}

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.accountNumber        = set.getString(1);
		retval.accountNumberDisplay = set.getString(2);
		retval.startDate            = set.getBigDecimal(3);
		retval.endDate              = set.getBigDecimal(4);
		retval.accountName          = set.getString(5);
		retval.companyId            = set.getBigDecimal(6);
		retval.orgId                = set.getString(6);

		return retval;
	}

}
