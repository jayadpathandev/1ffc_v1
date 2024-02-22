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
package com.sorrisotech.fffc.documentesign.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sorrisotech.client.KinectiveEsignClient;
import com.sorrisotech.client.model.request.AddDocumentRequest;
import com.sorrisotech.client.model.request.AddDocumentRequest.DocumentIndex;
import com.sorrisotech.client.model.request.AddDocumentRequest.Field;
import com.sorrisotech.client.model.request.AddDocumentRequest.PartyMapping;
import com.sorrisotech.client.model.request.CreateSessionRequest;
import com.sorrisotech.client.model.request.CreateSessionRequest.Party;
import com.sorrisotech.client.model.request.RemoteSigningRequest;
import com.sorrisotech.client.model.request.RemoteSigningRequest.RemotePartyDetail;

/******************************************************************************
 * Pdf Esign service.
 * 
 * @author Rohit Singh
 */
public class PDFEsignService {
	
	/**********************************************************************************************
	 * The Esign provider client.
	 */
	private KinectiveEsignClient client;
	
	/**********************************************************************************************
	 * The short name of the add document request.
	 * 
	 * Maximum of 20 characters. Can only contain A-Z, a-z, 0-9, and _.
	 */
	private String shortName;
	
	/**********************************************************************************************
	 * The long name of the add document request.
	 * 
	 * Maximum of 100 characters. Can only contain A-Z, a-z, 0-9, and _.
	 */
	private String longName;
	
	/**********************************************************************************************
	 * The description of the add document request.
	 * 
	 * Maximum of 150 characters. This is the user friendly name of the document.
	 */
	private String description;
	
	/**********************************************************************************************
	 * The signature field details and location on the PDF.
	 */
	private Field signatureField;
	
