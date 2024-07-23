package com.sorrisotech.svcs.accountstatus.dao;


/**
 * Interface for retrieving the list of eligible accounts
 * 
 * @author john kowalonek
 * @since  2024-Jul-23
 * @version 2024-Jul-23	jak	1st version
 */
public interface IAcctFromExtAcctDao {

	/**
	 * Returns internal account information given the external "display" account name and the payment group
	 * 
	 * @param cszPmtGroup -- pmt group for bills will get turned into status pmt group
	 * @param cszExtAcct --  the account number for which we want the customerId and the internal acct number
	 * @return
	 */
	public AcctFromExtAcctElement getAcctInfoFromExtAccount(final String cszPmtGroup, final String cszExtAcct );
}
