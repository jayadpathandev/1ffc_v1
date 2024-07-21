/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that encapsulates a token map and validates that the token is assigned to the 
 * correct account during a get operation
 * 
 * @author johnk
 * @since	2024-Jul-19
 * @version 2024-Jul-19 jak first version
 * 
 */
public class TokenMap {

	private static final Logger LOG = LoggerFactory.getLogger(TokenMap.class);

	private HashMap<String, IExternalToken> m_TokenMap = new HashMap<String, IExternalToken>(); 
	private HashMap<String, IExternalToken> m_AcctMap = new HashMap<String, IExternalToken>();
	
	/**
	 * Adds a token to the token map
	 * 
	 * @param cszIndex
	 * @param ciToken
	 */
	public void put(final IExternalToken ciToken) {
		m_TokenMap.put(ciToken.getToken(), ciToken);
		m_AcctMap.put(ciToken.getAccountId(), ciToken);
	}

	/** 
	 * Retrieves a token from the map for a token/account pair.  Returns null
	 * if the token is not assigned to the account, or if the token cannot be 
	 * found.
	 * 
	 * @param cszTokenId
	 * @param cszExternalAcct
	 * @return
	 */
	public IExternalToken getByToken(final String cszTokenId, final String cszExternalAcct) {
		IExternalToken lRet = null;
		
		IExternalToken lToken = m_TokenMap.get(cszTokenId);
		if (null != lToken) {
			if (lToken.getAccountId().equals(cszExternalAcct)) {
				lRet = lToken;
			} else {
				LOG.info("TokenMap.getByToken -- found token {} but not linked to account {}. Instead linked to {}.",
						cszTokenId, cszExternalAcct, lToken.getAccountId());
			}
		} else {
			LOG.info("TokenMap.getByToken -- could not find token {} for account {}. ",
						cszTokenId, cszExternalAcct);
		}
		return lRet;
	}
	
	/** 
	 * Gets a token using account number, last 4 digits of debit card, and expiration data as a check.
	 * 
	 * @param cszBillingAcct
	 * @param cszLast4Digits
	 * @return
	 */
	public IExternalToken getByAccount(final String cszBillingAcct, 
									   final String cszLast4Digits) {
		IExternalToken lRet = null;
				
		IExternalToken lToken = m_AcctMap.get(cszBillingAcct);
		if (null != lToken) {
			if (lToken.getLast4().equals(cszLast4Digits)) {
				lRet = lToken;
			} else {
				LOG.info("TokenMap.getByAccount -- found token for account: {}, but mismatch in last 4 found: {} vs. call: {}." +
						cszBillingAcct, lToken.getLast4(), cszLast4Digits);
			}
		} else {
			LOG.info("TokenMap.getByAccount -- could not find token for account {}. ",
						 cszBillingAcct);
		}
		
		return lRet;
	}
	
	/**
	 * Retuns size of this map
	 * 
	 * @return
	 */
	public Integer size() {
		return m_TokenMap.size();
	}
}
