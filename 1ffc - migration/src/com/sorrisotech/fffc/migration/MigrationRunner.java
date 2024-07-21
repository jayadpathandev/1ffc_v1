package com.sorrisotech.fffc.migration;

import java.util.List;

public class MigrationRunner {

	public static void main(String[] args) {
		
		String szTokenFile = "/home/johnk/migration-files/resp_fund_extract_07172024.txt";
		TokenMap tokens = ACIToken.createACITokenMap(szTokenFile);
		
		String szSchedPmtFile = "/home/johnk/migration-files/Scheduled Payments 2024-Jul-20.txt";
		List<IScheduledPayment> schedPayments = 
				OneTimeScheduledPayment.createScheduledPaymentList(szSchedPmtFile, tokens);
		
		String szAutoPmtFile = "/home/johnk/migration-files/20240524_KY and VA Recuring Future Dated Debit.txt";
		List<IAutomaticPaymentRule> autoPayments = 
				MonthlyAutomaticPaymentRule.createAutomaticPaymentList(szAutoPmtFile, tokens);
	}

}
