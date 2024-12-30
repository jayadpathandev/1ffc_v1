package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PmtAutomaticGrouping implements RowMapper<PmtAutomaticGrouping.Record> {
	public class Record {
		public BigDecimal automaticId;
		public String     internalAccountNum;
		public String     displayAccountNum;
	} 

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.automaticId        = set.getBigDecimal(1);
		retval.internalAccountNum = set.getString(2);
		retval.displayAccountNum  = set.getString(3);

		return retval;
	}

}
