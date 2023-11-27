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
package com.sorrisotech.client.main;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sorrisotech.client.exception.InvalidRequestException;
import com.sorrisotech.client.main.model.request.ContactPreferencesRequest;
import com.sorrisotech.client.main.model.request.LoanNicknameRequest;
import com.sorrisotech.client.main.model.response.BranchResponse;
import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.client.main.model.response.ConvenienceFeeResponse;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.client.main.model.response.CurrentBalanceResponse;
import com.sorrisotech.client.main.model.response.SimpleNLSResponse;
import com.sorrisotech.client.main.model.response.UpdatedContactPrefrencesResponse;
import com.sorrisotech.client.utils.ApiUtils;
import com.sorrisotech.client.utils.EndPoints;
import com.sorrisotech.client.utils.RequestValidator;

/******************************************************************************
 * Main API clients
 * 
 * @author Asrar Saloda
 */
public class MainClient {
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MainClient.class);
	
	/**********************************************************************************************
	 * Utility class that executes API requests.
	 */
	private ApiUtils m_cApiUtils;
	
	/**********************************************************************************************
	 * Initialize the member variables.
	 * 
	 */
	public MainClient() {
		m_cApiUtils = new ApiUtils();
	}
	
	/**********************************************************************************************
	 * Obtain the user's preferred method of contact.
	 * 
	 * @param cContactPreferencesRequest Contact preferences request.
	 * @param szBaseUrl                  Base URL of server.
	 * @param szVersion                  Version of server.
	 * @param szToken                    Authorization token.
	 * 
	 * @return The contact preferences of user with success status.
	 * 
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 */
	public CreateContactPrefrencesResponse getContactPreferencesOfUser(
	        ContactPreferencesRequest cContactPreferencesRequest,
	        String szBaseUrl,
	        String szVersion,
	        String szToken) throws RestClientException {
		
		LOG.debug("Create Contact Preferences Request : " + cContactPreferencesRequest);
		
		RequestValidator.ValidateRequest(cContactPreferencesRequest);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.CONTACT_PREFERENCES.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Create Contact Preferences Url : " + szEndPointURL);
		
		var szResponse = m_cApiUtils.executeApiRequest(cContactPreferencesRequest, HttpMethod.POST,
		        szEndPointURL, szToken, CreateContactPrefrencesResponse.class);
		LOG.debug("Create Contact Preferences Response:" + szResponse);
		return szResponse;
	}
	
	/**********************************************************************************************
	 * 
	 * Update the user's contact preferences.
	 * 
	 * @param cContactPreferences contact preference request.
	 * @param szBaseUrl           base URL of server.
	 * @param szVersion           version of server.
	 * @param szToken             Authorization token.
	 * 
	 * @return The updated contact preferences of user with success status.
	 * 
	 * @throws InvalidRequestException This exception is thrown when invalid request
	 *                                 is received.
	 * @throws RestClientException     Exception thrown by {@link RestTemplate}
	 *                                 whenever it encounters client-side HTTP
	 *                                 errors.
	 */
	public SimpleNLSResponse updateContactPreferences(
	        ContactPrefrences cContactPreferences,
	        String szBaseUrl,
	        String szVersion,
	        String szToken) throws InvalidRequestException,
	        RestClientException {
		LOG.debug("Update Contact Preferences Request : " + cContactPreferences);
		
		RequestValidator.ValidateRequest(cContactPreferences);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.CONTACT_PREFERENCES.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Update Contact Preferences Url : " + szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(cContactPreferences, HttpMethod.PATCH,
		        szEndPointURL, szToken, SimpleNLSResponse.class);
		LOG.debug("Update Contact Preferences Response:" + cResponse.toString());
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Get all updated contact preferences of user that have been updated since the
	 * previous date.
	 * 
	 * @param cContactPreferencesRequest Contact preferences request.
	 * @param szBaseUrl                  Base URL of server.
	 * @param szVersion                  Version.
	 * @param szToken                    Authorization token.
	 * 
	 * @return The contact preferences updates of user.
	 * 
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 */
	public UpdatedContactPrefrencesResponse getAllContactPreferenceUpdates(
	        ContactPreferencesRequest cContactPreferencesRequest,
	        String szBaseUrl,
	        String szVersion,
	        String szToken) throws RestClientException {
		
		LOG.debug("Get Contact Preferences Request : " + cContactPreferencesRequest);
		
		RequestValidator.ValidateRequest(cContactPreferencesRequest);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.CONTACT_PREFERENCE_UPDATES.buildURL(szBaseUrl,
		        cPathVariables);
		LOG.debug("Get Contact Preference Updates Url: " + szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(cContactPreferencesRequest, HttpMethod.POST,
		        szEndPointURL, szToken, UpdatedContactPrefrencesResponse.class);
		LOG.debug("Get Contact Preference Updates Response:" + cResponse);
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Retrieves a binary file of a document based on its document ID.
	 * 
	 * @param szBaseUrl   base URL of NLS server.
	 * @param cDocumentId Document id to fetch specific document.
	 * @param szVersion   version.
	 * @param szToken     Authorization token.
	 * 
	 * @return The binary file of a document of user.
	 * 
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 */
	public SimpleNLSResponse createDocument(
	        String szBaseUrl,
	        String cDocumentId,
	        String szVersion,
	        String szToken) throws RestClientException {
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.DOCUMENT.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Create Document Url : " + szEndPointURL);
		
		var szRequestBody = String.format("\"%s\"", cDocumentId);
		
		var cResponse = m_cApiUtils.executeApiRequest(szRequestBody, HttpMethod.POST, szEndPointURL,
		        szToken, SimpleNLSResponse.class);
		LOG.debug("Create Document Response:" + cResponse);
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Fetch the current principal balance and any upcoming payments with their due
	 * dates when the user accesses the overview page.
	 * 
	 * @param szBaseUrl base URL of server.
	 * @param szVersion version.
	 * @param szLoanId  Loan id of user
	 * @param szToken   Authorization token.
	 * 
	 * @return The current balance and upcoming payment due dates of user.
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 * @throws Exception           If loan id is null or empty and/or any unknown
	 *                             error occurred.
	 */
	public CurrentBalanceResponse getCurrentBalance(
	        String szBaseUrl,
	        String szVersion,
	        String szLoanId,
	        String szToken) throws Exception {
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.CURRENT_BALANCE.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Get Current Balance Url: " + szEndPointURL);
		
		if (null == szLoanId || szLoanId.isBlank()) {
			LOG.error("Get Current Balance .... loan id can not be null or empty");
			throw new Exception("Loan id can not be null or empty");
		}
		
		var cResponse = m_cApiUtils.executeApiRequest(String.format("\"%s\"", szLoanId),
		        HttpMethod.POST, szEndPointURL, szToken, CurrentBalanceResponse.class);
		LOG.debug("Get Current Balance Response:" + cResponse);
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Updates the nickname associated with a loan.
	 * 
	 * @param szBaseUrl            base URL of server.
	 * @param cLoanNicknameRequest Nickname of borrower
	 * @param szVersion            version of server.
	 * @param szLoanId             Loan id of user
	 * @param szToken              Authorization token.
	 * 
	 * @return The updated nickname of borrower.
	 * 
	 * @throws InvalidRequestException This exception is thrown when invalid request
	 *                                 is received.
	 * @throws RestClientException     Exception thrown by {@link RestTemplate}
	 *                                 whenever it encounters client-side HTTP
	 *                                 errors.
	 */
	public SimpleNLSResponse updateBorrowerNickname(
	        String szBaseUrl,
	        LoanNicknameRequest cLoanNicknameRequest,
	        String szVersion,
	        String szToken) throws RestClientException {
		LOG.debug("Update Borrower Nickname Request : " + cLoanNicknameRequest);
		
		RequestValidator.ValidateRequest(cLoanNicknameRequest);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.BORROWER_NICKNAME.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Update Borrower Nickname Url : " + szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(cLoanNicknameRequest, HttpMethod.PUT,
		        szEndPointURL, szToken, SimpleNLSResponse.class);
		LOG.debug("Update Borrower Nickname Response:" + cResponse);
		
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Fetch Convenience Fee.
	 * 
	 * @param szBaseUrl base URL of server.
	 * @param szVersion version of server.
	 * @param szLoanId  Loan id of user
	 * @param szToken   Authorization token.
	 * 
	 * @return The result of operation is received.
	 * @throws Exception If loan id is null or empty and/or any unknown error
	 *                   occurred.
	 */
	public ConvenienceFeeResponse getConvenienceFee(
	        String szBaseUrl,
	        String szVersion,
	        String szLoanId,
	        String szToken) throws Exception {
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.CONVENIENCE_FEE.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Get Convenience Fee Url: " + szEndPointURL);
		
		if (null == szLoanId || szLoanId.isBlank()) {
			LOG.error("Get Convenience Fee .... loan id can not be null or empty");
			throw new Exception("Loan id can not be null or empty");
		}
		
		var cResponse = m_cApiUtils.executeApiRequest(String.format("\"%s\"", szLoanId),
		        HttpMethod.POST, szEndPointURL, szToken, ConvenienceFeeResponse.class);
		LOG.debug("Get Convenience Fee Response:" + cResponse);
		return cResponse;
	}
	
	/**********************************************************************************************
	 * Provides basic information for all branches in NLS.
	 * 
	 * @param szBaseUrl base URL of server.
	 * @param szVersion version.
	 * @param szToken   Authorization token.
	 * 
	 * @return The result of operation is received.
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 */
	public BranchResponse getBranches(String szBaseUrl, String szVersion, String szToken) {
		var cPathVariable = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.BRANCHES.buildURL(szBaseUrl, cPathVariable);
		LOG.debug("Get All Branches URL: ", szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(null, HttpMethod.GET, szEndPointURL, szToken,
		        BranchResponse.class);
		LOG.debug("Get All Branches Response:", cResponse);
		
		return cResponse;
	}
}
