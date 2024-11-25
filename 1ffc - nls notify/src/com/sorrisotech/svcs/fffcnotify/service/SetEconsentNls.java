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
package com.sorrisotech.svcs.fffcnotify.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.client.main.model.request.ContactPreferencesRequest;
import com.sorrisotech.client.main.model.response.ContactPrefrences.Econsent;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/******************************************************************************
 * This class sets E-consent values in NLS.
 * 
 * @author Asrar Saloda
 */
public class SetEconsentNls extends SetEconsentNlsBase {
	
	/**************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = 885781244342760164L;
	
	/******************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SetEconsentNls.class);
	
	/**************************************************************************
	 * 1. Turn the request around. 2. Insert all the configuration parameters. 3.
	 * Return the request with success.
	 */
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;
		
		final String szCustomerId = request.getString(IApiFffcNotify.SetUserAddressNls.customerId);
		
		final String szIsConsentActive = request
		        .getString(IApiFffcNotify.RegisterUserNls.sConsentActive);
		
		CreateContactPrefrencesResponse contactPreferencesofUser = null;
		
		String szDateTime;
		
		try {
			// --------------------------------------------------------------------------------------
			// Converting current date to ISO 8601 format.
			// Note: NLS is using this format.
			szDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
		} catch (Exception e) {
			LOG.error("SetEconsentNls:processInternal ..... Failed to get current date", e, e);
			request.setToResponse();
			request.setStatus(eReturnCode);
			return eReturnCode;
		}
		
		if (null != szCustomerId) {
			LOG.debug(
			        "SetEconsentNls:processInternal ..... entered method for customer id: {}, consent: {}",
			        szCustomerId, szIsConsentActive);
			try {
				// --------------------------------------------------------------------------------------
				// Fetching users contact preferences by calling NLS API.
				contactPreferencesofUser = NLSClient.getContactPreferencesofUser(
				        new ContactPreferencesRequest(szCustomerId, szDateTime));
			} catch (Exception e) {
				LOG.error(
				        "SetUserAddress:processInternal ..... failed to get contact prefrences for customer id: {}",
				        szCustomerId, e, e);
			}
		}
		
		if (null != contactPreferencesofUser) {
			
			try {
				// --------------------------------------------------------------------------------------
				// Setting new Date() as an date time in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
				contactPreferencesofUser.getPayload().setDateTime(szDateTime);
				
				// --------------------------------------------------------------------------------------
				// Setting consent status and last updated date as current date.
				contactPreferencesofUser.getPayload().setEconsent(
				        new Econsent(Boolean.parseBoolean(szIsConsentActive), szDateTime));
				
				NLSClient.updateContactPreferences(contactPreferencesofUser.getPayload());
				eReturnCode = ServiceAPIErrorCode.Success;
			} catch (Exception e) {
				LOG.error(
				        "SetEconsentNls:processInternal ..... failed to update econsent data for customer id: {}, Request body: {}",
				        szCustomerId, contactPreferencesofUser.getPayload().toString(), e, e);
			}
		} else {
			LOG.warn("SetEconsentNls:processInternal ..... no record found for customer id: {}",
			        szCustomerId);
			eReturnCode = ServiceAPIErrorCode.Success;
		}
		
		request.setToResponse();
		request.setStatus(eReturnCode);
		return eReturnCode;
	}
	
}
