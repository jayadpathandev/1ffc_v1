package com.sorrisotech.fffc.batch.reimport.report;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PmtGrouping implements RowMapper<PmtGrouping.Record> {
	public class Record {
		public String onlineTransId;
		public String internalAccountNum;
		public String displayAccountNum;
	}

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.onlineTransId      = set.getString(1);
		retval.internalAccountNum = set.getString(2);
		retval.displayAccountNum  = set.getString(3);

		return retval;
	}

}
