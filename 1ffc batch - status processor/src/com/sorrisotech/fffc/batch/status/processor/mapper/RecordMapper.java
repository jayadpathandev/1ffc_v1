/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
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

import com.sorrisotech.fffc.batch.status.processor.bean.Record;

/**************************************************************************************************
 * RowMapper implementation class for Record
 * 
 * @author Rohit Singh
 * 
 */
public class RecordMapper implements RowMapper<Record> {

	@Override
	public Record mapRow(ResultSet rs, int index) throws SQLException {
		Record cUser = new Record();
		cUser.setInternalAccNumber(rs.getString("account"));
		cUser.setPaymentDisabled("Y".equals(rs.getString("payment_disabled")));
		cUser.setAchDisabled("Y".equals(rs.getString("ach_disabled")));
		cUser.setRecurringPaymentDisabled("Y".equals(rs.getString("recurring_payment_disabled")));
		cUser.setPaymentDisabledDQ("Y".equals(rs.getString("payment_disabled_dq")));
		cUser.setRecurringPaymentDisabledUntilCurrent("Y".equals(rs.getString("recurring_payment_disabled_until_current")));
		
		return cUser;
	}

}