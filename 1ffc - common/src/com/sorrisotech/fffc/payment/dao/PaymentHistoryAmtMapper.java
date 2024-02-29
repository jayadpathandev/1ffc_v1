package com.sorrisotech.fffc.payment.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Maps row returned from SQL to a Payment History Amount BigDecimal
 * 
 * @author johnK
 * @since  2024-Jan-31
 * @version 2024-Jan-31 jak First and hopefully only version
 * 		
 */
public class PaymentHistoryAmtMapper implements RowMapper<BigDecimal> {

	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentHistoryAmtMapper.class);

	@Override
	public BigDecimal mapRow(ResultSet arg0, int arg1) throws SQLException {
		BigDecimal ldRval = BigDecimal.ZERO;

		
		// -- get the total payment amount returned --
		ldRval = arg0.getBigDecimal("total_payments");    

		LOG.debug("PaymentHistoryAmt mapping complete, value: ().", ldRval);

		return ldRval;
	}

	
}
