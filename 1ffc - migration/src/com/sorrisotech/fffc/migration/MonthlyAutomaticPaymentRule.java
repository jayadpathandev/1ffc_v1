/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
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
		final Integer Branch = 0;
		final Integer Account = 1;
		final Integer PayAcctHolderName = 10;
		final Integer PayMethodLast4 = 9;
		final Integer ExpireDate = 11; // -- this is often invalid
		final Integer PayDate = 8;

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
				
				// -- create a full billing account number --
				{
					if (null != record[Branch] && null != record[Account] &&
						!record[Branch].isBlank() && (4 == record[Branch].length()) &&
						!record[Account].isBlank() && (6 == record[Account].length())) {
						// -- good account/branch combination we think --
						lszBillingAcctNumber = record[Branch] + record[Account];
					} else {
						LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Can't construct loan number from branch: {}, account: {}. Skipping payment.",
								record[Branch], record[Account]);
						iSkipped++;
						continue;
						
					}
				}
				
				
				// -- convert date to day of month --
				{
	
					lszPayDate = convertPaymentDateToDay(record[PayDate]);
					if (null == lszPayDate) {
						LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Parse exception for account: {}, date: {}. Skipping payment.",
								lszBillingAcctNumber, record[PayDate]);
						iSkipped++;
						continue;
					}
				}
				
				// -- retrieve customerID and internalAcct given the external
				//		account number -- 
				{
					lszCustomerId = "CUSTOMERID";
					lszInternalAcct = "INTERNALACCT";
				}
				
				// -- set up payment structure
				{
					loPmtAcct = new PmtAcct();
					Boolean lbSuccess = loPmtAcct.createPayAcctFromBillAccount(
							lszPayMethod, 
							lszBillingAcctNumber,
							cTokenMap,
							record[PayMethodLast4]);
					if (!lbSuccess) {
						LOG.error("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- Account {}, failure in mapping payment method: {}, last 4 digits: {}",
								lszBillingAcctNumber, lszBillingAcctNumber, record[PayMethodLast4]);
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
						record[PayAcctHolderName]);
				
				lAutoPaymentList.add(iPayment);

				LOG.debug("MonthlyAutomaticPaymentRule:createAutomaticPaymentList -- created sheduled" + 
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
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
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
	public String getPayAmount() {
		return null;
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

}
