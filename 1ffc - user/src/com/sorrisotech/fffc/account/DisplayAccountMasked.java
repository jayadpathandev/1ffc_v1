package com.sorrisotech.fffc.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.account.dao.AccountDao;

public class DisplayAccountMasked {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DisplayAccountMasked.class);
	
	/**********************************************************************************************
	 * Dao instance for accessing the database.
	 */
	private static final AccountDao m_cDao = AccountDao.get();
	
	/**************************************************************************
	 * This method returns the masked email address.
	 * 
	 * @param szInternalAccountNumber Internal account number.
	 * 
	 * @return masked display account number or nickname.
	 */
	public String getMaskedDisplayAccount(final String szInternalAccountNumber) {
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...internalAccountNumber: {}"
		        + szInternalAccountNumber);
		
		var szMaskedDisplayAccount = "";
		
		var displayAccountNumber = m_cDao.queryDisplayAccountNumber(szInternalAccountNumber);
		
		if (null != displayAccountNumber)
			szMaskedDisplayAccount = maskDisplayAccount(displayAccountNumber);
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...displayAccount: {}"
		        + szMaskedDisplayAccount);
		
		return szMaskedDisplayAccount;
	}
	
	private static String maskDisplayAccount(final String displayAccount) {
		
		var maskedDigits = new StringBuilder("X".repeat(Math.max(0, displayAccount.length() - 4)));
		
		var lastFourDigits = displayAccount.substring(Math.max(0, displayAccount.length() - 4));
		
		return maskedDigits.append(lastFourDigits).toString();
	}
	
}
