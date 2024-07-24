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
 *   Implements a mmonthly autoamtic payment rule 
 *   
 *   @author johnk
 *   @since  2024-Jul-19
 *   @version 2024-Jul-19 jak first version
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
			LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Failed to open file: ().", cszFilePath);
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
				String lszPayDate = null;
				String lszBillingAcctNumber = null;
				String lszPayMethod = "Debit"; // -- current file contains all debit --
				
				// -- this served a purpose with a previous listing that did not combined
				//		the branch and loan number 
				{
					lszBillingAcctNumber = record[ciBillingAccountNumberPos];
				}
				
				
				// -- convert date to day of month --
				{
	
					lszPayDate = convertPaymentDateToDay(record[ciPaymentDatePos]);
					if (null == lszPayDate) {
						LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Parse exception for account: {}, date: {}. Skipping payment.",
								lszBillingAcctNumber, record[ciPaymentDatePos]);
						iSkipped++;
						continue;
					}
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
						LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- No account data for account: {}. Skipping payment.",
								record[ciBillingAccountNumberPos]);
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
																		record[ciBillingAccountNumberPos]);
					if (!lbSuccess) {
						LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Account {}, failure in mapping payment method: {}, last 4 digits: {}",
								lszBillingAcctNumber, lszBillingAcctNumber, record[ciPaymentAcctMasked]);
						iSkipped++;
						continue;
					}
				}
				IAutomaticPaymentRule iPayment = new MonthlyAutomaticPaymentRule(
						lszCustomerId,
						lszInternalAcct,
						lszBillingAcctNumber,
						lszPayDate,
						loPmtAcct,
						record[ciLastNamePos]);
				
				lAutoPaymentList.add(iPayment);

				LOG.debug("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- created automatic" + 
						" payment record for acccount: {}.", 
						lszBillingAcctNumber);
				
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			}
			
		} while (record != null);
		System.out.println();
		LOG.info("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				cszFilePath, iLoopCount-1, lAutoPaymentList.size(), iSkipped);
		return lAutoPaymentList;
	}
		
		/** Converts payment date fed in as MM/dd/yyyy into a day of the month
		 * 
		 * @param cszPayDate
		 * @param cszAcctNum
		 * @return
		 */
		private static String convertPaymentDateToDay(final String cszPayDate) {
			Calendar lcPayDate = null;
			String szRet = null;
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(cszPayDate);
				lcPayDate = Calendar.getInstance();
				lcPayDate.setTime(date);
				Integer day = lcPayDate.get(Calendar.DAY_OF_MONTH);
				szRet = day.toString();
			} catch (ParseException e) {
				szRet = null;
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
	public WebSvcReturnCode createAutomaticPaymentRule() {
		return null;
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
