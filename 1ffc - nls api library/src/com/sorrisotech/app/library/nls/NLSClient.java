/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
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
package com.sorrisotech.app.library.nls;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.sorrisotech.client.auth.AuthClient;
import com.sorrisotech.client.auth.model.request.AuthAccessTokenRequest;
import com.sorrisotech.client.auth.model.request.AuthRefreshTokenRequest;
import com.sorrisotech.client.auth.model.response.AuthResponse;
import com.sorrisotech.client.main.MainClient;
import com.sorrisotech.client.main.model.request.ContactPreferencesRequest;
import com.sorrisotech.client.main.model.request.LoanNicknameRequest;
import com.sorrisotech.client.main.model.response.BranchResponse;
import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.client.main.model.response.ConvenienceFeeResponse;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.client.main.model.response.CurrentBalanceResponse;
import com.sorrisotech.client.main.model.response.SimpleNLSResponse;
import com.sorrisotech.client.main.model.response.UpdatedContactPrefrencesResponse;

/******************************************************************************
 * Service NLS API Client library. Can be used to call NLS APIs.
 * 
 * @author Asrar Saloda
 */
public class NLSClient {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(NLSClient.class);
	
	/**********************************************************************************************
	 * Base URL of NLS Server.
	 */
	private static String m_szBaseUrl = null;
	
	/**********************************************************************************************
	 * Client id to access NLS Server.
	 */
	private static String m_szClientId = null;
	
	/**********************************************************************************************
	 * Client Secret to access NLS Server.
	 */
	private static String m_szClientSecret = null;
	
	/**********************************************************************************************
	 * Version of NLS Server.
	 */
	private static String m_szVersion = null;
	
	/******************************************************************************
	 * Main API client of NLS server.
	 */
	private static MainClient m_cMainClient = null;
	
	/******************************************************************************
	 * Authorization API client of NLS server.
	 */
	private static AuthClient m_cAuthClient = null;
	
	/**********************************************************************************************
	 * Initialize the member variables.
	 */
	public static boolean init(
	        String szBaseUrl,
	        String szClientId,
	        String szClientSecret,
	        String szVersion) {
		
		if (szBaseUrl != null && szBaseUrl.trim().length() != 0) {
			NLSClient.m_szBaseUrl = szBaseUrl.trim();
		} else {
			LOG.error("No base URL provided!");
			return false;
		}
		
		if (szClientId != null && szClientId.trim().length() != 0) {
			NLSClient.m_szClientId = szClientId.trim();
		} else {
			LOG.error("No client id provided!");
			return false;
		}
		
		if (szClientSecret != null && szClientSecret.trim().length() != 0) {
			NLSClient.m_szClientSecret = szClientSecret.trim();
		} else {
			LOG.error("No client secret provided!");
			return false;
		}
		
		if (szVersion != null && szVersion.trim().length() != 0) {
			NLSClient.m_szVersion = szVersion.trim();
		} else {
			LOG.error("No version provided!");
			return false;
		}
		
		m_cAuthClient = new AuthClient();
		
		m_cMainClient = new MainClient();
		
		return true;
	}
	
	/**********************************************************************************************
	 * This method can be used to call NLS service to retrieves a binary file of a
	 * document based on its document ID.
	 * 
	 * @param szDocumentId The document id of the statement/bill.
	 * 
	 * @return The binary file of a document of user.(Simple NLS response with
	 *         status code, success, pay load and error )
	 * @throws Exception This exception is thrown when any errors received from NLS
	 *                   server.
	 */
	public static SimpleNLSResponse getDocument(String szDocumentId) throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var createDocumentResponse = m_cMainClient.createDocument(m_szBaseUrl, szDocumentId,
		        m_szVersion, szAuthToken);
		
		if (!createDocumentResponse.getSuccess()
		        && !HttpStatus.valueOf(createDocumentResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(createDocumentResponse.getErrors());
		}
		
		return createDocumentResponse;
	}
	
	/**********************************************************************************************
	 * Fetch the current principal balance and any upcoming payments with their due
	 * dates when the user accesses the overview page.
	 * 
	 * @param szLoanId Loan id of user
	 * 
	 * @return The current balance and upcoming payment due dates of user as a pay
	 *         load.(Current balance response with status code, success, pay load
	 *         and errors).
	 * 
	 * @throws Exception This exception is thrown when any errors received from NLS
	 *                   server.
	 */
	public static CurrentBalanceResponse getCurrentBalance(String szLoanId) throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var currentBalanceResponse = m_cMainClient.getCurrentBalance(m_szBaseUrl, m_szVersion,
		        szLoanId, szAuthToken);
		
