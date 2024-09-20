package com.sorrisotech.fffc.batch.reimport.account;

import java.math.BigDecimal;

/******************************************************************************
 * This object holds the data read from the TM_ACCOUNT table.
 */
public class TmRecord {
	/**************************************************************************
	 * The ID column the TM_ACCOUNT table.
	 */
	public BigDecimal id;

	/**************************************************************************
	 * The ACCOUNT_NUMBER column the TM_ACCOUNT table.
	 */
	public String accountId;

	/**************************************************************************
	 * The START_DATE column the TM_ACCOUNT table.
	 */
	public BigDecimal startDate;
	
	/**************************************************************************
	 * The COMPANY_ID column the TM_ACCOUNT table.
	 */
	public BigDecimal companyId;

	/**************************************************************************
	 * The BILL_GROUP column from the TM_ACCOUNT table.
	 */
	public String billGroup;

	/**************************************************************************
	 * The ORG_ID column the TM_ACCOUNT table.
	 */
	public String orgId;
	
}
