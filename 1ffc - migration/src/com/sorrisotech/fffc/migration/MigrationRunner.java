package com.sorrisotech.fffc.migration;

import java.util.List;

public class MigrationRunner {

	public static void main(String[] args) {
		
		String szTokenFile = Config.get("tokenFile");
		TokenMap tokens = ACIToken.createACITokenMap(szTokenFile);
		
//		String szSchedPmtFile = Config.get("schedPaymentFile");
//		List<IScheduledPayment> schedPayments = 
//				OneTimeScheduledPayment.createScheduledPaymentList(szSchedPmtFile, tokens);
		
		String szAutoPmtFile = Config.get("autoPaymentFile");
		List<IAutomaticPaymentRule> autoPayments = 
				MonthlyAutomaticPaymentRule.createAutomaticPaymentList(szAutoPmtFile, tokens);
		
//		for (IScheduledPayment sp : schedPayments) {
//			sp.createScheduledPayment();
//		}

		for (IAutomaticPaymentRule ap : autoPayments) {
			ap.removeOldAutomaticPaymentInfo();
		}
		
		
		for (IAutomaticPaymentRule ap : autoPayments) {
			ap.createAutomaticPaymentRule();
		}
		
		MigrationRpt.closeReport();
	}

}
