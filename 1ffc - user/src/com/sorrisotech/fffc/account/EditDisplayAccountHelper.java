/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.app.utils.UserProfile;
import com.sorrisotech.client.main.model.request.LoanNicknameRequest;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaProfileException;

/*************************************************************************************************
 * Helper class for edit display account (Nickname) pop in.
 * 
 * @author Asrar Saloda
 */
public class EditDisplayAccountHelper {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EditDisplayAccountHelper.class);
	
	/*************************************************************************************************
	 * This method sends Nickname to NLS using NLSClient library.
	 * 
	 * @param szLoanid            Loan id (Internal account number)
	 * @param szNIcknameAttrValue Nickname of user.
	 * 
	 * @return true/false in string format (success/fail).
	 */
	public static String sendLoanNicknameToNls(final String szLoanid, String szNIcknameAttrValue) {
		
		var eReturnValue = "false";
		
		if (null != szLoanid) {
			
			try {
				
				// --------------------------------------------------------------------------------------
				// I believe we can't set empty string from usecase so sending "null" from
				// usecase if user removed nickname and sending empty string to NLS.
				if (szNIcknameAttrValue.equals("null"))
					szNIcknameAttrValue = "";
				
				var updateBorrowerNickNameResponse = NLSClient.updateBorrowerNickName(
				        new LoanNicknameRequest(szLoanid, szNIcknameAttrValue));
				
				if (updateBorrowerNickNameResponse.getSuccess()) {
					eReturnValue = "true";
					LOG.info(
					        "EditDisplayAccountHelper....... sendNicknameToNls ......... success payload: "
					                + updateBorrowerNickNameResponse.getPayload());
				}
				
			} catch (Exception e) {
				LOG.error("Error in calling NLS service : " + e.getMessage(), e);
			}
		}
		
		return eReturnValue;
	}
	
	/*************************************************************************************************
	 * This method update Nickname in auth_user_profile table.
	 * 
	 * @param szUserId            User id.
	 * @param szNicknameAttrName  attribute name
	 *                            (Nickname-<paymentGroup>-<internalaccountnumber>)
	 * @param szNIcknameAttrValue Nickname of user.
	 * 							
	 * @return true/false in string format (success/fail).
	 */
	public static String upsertNicknameToUserProfile(final IServiceLocator2 clocator2,
	        final String szUserId, final String szNicknameAttrName, String szNIcknameAttrValue) {
		
		String szReturnValue = "false";
		
		// --------------------------------------------------------------------------------------
		// I believe we can't set empty string from usecase so sending "null" from
		// usecase if user removed nickname and sending empty string to
		// auth_user_profile table.
		if (szNIcknameAttrValue.equals("null"))
			szNIcknameAttrValue = "";
		
		try {
			// --------------------------------------------------------------------------------------
			// Updating nickname to user profile.
			UserProfile.updateProfileAttrValue(clocator2, szUserId, szNicknameAttrName,
			        szNIcknameAttrValue);
			szReturnValue = "true";
		} catch (IllegalArgumentException | MargaritaProfileException e) {
			LOG.error("EditDisplayAccountHelper ........ upsertNicknameToUserProfile .... error: ",
			        e, e);
		}
		
		return szReturnValue;
		
	}
	
	/*************************************************************************************************
	 * This method is used to get users nickname.
	 * 
	 * @param szUserId           User id.
	 * @param szNicknameAttrName Attribute name
	 *                           (Nickname-<paymentGroup>-<internalaccountnumber>)
	 * 							
	 * @return "not set" if nickname is not set by user else returns nickname value.
	 */
	public static String getUsersNicknameValue(IServiceLocator2 cLocator2, final String szUserId,
	        final String szNicknameAttrName) {
		
		var szReturnValue = "not set";
		
		// --------------------------------------------------------------------------------------
		// Fetching nickname from user profile.
		final var szUserNickname = UserProfile.getProfileAttrValue(cLocator2, szUserId,
		        szNicknameAttrName);
		if (null != szUserNickname && !szUserNickname.isBlank()) {
			szReturnValue = szUserNickname;
		}
		return szReturnValue;
	}
}
