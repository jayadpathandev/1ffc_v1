/* (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
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
package com.sorrisotech.fffc.batch.status.processor.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sorrisotech.fffc.batch.status.processor.bean.RecurringPayment;

/**************************************************************************************************
 * RowMapper implementation class for RecurringPayment
 * 
 * @author Rohit Singh
 * 
 */
public class RecurringPaymentMapper implements RowMapper<RecurringPayment>{

	@Override
	public RecurringPayment mapRow(ResultSet rs, int rowNum) throws SQLException {
		RecurringPayment cRecurringPayment = new RecurringPayment();
		cRecurringPayment.setId(rs.getBigDecimal("automatic_id"));
		cRecurringPayment.setAccounutNumber(rs.getString("account_number"));
		cRecurringPayment.setWalletNickName(rs.getString("wallet_name"));
		cRecurringPayment.setSourceType(rs.getString("source_type"));
		cRecurringPayment.setInternalAccount(rs.getString("internal_account"));
		cRecurringPayment.setPayGroup(rs.getString("pay_group"));
		return cRecurringPayment;
	}

}
