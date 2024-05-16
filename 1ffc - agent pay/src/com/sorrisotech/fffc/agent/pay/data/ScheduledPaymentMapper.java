package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.springframework.jdbc.core.RowMapper;

public class ScheduledPaymentMapper implements RowMapper<ScheduledPaymentBean> {

	@Override
	public ScheduledPaymentBean mapRow(ResultSet result, int row) throws SQLException {
	
		ScheduledPaymentBean retVal = new ScheduledPaymentBean();
		
		retVal.paymentId = result.getString("paymentId");						// --online id
		retVal.paymentDate = result.getDate("paymentDate"); 					// -- date its scheduled for
		retVal.createdDate = result.getDate("createdDate"); 					// -- date its scheduled for
		retVal.paymentAmount = result.getBigDecimal("paymentAmount");			// -- amount applied to account
		retVal.paymentSurcharge = result.getBigDecimal("paymentSurcharge");		// -- any surcharge/convenience fee
		retVal.paymentTotalAmount = result.getBigDecimal("paymentTotalAmount");	// -- total charged to payment account					   
		retVal.paymentCategory = result.getString("paymentcategory");	 		// -- onetime or automatic
		retVal.paymentStatus = result.getString("paymentStatus");				// -- scheduled or processing
		
		// -- this is for currency --
		retVal.paymentAmount.setScale(2);
		retVal.paymentSurcharge.setScale(2);
		retVal.paymentTotalAmount.setScale(2);
		
		// -- sourcenames are a triplet of details separated by  | as delimiter --
		String lsNames = result.getString("paymentSourceNames");
		String [] lsSplitNames = lsNames.split("\\|");
		if (lsSplitNames.length == 3) {
			retVal.paymentAccountNickname = lsSplitNames[0];	// -- nickname if there is one
			retVal.paymentAccountType = lsSplitNames[1];		// -- <bank, credit, debit,
			retVal.paymentAccountMasked = lsSplitNames[2];		//. -- masked account number
		} else {
			// -- nothing we can understand --
			retVal.paymentAccountNickname = null;	
			retVal.paymentAccountType =  null;		
			retVal.paymentAccountMasked = null;		
		}

		return retVal;
	}

}