		if (!currentBalanceResponse.getSuccess()
		        && !HttpStatus.valueOf(currentBalanceResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(currentBalanceResponse.getErrors());
		}
		
		return currentBalanceResponse;
	}
	
	/**********************************************************************************************
	 * Updates the nickname associated with a loan.
	 * 
	 * @param cLoanNicknameRequest Nickname and loan id of borrower.
	 * @param szLoanId             Loan id of user
	 * 
	 * @return The response of NLS API with updated nickname (Simple NLS response
	 *         with status code, success, pay load and errors).
	 * 		
	 * @throws Exception This exception is thrown when any errors received from NLS
	 *                   server.
	 */
	public static SimpleNLSResponse updateBorrowerNickName(LoanNicknameRequest cLoanNicknameRequest)
	        throws Exception {
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var updateBorrowerNicknameResponse = m_cMainClient.updateBorrowerNickname(m_szBaseUrl,
		        cLoanNicknameRequest, m_szVersion, szAuthToken);
		
		if (!updateBorrowerNicknameResponse.getSuccess() && !HttpStatus
		        .valueOf(updateBorrowerNicknameResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(updateBorrowerNicknameResponse.getErrors());
		}
		
		return updateBorrowerNicknameResponse;
	}
	
	/**********************************************************************************************
	 * Fetch Convenience Fee
	 * 
	 * @param szLoanId Loan id of user
	 * 
	 * @return The result of operation(Simple NLS response with status code,
	 *         success, pay load and errors).
	 * 		
	 * @throws Exception This exception is thrown when any errors received from NLS
	 *                   server.
	 *
	 */
	public static ConvenienceFeeResponse getConvenienceFee(String szLoanId) throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var convenienceFeeResponse = m_cMainClient.getConvenienceFee(m_szBaseUrl, m_szVersion,
		        szLoanId, szAuthToken);
		
		if (!convenienceFeeResponse.getSuccess()
		        && !HttpStatus.valueOf(convenienceFeeResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(convenienceFeeResponse.getErrors());
		}
		
		return convenienceFeeResponse;
	}
	
	/**********************************************************************************************
	 * Provides basic information for all branches in NLS.
	 * 
	 * @return all branches in NLS. operation(Branches NLS response with status
	 *         code, success, pay load and errors).
	 * @throws Exception if error in response.
	 */
	public static BranchResponse getAllBranches() throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var cBranchResponse = m_cMainClient.getBranches(m_szBaseUrl, m_szVersion, szAuthToken);
		
		if (!cBranchResponse.getSuccess()
		        && !HttpStatus.valueOf(cBranchResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(cBranchResponse.getErrors());
		}
		
		return cBranchResponse;
	}
	
	/**********************************************************************************************
	 * Get all updated contact preferences of user that have been updated since the
	 * previous date.
	 * 
	 * @param cContactPreferencesRequest Contact preferences request.
	 * 
	 * @return The contact preferences updates of user.
	 * 
	 * @throws Exception in case of any errors.
	 */
	public static UpdatedContactPrefrencesResponse getAllUpdatedContactPreferences(
	        ContactPreferencesRequest cContactPreferencesRequest) throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var cContactPreferenceUpdates = m_cMainClient.getAllContactPreferenceUpdates(
		        cContactPreferencesRequest, m_szBaseUrl, m_szVersion, szAuthToken);
		
		if (!cContactPreferenceUpdates.getSuccess() && !HttpStatus
		        .valueOf(cContactPreferenceUpdates.getStatuscode()).is2xxSuccessful()) {
			handleError(cContactPreferenceUpdates.getErrors());
		}
		
		return cContactPreferenceUpdates;
	}
	
	/**********************************************************************************************
	 * Obtain the user's preferred method of contact.
	 * 
	 * @param cContactPreferencesRequest Contact preferences request.
	 * 
	 * @return The contact preferences of user.
	 * 
	 * @throws Exception in case of any errors.
	 */
	public static CreateContactPrefrencesResponse getContactPreferencesofUser(
	        ContactPreferencesRequest cContactPreferencesRequest) throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var cContactPreferenceUpdates = m_cMainClient.getContactPreferencesOfUser(
		        cContactPreferencesRequest, m_szBaseUrl, m_szVersion, szAuthToken);
		
