package com.sorrisotech.svcs.accountstatus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.cache.AccountStatusException;
import com.sorrisotech.svcs.accountstatus.cache.IUserStatusCacheItem;
import com.sorrisotech.svcs.accountstatus.cache.UserStatusCache;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class GetContractualMonthlyPaymentAmount extends GetContractualMonthlyPaymentAmountBase {

	private static final Logger LOG = LoggerFactory.getLogger(GetContractualMonthlyPaymentAmount.class);
	private static final long serialVersionUID = -7387189037595246231L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		final String sUser		 	= request.getString(IApiAccountStatus.GetContractualMonthlyPaymentAmount.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.GetContractualMonthlyPaymentAmount.paymentGroup);
		final String sAccount		= request.getString(IApiAccountStatus.GetContractualMonthlyPaymentAmount.account);

		LOG.debug("GetContractualMonthlyPaymentAmount:processInternal -- entered method for user {}, group {}, acct {}", 
				sUser, sPaymentGroup, sAccount);

		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				// -- returns the contractual conthly payment amount for a specific account number,
				//		newAccount, activeAccount, closedAccount --
				request.set(IApiAccountStatus.GetContractualMonthlyPaymentAmount.monthlyPaymentAmount, 
							cacheItem.getMonthlyPayment(sPaymentGroup, sAccount));
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("GetContractualMonthlyPaymentAmount:processInternal -- failed to get contractual payment amt for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
