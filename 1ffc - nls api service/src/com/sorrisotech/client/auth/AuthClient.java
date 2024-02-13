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
package com.sorrisotech.client.auth;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

import com.sorrisotech.client.auth.model.request.AuthAccessTokenRequest;
import com.sorrisotech.client.auth.model.request.AuthRefreshTokenRequest;
import com.sorrisotech.client.auth.model.response.AuthResponse;
import com.sorrisotech.client.exception.InvalidRequestException;
import com.sorrisotech.client.utils.ApiUtils;
import com.sorrisotech.client.utils.EndPoints;
import com.sorrisotech.client.utils.RequestValidator;

/******************************************************************************
 * Authorization API clients
 * 
 * @author Asrar Saloda
 */
public class AuthClient {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AuthClient.class);
	
	/**********************************************************************************************
	 * Utility class that executes API requests
	 */
	private ApiUtils m_cApiUtils = null;
	
	/**********************************************************************************************
	 * Initialize the member variables.
	 * 
	 */
	public AuthClient() {
		m_cApiUtils = new ApiUtils();
	}
	
	/**********************************************************************************************
	 * Fetch user token.
	 * 
	 * @param szBaseUrl           base URL of server.
	 * @param cAccessTokenRequest user name and password
	 * @param szVersion           Version of server.
	 * 
	 * @return The result of operation.
	 * 
	 * @throws InvalidRequestException This exception is thrown when invalid request
	 *                                 is received.
	 * @throws RestClientException     Exception thrown by {@link RestTemplate}
	 *                                 whenever it encounters client-side HTTP
	 *                                 errors.
	 */
	public AuthResponse getToken(
	        String szBaseUrl,
	        AuthAccessTokenRequest cAccessTokenRequest,
	        String szVersion) throws InvalidRequestException,
	        RestClientException {
		LOG.debug("Access Token Request : " + cAccessTokenRequest);
		
		RequestValidator.ValidateRequest(cAccessTokenRequest);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.AUTH_GET_TOKEN.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Access Token Url : " + szEndPointURL);
		
		var szResponse = m_cApiUtils.executeApiRequest(cAccessTokenRequest, HttpMethod.POST,
		        szEndPointURL, null, AuthResponse.class);
		LOG.debug("Access Token Response : " + szResponse.getBody());
		return szResponse.getBody();
	}
	
	/**********************************************************************************************
	 * Refresh user token.
	 * 
	 * @param szBaseUrl                base URL of server.
	 * @param cAuthRefreshTokenRequest access and refresh token.
	 * @param szVersion                Version of server.
	 * 
	 * @return The result of operation.
	 * 
	 * @throws InvalidRequestException This exception is thrown when invalid request
	 *                                 is received.
	 * @throws RestClientException     Exception thrown by {@link RestTemplate}
	 *                                 whenever it encounters client-side HTTP
	 *                                 errors.
	 */
	public AuthResponse refreshToken(
	        String szBaseUrl,
	        AuthRefreshTokenRequest cAuthRefreshTokenRequest,
	        String szVersion) throws InvalidRequestException,
	        RestClientException {
		LOG.debug("Refresh Token Request : " + cAuthRefreshTokenRequest);
		
		RequestValidator.ValidateRequest(cAuthRefreshTokenRequest);
		
		var cPathVariables = Map.of("version", szVersion);
		
		var szEndPointURL = EndPoints.AUTH_REFRESH.buildURL(szBaseUrl, cPathVariables);
		LOG.debug("Refresh Token Url : " + szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(cAuthRefreshTokenRequest, HttpMethod.POST,
		        szEndPointURL, null, AuthResponse.class);
		LOG.debug("Refresh Token Response : " + cResponse.getBody());
		return cResponse.getBody();
	}
}
