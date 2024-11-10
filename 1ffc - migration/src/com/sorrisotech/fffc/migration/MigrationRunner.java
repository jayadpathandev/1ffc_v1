package com.sorrisotech.fffc.migration;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MigrationRunner {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		String szTokenFile = Config.get("tokenFile");
		TokenMap tokens = ACIToken.createACITokenMap(szTokenFile);

		// -- tennessee only -- 8 digit routing failure 3 payments remain --
/*		{
			PaymentAPI pay = new PaymentAPI();
									//  custId	internalact  externalid  pmtTransId
			pay.deleteScheduledPayment("2712581","2939157", "8901006821", "A00000000133148");
			pay.deleteScheduledPayment("2786005","2961313", "8944002486", "A00000000133149");
			pay.deleteScheduledPayment("2748165","2967363", "8944003335", "A00000000133150");
			
		}
*/		
				
		// -- create scheduled payment objects for debit cards --
/*		String szSchedPmtFileDebit = Config.get("schedPaymentFileDebit");
		List<IScheduledPayment> schedPaymentsDebit = 
				OneTimeScheduledPayment.createScheduledPaymentListDebit(szSchedPmtFileDebit, tokens);
		
		// -- create scheduled payment objects for ACH --
		String szSchedPmtFileACH = Config.get("schedPaymentFileACH");
		List<IScheduledPayment> schedPaymentsACH = 
				OneTimeScheduledPayment.createScheduledPaymentListACH(szSchedPmtFileACH);
*/		
/*		// -- create autopay objects for debit cards --
		String szAutoPmtFileDebit = Config.get("autoPaymentFileDebit");
		List<IAutomaticPaymentRule> autoPaymentsDebit = 
				MonthlyAutomaticPaymentRule.createAutomaticPaymentListDebit(szAutoPmtFileDebit, tokens);
*/		
		
		String szAutoPmtFileACH = Config.get("autoPaymentFileACH");
		List<IAutomaticPaymentRule> autoPaymentsACH = 
				MonthlyAutomaticPaymentRule.createAutomaticPaymentListACH(szAutoPmtFileACH);

		for (IAutomaticPaymentRule ap: autoPaymentsACH) {
			if (0 < ap.getExtraPayment().compareTo(BigDecimal.ZERO)) {
				ap.removeOldAutomaticPaymentInfo();
				ap.createAutomaticPaymentRule();
			}
		}

		
/*		for (IScheduledPayment sp : schedPaymentsDebit) {
			sp.createScheduledPayment();
		}

		for (IScheduledPayment sp : schedPaymentsACH) {
			sp.createScheduledPayment();
		}
*/
//		for (IAutomaticPaymentRule ap : autoPaymentsDebit) {
//			ap.removeOldAutomaticPaymentInfo();
//		}
		
		
/*		for (IAutomaticPaymentRule ap : autoPaymentsDebit) {
			ap.createAutomaticPaymentRule();
		}
*/

/*		for (IAutomaticPaymentRule ap : autoPaymentsACH) {
			ap.removeOldAutomaticPaymentInfo();
		}
*/		
/*		for (IAutomaticPaymentRule ap : autoPaymentsACH) {
			ap.createAutomaticPaymentRule();
		}
*/		
		MigrationRpt.closeReport();
	}

}
