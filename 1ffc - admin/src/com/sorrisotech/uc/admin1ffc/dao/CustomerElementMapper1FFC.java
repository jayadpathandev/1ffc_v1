package com.sorrisotech.uc.admin1ffc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * CustomerElementMapper1FFC maps a result row in a query result to the correct element
 * in the list returned to the application for 1st Franklin.
 * 
 * @author john kowalonek
 * @version 2024-Jan-16  jak -- initial version
 * @since 2024-Jan-16
 */
public class CustomerElementMapper1FFC implements RowMapper<CustomerElement1FFC>{

	
	private static final Logger LOG = LoggerFactory.getLogger(CustomerElementMapper1FFC.class);
	
	@Override
	public CustomerElement1FFC mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		CustomerElement1FFC lCElement = new CustomerElement1FFC();
		String lszFullName = arg0.getString("FirstName") + " " + arg0.getString("LastName");
		
		lCElement.setCustomerId(arg0.getString("CustomerId"));
		lCElement.setFullName(lszFullName);
		lCElement.setEmail(arg0.getString("EmailAddress"));
		lCElement.setId(arg0.getString("CustomerId"));
		lCElement.setUserName(arg0.getString("UserName"));
		
		return lCElement;
	}
}

