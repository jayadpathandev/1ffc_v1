package com.sorrisotech.fffc.payment.dao;

import java.math.BigDecimal;


/**
 * Interface for returning the total of 'posted' and 'inprogress' payments for an 
 * account since the specified date
 * 
 * @author johnK
 * @since 2024-Jan-30
 * @version 2024-Jan-30
 * 		2024-Jan-30 jak first and probably last version
 */
public interface IGetPaymentHistoryAmountDao {
	
	public BigDecimal getPaymentHistoryAmountForAccount ( final String cszPaymentGroup, 
												  		  final String cszInternalAccount,
												  		  final String cszStartDate);
	
}
