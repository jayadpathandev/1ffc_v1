package com.sorrisotech.svcs.accountstatus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.cache.AccountStatusException;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst;
import com.sorrisotech.svcs.accountstatus.cache.IUserStatusCacheItem;
import com.sorrisotech.svcs.accountstatus.cache.UserStatusCache;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/**
 *  Provides use case with indication of whether the user has access to the specified account. Specifically
 *   implements response to the <code>HasAccountAccess</code> call in the 
 *  <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param paymentGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>accessEnabled</b> -	True if user has online access to this account
 *  
 *  @version 24-Sep-2023
 *  @since 24-Sep-2023
 *  @author John A. Kowalonek 
 */
public class HasAccountAccess extends HasAccountAccessBase {

	private static final Logger LOG = LoggerFactory.getLogger(HasAccountAccess.class);
	private static final long serialVersionUID = -617722867267893519L;

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		final String sUser		 	= request.getString(IApiAccountStatus.HasAccountAccess.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.HasAccountAccess.paymentGroup);
		final String sAccount		= request.getString(IApiAccountStatus.HasAccountAccess.account);
		
		LOG.debug("HasAccountAccess:processInternal -- entered method for user {}, group {}, acct {}", 
				sUser, sPaymentGroup, sAccount);
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		boolean bHasAccess = false;
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				
				//-- if the user has view access to information about this account return true --
				if (cacheItem.getViewAccount(sPaymentGroup, sAccount) == EnumConst.ViewAcct.enabled)
					bHasAccess = true;
				request.set(IApiAccountStatus.HasAccountAccess.bAccessEnabled, bHasAccess);
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("HasAccountAccess:processInternal -- failed to get access info for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}
		
		request.setStatus(rVal);
		return rVal;

	}

}
