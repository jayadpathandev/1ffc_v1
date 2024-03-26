package com.sorrisotech.svcs.accountstatus.dao;

import java.util.List;

/**
 * Interface for retrieving the list of eligible accounts
 * 
 * @author john kowalonek
 * @since  2024-Mar-22
 * @version 2024-Mar-22	jak	1st version
 */
public interface IAccountsForRegistrationDao {

	/**
	 * Returns list of eligibleAccounts given the payment group for bills and the account number
	 * returned when registering. Information is gathered from the status feed. I looks up the org_id
	 * for the specified account number, and then uses that org id to find all accounts in the status
	 * feed that match that orgId and are eligible for online enrollment.
	 * 
	 * @param cszPmtGroup -- pmt group for bills will get turned into status pmt group
	 * @param cszBaseAcct -- one account number from the organization that we want the account list
	 * @return
	 */
	public List<AccountForRegistrationElement> getAccountsForRegistration(final String cszPmtGroup, final String cszBaseAcct );
}
