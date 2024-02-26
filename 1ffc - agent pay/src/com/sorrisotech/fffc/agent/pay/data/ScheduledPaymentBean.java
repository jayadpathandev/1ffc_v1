package com.sorrisotech.fffc.agent.pay.data;

import java.math.BigDecimal;
import java.sql.Date;


public class ScheduledPaymentBean {

	public String		paymentId;				// --online id
	public Date	 		paymentDate; 			// -- date its scheduled for
	public BigDecimal	paymentAmount; 			// -- amount applied to account
	public BigDecimal	paymentSurcharge;		// -- any surcharge/convenience fee
	public BigDecimal	paymentTotalAmount;		// -- total charged to payment account					   
	public String		paymentCategory; 		// -- onetime or automatic
	public String		paymentStatus;			// -- scheduled or processing
	public String		paymentAccountNickname;	// -- nickname if there is one
	public String		paymentAccountMasked;	// -- masked account number
	public String		paymentAccountType;		// -- <bank, credit, debit, sepa>
}
