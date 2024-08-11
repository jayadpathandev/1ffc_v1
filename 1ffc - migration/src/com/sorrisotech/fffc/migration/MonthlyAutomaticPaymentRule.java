/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *   Implements a monthly automatic payment rule object.
 *   
 *   @author johnk
 *   @since  2024-Jul-19
 *   @version 2024-Jul-19 jak first version
 *   @version 2024-Jul-25 jak cleanup messaging
 *   @version 2024-Aug-11 jak Updated to remove old automatic payments if needed
 */
public class MonthlyAutomaticPaymentRule implements IAutomaticPaymentRule {

	private static final Logger LOG = LoggerFactory.getLogger(MonthlyAutomaticPaymentRule.class);
	
	String m_szInternalAccount = null;
	String m_szExternalAccount = null;
	String m_szCustomerId = null;
	String m_szPaymentDay = null;
	PmtAcct m_oPayAcct = null;
	String m_szPayAcctHolder = null;
	
	/**
	 * Reads an automatic payment file and creates a map of MonthlyAutomaticPaymentRule objects
	 * 
	 * @param czFilePath
	 * @param cTokenMap
	 * @return null if it failed, a List of IAutomaticPaymentRule interface objects if it succeeds
	 */
	static public List<IAutomaticPaymentRule> createAutomaticPaymentList(String cszFilePath, final TokenMap cTokenMap) {
		final Integer ciBillingAccountNumberPos = 0;
		final Integer ciLastNamePos = 1;
		final Integer ciAddressPos = 2;
		final Integer ciPaymentAmountPos = 3;
		final Integer ciPaymentDatePos = 4;
		final Integer ciPaymentMethodPos = 5;
		final Integer ciPaymentAcctMasked = 6;
		final Integer ciPaymentAccountIdPos = 7;
		final Integer ciErrorPos = 8;
		final Integer ciNoErrorsLength = 8;
		final Integer ciErrorsLength = 9;

		List<IAutomaticPaymentRule> lAutoPaymentList = new ArrayList<IAutomaticPaymentRule>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(cszFilePath)) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- failed to open file: ().", cszFilePath);
			return null;
		}
		
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- empty file: ().", cszFilePath);
			return null;
		}
		
		do {
			record = input.getSplitLine();
			iLoopCount++;
			if (null != record) {
				String lszCustomerId = null;
				String lszInternalAcct = null;
				PmtAcct loPmtAcct = null;
				String lszPayDayOfMonth = null;
				String lszBillingAcctNumber = null;
				String lszPayMethod = "Debit"; // -- current file contains all debit --
				Integer liExpectedRecordSize = 8;
				
				if (record.length < liExpectedRecordSize) {
					LOG.warn("OneTimeScheduledPayment:createScheduledPaymentList -- Record too short account: {}. Skipping payment.",
							record[ciBillingAccountNumberPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciBillingAccountNumberPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReaon = "record to short.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
					
				
				// -- this served a purpose with a previous listing that did not combined
				//		the branch and loan number 
				{
					lszBillingAcctNumber = record[ciBillingAccountNumberPos];
				}
				
				
				// -- retrieve customerID and internalAcct given the external
				//		account number -- 
				{
					GetInternalAccountInfo info = new GetInternalAccountInfo();
					GetInternalAccountInfo.InternalAccts accts = null;
					accts = info.getAccts(record[ciBillingAccountNumberPos]);
					if (null != accts) {
					  lszCustomerId = accts.CustomerId;
					  lszInternalAcct = accts.InternalAcctId;
					} else {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentList -- Account not in online system, account: {}. Skipping payment.",
								record[ciBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReaon = "Account not available in database.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- convert date to day of month --
				{
	
					lszPayDayOfMonth = convertPaymentDateToDay(record[ciPaymentDatePos]);
					if (null == lszPayDayOfMonth) {
						LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- parse exception for day of month, account: {}, date parsed: {}. Skipping payment.",
								lszBillingAcctNumber, record[ciPaymentDatePos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReaon = "Invalid date in intput file.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}

				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctByToken( record[ciPaymentMethodPos], 
																		record[ciPaymentAccountIdPos],
																		cTokenMap,
																		record[ciBillingAccountNumberPos],
																		record[ciLastNamePos]);
					if (!lbSuccess) {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentList -- account {}, Payment data not in ACI ported data, payment method: {}, account: {}",
								lszBillingAcctNumber, lszBillingAcctNumber, record[ciPaymentAcctMasked]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.recurDay = lszPayDayOfMonth;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReaon = "Invalid or expired payment token.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- create the automatic payment rule object --
				IAutomaticPaymentRule iPayment = new MonthlyAutomaticPaymentRule(
						lszCustomerId,
						lszInternalAcct,
						lszBillingAcctNumber,
						lszPayDayOfMonth,
						loPmtAcct,
						record[ciLastNamePos]);
				
				lAutoPaymentList.add(iPayment);

				LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- created automatic" + 
						" payment record for acccount: {}, day of month: {}.", 
						lszBillingAcctNumber, lszPayDayOfMonth);
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			}
			
		} while (record != null);
		MigrationRpt.flushReport();
		System.out.println();
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				cszFilePath, iLoopCount-1, lAutoPaymentList.size(), iSkipped);
		return lAutoPaymentList;
	}
		
		/** Converts payment date fed in as date format specified in the properties
		 * 		file and convert that into a day of the month
		 * 
		 * @param cszPayDate
		 * @param cszAcctNum
		 * @return
		 */
		private static String convertPaymentDateToDay(final String cszPayDate) {
			Calendar lcPayDate = null;
			String szRet = null;

			// -- get the configuration -- 
			final String cszFormatDayOnly = "DayOfMonth";
			final String cszDateFormat = "AutoPayInputDateFormat";
			String szDateFormat = Config.get(cszDateFormat);

			if ((null != szDateFormat) && (0 == szDateFormat.compareTo(cszFormatDayOnly))) {
				szRet= cszPayDate;
			} else {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(cszDateFormat);
					Date date = sdf.parse(cszPayDate);
					lcPayDate = Calendar.getInstance();
					lcPayDate.setTime(date);
					Integer day = lcPayDate.get(Calendar.DAY_OF_MONTH);
					szRet = day.toString();
				} catch (ParseException e) {
					LOG.error("MonthlyAutomaticPaymentRule:convertPaymentDateToDate -- " + 
							"Date format failure, check properties file. " + 
							"Format processed = ", szDateFormat);
					szRet = null;
				}
			}
			return szRet;
		}
	
	/**
	 * Creates a monthly autopay rule object
	 * 
	 * @param cszInternalAccount
	 * @param cszExternalAccount
	 * @param cszCustomerId
	 * @param cszPaymentDay
	 * @param coPayAcct
	 * @param cszPayAcctHolder
	 */
	private MonthlyAutomaticPaymentRule(
			final String cszCustomerId,
			final String cszInternalAccount,
			final String cszExternalAccount,
			final String cszPaymentDay,
			final PmtAcct coPayAcct,
			final String cszPayAcctHolder
			) {
		m_szInternalAccount = cszInternalAccount;
		m_szExternalAccount = cszExternalAccount;
		m_szCustomerId = cszCustomerId;
		m_szPaymentDay = cszPaymentDay;
		m_oPayAcct = coPayAcct;
		m_szPayAcctHolder = cszPayAcctHolder;
	}
	

	@Override
	public void createAutomaticPaymentRule() {
		PaymentAPI pmt = new PaymentAPI();
		WebSvcReturnCode code = pmt.createAutomaticPaymentRule(this);
		if (null == code){
			WebSvcReturnCode code2 = pmt.getAutomaticPaymentRule(m_szCustomerId,
																 m_szInternalAccount,
																 m_szExternalAccount);
			
			MigrateRecord rcd = new MigrateRecord();
			if (null == code2) rcd.validated = "true";
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			rcd.recurDay = m_szPaymentDay;
			rcd.migrationStatus = "success";
			rcd.pmtType = "recurring";
			MigrationRpt.reportItem(rcd);
		}
		else {
			MigrateRecord rcd = new MigrateRecord();
			rcd.validated = "false";
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			rcd.recurDay = m_szPaymentDay;
			rcd.migrationStatus = "failed";
			rcd.pmtType = "recurring";
			rcd.failReaon = code.getFriendlyMessage();
			MigrationRpt.reportItem(rcd);
		}
		MigrationRpt.flushReport();
	}
	

	@Override
	public void removeOldAutomaticPaymentInfo() {
		PaymentAPI pmt = new PaymentAPI();
		WebSvcReturnCode code = pmt.deleteAutomaticPaymentRuleAndPmts(this);
		if (null == code){
			MigrateRecord rcd = new MigrateRecord();
			if (null == code) rcd.validated = "true";
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			rcd.recurDay = m_szPaymentDay;
			rcd.migrationStatus = "success";
			rcd.pmtType = "delete existing recurring pmt";
			MigrationRpt.reportItem(rcd);
		}
		else {
			MigrateRecord rcd = new MigrateRecord();
			rcd.validated = "false";
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			rcd.recurDay = m_szPaymentDay;
			if (code.success)
				rcd.migrationStatus = "success";
			else
				rcd.migrationStatus = "failed";
			rcd.pmtType = "delete exiting recurring pmt";
			rcd.failReaon = code.getFriendlyMessage();
			MigrationRpt.reportItem(rcd);
		}
		MigrationRpt.flushReport();
		
		
	}
	
	@Override
	public String getDisplayAccount() {
		return m_szExternalAccount;
	}

	@Override
	public String getInternalAccount() {
		return m_szInternalAccount;
	}

	@Override
	public String getPayDay() {
		return m_szPaymentDay;
	}

	@Override
	public String getInfoAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCustomerId() {
		return m_szCustomerId;
	}

	@Override
	public PmtAcct getPayAcct() {
		return m_oPayAcct;
	}


}
