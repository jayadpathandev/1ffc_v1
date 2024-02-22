package com.sorrisotech.fffc.documentesign.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class WalletInfo {

	private String sourceName;
	private String sourceType;
	private String sourceNum;
	private String sourceExpiry;
	
	public WalletInfo(
			String sourceName, 
			String sourceType, 
			String sourceNum, 
			String sourceExpiry) {
		this.sourceName = sourceName;
		this.sourceType = sourceType;
		this.sourceNum = sourceNum;
		this.sourceExpiry = sourceExpiry;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getSourceNum() {
		return sourceNum;
	}

	public String getSourceExpiry() {
		return sourceExpiry;
	}
	
	public static class WalletInfoWrapper implements RowMapper<WalletInfo> {

		@Override
		public WalletInfo mapRow(ResultSet rs, int index) throws SQLException {
			return new WalletInfo(
					rs.getString("source_name"), 
					rs.getString("source_type"), 
					rs.getString("source_num"), 
					rs.getString("source_expiry")
			);
		}

	}
}
