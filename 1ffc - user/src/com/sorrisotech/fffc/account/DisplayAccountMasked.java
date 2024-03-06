/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.utils.UserProfile;
import com.sorrisotech.fffc.account.dao.AccountDao;
import com.sorrisotech.svcs.external.IServiceLocator2;

/******************************************************************************
 * This class have methods that are masking the display account number.
 * 
 * @author Asrar Saloda
 */
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
	 * This method returns the masked display account number.
	 * 
	 * @param szInternalAccountNumber Internal account number.
	 * 
	 * @return masked display account number.
	 */
	public String getMaskedDisplayAccount(final String szInternalAccountNumber) {
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...internalAccountNumber: {}"
		        + szInternalAccountNumber);
		
		var szMaskedDisplayAccount = "";
		
		// --------------------------------------------------------------------------------------
		// Fetching display account number from database using internal account number.
		var displayAccountNumber = m_cDao.queryDisplayAccountNumber(szInternalAccountNumber);
		
		if (null != displayAccountNumber)
			szMaskedDisplayAccount = maskDisplayAccount(displayAccountNumber);
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...displayAccount: {}"
		        + szMaskedDisplayAccount);
		
		return szMaskedDisplayAccount;
	}
	
	/**************************************************************************
	 * This method returns the masked display account number.
	 * 
	 * @param szDisplayAccount display account number.
	 * 
	 * @return masked display account number.(*** followed by last 4 digits)
	 */
	private static String maskDisplayAccount(final String szDisplayAccount) {
		
		var maskedDigits = new StringBuilder("***");
		
		var lastFourDigits = szDisplayAccount.substring(Math.max(0, szDisplayAccount.length() - 4));
		
		return maskedDigits.append(lastFourDigits).toString();
	}
	
	/**************************************************************************
	 * This lookup method which returns masked display account number(e.g. ***1234)
	 * or if nick name available it will return nickname space 3 stars followed by
	 * last 4 digits of display account number(e.g. TestBoat ***1234)
	 * 
	 * @param sUserId   The user id.
	 * @param sAccount  The internal account number.
	 * @param sPayGroup Pay group.
	 * 
	 * @return Masked display account number.(*** followed by last 4 digits) or
	 *         nickname space 3 star followed by last 4 digits of display account
	 *         number(e.g. TestNls ***1234)
	 */
	public String displayAccountLookup(
	        IServiceLocator2 cLocator,
	        final String sUserId,
	        final String sAccount,
	        final String sPayGroup) {
		
		String szReturnValue = null;
		
		if (!sAccount.isBlank() && !sPayGroup.isBlank()) {
			
			// --------------------------------------------------------------------------------------
			// Fetching display account number using internal account number.
			final String szMaskedDisplayAccount = getMaskedDisplayAccount(sAccount);
			
			// --------------------------------------------------------------------------------------
			// Creating nick name attribute name as per requirement.
			// Nickname-<paymentGroup>-<internalaccountnumber>
			final String sNicknameAttrName = "Nickname" + "-" + sPayGroup + "-" + sAccount;
			
			String szNicknameValue = null;
			
			if (null != cLocator) {
				// --------------------------------------------------------------------------------------
				// Fetching nickname from user profile.
				szNicknameValue = UserProfile.getProfileAttrValue(cLocator, sUserId,
				        sNicknameAttrName);
			} else {
				szNicknameValue = m_cDao.queryNickname(sUserId, sNicknameAttrName);
			}
			
			if (null != szNicknameValue && !szNicknameValue.isBlank()) {
				szReturnValue = szNicknameValue + " " + szMaskedDisplayAccount;
			} else {
				szReturnValue = szMaskedDisplayAccount;
			}
		}
		
		return szReturnValue;
	}
	
}
