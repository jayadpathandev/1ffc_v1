package com.sorrisotech.fffc.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.utils.Session;
import com.sorrisotech.fffc.account.DisplayAccountMasked;
import com.sorrisotech.svcs.itfc.data.IUserData;

public class FFFCSession {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FFFCSession.class);
	
	public String[][] getAllDisplayAccountMasked(final IUserData cData) {
		
		LOG.debug("FFFCSession.....getAllDisplayAccountMasked()...started");
		
		String[][] maskedAccountArray = null;
		
		try {
			final Session cSession = cData.getJavaObj(Session.class);
			
			final String[][] cDisplayAccountsArray = cSession.getAccounts();
			
			maskedAccountArray = new String[cDisplayAccountsArray.length][2];
	        
	        for (int i = 0; i < cDisplayAccountsArray.length; i++) {
	        	maskedAccountArray[i][0] = cDisplayAccountsArray[i][0];
	        	maskedAccountArray[i][1] =  DisplayAccountMasked.maskDisplayAccount(cDisplayAccountsArray[i][1]);
	        }
			
		} catch (Exception e) {
			LOG.error("FFFCSession.....getAllDisplayAccountMasked()...An exception was thrown", e);
			return maskedAccountArray;
		}
		
		LOG.debug("FFFCSession.....getAllDisplayAccountMasked()...masked account number: {}"
		        + maskedAccountArray);
		return maskedAccountArray;
	}
}
