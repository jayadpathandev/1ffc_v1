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
 *  Provides use case with information on whether this account is required to make a minimum payment
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>achEnabled</b> -		is ACH enabled and if not why: enabled, disabledNSF.
 *  
 *  @version 13-Nov-2023
 *  @since 13-Nov-2023
 *  @author John A. Kowalonek 
 */
public class IsMinimumPaymentRequired extends IsMinimumPaymentRequiredBase {

	private static final Logger LOG = LoggerFactory.getLogger(IsMinimumPaymentRequired.class);
	private static final long serialVersionUID = -1887959557256749637L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		final String sUser		 	= request.getString(IApiAccountStatus.IsMinimumPaymentRequired.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.IsMinimumPaymentRequired.paymentGroup);
		final String sAccount	= request.getString(IApiAccountStatus.IsMinimumPaymentRequired.account);

		LOG.debug("IsMinimumPaymentRequired:processInternal -- entered method for user {}, group {}", 
				sUser, sPaymentGroup, sAccount );
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		IUserStatusCacheItem.MinimumPaymentData minData = null;
		request.setToResponse();
			
		if (null != cacheItem) {
			// -- returns true if ACH is enabled for this account --
			try {
				// -- get minimum payment data --
				minData = cacheItem.getMinimumPaymentAmt(sPaymentGroup, sAccount);
				request.set(IApiAccountStatus.IsMinimumPaymentRequired.bMinimumRequired, minData.getIsMinimumRequired());
				request.set(IApiAccountStatus.IsMinimumPaymentRequired.sAmountRequired, minData.getMinimumAmount().toString());
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("IsAchEnabled:processInternal -- failed to get ACH info for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
