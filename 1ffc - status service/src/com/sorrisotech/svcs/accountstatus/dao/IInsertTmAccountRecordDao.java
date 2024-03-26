package com.sorrisotech.svcs.accountstatus.dao;

/**
 * Inserts a TmAccountRecord
 * 
 * @author john kowalonek 2024-Mar-24
 * @since 2024-Mar-24
 * @version 2024-Mar-24 jak First Version
 */
public interface IInsertTmAccountRecordDao {

	/**
	 * Inserts a new tm_account record using the base data provided in the element.
	 * The Dao assigns a new sequence number automatically so that's not needed 
	 * here.
	 * 
	 * @param oElement
	 * @param cszPayGroup
	 * @return
	 */
	public boolean InsertTmAccountRecord(AccountForRegistrationElement oElement, String cszPayGroup);
}
