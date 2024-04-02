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
 * Implements IsCustomerEligibleForPortal call returning a boolean indicating that 
 * the specified customers is eligible for online registration given the orgId associated
 * with the account and payment group handed down through the api. A list of accounts
 * is pulled from  * the payment group specified (which should be the status group). It
 * checks the online eligibility flag in the status flex 14 (as of this writing) for each 
 * account in the list. If at least one account this api returns true so the user can finish
 * registering.
 * 
 * @author john kowalonek
 * @since  2024-Apr-01
 * @version 2024-Apr-01 jak First version.
 */
public class IsCustomerEligibleForPortal extends IsCustomerEligibleForPortalBase {

	private static final Logger LOG = LoggerFactory.getLogger(IsCustomerEligibleForPortal.class);
	private static final long serialVersionUID = -6258089615499225165L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;

		final String sStatusPaymentGroup = request.getString(IApiAccountStatus.IsCustomerEligibleForPortal.statusPaymentGroup);
		final String sAccountId = request.getString(IApiAccountStatus.IsCustomerEligibleForPortal.account);

		request.setToResponse();
		
		// -- retrieve the accounts to be registered --
		AccountsForRegistrationDaoImpl DAO = new AccountsForRegistrationDaoImpl();
		List<AccountForRegistrationElement> lAccountsForRegistration = null;
		try {
			lAccountsForRegistration = DAO.getAccountsForRegistration(sStatusPaymentGroup, sAccountId);
		}
		catch (Exception e) {
			LOG.error("IsCustomerEligibleForPortal:processInternal -- exception thrown getting list", e);
			return eReturnCode;
		}
		
		if ((lAccountsForRegistration == null) || (lAccountsForRegistration.size() == 0)) {
			LOG.debug("IsCustomerEligibleForPortal:processInternal -- no eligible accounts");
			return eReturnCode;
		}
		
		// -- go through the list to see if any accounts are eligible. only need one  --
		Boolean bRetValue = false;
		for (int i = 0; i < lAccountsForRegistration.size(); i++) {
			if (lAccountsForRegistration.get(i).m_bIsEligibleForPortal) {
				bRetValue = true;
				break;
			}
		}
		request.set(IApiAccountStatus.IsCustomerEligibleForPortal.isEligible, bRetValue);
		eReturnCode = ServiceAPIErrorCode.Success;
		request.setStatus(eReturnCode);
		
		return eReturnCode;	
	} // -- end of processInternal --

}
