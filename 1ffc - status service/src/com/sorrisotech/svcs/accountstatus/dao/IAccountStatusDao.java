package com.sorrisotech.svcs.accountstatus.dao;

import java.util.List;

/**
 * Returns list of AccountStatusElements associated with a user
 * 
 * @author John A. Kowalonek
 * @since 09-Oct-2023
 * @version 09-Oct-2023
 */
public interface IAccountStatusDao {

	/**
	 * Returns list of AccountStatusElements for the specified user identifier
	 * 
	 * @param cszUserId -- internal user identifier
	 * @return
	 */
	public List<AccountStatusElement>	getAccountElementsForUser(final String cszUserId);
	
	
	
}
