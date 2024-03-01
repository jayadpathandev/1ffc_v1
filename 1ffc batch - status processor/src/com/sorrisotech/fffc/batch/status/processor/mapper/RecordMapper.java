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
import java.text.SimpleDateFormat;

import org.springframework.jdbc.core.RowMapper;

import com.sorrisotech.fffc.batch.status.processor.bean.Record;
import com.sorrisotech.fffc.payment.BalanceHelper;

/**************************************************************************************************
 * RowMapper implementation class for User
 * 
 * @author Rohit Singh
 * 
 */
public class RecordMapper implements RowMapper<Record> {
	
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

	@Override
	public Record mapRow(ResultSet rs, int index) throws SQLException {
		Record cUser = new Record();
		cUser.setUserId(rs.getString("userid"));
		cUser.setPaymentDisabled("Y".equals(rs.getString("payment_disabled")));
		cUser.setPaymentDisabledDQ("Y".equals(rs.getString("payment_disabled_dq")));
		cUser.setAchDisabled("Y".equals(rs.getString("ach_disabled")));
		cUser.setPortalAcessDisabled("Y".equals(rs.getString("portal_access_disabled")));
		cUser.setRecurringPaymentDisabled("Y".equals(rs.getString("recurring_payment_disabled")));
		cUser.setRecurringPaymentDisabledUntilCurrent("Y".equals(rs.getString("recurring_payment_disabled_until_current")));
		cUser.setBillDate(rs.getString("bill_date"));
		cUser.setInternalAccount(rs.getString("internal_account"));
		cUser.setCurrentAmountDue(rs.getString("current_amount_due"));
		cUser.setMonthlyPaymentAmount(rs.getString("monthly_payment_amount"));
		cUser.setPaymentGroup(rs.getString("payment_group"));
		cUser.setStatusDate(DATE_FORMATTER.format(rs.getDate("status_date")));
		cUser.setAccountCurrent(
			new BalanceHelper().isAccountCurrent(
				null, null, 
				cUser.getPaymentGroup(), 
				cUser.getInternalAccount(), 
				cUser.getBillDate(), 
				cUser.getMonthlyPaymentAmount(), 
				cUser.getStatusDate(), 
				cUser.getCurrentAmountDue()
			).equals("true")
		);
		return cUser;
	}

}
