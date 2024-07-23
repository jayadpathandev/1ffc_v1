/**
 * 
 */
package com.sorrisotech.fffc.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 	used for setting payment account type in scheduled and recurring payments.
 * 
 * @author johnk
 * @since  2024-Jul-19
 * @version 20240-Jul-19 jak first version.
 * 
 */
public class PmtAcct {

	private static final Logger LOG = LoggerFactory.getLogger(PmtAcct.class);

	
	public enum PayType {bank, debit, credit};
	public PayType m_payType = null;
	public String m_szTokenId = null;
	public String m_szBankRouting = null;
	public  String m_szBankAcct = null;
	
	/** 
	 * Creates the payment account record details using a tokenId
	 * 	for lookup
	 * 
	 * @param PayMethod
	 * @param PayAcctId
	 * @param cTokenMap
	 * @param BillingAcctId
	 * @return
	 */
	public Boolean createPayAcctByToken ( final String PayMethod, 
								  		  final String PayAcctId, 
								  		  final TokenMap cTokenMap, 
								  		  final String BillingAcctId)
	{
		Boolean lbRet = false;
		
		switch (PayMethod) {
		
		case "Debit": 
			m_payType = PayType.debit;
			// -- is there a value? ==
			if (PayAcctId == null || PayAcctId.isBlank() || PayAcctId.isEmpty()) {
				LOG.error("PmtAcct:createPayAcct -- invalid debit card token for account: {}, debit acct: {}. Skipping payment.",
						BillingAcctId, PayAcctId);
				break;
				
			}
			// -- is it a valid ported debit card --
			if (null == cTokenMap.getByToken(PayAcctId, BillingAcctId)) {
				LOG.error("OneTimeScheduledPayment:createScheduledPaymentList -- ported debit card lookup failed on card token for account: {}, debit acct: {},Skipping payment.",
						BillingAcctId, PayAcctId);
				break;
				
			} 
			
			m_szTokenId = PayAcctId;
			lbRet = true;
			break;
		
		case "Bank":
			m_payType = PayType.bank;
			// -- split routing and account --
			{
				String[] lBankAcct = PayAcctId.split("\\Q|\\E", 0);
				if ((lBankAcct == null) || (lBankAcct.length !=2)) {
					LOG.error("PmtAcct:createPayAcct -- invalid bank acct for account: {}, bank acct: {}. Skipping payment.",
							BillingAcctId, PayAcctId);
					break;
				}
				m_szBankRouting = lBankAcct[0];
				m_szBankAcct = lBankAcct[1];
				lbRet = true;
			}
			break;
		default:
			LOG.error("PmtAcct:createPayAcct -- Invalid payment method for account: {}, pay method: {}. Skipping payment.",
					BillingAcctId, PayMethod);
			break;
		}
		return lbRet;
	}
	
	/**
	 * creates payment account details by using the billing account for lookup
	 * 	of the token
	 * 
	 * @param PayMethod
	 * @param cszBillingAccount
	 * @param cTokenMap
	 * @param cszLast4Digits
	 * @return
	 */
	public Boolean  createPayAcctFromBillAccount(final String PayMethod,
												 final String cszBillingAccount, 
												 final TokenMap cTokenMap,
												 final String cszLast4Digits) {
		Boolean lbRet = false;
		switch (PayMethod) {
		
		case "Debit": 
			m_payType = PayType.debit;
			// -- is there a value? ==
			if (cszBillingAccount == null || cszBillingAccount.isBlank() || cszBillingAccount.isEmpty()) {
				LOG.error("PmtAcct:createPayAcctFromBillAccount -- invalid billing account: {}. Skipping payment.",
						cszBillingAccount);
				break;
				
			}
			// -- is it a valid ported debit card --
			IExternalToken lToken = cTokenMap.getByAccount(cszBillingAccount, cszLast4Digits);
			if (null == lToken) {
				LOG.error("PmtAcct:createPayAcctFromBillAccount -- ported debit card lookup failed on  account: {}, last 4 digits: {},Skipping payment.",
						cszBillingAccount, cszLast4Digits);
				break;				
			} 
			m_szTokenId = lToken.getToken();
			lbRet = true;
			break;
		
		case "Bank":
			// -- we don't have bank accounts in this file --
	/*		m_payType = PayType.bank;
			// -- split routing and account --
			{
				String[] lBankAcct = PayAcctId.split("\\Q|\\E", 0);
				if ((lBankAcct == null) || (lBankAcct.length !=2)) {
					LOG.error("PmtAcct:createPayAcctFromBillAccount -- invalid bank acct for account: {}, bank acct: {}. Skipping payment.",
							BillingAcctId, PayAcctId);
					break;
				}
				m_szBankRouting = lBankAcct[0];
				m_szBankAcct = lBankAcct[1];
				lbRet = true;
			}
			break; */
		default:
			LOG.error("PmtAcct:createPayAcct -- Invalid payment method for account: {}, pay method: {}. Skipping payment.",
					cszBillingAccount, PayMethod);
			break;
		}
		return lbRet;
	}
}
