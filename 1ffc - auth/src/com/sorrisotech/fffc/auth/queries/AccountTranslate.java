/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.auth.queries;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

// ************************************************************************************************
// Used with the translate1ffcAccount bean in 1ffc_auth.xml to read the row returned.
public class AccountTranslate implements RowMapper<AccountTranslate.Data> {
	
	// ********************************************************************************************
	// The data retrieved from the query.
	public static class Data {
		public BigDecimal accountId;
		public String     orgId;
	}

	// ********************************************************************************************
	// Process the row and create the Data object.
	@Override
	public Data mapRow(
				ResultSet resultSet, 
				int       row
			) throws SQLException {
		final Data retval = new Data();
		retval.accountId = resultSet.getBigDecimal(1);
		retval.orgId     = resultSet.getString(2);
		return retval;
	}
}
