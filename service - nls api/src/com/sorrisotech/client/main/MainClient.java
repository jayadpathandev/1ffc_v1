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
import com.sorrisotech.client.main.model.response.SimpleNLSResponse;
import com.sorrisotech.client.utils.ApiUtils;
import com.sorrisotech.client.utils.EndPoints;

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
	 * Retrieves a binary file of a document based on its document ID.
	 * 
	 * @param szBaseUrl   Authorization token.
	 * @param cDocumentId Document id to fetch specific document.
	 * @param szVersion   version.
	 * @param szToken     Authorization token.
	 * 
	 * @return The binary file of a document of user.
	 * 
	 * @throws InvalidRequestException This exception is thrown when invalid request
	 *                                 is received.
	 * @throws RestClientException     Exception thrown by {@link RestTemplate}
	 *                                 whenever it encounters client-side HTTP
	 *                                 errors.
	 */
	public SimpleNLSResponse createDocument(
	        String szBaseUrl,
	        String cDocumentId,
	        String szVersion,
	        String szToken) throws InvalidRequestException,
	        RestClientException {
		var szPathVariables = Map.of("version", szVersion);
		var szQueryParams   = Map.of("documentid", cDocumentId);
		
		var szEndPointURL = EndPoints.DOCUMENT.buildURL(szBaseUrl, szPathVariables, szQueryParams);
		LOG.info("Create Document Url : " + szEndPointURL);
		
		var cResponse = m_cApiUtils.executeApiRequest(null, HttpMethod.POST, szEndPointURL, szToken,
		        SimpleNLSResponse.class);
		LOG.info("Create Document Response:" + cResponse);
		return cResponse;
	}
}
