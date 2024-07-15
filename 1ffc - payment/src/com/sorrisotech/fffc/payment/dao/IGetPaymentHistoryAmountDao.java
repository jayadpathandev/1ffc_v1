package com.sorrisotech.fffc.payment.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * Interface for returning the total of 'posted' and 'inprogress' payments for an 
 * account since the specified date
 * 
 * @author johnK
 * @since 2024-Jan-30
 * @version 2024-Jul-13
 * 		2024-Jan-30 jak first and probably last version
 * 		2024-Jul-13 jak	change to use full timestamp for query
 */
public interface IGetPaymentHistoryAmountDao {
	
	public BigDecimal getPaymentHistoryAmountForAccount ( final String cszPaymentGroup, 
												  		  final String cszInternalAccount,
												  		  final Timestamp ctStartTime);
	
	public BigDecimal getTotalScheduledPaymentBeforeBillDue(final String cszUserId, 
															final String cszBillDueDate, 
															final String cszInternalAccountNumber);
	
}
