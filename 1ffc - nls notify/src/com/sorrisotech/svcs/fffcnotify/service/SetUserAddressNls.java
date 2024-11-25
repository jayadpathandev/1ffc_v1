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
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.client.main.model.request.ContactPreferencesRequest;
import com.sorrisotech.client.main.model.response.ContactPrefrences.ChannelAddress;
import com.sorrisotech.client.main.model.response.ContactPrefrences.Econsent;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.fffcnotify.data.Location;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/******************************************************************************
 * This class sets or deletes the user address values in NLS.
 * 
 * @author Asrar Saloda
 */
public class SetUserAddressNls extends SetUserAddressNlsBase {
	
	/**************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = 5360527968252713653L;
	
	/******************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SetUserAddressNls.class);
	
	/**************************************************************************
	 * 1. Turn the request around. 2. Insert all the configuration parameters. 3.
	 * Return the request with success.
	 */
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;
		
		final String szCustomerId = request.getString(IApiFffcNotify.SetUserAddressNls.customerId);
		
		final String szChannel = request.getString(IApiFffcNotify.SetUserAddressNls.channel);
		
		final String szAddress = request.getString(IApiFffcNotify.SetUserAddressNls.address);
		
		final String szBrowserGeo = request.getString(IApiFffcNotify.SetUserAddressNls.browserGeo);
		
		final String szIpGeolocation = request.getString(IApiFffcNotify.SetUserAddressNls.ipGeo);
		
		final String szIpAddress = request.getString(IApiFffcNotify.SetUserAddressNls.ipAddress);
		
		final String szIsConsentActive = request
		        .getString(IApiFffcNotify.RegisterUserNls.sConsentActive);
		
		CreateContactPrefrencesResponse contactPreferencesofUser = null;
		
		final Location cLocation = Location.getLocation(szBrowserGeo, szIpGeolocation);
		
		String latitude = null;
		
		String longitude = null;
		
		// --------------------------------------------------------------------------------------
		// Log error if failed to get Location.
		if (null == cLocation) {
			LOG.error("SetUserAddressNls:processInternal .... failed to get location for user");
		} else {
			latitude = cLocation.getLatitude();
			longitude = cLocation.getLongitude();
		}
		
		String szDateTime;
		
		try {
			// --------------------------------------------------------------------------------------
			// Converting current date to ISO 8601 format.
			// Note: NLS is using this format.
			szDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
		} catch (Exception e) {
			LOG.error("SetUserAddressNls:processInternal ..... Failed to get current date", e, e);
			request.setToResponse();
			request.setStatus(eReturnCode);
			return eReturnCode;
		}
		
		if (null != szCustomerId) {
			LOG.debug(
			        "SetUserAddressNls:processInternal ..... entered method for customer id: {}, channel: {}, address: {}",
			        szCustomerId, szChannel, szAddress);
			try {
				// --------------------------------------------------------------------------------------
				// Fetching users contact preferences by calling NLS API.
				contactPreferencesofUser = NLSClient.getContactPreferencesofUser(
				        new ContactPreferencesRequest(szCustomerId, szDateTime));
			} catch (Exception e) {
				LOG.error(
				        "SetUserAddress:processInternal ..... failed to get contact prefrences for customer id: {}, date and time: {}",
				        szCustomerId, szDateTime, e, e);
			}
		}
		
		if (null != contactPreferencesofUser) {
			final String lLatitude  = latitude;
			final String lLongitude = longitude;
			
			boolean hasChange = false;
			
			if (null != contactPreferencesofUser.getPayload().getChannelAddresses()) {
				
				// --------------------------------------------------------------------------------------
				// If channel is present at NLS side updating consent address as per request.
				// The hasChange check address is changed or not.
				hasChange = contactPreferencesofUser.getPayload().getChannelAddresses().stream()
				        .filter(cChannelAddress -> cChannelAddress.getChannelName()
				                .equalsIgnoreCase(szChannel))
				        .map(cChannelAddress -> updateChannelAddress(cChannelAddress, szAddress,
				                szDateTime, szIpAddress, lLatitude, lLongitude))
				        .findFirst().orElse(false);
			} else {
				// --------------------------------------------------------------------------------------
				// If channel address is null we are adding the new channel.
				var channelAddressList = new ArrayList<ChannelAddress>();
				
				channelAddressList.add(new ChannelAddress(szChannel, szAddress, szIpAddress,
				        szDateTime, latitude, longitude));
				
				contactPreferencesofUser.getPayload().setChannelAddresses(channelAddressList);
				
				hasChange = true;
				
			}
			
			// --------------------------------------------------------------------------------------
			// Flag that indicates that channel is new or not.
			boolean isNewChannel = !hasChange;
			
			// --------------------------------------------------------------------------------------
			// If channel is not present at NLS side then adding this channel and address
			// and setting consent date as new Date().
			if (isNewChannel) {
				contactPreferencesofUser.getPayload().getChannelAddresses().add(new ChannelAddress(
				        szChannel, szAddress, szIpAddress, szDateTime, latitude, longitude));
				hasChange = true;
			}
			
			if (hasChange) {
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
					        "SetUserAddress:processInternal ..... failed to update contact prefrences for customer id: {}, , Request body: {}",
					        szCustomerId, contactPreferencesofUser.getPayload().toString(), e, e);
				}
			} else {
				LOG.warn(
				        "SetUserAddressNls:processInternal ..... no record found for customer id: {}, channel: {}",
				        szCustomerId, szChannel);
				eReturnCode = ServiceAPIErrorCode.Success;
			}
		}
		
		request.setToResponse();
		request.setStatus(eReturnCode);
		return eReturnCode;
	}
	
	/**********************************************************************************************
	 * This method is used to update channel address request.
	 * 
	 * @param cChannelAddress Channel address request.
	 * @param szAddress       Channel Address.
	 * @param szDateTime      Consent date.
	 * @param szIpAddress     Source IP Address.
	 * @param latitude        Latitude of location.
	 * @param longitude       Longitude of location.
	 * 
	 * @return boolean is value changed or not.
	 */
	private boolean updateChannelAddress(ChannelAddress cChannelAddress, String szAddress,
	        String szDateTime, String szIpAddress, String latitude, String longitude) {
		
		// --------------------------------------------------------------------------------------
		// If the address is null or empty or changed then setting the address to NLS as
		// received and setting source IP address, latitude and longitude and consent
		// date as new Date()
		cChannelAddress.setConsentAddress(szAddress);
		cChannelAddress.setConsentDate(szDateTime);
		cChannelAddress.setSourceIpAddress(szIpAddress);
		cChannelAddress.setLatitude(latitude);
		cChannelAddress.setLongitude(longitude);
		
		return true;
	}
	
}
