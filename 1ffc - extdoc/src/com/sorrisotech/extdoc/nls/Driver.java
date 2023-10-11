/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 400 West Cummings Park, Suite 1725-184 Woburn, MA
 * 01801 +1.978.635.3900
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
package com.sorrisotech.extdoc.nls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.extdoc.driver.Document;
import com.sorrisotech.extdoc.driver.IDriver;

public class Driver implements IDriver {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Driver.class);
	
	/**********************************************************************************************
	 * Initialize the member variables.
	 */
	@Override
	public boolean initialize(File cDriverDirectory, File cDriverInitialization) {
		
		try {
			
			Properties      cProperties      = new Properties();
			FileInputStream cFileInputStream = new FileInputStream(cDriverInitialization);
			cProperties.load(cFileInputStream);
			cFileInputStream.close();
			
			final var szBaseUrl      = cProperties.getProperty("nls.api.base.url");
			final var szClientId     = cProperties.getProperty("nls.api.client.id");
			final var szClientSecret = cProperties.getProperty("nls.api.client.secret");
			final var szVersion      = cProperties.getProperty("nls.api.client.version");
			
			return NLSClient.init(szBaseUrl, szClientId, szClientSecret, szVersion);
			
		} catch (FileNotFoundException e) {
			LOG.error("Configuraiton file doesn't exist: " + cDriverInitialization.getPath(), e);
			return false;
		} catch (IOException e) {
			LOG.error("Error loading configuration file: " + cDriverInitialization.getPath(), e);
			return false;
		}
	}
	
	/**********************************************************************************************
	 * Used to retrieve the document from NLS Server using document id.
	 * 
	 * @param szDocumentId         Document id of invoice.
	 * @param szExternalDocumentId
	 * @param szDocumentPart
	 * 
	 * @return The document.
	 */
	@Override
	public Document getDocument(
	        String szDocumentId,
	        String szExternalDocumentId,
	        String szDocumentPart) {
		
		String szMimeType = "application/pdf";
		byte[] data       = null;
		
		try {
			var createDocumentResponse = NLSClient.getDocument(szExternalDocumentId);
			data = Base64.getDecoder().decode(createDocumentResponse.getPayload());
		} catch (Exception e) {
			LOG.error("Error in calling NLS service : " + e.getMessage(), e);
		}
		
		if (data == null)
			return null;
		
		Document doc = new Document(szMimeType, data);
		return doc;
	}
	
	public static void main(String[] args) {
		
		Driver   driver = new Driver();
		Document doc    = driver.getDocument("", "", "");
		System.out.println("Mime Type: " + doc.getMimeType());
		System.out.println("Doc Size: " + doc.getDocumentContent().length);
	}
}
