/**
 * 
 */
package com.sorrisotech.fffc.migration;

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
 * 
 */
public class OneTimeScheduledPayment implements IScheduledPayment {

	private static final Logger LOG = LoggerFactory.getLogger(OneTimeScheduledPayment.class);
	
	String m_szInternalAccount = null;
	String m_szExternalAccount = null;
	String m_szLastName = null;
	String m_szAddress = null;
	String m_szCustomerId = null;
	BigDecimal m_dPayAmount = null;
	Calendar m_calPaymentDate = null;

	PmtAcct m_clPaymentAccount;
	
	/**
	 * Reads a scheduled payment file and creates a list of OneTimeScheduledPayment objects
	 * 
	 * @param czFilePath
	 * @return null if it failed, a List of IScheduledPayment interface objects if it succeeds
	 */
	static public List<IScheduledPayment> createScheduledPaymentList(String czFilePath, TokenMap mTokenMap) {
		final Integer BillingAccountNumber = 0;
		final Integer LastName = 1;
		final Integer Address = 2;
		final Integer PaymentAmount = 3;
		final Integer PaymentDate = 4;
		final Integer PaymentMethod = 5;
		final Integer PaymentAccountId = 6;
		final Integer Errors = 7;
		final Integer NoErrorsLength = 7;
		final Integer ErrorsLength = 8;

		List<IScheduledPayment> lSchedPaymentList = new ArrayList<IScheduledPayment>();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(czFilePath)) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- Failed to open file: ().", czFilePath);
			return null;
		}
		
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentList -- processing one-time scheduled payment records.");
		Integer iLoopCount = 0;
		Integer iSkipped = 0;
		
		//-- skip first line, just the labels.
		record = input.getSplitLine();
		if (null == record) {
			LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- empty file: ().", czFilePath);
			return null;
		}
		
		do {
			record = input.getSplitLine();
			iLoopCount++;
			if ((null != record) && (record.length == NoErrorsLength)) {
				String lszCustomerId = null;
				String lszInternalAcct = null;
				BigDecimal ldPayAmt = null;
				PmtAcct loPmtAcct = null;
				Calendar lcPayDate = null;
				
				// -- convert pay amount --
				try {
					ldPayAmt = new BigDecimal(record[PaymentAmount]);
				} catch (NumberFormatException e) {
					LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- Number format exception for account: {}, pay amount: {}. Skipping payment.",
							record[BillingAccountNumber], record[PaymentAmount]);
					iSkipped++;
					continue;
				}
				
				// -- convert date --
				lcPayDate = convertPaymentDate(record[PaymentDate]);
				if (null == lcPayDate) {
					LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- Parse exception for account: {}, date: {}. Skipping payment.",
							record[BillingAccountNumber], record[PaymentDate]);
					iSkipped++;
					continue;
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
					Boolean lbSuccess = loPmtAcct.createPayAcct(record[PaymentMethod], 
											record[PaymentAccountId],
											mTokenMap,
											record[BillingAccountNumber]);
					if (!lbSuccess) {
						LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- Account {}, failure in mapping payment method: {}, account: {}",
								record[BillingAccountNumber], record[PaymentMethod], record[PaymentAccountId]);
						iSkipped++;
						continue;
					}
				}
				
				// -- create the payment object  and add to list --
				IScheduledPayment iPayment = new OneTimeScheduledPayment (
						lszCustomerId,
						lszInternalAcct,
						record[BillingAccountNumber],
						record[LastName],
						record[Address],
						ldPayAmt,
						lcPayDate,
						loPmtAcct			) ;
				lSchedPaymentList.add(iPayment); 
				LOG.debug("neTimeScheduledPayment:createScheduledPaymentList -- created sheduled" + 
						" payment record for acccount: {}.", 
						record[BillingAccountNumber]);
				
				if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
					System.out.print(".");
				}
			} else {
				if ((null != record) && (record.length == ErrorsLength)) {
					LOG.info("neTimeScheduledPayment:createScheduledPaymentList -- Error in scheduled" + 
								" payment record for acccount: {}, error value: {}.", 
								record[BillingAccountNumber], record[Errors]);
					iSkipped++;
				}
			}
			
		} while (record != null);
		System.out.println();
		LOG.info("OneTimeScheduledPayment:createScheduledPaymentList -- " + 
		"finished processing file: {}, {} payment records processed, {} payment objects created, {} records skipped.", 
				czFilePath, iLoopCount-1, lSchedPaymentList.size(), iSkipped);
		return lSchedPaymentList;
	}
	
	/**
	 * Converts payment date fed in as MM/dd/yyyy into a Calendar object
	 * 
	 * @param cszPayDate
	 * @param cszAcctNum
	 * @return
	 */
	private static Calendar convertPaymentDate(final String cszPayDate) {
		Calendar lcPayDate = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date date = sdf.parse(cszPayDate);
			lcPayDate = Calendar.getInstance();
			lcPayDate.setTime(date);
		} catch (ParseException e) {
			lcPayDate = null;
		}
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
			final Calendar ccalPaymentDate,
			final PmtAcct clPayAcct			) {
		m_szInternalAccount = cszInternalAccount;
		m_szExternalAccount = cszExternalAccount;
		m_szLastName = cszLastName;
		m_szAddress = cszAddress;
		m_szCustomerId = cszCustomerId;
		m_dPayAmount = cdPayAmount;
		m_calPaymentDate = ccalPaymentDate;
		m_clPaymentAccount = clPayAcct;
	}
	
	
	@Override
	public WebSvcReturnCode createScheduledPayment() {
		// TODO Auto-generated method stub
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
	public BigDecimal getPayAmount() {
		return m_dPayAmount;
	}

	@Override
	public Calendar getPayDate() {
		return m_calPaymentDate;
	}
	

	@Override
	public String getInfoAsString() {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String date = formatter.format(m_calPaymentDate.getTime());            
		String lszInfoReturned = 
				"OneTimeScheduledPayment -- CustomerId: " +
				m_szCustomerId + ", External account: " +
				m_szExternalAccount + ", Internal account: " +
				m_szInternalAccount + ", Payment date: " + 
				date + " Payment amount: " + m_dPayAmount +
				", paymentType: " + m_clPaymentAccount.m_payType.toString();
				
		return lszInfoReturned;
	}

}
