package com.sorrisotech.svcs.accountstatus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.cache.AccountStatusException;
import com.sorrisotech.svcs.accountstatus.cache.IUserStatusCacheItem;
import com.sorrisotech.svcs.accountstatus.cache.UserStatusCache;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/**
 *  Provides use case with just the basic account status information. Implements response to the 
 *  <code>GetBasicStatus</code> call in the <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>accountStatus</b> -	status of the account: new, closed, active.
 *  
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class GetBasicStatus extends GetBasicStatusBase {

	private static final Logger LOG = LoggerFactory.getLogger(GetBasicStatus.class);
	private static final long serialVersionUID = 3351923245751186591L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		final String sUser		 	= request.getString(IApiAccountStatus.GetBasicStatus.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.GetBasicStatus.paymentGroup);
		final String sAccount		= request.getString(IApiAccountStatus.GetBasicStatus.account);

		LOG.debug("GetBasicStatus:processInternal -- entered method for user {}, group {}, acct {}", 
				sUser, sPaymentGroup, sAccount);

		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				// -- returns the basic account status for a specific account number,
				//		newAccount, activeAccount, closedAccount --
				request.set(IApiAccountStatus.GetBasicStatus.accountStatus, 
							cacheItem.getAccountStatus(sPaymentGroup, sAccount).toString());
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("GetBasicStatus:processInternal -- failed to get basic status for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
