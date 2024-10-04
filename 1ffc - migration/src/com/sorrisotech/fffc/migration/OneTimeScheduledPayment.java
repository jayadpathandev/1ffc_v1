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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Implements a one time scheduled paymnet object.
 *  
 *   @author johnk
 *   @since  2024-Jul-19
 *   @version 2024-Jul-19 jak first version
 *   @version 2024-Jul-25 jak cleanup messaging
 *   @version 2024-Sep-29 jak add ach payment type
 *  
 */
public class OneTimeScheduledPayment implements IScheduledPayment {

	private static final Logger LOG = LoggerFactory.getLogger(OneTimeScheduledPayment.class);
	
	private String m_szInternalAccount = null;
	private String m_szExternalAccount = null;
	private String m_szLastName = null;
	private String m_szAddress = null;
	private String m_szCustomerId = null;
	private BigDecimal m_dPayAmount = null;
	private LocalDate m_calPaymentDate = null;
	private PmtAcct m_oPmtAccount = null;
	private String m_szPaymentId = null;
	private String m_szPaymentStatus = null;
	
	/**
	 * Reads a scheduled payment file and creates a list of OneTimeScheduledPayment objects
	 * 
	 * @param czFilePath
	 * @return null if it failed, a List of IScheduledPayment interface objects if it succeeds
	 */
	static public List<IScheduledPayment> createScheduledPaymentListDebit(String czFilePath, TokenMap mTokenMap) {
		final Integer ciBillingAccountNumberPos = 0;
		final Integer ciLastNamePos = 1;
		final Integer ciAddressPos = 2;
		final Integer ciPaymentAmountPos = 3;
		final Integer ciContractualAmount = 4;
		final Integer ciExtraAmount = 5;
		final Integer ciPaymentDatePos = 6;
		final Integer ciPaymentMethodPos = 7;
		final Integer ciPaymentAcctMasked = 8;
		final Integer ciPaymentAccountIdPos = 9;
		final Integer ciNoErrorsLength = 10;

		List<IScheduledPayment> lSchedPaymentList = new ArrayList<IScheduledPayment>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(czFilePath)) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentListDebit -- Failed to open file: ().", czFilePath);
			return null;
		}
		
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentListDebit -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentListDebit -- empty file: ().", czFilePath);
			return null;
		}
		
		do {
			record = input.getSplitLine();
			iLoopCount++;
			if ((null != record) && (record.length == ciNoErrorsLength)) {
				String lszCustomerId = null;
				String lszInternalAcct = null;
				BigDecimal ldPayAmt = null;
				PmtAcct loPmtAcct = null;
				LocalDate lcPayDate = null;

				
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
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- Account not in online system, account: {}. Skipping payment.",
								record[ciBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = null;
						rcd.internalAcct =  null;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Account not available in database.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- convert pay amount --
				try {
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setGroupingSeparator(',');
					symbols.setDecimalSeparator('.');
					String pattern = "#,##0.0#";
					DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
					decimalFormat.setParseBigDecimal(true);
					ldPayAmt  = (BigDecimal) decimalFormat.parse(record[ciPaymentAmountPos]);

				} catch (NullPointerException | ParseException e) {
					LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- number format exception for account: {}, pay amount: {}. Skipping payment.",
							record[ciBillingAccountNumberPos], record[ciPaymentAmountPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.schedAmt = record[ciPaymentAmountPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "scheduled";
					rcd.failReason = "Invalid format for scheduled amount.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- convert date --
				lcPayDate = convertPaymentDate(record[ciPaymentDatePos]);
				if (null == lcPayDate) {
					LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- parse exception for account: {}, date: {}. Skipping payment.",
							record[ciBillingAccountNumberPos], record[ciPaymentDatePos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.schedAmt = record[ciPaymentAmountPos];
					rcd.schedDate = record[ciPaymentDatePos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "scheduled";
					rcd.failReason = "Invalid format for scheduled pmt date.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- don't schedule payments in the pasts -- 
				{
					LocalDate now = LocalDate.now();
					if (now.isAfter(lcPayDate)) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
						String szNow = formatter.format(now);
						String szSched = formatter.format(lcPayDate);
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- " + 
						"scheduled payment before current date payment date: {}, current date: {}", szSched, szNow);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.schedAmt = record[ciPaymentAmountPos];
						rcd.schedDate = record[ciPaymentDatePos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Scheduled pmt date in past.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				
				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctByToken( record[ciPaymentAccountIdPos],
																		mTokenMap,
																		record[ciBillingAccountNumberPos],
																		record[ciLastNamePos]);
					if (!lbSuccess) {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- account {}, Payment data not in ACI ported data, payment method: {}, account: {}",
								record[ciBillingAccountNumberPos], record[ciPaymentMethodPos], record[ciPaymentAccountIdPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.schedAmt = record[ciPaymentAmountPos];
						rcd.schedDate = record[ciPaymentDatePos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Invalid or expired payment token.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- create the payment object  and add to list --
				IScheduledPayment iPayment = new OneTimeScheduledPayment (
						lszCustomerId,
						lszInternalAcct,
						record[ciBillingAccountNumberPos],
						record[ciLastNamePos],
						record[ciAddressPos],
						ldPayAmt,
						lcPayDate,
						loPmtAcct			) ;
				lSchedPaymentList.add(iPayment); 
				LOG.info("OneTimeScheduledPayment:createScheduledPaymentListDebit -- created sheduled" + 
						" payment record for acccount: {}. Payment date: {}, ammount: {}.", 
						record[ciBillingAccountNumberPos],
						record[ciPaymentDatePos], 
						ldPayAmt.toString());
				
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			} else {
				if (null != record) {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListDebit -- error in scheduled" + 
									" payment record for acccount: {}.", 
									record[ciBillingAccountNumberPos]);
					iSkipped++;
				}
			}
			
		} while (record != null);
		MigrationRpt.flushReport();
		System.out.println();
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentListDebit -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				czFilePath, iLoopCount-1, lSchedPaymentList.size(), iSkipped);
		return lSchedPaymentList;
	}

	static public List<IScheduledPayment> createScheduledPaymentListACH(String czFilePath) {
		final Integer ciBillingAccountNumberPos = 0;
		final Integer ciLastNamePos = 1;
		final Integer ciBankAcctType = 2;
		final Integer ciRoutingNumber = 3;
		final Integer ciBankAcctNumber = 4;
		final Integer ciPaymentAmountPos = 5;
		final Integer ciPaymentType = 6;
		final Integer ciPaymentDatePos = 7;
		final Integer ciNoErrorsLength = 8;

		List<IScheduledPayment> lSchedPaymentList = new ArrayList<IScheduledPayment>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(czFilePath)) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentListACH -- Failed to open file: ().", czFilePath);
			return null;
		}
		
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentListACH -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentListACH -- empty file: ().", czFilePath);
			return null;
		}
		
		do {
			record = input.getSplitLine();
			iLoopCount++;
			if ((null != record) && (record.length == ciNoErrorsLength)) {
				String lszCustomerId = null;
				String lszInternalAcct = null;
				BigDecimal ldPayAmt = null;
				PmtAcct loPmtAcct = null;
				LocalDate lcPayDate = null;

				
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
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- Account not in online system, account: {}. Skipping payment.",
								record[ciBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = null;
						rcd.internalAcct =  null;
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Account not available in database.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- convert pay amount --
				try {
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setGroupingSeparator(',');
					symbols.setDecimalSeparator('.');
					String pattern = "#,##0.0#";
					DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
					decimalFormat.setParseBigDecimal(true);
					ldPayAmt  = (BigDecimal) decimalFormat.parse(record[ciPaymentAmountPos]);

				} catch (NullPointerException | ParseException e) {
					LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- number format exception for account: {}, pay amount: {}. Skipping payment.",
							record[ciBillingAccountNumberPos], record[ciPaymentAmountPos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.schedAmt = record[ciPaymentAmountPos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "scheduled";
					rcd.failReason = "Invalid format for scheduled amount.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- convert date --
				lcPayDate = convertPaymentDate(record[ciPaymentDatePos]);
				if (null == lcPayDate) {
					LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- parse exception for account: {}, date: {}. Skipping payment.",
							record[ciBillingAccountNumberPos], record[ciPaymentDatePos]);
					MigrateRecord rcd = new MigrateRecord();
					rcd.displayAcct = record[ciBillingAccountNumberPos];
					rcd.customerId = lszCustomerId;
					rcd.internalAcct =  lszInternalAcct;
					rcd.schedAmt = record[ciPaymentAmountPos];
					rcd.schedDate = record[ciPaymentDatePos];
					rcd.migrationStatus = "failed";
					rcd.pmtType = "scheduled";
					rcd.failReason = "Invalid format for scheduled pmt date.";
					MigrationRpt.reportItem(rcd);
					iSkipped++;
					continue;
				}
				
				// -- don't schedule payments in the pasts -- 
				{
					LocalDate now = LocalDate.now();
					if (now.isAfter(lcPayDate)) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
						String szNow = formatter.format(now);
						String szSched = formatter.format(lcPayDate);
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- " + 
						"scheduled payment before current date payment date: {}, current date: {}", szSched, szNow);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.schedAmt = record[ciPaymentAmountPos];
						rcd.schedDate = record[ciPaymentDatePos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Scheduled pmt date in past.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctByBank( record[ciRoutingNumber],
																	   record[ciBankAcctNumber],
																	   record[ciBankAcctType],
																	   record[ciBillingAccountNumberPos],
																	   record[ciLastNamePos]);
					if (!lbSuccess) {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- account {}, ailed validation, probably acsount type.",
								record[ciBillingAccountNumberPos]);
						MigrateRecord rcd = new MigrateRecord();
						rcd.displayAcct = record[ciBillingAccountNumberPos];
						rcd.customerId = lszCustomerId;
						rcd.internalAcct =  lszInternalAcct;
						rcd.schedAmt = record[ciPaymentAmountPos];
						rcd.schedDate = record[ciPaymentDatePos];
						rcd.migrationStatus = "failed";
						rcd.pmtType = "scheduled";
						rcd.failReason = "Invalid bank information.";
						MigrationRpt.reportItem(rcd);
						iSkipped++;
						continue;
					}
				}
				
				// -- create the payment object  and add to list --
				IScheduledPayment iPayment = new OneTimeScheduledPayment (
						lszCustomerId,
						lszInternalAcct,
						record[ciBillingAccountNumberPos],
						record[ciLastNamePos],
						"Not Available",
						ldPayAmt,
						lcPayDate,
						loPmtAcct			) ;
				lSchedPaymentList.add(iPayment); 
				LOG.info("OneTimeScheduledPayment:createScheduledPaymentListACH -- created sheduled" + 
						" payment record for acccount: {}. Payment date: {}, ammount: {}.", 
						record[ciBillingAccountNumberPos],
						record[ciPaymentDatePos], 
						ldPayAmt.toString());
				
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			} else {
				if (null != record) {
						LOG.warn("OneTimeScheduledPayment:createScheduledPaymentListACH -- error in scheduled" + 
									" payment record for acccount: {}.", 
									record[ciBillingAccountNumberPos]);
					iSkipped++;
				}
			}
			
		} while (record != null);
		MigrationRpt.flushReport();
		System.out.println();
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentListACH -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				czFilePath, iLoopCount-1, lSchedPaymentList.size(), iSkipped);
		return lSchedPaymentList;
	}
	
	/**
	 * Converts payment date fed in as MM/dd/yyyy into a LocalDate object
	 * 
	 * @param cszPayDate
	 * @param cszAcctNum
	 * @return
	 */
	private static LocalDate convertPaymentDate(final String cszPayDate) {
		LocalDate lcPayDate = null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		lcPayDate = LocalDate.parse(cszPayDate, formatter);
		return lcPayDate;
	}
	

		
	/**
	 * Creates a OneTimeScheduledPayment object
	 * 
	 *  @param cszCustomerId
	 *  @param cszInternalAccount
	 *  @param cszExternalAccount
	 *	@param cszLastName
	 *  @param cszAddress
	 *  @param cdPayAmount
	 *  @param cszPaymentDate
	 *  @param clPayAcct
	 */
	private OneTimeScheduledPayment (
			final String cszCustomerId,
			final String cszInternalAccount,
			final String cszExternalAccount,
			final String cszLastName,
			final String cszAddress,
			final BigDecimal cdPayAmount,
			final LocalDate ccalPaymentDate,
			final PmtAcct clPayAcct			) {
		m_szInternalAccount = cszInternalAccount;
		m_szExternalAccount = cszExternalAccount;
		m_szLastName = cszLastName;
		m_szAddress = cszAddress;
		m_szCustomerId = cszCustomerId;
		m_dPayAmount = cdPayAmount;
		m_calPaymentDate = ccalPaymentDate;
		m_oPmtAccount = clPayAcct;
	}
	
	
	@Override
	public void createScheduledPayment() {

		PaymentAPI pmt = new PaymentAPI();
		WebSvcReturnCode code = pmt.createScheduledPayment(this);
		if(null == code) {
			WebSvcReturnCode code2 = pmt.getScheduledPayment(m_szCustomerId,  m_szInternalAccount, m_szExternalAccount, m_szPaymentId);
			MigrateRecord rcd = new MigrateRecord();
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			if (null != m_szPaymentId && !m_szPaymentId.isBlank())
				rcd.validated = "true"; 
			else 
				rcd.validated = "false";
			rcd.schedPmtId = m_szPaymentId;
			rcd.schedAmt = getPayAmount();
			rcd.schedDate = getPayDate();
			rcd.migrationStatus = "success";
			rcd.pmtType = "scheduled";
			MigrationRpt.reportItem(rcd);
		}
		else {
			MigrateRecord rcd = new MigrateRecord();
			rcd.displayAcct = m_szExternalAccount;
			rcd.customerId = m_szCustomerId;
			rcd.internalAcct =  m_szInternalAccount;
			if (null != m_szPaymentId && !m_szPaymentId.isBlank())
				rcd.validated = "true"; 
			else 
				rcd.validated = "false";
			rcd.schedPmtId = m_szPaymentId;
			rcd.schedAmt = getPayAmount();
			rcd.schedDate = getPayDate();
			rcd.migrationStatus = "failed";
			rcd.pmtType = "scheduled";
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
	public String getPayAmount() {
		m_dPayAmount.setScale(2);
		return m_dPayAmount.toString();
	}

	@Override
	public String getPayDate() {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String lszDate = null;
		try {
	        lszDate = formatter.format(m_calPaymentDate);						
		} catch(NullPointerException e) {
			LOG.error("Could not parse date [" + m_calPaymentDate + "].");
		}
		return lszDate;
	}
	

	@Override
	public String getInfoAsString() {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

		String date = formatter.format(m_calPaymentDate);            
		String lszInfoReturned = 
				"OneTimeScheduledPayment -- CustomerId: " +
				m_szCustomerId + ", External account: " +
				m_szExternalAccount + ", Internal account: " +
				m_szInternalAccount + ", Payment date: " + 
				date + " Payment amount: " + m_dPayAmount +
				", paymentType: " + m_oPmtAccount.m_payType.toString();
				
		return lszInfoReturned;
	}

	@Override
	public String getCustomerId() {
		return m_szCustomerId;
	}

	@Override
	public PmtAcct getPayAcct() {
		return m_oPmtAccount;
	}

	@Override
	public void setPayTransactionInfo(String cszPaymentId, String cszPaymentStatus) {
		m_szPaymentId = cszPaymentId;
		m_szPaymentStatus = cszPaymentStatus;
	}

	@Override
	public String getPaymentId() {
		return m_szPaymentId;
	}

	@Override
	public String getPaymentStatus() {
		return m_szPaymentStatus;
	}

	@Override
	public String getOrgIdWhereRoutingNumberIsZero() {
		String szRetValue = null;
		if (m_oPmtAccount.m_szBankRouting.charAt(0) == "0".charAt(0)) {
			szRetValue = getCustomerId(); 
			LOG.info("getOrgIdWhereRoutingNumberIsZero-- OrgId {}, Routing Number {}.",
					szRetValue, m_oPmtAccount.m_szBankRouting);
			CustomerId rcd = new CustomerId();
			rcd.m_szCustomerId = szRetValue;
			RoutingLeading0s.reportItem(rcd);

		}
		return szRetValue;
	}	
}
