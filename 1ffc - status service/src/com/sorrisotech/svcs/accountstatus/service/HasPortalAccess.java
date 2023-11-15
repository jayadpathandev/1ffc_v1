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
 *  Provides use case with indication of whether the user has access to the online portal. Specifically
 *   implements response to the <code>HasPortalAccess</code> call in the 
 *  <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  
 *  @return <li><b>accessEnabled</b> -	True if user has online access to this account
 *  
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class HasPortalAccess extends HasPortalAccessBase {

	private static final Logger LOG = LoggerFactory.getLogger(HasPortalAccess.class);
	private static final long serialVersionUID = 8213726122399419063L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		final String sUser		 	= request.getString(IApiAccountStatus.HasPortalAccess.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.HasPortalAccess.paymentGroup);
		
		LOG.debug("HasPortalAccess:processInternal -- entered method for user {}, group {}", 
				sUser, sPaymentGroup );
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		boolean bHasAccess = false;
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				// -- returns true if this USER has portal access... not account dependent --
				if (cacheItem.hasPortalAccess())
					bHasAccess = true;
				request.set(IApiAccountStatus.HasAccountAccess.bAccessEnabled, bHasAccess);
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("HasPortalAccess:processInternal -- failed to get access info for user {}, group {}",
						sUser, sPaymentGroup, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}

		request.setStatus(rVal);
		return rVal;
	}

}
