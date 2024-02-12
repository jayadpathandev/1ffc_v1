/*
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.client.model.request.AddDocumentRequest;
import com.sorrisotech.client.model.request.CreateSessionRequest;
import com.sorrisotech.client.model.request.LoginRequest;
import com.sorrisotech.client.util.EndPoints;

/******************************************************************************
 * Kinective Esign API clients
 * 
 * @author Rohit Singh
 */
public class KinecticeEsignClient {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KinecticeEsignClient.class);

	/**********************************************************************************************
	 * Constant key for accesstoken headers.
	 */
	private static final String ACCESS_TOKEN = "access-token";
	
	/**********************************************************************************************
	 * Constant key for hostSessionId headers.
	 */
	private static final String HOST_SESSION_ID = "hostSessionId";

	/**********************************************************************************************
	 * Base URL of the API.
	 */
	private final String m_szBaseUrl;

	/**********************************************************************************************
	 * HostFIID for the API. (credentials)
	 */
	private final String m_szHostFIID;

	/**********************************************************************************************
	 * UserId for the API. (credentials)
	 */
    private final String m_szUserID;
    
    /**********************************************************************************************
	 * BusinessAppUserId for the API. (credentials)
	 */
    private final String m_szBusinessAppUserID;
    
    /**********************************************************************************************
	 * PartnerId for the API. (credentials)
	 */
    private final String m_szPartnerID;
    
    /**********************************************************************************************
	 * The API key. (credentials)
	 */
    private final String m_szAPIKey;
    
    /**********************************************************************************************
	 * The default object mapper instance.
	 */
    private final ObjectMapper m_cObjectMapper;
    
    /**********************************************************************************************
	 * The default rest template instance.
	 */
    private final RestTemplate m_cRestTemplate;
    
    /**********************************************************************************************
     * Required args constructor for this class.
     * 
     * @param szBaseUrl
     * @param szHostFIID
     * @param szUserID
     * @param szBusinessAppUserID
     * @param szPartnerID
     * @param szAPIKey
     * @param cObjectMapper
     * @param cRestTemplate
     */
	public KinecticeEsignClient(
			String szBaseUrl,
			String szHostFIID, 
			String szUserID, 
			String szBusinessAppUserID,
			String szPartnerID, 
			String szAPIKey,
			ObjectMapper cObjectMapper,
			RestTemplate cRestTemplate) {
		this.m_szBaseUrl = szBaseUrl;
		this.m_szHostFIID = szHostFIID;
		this.m_szUserID = szUserID;
		this.m_szBusinessAppUserID = szBusinessAppUserID;
		this.m_szPartnerID = szPartnerID;
		this.m_szAPIKey = szAPIKey;
		this.m_cObjectMapper = cObjectMapper;
		this.m_cRestTemplate = cRestTemplate;
	}
	
	/**********************************************************************************************
	 * Returns the accessToken for ffurther API calls.
	 * 
	 * @return accessToken
	 */
	public String login() {
		LoginRequest cLoginRequest = new LoginRequest(
				m_szHostFIID, 
				m_szUserID, 
				m_szBusinessAppUserID, 
				m_szPartnerID, 
				m_szAPIKey
		);
		
		HttpHeaders cHeader = new HttpHeaders();
		cHeader.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		HttpEntity<LoginRequest> cEntity = new HttpEntity<LoginRequest>(
				cLoginRequest, 
				cHeader
		);
		
		String url = EndPoints.LOGIN.buildURL(m_szBaseUrl);
		
		try {
			ResponseEntity<String> cResponse = this.m_cRestTemplate.postForEntity(
					url, 
					cEntity, 
					String.class
			);
			
			if (cResponse.getStatusCode().is2xxSuccessful()) {
				return cResponse.getHeaders().getFirst(ACCESS_TOKEN);
			}
		} catch(RuntimeException ex) {
			LOGGER.error("Error occured while calling URL : {} Exception : {}", url, ex);
		}
		
		return null;
	}

	/**********************************************************************************************
	 * Creates a new session for document esign.
	 * 
	 * @param cSessionRequest
	 * @param szAccessToken
	 * @return hostSessionId
	 */
	public String createNewSession(
			CreateSessionRequest cSessionRequest, 
			String szAccessToken) {
		
		HttpHeaders cHeader = new HttpHeaders();
		cHeader.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(ACCESS_TOKEN, szAccessToken);
		cHeader.setBearerAuth(szAccessToken);
		
		HttpEntity<CreateSessionRequest> cEntity = new HttpEntity<CreateSessionRequest>(
				cSessionRequest, 
				cHeader
		);
		
		String url = EndPoints.CREATE_SESSION.buildURL(m_szBaseUrl);
		
		try {
			ResponseEntity<String> cResponse = this.m_cRestTemplate.postForEntity(
					url, 
					cEntity, 
					String.class
			);
			
			if (cResponse.getStatusCode().is2xxSuccessful()) {
				JsonNode cResponseBody = m_cObjectMapper.readTree(cResponse.getBody());
				String szHostSessionId = cResponseBody.get("HostSessionId").asText();
				return szHostSessionId;
			}

		} catch(Exception ex) {
			LOGGER.error("Error occured while calling URL : {} Exception : {}", url, ex);
		}
		
		return null;
	}
	
	/**********************************************************************************************
	 * Adds the document to be signed to the session.
	 * 
	 * @param cDocumentRequest
	 * @param szAccessToken
	 * @param szHostSessionId
	 * @return
	 */
	public String addDocument(
			AddDocumentRequest cDocumentRequest, 
			String szAccessToken, 
			String szHostSessionId) {
		
		HttpHeaders cHeader = new HttpHeaders();
		cHeader.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HOST_SESSION_ID, szHostSessionId);
		cHeader.set(ACCESS_TOKEN, szAccessToken);
		cHeader.setBearerAuth(szAccessToken);
		
		HttpEntity<AddDocumentRequest> cEntity = new HttpEntity<AddDocumentRequest>(
				cDocumentRequest, 
				cHeader
		);
		
		String url = EndPoints.ADD_DOCUMENT.buildURL(m_szBaseUrl);
		
		try {
			ResponseEntity<String> cResponse = this.m_cRestTemplate.postForEntity(
					url, 
					cEntity, 
					String.class, 
					szHostSessionId
			);
			
			if (cResponse.getStatusCode().is2xxSuccessful()) {
				return cResponse.getBody();
			}
		} catch(Exception ex) {
			LOGGER.error("Error occured while calling URL : {} Exception : {}", url, ex);
		}
		
		return null;
	}
    
	/**********************************************************************************************
	 * Gets the remote status for the document esign
	 * 
	 * @param szAccessToken
	 * @param szHostSessionId
	 * 
	 * @return status of the docment esign
	 */
	public String getRemoteStatus(
			String szAccessToken, 
			String szHostSessionId) {
		
		HttpHeaders cHeader = new HttpHeaders();
		cHeader.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HOST_SESSION_ID, szHostSessionId);
		cHeader.set(ACCESS_TOKEN, szAccessToken);
		cHeader.setBearerAuth(szAccessToken);

		HttpEntity<?> cEntity = new HttpEntity<>(cHeader);
		
		String url = EndPoints.GET_REMOTE_STATUS.buildURL(m_szBaseUrl);
		
		try {
			ResponseEntity<String> cResponse = this.m_cRestTemplate.exchange(
					url, 
					HttpMethod.GET, 
					cEntity, 
					String.class, 
					szHostSessionId
			);
			
			if (cResponse.getStatusCode().is2xxSuccessful()) {
				return cResponse.getBody();
			}
		} catch(Exception ex) {
			LOGGER.error("Error occured while calling URL : {} Exception : {}", url, ex);
		}
		
		return null;
	}
	
	/**********************************************************************************************
	 * Cancels the session
	 * 
	 * @param szAccessToken
	 * @param szHostSessionId
	 * @return true if session canceled sucessfully.
	 */
	public boolean cancelSession(
			String szAccessToken, 
			String szHostSessionId) {
		
		HttpHeaders cHeader = new HttpHeaders();
		cHeader.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		cHeader.set(HOST_SESSION_ID, szHostSessionId);
		cHeader.set(ACCESS_TOKEN, szAccessToken);
		cHeader.setBearerAuth(szAccessToken);

		HttpEntity<?> cEntity = new HttpEntity<>(cHeader);
		
		String url = EndPoints.CANCEL.buildURL(m_szBaseUrl);
		
		try {
			ResponseEntity<String> cResponse = this.m_cRestTemplate.exchange(
					url, 
					HttpMethod.PUT, 
					cEntity, 
					String.class, 
					szHostSessionId
			);
			
			if (cResponse.getStatusCode().is2xxSuccessful()) {
				return true;
			}
		} catch(Exception ex) {
			LOGGER.error("Error occured while calling URL : {} Exception : {}", url, ex);
		}
		
		return false;
	}
	
}
