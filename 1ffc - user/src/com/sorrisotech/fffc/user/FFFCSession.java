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
package com.sorrisotech.fffc.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.utils.OneAccount;
import com.sorrisotech.app.utils.Session;
import com.sorrisotech.fffc.account.DisplayAccountMasked;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;

public class FFFCSession implements IExternalReuse {
	
	/***************************************************************************
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1967420762454610497L;
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FFFCSession.class);
	
	/*************************************************************************************************
	 * This method returns the list of display account masked or nickname to be able
	 * to populate to the drop down list. The first string being the offset and the
	 * second being the account number at that offset.
	 * 
	 */
	public String[][] getAllDisplayAccountMasked(final IServiceLocator2 locator,
	        final IUserData cData) {
		
		LOG.debug("FFFCSession.....getAllDisplayAccountMasked()...started");
		
		String[][] cAccountsArray = null;
		
		try {
			final Session cSession = cData.getJavaObj(Session.class);
			
			final String szUserId = cSession.getUserId();
			
			final OneAccount[] m_aAccounts = cSession.getAccountsArray();
			
			cAccountsArray = new String[m_aAccounts.length][2];
			
			for (int i = 0; i < m_aAccounts.length; i++) {
				cAccountsArray[i][0] = Integer.toString(i);
				cAccountsArray[i][1] = new DisplayAccountMasked().displayAccountLookup(locator,
				        szUserId, m_aAccounts[i].account(), m_aAccounts[i].payGroup());
			}
			
		} catch (Exception e) {
			LOG.error("FFFCSession.....getAllDisplayAccountMasked()...An exception was thrown", e);
			return cAccountsArray;
		}
		
		LOG.debug("FFFCSession.....getAllDisplayAccountMasked()...masked account number: {}"
		        + cAccountsArray);
		return cAccountsArray;
	}
	
	/**********************************************************************************************
	 * Tie the class to a user's session.
	 *
	 * @return Always returns IExternalReuse.REUSE_SESSION_SINGLETON.
	 */
	@Override
	public int getReuse() {
		return (IExternalReuse.REUSE_SESSION_SINGLETON);
	}
}
