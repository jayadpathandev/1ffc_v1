/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.common.util.StringUtils;



/**
 *   Implements a monthly automatic payment rule object.
 *   
 *   @author johnk
 *   @since  2024-Jul-19
 *   @version 2024-Jul-19 jak first version
 *   @version 2024-Jul-25 jak cleanup messaging
 *   @version 2024-Aug-11 jak Updated to remove old automatic payments if needed
 *   @version 2024-Sep-29	jak Support for extra payment in automatic payments
 */
public class MonthlyAutomaticPaymentRule implements IAutomaticPaymentRule {

	private static final Logger LOG = LoggerFactory.getLogger(MonthlyAutomaticPaymentRule.class);
	
	String m_szInternalAccount = null;
	String m_szExternalAccount = null;
	String m_szCustomerId = null;
	String m_szPaymentDay = null;
	PmtAcct m_oPayAcct = null;
	String m_szPayAcctHolder = null;
	BigDecimal m_bdExtraPayment = BigDecimal.ZERO;
	LocalDate m_ldStartDate = LocalDate.now();
	
	/**
	 * Reads an automatic payment file and creates a map of MonthlyAutomaticPaymentRule objects
	 * 
	 * @param czFilePath
	 * @param cTokenMap
	 * @return null if it failed, a List of IAutomaticPaymentRule interface objects if it succeeds
	 */
	static public List<IAutomaticPaymentRule> createAutomaticPaymentListDebit(String cszFilePath, final TokenMap cTokenMap) {
		// -- 
		final Integer ciDebitBillingAccountNumberPos = 0;
		final Integer ciDebitLastNamePos = 1;
		final Integer ciDebitAddressPos = 2;
		final Integer ciDebitPaymentAmountPos = 3;
		final Integer ciDebitContractedAmountPos = 4;
		final Integer ciDebitExtraAmountPos = 5;
		final Integer ciDebitPaymentDatePos = 6;
		final Integer ciDebitPaymentMethodPos = 7;
		final Integer ciDebitPaymentAcctMasked = 8;
		final Integer ciDebitPaymentAccountIdPos = 9;
		
		

		List<IAutomaticPaymentRule> lAutoPaymentList = new ArrayList<IAutomaticPaymentRule>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(cszFilePath)) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- failed to open file: ().", cszFilePath);
			return null;
		}
		
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- empty file: ().", cszFilePath);
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
				BigDecimal lbdExtraPayment = BigDecimal.ZERO;
				LocalDate ldStartDate = LocalDate.now();
				Integer liExpectedRecordSize = 8;
				
				if (record.length < liExpectedRecordSize) {
					LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- Record too short account: {}. Skipping payment.",
							record[ciDebitBillingAccountNumberPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "record to0 short.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
					
				
				// -- this served a purpose with a previous listing that did not combined
				//		the branch and loan number 
				{
					lszBillingAcctNumber = record[ciDebitBillingAccountNumberPos];
				}
				
				
				// -- retrieve customerID and internalAcct given the external
				//		account number -- 
				{
					GetInternalAccountInfo info = new GetInternalAccountInfo();
					GetInternalAccountInfo.InternalAccts accts = null;
					accts = info.getAccts(record[ciDebitBillingAccountNumberPos]);
					if (null != accts) {
					  lszCustomerId = accts.CustomerId;
					  lszInternalAcct = accts.InternalAcctId;
					} else {
						LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- Account not in online system, account: {}. Skipping payment.",
								record[ciDebitBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Account not available in database.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- convert date string to a LocalDate for processing --
				try	{
					 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
					 ldStartDate = LocalDate.parse(record[ciDebitPaymentDatePos], formatter);
				} catch (DateTimeParseException e) {
					LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- Date in wrong format, account {}. date value {}. Skipping payment.",
						record[ciDebitBillingAccountNumberPos], record[ciDebitPaymentDatePos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "Bad date format.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- convert date to day of month --
				{
	
					lszPayDayOfMonth = convertPaymentDateToDay(record[ciDebitPaymentDatePos]);
					if (null == lszPayDayOfMonth) {
						LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- parse exception for day of month, account: {}, date parsed: {}. Skipping payment.",
								lszBillingAcctNumber, record[ciDebitPaymentDatePos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Invalid date in intput file.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- get the extra payment amount if there is any --
				try {
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setGroupingSeparator(',');
					symbols.setDecimalSeparator('.');
					String pattern = "#,##0.0#";
					DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
					decimalFormat.setParseBigDecimal(true);
					lbdExtraPayment  = (BigDecimal) decimalFormat.parse(record[ciDebitExtraAmountPos]);
					if (0 > lbdExtraPayment.compareTo(BigDecimal.ZERO)) {
						lbdExtraPayment = BigDecimal.ZERO;
					}
				} catch (NullPointerException | ParseException e) {
					LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- parse exception for extra payment, account: {}, amount parsed: {}. Skipping payment.",
							lszBillingAcctNumber, record[ciDebitExtraAmountPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "Invalid extra amount in intput file.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}

				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctByToken( record[ciDebitPaymentAccountIdPos],
																		cTokenMap,
																		record[ciDebitBillingAccountNumberPos],
																		record[ciDebitLastNamePos]);
					if (!lbSuccess) {
						LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- account {}, Payment data not in ACI ported data, payment method: {}, account: {}",
								lszBillingAcctNumber, lszBillingAcctNumber, record[ciDebitPaymentAcctMasked]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciDebitBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.recurDay = lszPayDayOfMonth;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Invalid or expired payment token.";
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
						record[ciDebitLastNamePos],
						lbdExtraPayment,
						ldStartDate);
				
				lAutoPaymentList.add(iPayment);

				LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- created automatic" + 
						" payment record for acccount: {}, day of month: {}.", 
						lszBillingAcctNumber, lszPayDayOfMonth);
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			}
			
		} while (record != null);
		MigrationRpt.flushReport();
		System.out.println();
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				cszFilePath, iLoopCount-1, lAutoPaymentList.size(), iSkipped);
		return lAutoPaymentList;
	}

	static public List<IAutomaticPaymentRule> createAutomaticPaymentListACH(String cszFilePath) {
	
		final Integer ciACHBillingAccountNumberPos = 0;
		final Integer ciACHLastNamePos = 1;
		final Integer ciACHAccountTypePos = 2;
		final Integer ciACHRoutingNumberPos = 3;
		final Integer ciACHAcountNumberPos = 4;
		final Integer ciACHPayAmountPos = 5;
		final Integer ciACHContractedAmountPos = 6;
		final Integer ciACHExtraAmountPos = 7;
		final Integer ciACHStartDatePos = 8;

		List<IAutomaticPaymentRule> lAutoPaymentList = new ArrayList<IAutomaticPaymentRule>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(cszFilePath)) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentListACH -- failed to open file: ().", cszFilePath);
			return null;
		}
		
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListACH -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentListACH -- empty file: ().", cszFilePath);
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
				String lszPayMethod = "ACH"; // -- current file contains all ACH --
				BigDecimal lbdExtraPayment = BigDecimal.ZERO;
				LocalDate ldStartDate = LocalDate.now();
				Integer liExpectedRecordSize = 12;
				
				if (record.length < liExpectedRecordSize) {
					LOG.warn("MonthlyAutomaticPaymentRule:createScheduledPaymentListACH -- Record too short account: {}. Skipping payment.",
							record[ciACHBillingAccountNumberPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciACHBillingAccountNumberPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "record to0 short.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
					
				
				// -- this served a purpose with a previous listing that did not combined
				//		the branch and loan number 
				{
					lszBillingAcctNumber = record[ciACHBillingAccountNumberPos];
				}
				
				
				// -- retrieve customerID and internalAcct given the external
				//		account number -- 
				{
					GetInternalAccountInfo info = new GetInternalAccountInfo();
					GetInternalAccountInfo.InternalAccts accts = null;
					accts = info.getAccts(record[ciACHBillingAccountNumberPos]);
					if (null != accts) {
					  lszCustomerId = accts.CustomerId;
					  lszInternalAcct = accts.InternalAcctId;
					} else {
						LOG.warn("MonthlyAutomaticPaymentRule:createScheduledPaymentListDebit -- Account not in online system, account: {}. Skipping payment.",
								record[ciACHBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciACHBillingAccountNumberPos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Account not available in database.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- convert date string to a LocalDate for processing --
				try	{
					 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
					 ldStartDate = LocalDate.parse(record[ciACHStartDatePos], formatter);
				} catch (DateTimeParseException e) {
					LOG.warn("MonthlyAutomaticPaymentRule:createScheduledPaymentListDebit -- Date in wrong format, account {}. date value {}. Skipping payment.",
						record[ciACHBillingAccountNumberPos], record[ciACHStartDatePos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciACHBillingAccountNumberPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "Bad date format.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- convert date to day of month --
				{
	
					lszPayDayOfMonth = convertPaymentDateToDay(record[ciACHStartDatePos]);
					if (null == lszPayDayOfMonth) {
						LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- parse exception for day of month, account: {}, date parsed: {}. Skipping payment.",
								lszBillingAcctNumber, record[ciACHStartDatePos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciACHBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Invalid date in intput file.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- get the contracted amount in order to calculate the "extra amount" if any
				{
					// -- an api to get the contracted amount without user id does not exist --
				}
				
				// -- calculate the extra amount if there is any --
				try {
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setGroupingSeparator(',');
					symbols.setDecimalSeparator('.');
					String pattern = "#,##0.0#";
					DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
					decimalFormat.setParseBigDecimal(true);
					lbdExtraPayment  = (BigDecimal) decimalFormat.parse(record[ciACHExtraAmountPos]);
					if (0 > lbdExtraPayment.compareTo(BigDecimal.ZERO)) {
						lbdExtraPayment = BigDecimal.ZERO;
					}
				} catch (NullPointerException | ParseException e) {
					LOG.warn("MonthlyAutomaticPaymentRule:createAutomaticPaymentListDebit -- parse exception for extra payment, account: {}, amount parsed: {}. Skipping payment.",
							lszBillingAcctNumber, record[ciACHExtraAmountPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciACHBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.migrationStatus = "failed";
					rcd.pmtType = "recurring";
					rcd.failReason = "Invalid extra amount in intput file.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}				

				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctByBank( 
																		record[ciACHRoutingNumberPos],
																		record[ciACHAcountNumberPos],
																		record[ciACHAccountTypePos],
																		record[ciACHBillingAccountNumberPos],
																		record[ciACHLastNamePos]);
					if (!lbSuccess) {
						LOG.warn("MonthlyAutomaticPaymentRule:createScheduledPaymentListACH -- account {}, failed validation, probably acsount type.",
								lszBillingAcctNumber);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciACHBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.recurDay = lszPayDayOfMonth;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "recurring";
						rcd.failReason = "Invalid bank information.";
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
						record[ciACHLastNamePos],
						lbdExtraPayment,
						ldStartDate);
				
				lAutoPaymentList.add(iPayment);

				LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListACH -- created automatic" + 
						" payment record for acccount: {}, day of month: {}.", 
						lszBillingAcctNumber, lszPayDayOfMonth);
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			}
			
		} while (record != null);
		MigrationRpt.flushReport();
		System.out.println();
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentListACH -- " + 
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
	 * @param cbdExtraPayment
	 * @param cldStartDate
	 */
	private MonthlyAutomaticPaymentRule(
			final String cszCustomerId,
			final String cszInternalAccount,
			final String cszExternalAccount,
			final String cszPaymentDay,
			final PmtAcct coPayAcct,
			final String cszPayAcctHolder,
			final BigDecimal cbdExtraPayment,
			final LocalDate  cldStartDate
			) {
		m_szInternalAccount = cszInternalAccount;
		m_szExternalAccount = cszExternalAccount;
		m_szCustomerId = cszCustomerId;
		m_szPaymentDay = cszPaymentDay;
		m_oPayAcct = coPayAcct;
		m_szPayAcctHolder = cszPayAcctHolder;
		m_bdExtraPayment = cbdExtraPayment;
		m_ldStartDate = cldStartDate;
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
			rcd.failReason = code.getFriendlyMessage();
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
			rcd.failReason = code.getFriendlyMessage();
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

	@Override
	public BigDecimal getExtraPayment() {
		return m_bdExtraPayment;
	}

	@Override
	public LocalDate getStartDate() {
		return m_ldStartDate;
	}

	@Override
	public String getOrgIdWhereRoutingNumberIsZero() {
		String szRetValue = null;
		
		if (m_oPayAcct.m_szBankRouting.charAt(0) == "0".charAt(0)) {
			szRetValue = getCustomerId(); 
			LOG.info("getOrgIdWhereRoutingNumberIsZero-- OrgId {}, Routing Number {}.",
					szRetValue, m_oPayAcct.m_szBankRouting);
			CustomerId rcd = new CustomerId();
			rcd.m_szCustomerId = szRetValue;
			RoutingLeading0s.reportItem(rcd);

		}
		return szRetValue;
	}


}
