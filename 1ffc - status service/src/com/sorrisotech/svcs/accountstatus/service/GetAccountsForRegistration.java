package com.sorrisotech.svcs.accountstatus.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.dao.AccountForRegistrationElement;
import com.sorrisotech.svcs.accountstatus.dao.AccountsForRegistrationDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.InsertTmAccountRecordDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.TmAccountsFromOrgIdDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.ITmAccountsFromOrgIdDao;
import com.sorrisotech.svcs.accountstatus.dao.IInsertTmAccountRecordDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/**
 * Implements GetEligibleAccounts call returning a table of accounts that are 
 * eligible for online registration given the orgId associated with the account
 * provided in the api and payment group handed down. The list is pulled from
 * the payment group specified (which should be the status group). It checks 
 * the online eligibility flag in the status flex 14 (as of this writing) so accounts
 * with flex14 = 'true' are the only ones returned.
 * 
 * @author john kowalonek
 * @since  2024-Mar-22
 * @version 2024-Mar-22  jak  First version.
 */
public class GetAccountsForRegistration extends GetAccountsForRegistrationBase {

	private static final Logger LOG = LoggerFactory.getLogger(GetAccountsForRegistration.class);
	private static final long serialVersionUID = -6258089615499225165L;

	@SuppressWarnings("unchecked")
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;

		final String sStatusPaymentGroup = request.getString(IApiAccountStatus.GetAccountsForRegistration.statusPaymentGroup);
		final String sBillPaymentGroup = request.getString(IApiAccountStatus.GetAccountsForRegistration.billPaymentGroup);
		final String sAccountId = request.getString(IApiAccountStatus.GetAccountsForRegistration.account);

		request.setToResponse();
		
		// -- retrieve the accounts to be registered --
		AccountsForRegistrationDaoImpl DAO = new AccountsForRegistrationDaoImpl();
		List<AccountForRegistrationElement> lAccountsForRegistration = null;
		try {
			lAccountsForRegistration = DAO.getAccountsForRegistration(sStatusPaymentGroup, sAccountId);
		}
		catch (Exception e) {
			LOG.error("GetAccountsForRegistration:processInternal -- exception thrown getting list", e);
			return eReturnCode;
		}
		
		if ((lAccountsForRegistration == null) || (lAccountsForRegistration.size() == 0)) {
			LOG.debug("GetAccountsForRegistration:processInternal -- no eligible accounts");
			return eReturnCode;
		}
		
		// -- create tm_account entries for any status accounts that don't have bills --
		boolean bRetAcctEntries = createAccountEntries(lAccountsForRegistration, sStatusPaymentGroup, sBillPaymentGroup, 
				lAccountsForRegistration.get(0).m_szOrgId);
		if (!bRetAcctEntries) {
			LOG.debug("GetAccountsForRegistration:createAccountEntries -- failure");
			return eReturnCode;
		}
		
		// -- put them in an array of HashMaps for return as a table to use in the application --
		
		HashMap<String, String> lAccounts[] = new HashMap[lAccountsForRegistration.size()];
		String lszOrgId = null;
		
		LOG.debug("GetAccountsForRegistration:processInternal -- found {} eligible accounts for pmt group {}, account {}",
				lAccountsForRegistration.size(), sStatusPaymentGroup, sAccountId);
		
		for (int i = 0; i < lAccountsForRegistration.size(); i++) {
			if (i == 0) lszOrgId = lAccountsForRegistration.get(i).m_szOrgId; // -- they are all the same orgId by definition
			lAccounts[i] = new HashMap<String, String>();
			lAccounts[i].put("internalAccountId", lAccountsForRegistration.get(i).m_szInternalAccountId);
		}

		request.set(IApiAccountStatus.GetAccountsForRegistration.accounts, lAccounts);	
		request.set(IApiAccountStatus.GetAccountsForRegistration.orgId, lszOrgId);
		eReturnCode = ServiceAPIErrorCode.Success;
		request.setStatus(eReturnCode);
		
		return eReturnCode;	
	}
	
	/**
	 * Sees if there are tm_account entries in the bill pay group for any accounts we picked up in the status pay group.
	 * Adds the account to the bill pay group if its not there. This ensures accounts without bills can be seen in the
	 * application.
	 *  
	 * @param lAccountsForRegistration
	 * @param cszStatusPayGroup
	 * @param cszBillPayGroup
	 * @param cszOrgId
	 * @return
	 */
	private boolean createAccountEntries (List<AccountForRegistrationElement> lAccountsForRegistration, 
																			final String cszStatusPayGroup,
																			final String cszBillPayGroup,
																			final String cszOrgId) {
		boolean bRetValue = false;
		
		// -- get list of accounts that are already in tm_accounts --
		ITmAccountsFromOrgIdDao DAO = new TmAccountsFromOrgIdDaoImpl();
		List<String> lTmAccountsForRegistration	= null;
		try {
			lTmAccountsForRegistration = DAO.getTmAccountsList(cszOrgId, cszStatusPayGroup);
		}
		catch (Exception e) {
			LOG.error("GetAccountsForRegistration:processInternal -- exception thrown getting list", e);
			return bRetValue;
		}
		
		// -- For each account we plan to register, check to see if has an account record
		//		 in tm account. For those that don't, create one from the one on the status feed 
		//
		//		NOTE: this is a bit brute force... if we start search through lists of accounts like
		//			a couple hundred or more we should make the getTmAcocuntList return a map --
		//
		for (int i=0; i < lAccountsForRegistration.size(); i++) {
			boolean bFound = false;
			int iElementNum = 0;
			
			for (int j=0; j < lTmAccountsForRegistration.size(); j++) {
				// -- look for the equivalent of status account in the bill payment group --
				if (lTmAccountsForRegistration.get(j).equalsIgnoreCase(lAccountsForRegistration.get(i).m_szInternalAccountId)) {
					bFound = true;
					iElementNum = i;
					break;
				}
			} // -- end for j.. --
			
			if (!bFound) {
				// -- if not found, then add a tm account record into the status field --
				bRetValue = addAccountToTmAccounts (cszBillPayGroup, lAccountsForRegistration.get(iElementNum));
				if (!bRetValue) {
					LOG.error ("GetAccountsForRegistration:processInternal -- failed to add tm_account record for internal account id {}.", 
							lAccountsForRegistration.get(iElementNum).m_szInternalAccountId);
					return bRetValue;
				}
			}
		} // -- end for i.. -
		
		bRetValue = true;
		return bRetValue;
	}

	/**
	 * Inserts the appropriate bill pay group entry for a given status account group entry into tm_accounts.
	 * This is only called if the bill pay group record for a given status pay group record doesn't exist.
	 * 
	 * @param cszBillPayGroup
	 * @param coStatusAccount
	 * @return
	 */
	private boolean addAccountToTmAccounts(final String cszBillPayGroup,
																					final AccountForRegistrationElement coStatusAccount ) {
	
		IInsertTmAccountRecordDao DAO = new InsertTmAccountRecordDaoImpl();
		
		return DAO.InsertTmAccountRecord(coStatusAccount, cszBillPayGroup);
	}
}
