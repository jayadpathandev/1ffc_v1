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
 *  Provides use case with status indicating if payment is enabled, and if not why. Specifically
 *  implements response to the <code>IsPaymentEnabled</code> call in the <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>paymentEnabled</b> -	can this account be paid, if not why: enabled, 
 *  		disabledDelinquency, disabledClosed.
 *  
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class IsPaymentEnabled extends IsPaymentEnabledBase {
	
	private static final Logger LOG = LoggerFactory.getLogger(IsPaymentEnabled.class);
	private static final long serialVersionUID = 285782700486402320L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		final String sUser		 	= request.getString(IApiAccountStatus.IsPaymentEnabled.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.IsPaymentEnabled.paymentGroup);
		final String sAccount		= request.getString(IApiAccountStatus.IsPaymentEnabled.account);
		
		LOG.debug("IsPaymentEnabled:processInternal -- entered method for user {}, group {}, acct {}", 
				sUser, sPaymentGroup, sAccount);
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				// -- return true if payment enabled for this account -- 
					request.set(IApiAccountStatus.IsPaymentEnabled.bPaymentEnabled, 
							cacheItem.isPaymentEnabled(sPaymentGroup, sAccount));
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("IsPaymentEnabled:processInternal -- failed to get payment enabled status for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
