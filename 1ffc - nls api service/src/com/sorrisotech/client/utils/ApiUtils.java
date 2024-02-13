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
package com.sorrisotech.client.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/******************************************************************************
 * Utility class.
 * 
 * @author Asrar Saloda
 */
public class ApiUtils {
	
	/**********************************************************************************************
	 * Bearer constant to make bearer token.
	 */
	private static final String BEARER_ = "Bearer ";
	
	/**********************************************************************************************
	 * RestTemplate for calling external APIs
	 */
	private RestTemplate m_cRestTemplate;
	
	/**********************************************************************************************
	 * Initialize the member variables.
	 */
	public ApiUtils() {
		
		m_cRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		
		// --------------------------------------------------------------------------------------
		// Removing MappingJackson2XmlHttpMessageConverter due to XML format conversion,
		// while our NLS server only accepts JSON format.
		// Note: Jackson-dataformat-XML dependency is in some projects.
		m_cRestTemplate.getMessageConverters()
		        .removeIf(converter -> converter instanceof MappingJackson2XmlHttpMessageConverter);
		
	}
	
	/**********************************************************************************************
	 * This generic function accepts parameters such as request, path variables,
	 * HTTP method, end point URL, and response class type. It then makes a
	 * third-party REST call using the RestTemplate and returns the response.
	 * 
	 * @param cRequestPayload    The request to issue.
	 * @param cHttpMethod        HTTP request method.
	 * @param szEndpointUrl      End point URL
	 * @param szToken            User token.
	 * @param cResponseTypeClass Response type class.
	 * 
	 * @return The result of the operation with status.
	 * 
	 * @throws RestClientException Exception thrown by {@link RestTemplate} whenever
	 *                             it encounters client-side HTTP errors.
	 */
	public <RequestType, ResponseType> ResponseEntity<ResponseType> executeApiRequest(
	        RequestType cRequestPayload,
	        HttpMethod cHttpMethod,
	        String szEndpointUrl,
	        String szToken,
	        Class<ResponseType> cResponseTypeClass) throws RestClientException {
		
		// --------------------------------------------------------------------------------------
		// Used to create HTTP headers.
		var cHeaders = new HttpHeaders();
		cHeaders.setContentType(MediaType.APPLICATION_JSON);
		if (szToken != null)
			cHeaders.set(HttpHeaders.AUTHORIZATION, BEARER_ + szToken);
			
		// --------------------------------------------------------------------------------------
		// Create a new HttpEntity with the given body and headers.
		var entity = new HttpEntity<RequestType>(cRequestPayload, cHeaders);
		
		// --------------------------------------------------------------------------------------
		// Execute the HTTP method to the given URI template, writing the given request
		// entity to the request, and returns the response as ResponseEntity.
		
		var	cResponse = m_cRestTemplate.exchange(szEndpointUrl, cHttpMethod, entity,
			        cResponseTypeClass);
		return cResponse;
	}
}
