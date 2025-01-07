/* (c) Copyright 2016-2025 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
 * +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona 
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition", 
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network", 
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay", 
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active 
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.batch.reimport.sanity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ProfCompanyAccount implements RowMapper<ProfCompanyAccount.Record> {
	public static class Record {
		public BigDecimal id;
		public BigDecimal accountId;
		public BigDecimal startDate;
		public BigDecimal endDate;
	}
	
	@Override
	public Record mapRow(
			ResultSet set, 
			int row
			) throws SQLException {
		var result = new Record();
		result.id        = set.getBigDecimal(1);
		result.accountId = set.getBigDecimal(2);
		result.startDate = set.getBigDecimal(3);
		result.endDate   = set.getBigDecimal(4);
		
		return result;
	}	
	
}
