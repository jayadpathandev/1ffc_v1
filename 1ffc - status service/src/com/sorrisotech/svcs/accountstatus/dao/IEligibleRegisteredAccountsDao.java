package com.sorrisotech.svcs.accountstatus.dao;

import java.util.List;

/**
 * Provides list of accounts registered to a user that are eligible for online access
 * 
 * @author john kowalonek
 * @since 2024-Mar-24
 * @version 2024-Mar-24 jak First version
 */
public interface IEligibleRegisteredAccountsDao {

		/**
		 * Returns list of registered accounts for this user that are eligible for online access
		 * 
		 * @param cszUserId
		 * @param cszStatusPayGroup
		 * @return
		 */
		List<EligibleRegisteredAcctElement> getEligibleRegisteredAccts (
																				final String cszUserId,
																				final String cszStatusPayGroup);
}
