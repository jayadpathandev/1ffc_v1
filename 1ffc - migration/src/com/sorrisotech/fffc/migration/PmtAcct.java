/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

	
	public enum PayType {bank, debit, credit, sepa};
	public PayType m_payType = null;
	public String m_szTokenId = null;
	public String m_szBankRouting = null;
	public String m_szBankAcct = null;
	public String m_szAcctHolder = null;
	public String m_szLast4 = null;
	public String m_szMaskedName = null;
	public String m_szExpiration = null;
	
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
								  		  final String BillingAcctId,
								  		  final String cszCardHolderName)
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
			IExternalToken token = cTokenMap.getByToken(PayAcctId, BillingAcctId);
			// -- is it a valid ported debit card --
			if (null == token) {
				LOG.error("PmtAcct:createPayAcctByToken -- ported debit card lookup failed on card token for account: {}, debit acct: {},Skipping payment.",
						BillingAcctId, PayAcctId);
				break;
				
			} 
			m_szAcctHolder = cszCardHolderName; // -- aci token file doesn't give it to us so we pick it up here.
			m_szLast4 = token.getLast4();
			m_szTokenId= token.getToken();
			m_szMaskedName = token.getMaskedName();
			m_szExpiration = token.getExpirationDate();
			
			// -- check for expired accounts --
			if (isExpired(m_szExpiration)) {
				LOG.error("PmtAcct:createPayAcctByToken -- ported debit card is expired. for account: {}, debit acct: {},Skipping payment.",
						BillingAcctId, PayAcctId);
				break;
			}
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
	 * Check expiration date on card
	 * 
	 * @param expirationDate
	 * @return
	 */
	private Boolean isExpired(final String expirationDate) {

		Boolean expired = true;
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Date date = null;
		try {
			date = sdf.parse(expirationDate);
		} catch (ParseException e) {
			LOG.error("PmtAcct:isExpired -- Can't parse date {}.", expirationDate);
			return expired;
		}
		Calendar expDate = Calendar.getInstance();
		expDate.setTime(date);
		Calendar curDate = Calendar.getInstance();
		
		expired = false;
		if (expDate.get(Calendar.YEAR) < curDate.get(Calendar.YEAR)) {
			expired = true;
		} else if (((expDate.get(Calendar.YEAR)) == curDate.get(Calendar.YEAR))
				&& ((expDate.get(Calendar.MONTH)) < curDate.get(Calendar.MONTH))) {
			expired = true;
		}
		
		return expired;
		
	}
}
