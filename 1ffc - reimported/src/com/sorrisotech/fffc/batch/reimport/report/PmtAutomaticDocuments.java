package com.sorrisotech.fffc.batch.reimport.report;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PmtAutomaticDocuments implements RowMapper<PmtAutomaticDocuments.Record> {
	public class Record {
		public String     internalAccountNum;
		public String     documentNumber;
		public String     status;
	} 

	@Override
	public Record mapRow(
				final ResultSet set, 
				final int       row
			) throws SQLException {
		final Record retval = new Record();
		
		retval.internalAccountNum = set.getString(1);
		retval.documentNumber     = set.getString(2);
		retval.status             = set.getString(3);

		return retval;
	}

}
