package com.sorrisotech.svcs.accountstatus.dao;

import java.util.List;

/**
 * Interface for getting tm_accounts list for an orgId
 * 
 * @author john kowalonek
 * @since   2024-Mar-23
 * @version 2024-Mar-23 jak first version
 */
public interface ITmAccountsFromOrgIdDao {

	/**
	 * Returns a list of accounts associated with the specified OrgId from tm_accounts table that
	 * are not under the cszStatusPayGroup. Used during registration to add tm_accounts record
	 * for accounts in the status group that don't yet have a tm_accounts record in their bill_group.
	 * 
	 * @param cszOrgId
	 * @param cszStatusPayGroup
	 * @return
	 */
	public List<String> getTmAccountsList (final String cszOrgId, final String cszStatusPayGroup);
}
