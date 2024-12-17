package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ProfCompanyOrgid implements RowMapper<ProfCompanyOrgid.Record> {
	public class Record {
		public BigDecimal companyid;
		public String     orgId;
	}

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.companyid = set.getBigDecimal(1);
		retval.orgId     = set.getString(2);

		return retval;
	}

}