	/**********************************************************************************************
	 * The context for this service.
	 */
	private static ApplicationContext context = new ClassPathXmlApplicationContext("documentEsignServiceContext.xml");
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFEsignService.class);
	
	/**********************************************************************************************
	 * Private constructor for preventing instance creation.
	 */
	private PDFEsignService() {}
	
	/**********************************************************************************************
	 * Instance field of this class
	 */
	private static PDFEsignService instance = null;
	
	/**********************************************************************************************
	 * @return the instance of this class
	 */
	@SuppressWarnings("resource")
	public static PDFEsignService getInstance() {
		if (instance != null) return instance;
		
		instance = (PDFEsignService) context.getBean("pdfService");
		return instance;
	}

	
	/**********************************************************************************************
	 * Creates new esign session.
	 * 
	 * @param parties
	 * @param accessToken
	 * @return session ID
	 */
	public String createNewEsignSession(List<Party> parties, String accessToken) {
		if (parties == null || parties.isEmpty()) {
			LOGGER.debug("Parties are empty unable to create new document session.");
			return null;
		}
		
		final var request = new CreateSessionRequest(parties);
		final var sessionId = client.createNewSession(request, accessToken);
		
		LOGGER.debug("Successfully created session for parties : {}", parties);
		
		return sessionId;
	}
	
	/**********************************************************************************************
	 * Adds the document to the session provided for esign.
	 * 
	 * @param pdfDetails key value pairs
	 * @param sessionId
	 * @param accessToken
	 * @param userFullName
	 * @param accountNumber
	 * @return true if added successfully else false
	 */
	public boolean addDocumentToSession(
			Map<String, String> pdfDetails, 
			String sessionId, 
			String accessToken,
			String userFullName, 
			Map<String, String> metadata) {
		
		LOGGER.debug(
				"Adding document to session, pdfDetails : {}, sessionId : {}, accessToken : {}, userFullName : {}, metadata : {}", 
				pdfDetails, 
				sessionId, 
				accessToken,
				userFullName,
				metadata
		);
		
		PDFDocument document = (PDFDocument) context.getBean("pdfDocument");
		
		final var filledDocument = this.fillPDFDetails(pdfDetails, document);
		if (filledDocument == null) {
			LOGGER.debug("Unable to get the PDF data");
			return false;
		}
		
		final var base64Document = Base64.getEncoder().encodeToString(filledDocument);
		
		final var addDocumentRequest = new AddDocumentRequest(
				shortName, 
				longName, 
				description, 
				base64Document
		);
		
		final var partyMappingDoc = new PartyMapping(
				userFullName, 
				"P1"
		);
		
		
		addDocumentRequest.m_cPartyMappings = List.of(partyMappingDoc);
		addDocumentRequest.m_cFields = List.of(signatureField);
		addDocumentRequest.m_bFlatten = false;
		
		if (metadata != null && !metadata.isEmpty()) {
			addDocumentRequest.m_cDocumentIndexes = metadata.entrySet().stream()
					.map(entry -> new DocumentIndex(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());
		}
		
		LOGGER.debug(
				"Created request for adding document to session : {} => {}", 
				sessionId, 
				addDocumentRequest
		);
		
		final var response = client.addDocument(
				addDocumentRequest, 
				accessToken, 
				sessionId
		);
		
		if (response) {
			LOGGER.debug("Document added successfully!");
		} else {
			LOGGER.debug("Unable to add document to the session.");
		}

		return response;
	}
	
	/**********************************************************************************************
	 * Gets the esign URL for the provided session.
	 * 
	 * @param sessionId
	 * @param accessToken
	 * @param fullName
	 * @param email
	 * @return the esign URL using which user will sign the document.
	 */
	public String getEsignUrlForSession(
			String sessionId, 
			String accessToken,
			String fullName, 
			String email) {
		
		LOGGER.debug(
				"Creating esign URL, sessionId : {}, accessToken : {}, userFullName : {}, email : {}", 
				sessionId, 
				accessToken,
				fullName,
				email
		);
		
		final var request = new RemoteSigningRequest(
				List.of(new RemotePartyDetail(fullName, email))
		);
		
		final var response = client.getRemoteSigningUrl(accessToken, sessionId, request);
		
		return response;
	}
	
	/**********************************************************************************************
	 * Returns the Esign status of the session.
	 * 
	 * @param sessionId
	 * @return esign status
	 */
	public String getEsignStatus(String sessionId) {
		
		LOGGER.debug(
				"Getting esign status, sessionId : {}", 
				sessionId
		);
		final var accessToken = client.login();
		
		final var response = client.getRemoteStatus(accessToken, sessionId);
		
		LOGGER.debug("Esign status for sessionId : {} => {}", sessionId, response);
		
		return response;
	}
	
	/**********************************************************************************************
	 * Cancels the provided session.
	 * 
	 * @param sessionId
	 * @return true if cancelled successfully else false.
	 */
	public boolean cancelSession(String sessionId) {
		
		LOGGER.debug(
				"Canceling session : sessionId : {}", 
				sessionId
		);
		
		final var accessToken = client.login();
		
		final var response = client.cancelSession(accessToken, sessionId);
		
		if (response) {
			LOGGER.debug("Session : {}  cancelled successfully!", sessionId);
		} else {
			LOGGER.debug("Unable to cancel session : {}", sessionId);
		}
		
		return response;
	}
	
	/**********************************************************************************************
	 * Returns access token and logs if not getting access token.
	 * 
	 * @return access token
	 */
	public String getAccessToken() {
		final var accessToken = client.login();
		
		if (accessToken == null) { 
			LOGGER.debug("Unable to retrieve the access token at the moment");
		}
		
		return accessToken;
	}
	
	/**********************************************************************************************
	 * Helper method that will fill the document detials to the template docuemnt.
	 * 
	 * @param pdfDetails
	 * @return bytes of the filled document
	 */
	private byte[] fillPDFDetails(Map<String, String> pdfDetails, PDFDocument templatePdf) {
		
		for (var entry : pdfDetails.entrySet()) {
			try {
				templatePdf.setFieldValue(entry.getKey(), entry.getValue());
			} catch (IOException e) {
				LOGGER.error("Error occured while populating the PDF template : {}", e);
			}
		}
		
		try {
			return templatePdf.getBytes();
		} catch (IOException e) {
			LOGGER.error("Error occured while getting filled PDF : {}", e);
		}
		
		return null;
	}
	
	// setters goes here
	
	/**********************************************************************************************
	 * sets the esign client.
	 * @param client
	 */
	public void setClient(KinectiveEsignClient client) {
		this.client = client;
	}

	/**********************************************************************************************
	 * sets the short name.
	 * @param shortName
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**********************************************************************************************
	 * sets the long name.
	 * @param longName
	 */
	public void setLongName(String longName) {
		this.longName = longName;
	}

	/**********************************************************************************************
	 * sets the description.
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**********************************************************************************************
	 * sets the signatureField details.
	 * @param signatureField
	 */
	public void setSignatureField(Field signatureField) {
		this.signatureField = signatureField;
	}

}
