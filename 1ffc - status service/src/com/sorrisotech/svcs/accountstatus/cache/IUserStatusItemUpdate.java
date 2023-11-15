/**
 * 
 */
package com.sorrisotech.svcs.accountstatus.cache;

/**
 * 	Interface used only by the cache that will set
 *  the contents of the result set for a user and
 *  returns the number of accounts associated with
 *  that user. Also used to determine if the item
 *  has expired.... i.e. hasn't 
 *  been used in more than a configured number of
 *  seconds.
 *  
 * 
 */
public interface IUserStatusItemUpdate {

	/**
	 * This is the worker that actually sets the contents of the UserStatusItem.
	 * It is called from the cache itself. This method reaches down to the DAO 
	 * and retrieves the data for the internal HashMap.
	 * 
	 * @param userId		user identifier for the user for which we are requesting this
	 * @return int 			number of accounts that match this object
	 * @throws 				AccountStatusException
	 */
	abstract Integer setContents(String userId) throws AccountStatusException;
	
	/**
	 * Returns true if item has not expired.
	 * 
	 * @param clTimeoutMilliseconds -- timeout value in milliseconds
	 * @return boolean
	 */
	abstract Boolean isBeingUsed (long clTimeoutMilliseconds);
}
