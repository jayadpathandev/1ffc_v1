package com.sorrisotech.svcs.accountstatus.dao;

import java.math.BigDecimal;

/**
 * Holder for information about an account
 * 
 * @author john kowalonek
 * @since	2024-Mar-23
 * @version 2024-Mar-23 jak first version
 */
public class AccountForRegistrationElement {

		public String m_szInternalAccountId;
		public String m_szOrgId;
		public String m_szDisplayAccountId;
		public String m_szAccountName;
		public int m_iBillerId;
		public BigDecimal m_dStartDate;
		public Boolean m_bIsEligibleForPortal;
}