		if (!cContactPreferenceUpdates.getSuccess() && !HttpStatus
		        .valueOf(cContactPreferenceUpdates.getStatuscode()).is2xxSuccessful()) {
			handleError(cContactPreferenceUpdates.getErrors());
		}
		
		return cContactPreferenceUpdates;
	}
	
	/**********************************************************************************************
	 * Update the user's contact preferences.
	 * 
	 * @param cContactPrefrences Contact preferences request.
	 * 
	 * @return The contact preferences of user.
	 * 
	 * @throws Exception in case of any errors.
	 */
	public static SimpleNLSResponse updateContactPreferences(ContactPrefrences cContactPrefrences)
	        throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var cContactPreferenceUpdates = m_cMainClient.updateContactPreferences(cContactPrefrences,
		        m_szBaseUrl, m_szVersion, szAuthToken);
		
		if (!cContactPreferenceUpdates.getSuccess() && !HttpStatus
		        .valueOf(cContactPreferenceUpdates.getStatuscode()).is2xxSuccessful()) {
			handleError(cContactPreferenceUpdates.getErrors());
		}
		
		return cContactPreferenceUpdates;
	}
	
	/**********************************************************************************************
	 * Check for new file is available for processing or not.
	 * 
	 * @return "true" if status code is 204 otherwise "false.
	 * 
	 * @throws Exception in case of any errors.
	 */
	public static String isFileAvailable() throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var szAuthToken = NLSClient.fetchAuthToken();
		
		var cisFileAvailable = m_cMainClient.getFileAvailable(m_szBaseUrl, m_szVersion,
		        szAuthToken);
		
		return cisFileAvailable;
	}
	
	/**********************************************************************************************
	 * Refresh user token.
	 * 
	 * @param szAuthToken    authorization token.
	 * @param szRefreshToken refresh token.
	 * 
	 * @return The result of operation.
	 * 
	 * @throws Exception if error in response.
	 */
	public static AuthResponse refreshAuthToken(String szAuthToken, String szRefreshToken)
	        throws Exception {
		
		if (!Boolean.valueOf(isConfigured())) {
			LOG.error("NLS Client Library is not configured yet!");
			throw new IllegalStateException("NLS Client Library is not configured yet");
		}
		
		var refreshTokenResponse = m_cAuthClient.refreshToken(m_szBaseUrl,
		        new AuthRefreshTokenRequest(szAuthToken, szRefreshToken), m_szVersion);
		
		if (!refreshTokenResponse.getSuccess()
		        && !HttpStatus.valueOf(refreshTokenResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(refreshTokenResponse.getErrors());
		}
		return refreshTokenResponse;
	}
	
	/**********************************************************************************************
	 * Checks for values are configured successfully or not.
	 * 
	 * @return String true/false.
	 */
	private static String isConfigured() {
		if (m_szBaseUrl != null && m_szClientId != null && m_szClientSecret != null
		        && m_szVersion != null && m_cAuthClient != null && m_cMainClient != null) {
			return "true";
		}
		return "false";
	}
	
	/**********************************************************************************************
	 * Use to fetch authorization token from NLS server.
	 * 
	 * @return String access token to access NLS server.
	 */
	private static String fetchAuthToken() throws Exception {
		var authResponse = m_cAuthClient.getToken(m_szBaseUrl,
		        new AuthAccessTokenRequest(m_szClientId, m_szClientSecret), m_szVersion);
		if (!authResponse.getSuccess()
		        && !HttpStatus.valueOf(authResponse.getStatuscode()).is2xxSuccessful()) {
			handleError(authResponse.getErrors());
		}
		return authResponse.getPayload().getAccessToken();
	}
	
	/**********************************************************************************************
	 * Used to handle error received from NLS server.
	 * 
	 * @throws Exception When any error received from NLS server.
	 */
	private static void handleError(ArrayList<String> errorMsgList) throws Exception {
		String errorMessage = errorMsgList.stream().collect(Collectors.joining("\n"));
		LOG.error("Error occured in service NLS API calling: " + errorMessage);
		throw new Exception(errorMessage);
	}
}
