package com.sorrisotech.svcs.accountstatus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.dao.AcctFromExtAcctElement;
import com.sorrisotech.svcs.accountstatus.dao.AcctFromExtAcctDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.IAcctFromExtAcctDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/**
 *  Provides use case with customer id and internal account id given an external accountid.
 *  Used during the migration process.
 *  
 *  	@param externalAccount
 *		@param billPaymentGroup
 *
 *		@return customerId
 *		@return internalAccount
 *
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class GetAccountInfoFromExtAcct extends GetAccountInfoFromExtAcctBase {

	private static final long serialVersionUID = 7294347210331342447L;
	private static final Logger LOG = LoggerFactory.getLogger(GetAccountInfoFromExtAcct.class);
	
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		final String sExternalAcct 	= request.getString(IApiAccountStatus.GetAccountInfoFromExtAcct.externalAccount);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.GetAccountInfoFromExtAcct.billPaymentGroup);

		LOG.debug("GetAccountInfoFromExtAcct:processInternal -- entered method for external acct {}, group {}", 
				sExternalAcct, sPaymentGroup);

		request.setToResponse();

		// -- retrieve the accounts to be registered --
		IAcctFromExtAcctDao DAO = new AcctFromExtAcctDaoImpl();
		AcctFromExtAcctElement lAcctDataFromExtAcct = null;
		try {
			lAcctDataFromExtAcct = DAO.getAcctInfoFromExtAccount(sPaymentGroup, sExternalAcct);
		}
		catch (Exception e) {
			LOG.error("GetAccountInfoFromExtAcct:processInternal -- exception thrown getting results", e);
			request.setStatus(ServiceAPIErrorCode.InvalidValue);
			return ServiceAPIErrorCode.InvalidValue;
		}
		
		request.set(IApiAccountStatus.GetAccountInfoFromExtAcct.customerId, lAcctDataFromExtAcct.m_szCustomerId);
		request.set(IApiAccountStatus.GetAccountInfoFromExtAcct.internalAccount, lAcctDataFromExtAcct.m_szInternalAccountId);

		request.setStatus(ServiceAPIErrorCode.Success);
		return ServiceAPIErrorCode.Success;
	}

}
