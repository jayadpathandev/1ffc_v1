package com.sorrisotech.uc.admin1ffc.dao;

/**
 * Class for result element returned from 1st Franklin 
 * search by customerId.
 * 
 * @author john kowalonek
 * @version 2024-jan-16 -- initial version
 * @since 2024-jan-16
 * 
 */
public class CustomerElement1FFC {

	String m_szInternalUserId = null;
	String m_szUserName = null;
	String m_szFullName = null;
	String m_szEmailAddress = null;
	String m_szCustomerId = null;
			
	/**
	 * returns the internal user identifier
	 * 
	 * @return
	 */
	public String getId() { return m_szInternalUserId; }
	
	/**
	 * sets the internal user identifier
	 * 
	 * @param cszUserId
	 */
	public void setId(final String cszUserId) { m_szInternalUserId = cszUserId; }
	
	/**
	 * gets the readable userName for the user
	 * 
	 * @return
	 */
	public String getUserName() { return m_szUserName; }
	
	/**
	 * sets the readable userName for the user
	 * 
	 * @param cszUserName
	 */
	public void setUserName(final String cszUserName) { m_szUserName = cszUserName; }
	
	/**
	 * gets the user's full name (first<sp>last)
	 * 
	 * @return
	 */
	public String getFullName() { return m_szFullName; }
	
	/**
	 * sets the user's full name (first<sp>last)
	 * 
	 * @param cszFullName
	 */
	public void setFullName(final String cszFullName) { m_szFullName = cszFullName; }
	
	/**
	 * returns the user's email address
	 * 
	 * @return
	 */
	public String getEmail() { return m_szEmailAddress; }
	
	/**
	 * sets the user's email address
	 * 
	 * @param cszEmailAddress
	 */
	public void setEmail(final String cszEmailAddress) { m_szEmailAddress = cszEmailAddress; }

	/**
	 * gets the 1st Franklin customer identifier associated with this user
	 * 
	 * @return
	 */
	public String getCustomerId() { return m_szCustomerId; }
	
	/** 
	 * sets the 1st Franklin customer identifier associated with this user
	 * 
	 * @param cszCustomerId
	 */
	public void setCustomerId(final String cszCustomerId) { m_szCustomerId = cszCustomerId; }
}
