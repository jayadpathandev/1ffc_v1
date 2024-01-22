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
 *  Provides use case with account status information. Specifically implements response to the <code>GetStatus</code> call in the 
 *  <code>accountStatus</code> service.
 *  
 *  @param user -			userId for the logged in user.
 *  @param statusGroup -	payment group containing account status information.
 *  @param accountNumber - 	internal account number for this request.
 *  
 *  @return <li><b>debitConvenienceFeeAmt</b> - convenience fee for debit card transactions
 *  
 *  @version 22-Jan-2024
 *  @since 22-Jan-2024
 *  @author John A. Kowalonek 
 */
public class GetDebitConvenienceFee extends GetDebitConvenienceFeeBase {
	
	private static final long serialVersionUID = 7969606031482386089L;
	private static final Logger LOG = LoggerFactory.getLogger(GetDebitConvenienceFee.class);

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		final String sUser		 	= request.getString(IApiAccountStatus.GetStatus.user);
		final String sPaymentGroup 	= request.getString(IApiAccountStatus.GetStatus.paymentGroup);
		final String sAccount		= request.getString(IApiAccountStatus.GetStatus.account);

		LOG.debug("GetDebitConvenienceFee:processInternal -- entered method for user {}, group {}, acct {}", 
				sUser, sPaymentGroup, sAccount);
		
		ServiceAPIErrorCode rVal = ServiceAPIErrorCode.Failure;
		IUserStatusCacheItem cacheItem = UserStatusCache.getItem(sUser);
		request.setToResponse();
		
		if (null != cacheItem) {
			try {
				// -- return detailed status on the specified account -- 
				request.set(IApiAccountStatus.GetDebitConvenienceFee.convenienceFeeAmt, cacheItem.getDebitConvenienceFee(sPaymentGroup, sAccount));
				
				rVal = ServiceAPIErrorCode.Success;
			} catch (AccountStatusException e) {
				LOG.error("GetDebitConvenienceFee:processInternal -- failed to get convenience fee for user {}, group {}, acct {}",
						sUser, sPaymentGroup, sAccount, e);
				rVal = ServiceAPIErrorCode.InternalFailure;
			}
		}
		request.setStatus(rVal);
		return rVal;
	}
}