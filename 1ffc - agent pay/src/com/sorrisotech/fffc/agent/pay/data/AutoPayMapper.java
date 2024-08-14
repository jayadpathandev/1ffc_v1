package com.sorrisotech.fffc.agent.pay.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.springframework.jdbc.core.RowMapper;

public class AutoPayMapper implements RowMapper<AutoPayBean> {

	@Override
	public AutoPayBean mapRow(
				final ResultSet result, 
				final int       row
			) throws SQLException {
		final var retval = new AutoPayBean();
		
		final var date = result.getBigDecimal(1);
		if (date != null) {
			retval.when = "dayOfMonth=" + date.toPlainString();
		} 
		final var days = result.getBigDecimal(2);
		if (days != null) {
			retval.when = "daysBefore=" + days.toPlainString();
		}			
		
		final var cancel = result.getString(3);
		if (cancel != null) {
			retval.stop = "untilCancelled";
		}
		final var expiry = result.getDate(4);
		if (expiry != null) {
			final var f = new SimpleDateFormat("yyyy/MM/dd");			
			retval.stop = "Date=" + f.format(expiry);					
		}
		final var count = result.getBigDecimal(5);
		if (count != null) {
			retval.stop = "PaymentCount=" + count.toPlainString();
		}
		
		final var rule = result.getString(6);
		if (rule != null) {
			if (rule.equals("minimum"))
				retval.amount = "minimumAmountDue";
			else
				retval.amount = "billAmount";
		}
		final var amt = result.getBigDecimal(7);
		if (amt != null) {
			retval.amount = "uptoAmount=" + amt.toPlainString();
		}
		
		final var id = result.getBigDecimal(8);
		if (id != null) {
			retval.id = id.toPlainString();
		}
		
		final var sourceId = result.getString(9);
		if (sourceId != null) {
			retval.sourceId = sourceId;
		}
		
		final var userId = result.getString(10);
		if (userId != null) {
			retval.userId = userId;
		}

		return retval; 
	}

}
