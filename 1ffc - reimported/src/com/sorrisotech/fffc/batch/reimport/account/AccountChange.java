package com.sorrisotech.fffc.batch.reimport.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/******************************************************************************
 * This object contains the details of an Account ID change.
 */
public class AccountChange {

	/**************************************************************************
	 * The account number we are updating.
	 */
	private final String mAccountNum;
	
	/**************************************************************************
	 * The new Account ID for the loan.
	 */
	private String mNewAccountId = null;

	/**************************************************************************
	 * The new ORG_ID, this may be the same as the old ORG_ID.
	 */
	private String mNewOrgId = null;

	/**************************************************************************
	 * The ID of the TM_ACCOUNT row that references a bill with the new AccountId.
	 */
	private BigDecimal mNewBillRecId = null;
	
	/**************************************************************************
	 * The start date that needs to be used in PROF_COMPANY_ACCOUNT.
	 */
	private BigDecimal mStartDate = null;
	
	/**************************************************************************
	 * The ID of the company. This may be null if the account is not assigned
	 * to a company.
	 */
	private BigDecimal mCompanyId = null;
		
	/**************************************************************************
	 * The old Account ID for the loan.
	 */
	private HashSet<String> mOldAccountIds = new HashSet<String>();

	/**************************************************************************
	 * The old ORG_IDs if the ORG_ID changed.
	 */
	private HashSet<String> mOldOrgIds = new HashSet<String>();
	
	/**************************************************************************
	 * The list of IDs from TM_ACCOUNT that are no longer needed.
	 */
	private ArrayList<BigDecimal> mOldTmRecs = new ArrayList<BigDecimal>();

	/**************************************************************************
	 * The ID of the TM_ACCOUNT row that references a bill with the old AccountId.
	 */
	private BigDecimal mOldBillRecId = null;
	
	/**************************************************************************
	 * Create a new instance of the object.
	 * 
	 * @param accountNum  The account number we are changing.
	 */
	public AccountChange(
			final String accountNum
			) {
		mAccountNum = accountNum;
	}
			
	/**************************************************************************
	 * Parse a record from TM_ACCOUNT.  It is expected that the first record
	 * passed in contains the new account ID/OrgId.
	 */
	public String parseRecord(
			final boolean  bill,
			final TmRecord record
			) {
		//---------------------------------------------------------------------
		// The first record should contain the new accountID and OrgId.
		if (mNewAccountId == null) {
			mNewAccountId = record.accountId;
			mNewOrgId     = record.orgId;
		}
		
		//---------------------------------------------------------------------
		// Extract the important data from the record.
		if (mNewAccountId.equals(record.accountId)) {
			// The record is the new account ID.
			if (record.companyId != null && mCompanyId == null) {
				mCompanyId = record.companyId;
			} else if (record.companyId != null) {
				if (mCompanyId.equals(record.companyId)) {
					return "Two (or more) records in TM_ACCOUNT have the new " +
							"ACCOUNT_NUMBER and are both assigned to the " +
							"same company.";
				} else {
					return "Two (or more) records in TM_ACCOUNT have the new " +
				           "ACCOUNT_NUMBER and both are assigned assigned " +
							"to different companies.";
				}
			}
			if (bill == true) {
				if (mNewBillRecId == null) {
					mNewBillRecId = record.id;
				} else {
					return "Two (or more) records in TM_ACCOUNT have the new " + 
						   "ACCOUNT_NUMBER and the same BILL_GROUP.";
				}				
			}
		} else {
			// The record is an old account ID.
			mOldAccountIds.add(record.accountId);
			
			if (mNewOrgId.equals(record.orgId) == false) {
				mOldOrgIds.add(record.orgId);	
			}
			
			if (record.companyId != null) {
				if (mCompanyId == null) {
					mCompanyId = record.companyId;
				} else if (mCompanyId.equals(record.companyId) == false) {
					return "Account is registered to at least 2 different companies.";					
				}
			}
			if (bill) {
				if (mStartDate == null) {
					mOldBillRecId = record.id;
					mStartDate    = record.startDate;
				} else if (record.startDate.compareTo(mStartDate) < 0) {
					mOldBillRecId = record.id;
					mStartDate    = record.startDate;
				}
			}
						
			mOldTmRecs.add(record.id);
		}
		
		return null;
	}

	
	/**************************************************************************
	 * Return the account number.
	 * 
	 * @return The account number
	 */
	public String getAccountNum() {
		return mAccountNum;
	}

	/**************************************************************************
	 * Simple test to check if the ORG_ID changed.
	 * 
	 * @return  True if the ORG_ID changed.
	 */
	public boolean orgIdChanged() {
		return mOldOrgIds.isEmpty() == false;
	}
	
	/**************************************************************************
	 * Return the new account id.
	 * 
	 * @return The new account id.
	 */
	public String getNewAccountId() {
		return mNewAccountId;
	}
	
	/**************************************************************************
	 * Return the new OrgId.
	 * 
	 * @return The new OrgId.
	 */
	public String getNewOrgId() {
		return mNewOrgId;
	}
	
	/**************************************************************************
	 * Return the ID of the new record in TM_ACCOUNT that represents the bills.
	 * 
	 * @return An ID from TM_ACCOUNT.
	 */
	public BigDecimal getNewBillRecId() {
		return mNewBillRecId;
	}
		
	/**************************************************************************
	 * Return the company id if the account was registered.
	 * 
	 * @return The company id if the account was registered, otherwise null.
	 */
	public BigDecimal getCompanyId() {
		return mCompanyId;
	}

	/**************************************************************************
	 * Return the ID of the old record in TM_ACCOUNT that represents the bills.
	 * 
	 * @return An ID from TM_ACCOUNT.
	 */
	public BigDecimal getOldBillRecId() {
		return mOldBillRecId;
	}
	
	/**************************************************************************
	 * Return the old account id(s).
	 * 
	 * @return The old account id(s).
	 */
	public Collection<String> getOldAccountIds() {
		return mOldAccountIds;
	}
	
	/**************************************************************************
	 * Return the old OrgId(s).
	 * 
	 * @return The old OrgId(s).
	 */
	public Collection<String> getOldOrgIds() {
		return mOldOrgIds;
	}

	/**************************************************************************
	 * Return the old row IDs from TM_ACCOUNT.
	 * 
	 * @return The old IDs from TM_ACCOUNT.
	 */
	public Collection<BigDecimal> getTmRecsToDelete() {
		final var retval = new HashSet<BigDecimal>(mOldTmRecs);
		
		if (mOldBillRecId != null) {
			retval.remove(mOldBillRecId);
			
			if (mNewBillRecId != null) {
				retval.add(mNewBillRecId);
			}
		} 
		
		return retval;
	}
	
}
