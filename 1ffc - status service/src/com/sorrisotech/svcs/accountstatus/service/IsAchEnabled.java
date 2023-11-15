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
 *  Provides use case with information on whether this user can use ACH. Specifically implements
 *  response to the <code>IsAchEnabled</code> call in the 
 *  <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>achEnabled</b> -		is ACH enabled and if not why: enabled, disabledNSF.
 *  
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class IsAchEnabled extends IsAchEnabledBase {

	private static final Logger LOG = LoggerFactory.getLogger(IsAchEnabled.class);
	private static final long serialVersionUID = -1887959557256749637L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		final String sUser		 	= request.getString(IApiAccountStatus.IsAchEnabled.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.IsAchEnabled.paymentGroup);

		LOG.debug("IsAchEnabled:processInternal -- entered method for user {}, group {}", 
				sUser, sPaymentGroup );
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		request.setToResponse();
			
		if (null != cacheItem) {
			// -- returns true if ACH is enabled for this account --
			try {
				request.set(IApiAccountStatus.IsAchEnabled.bAchEnabled, cacheItem.isAchEnabled());
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("IsAchEnabled:processInternal -- failed to get ACH info for user {}, group {}, acct {}",
						sUser, sPaymentGroup, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
