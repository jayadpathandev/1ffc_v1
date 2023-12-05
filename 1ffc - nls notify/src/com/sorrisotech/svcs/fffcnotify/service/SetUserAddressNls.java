/*
 * (c) Copyright 2017-2021 Sorriso Technologies, Inc(r), All Rights Reserved,
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
import com.sorrisotech.client.main.model.response.ContactPrefrences.ChannelAddress;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.fffcnotify.dao.FffcNotificationDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/******************************************************************************
 * This class sets or deletes the user address values in NLS.
 * 
 * @author Asrar Saloda
 */
public class SetUserAddressNls extends SetUserAddressNLsBase {

	/**************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = 5360527968252713653L;

	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SetUserAddressNls.class);

	private static final FffcNotificationDao m_cDaoFffc = FffcNotificationDao.get();

	/**************************************************************************
	 * 1. Turn the request around. 2. Insert all the configuration parameters. 3.
	 * Return the request with success.
	 */
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;

		final String szUserId = request.getString(IApiFffcNotify.SetUserAddressNLs.userid);

		final String szChannel = request.getString(IApiFffcNotify.SetUserAddressNLs.channel);

		final String szAddress = request.getString(IApiFffcNotify.SetUserAddressNLs.address);

		CreateContactPrefrencesResponse contactPreferencesofUser = null;

		String szDateTime = null;

		try {
			szDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
		} catch (Exception e) {
			LOG.error("SetUserAddressNls:processInternal ..... Failed to get current date");
			request.setToResponse();
			request.setStatus(eReturnCode);
			return eReturnCode;
		}
		
		// --------------------------------------------------------------------------------------
		// Fetching orgId (customerId) using userId from tm_account table.
		final String szCustomerId = m_cDaoFffc.queryOrgId(szUserId);

		if (null != szCustomerId && null != szDateTime) {
			LOG.debug(
					"SetUserAddressNls:processInternal ..... entered method for customer id: {}, channel: {}, address: {}",
					szCustomerId, szChannel, szAddress);
			try {
				// --------------------------------------------------------------------------------------
				// Fetching users contact preferences by calling NLS API.
				contactPreferencesofUser = NLSClient
						.getContactPreferencesofUser(new ContactPreferencesRequest(szCustomerId, szDateTime));
			} catch (Exception e) {
				LOG.error("SetUserAddress:processInternal ..... failed to get contact prefrences for customer id: {}",
						szCustomerId, e, e);
			}
		}

		if (null != contactPreferencesofUser) {

			boolean hasChange = false;

			boolean isNewChannel = true;

			for (ChannelAddress cChannelAddress : contactPreferencesofUser.getPayload().getChannelAddresses()) {
				// --------------------------------------------------------------------------------------
				// If channel is present at NLS side updating consent address as per request.
				if (cChannelAddress.getChannelName().equalsIgnoreCase(szChannel)) {
					if (szAddress == null || szAddress.isBlank()) {
						cChannelAddress.setConsentAddress(szAddress);
						hasChange = true;
					} else if (!cChannelAddress.getConsentAddress().equals(szAddress)) {
						cChannelAddress.setConsentAddress(szAddress);
						hasChange = true;
					}
					isNewChannel = false;
					break;
				}
			}

			// --------------------------------------------------------------------------------------
			// If channel is not present at NLS side then adding this channel and address
			// and setting consent new Date() and remaining field is kept null as of now
			// TODO Need to confirm.
			if (isNewChannel) {
				contactPreferencesofUser.getPayload().getChannelAddresses()
						.add(new ChannelAddress(szChannel, szAddress, null, szDateTime, null, null));
				hasChange = true;
			}

			if (hasChange) {
				try {
					// --------------------------------------------------------------------------------------
					// Setting new Date() as an date time in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
					contactPreferencesofUser.getPayload().setDateTime(szDateTime);
					NLSClient.updateContactPreferences(contactPreferencesofUser.getPayload());
					eReturnCode = ServiceAPIErrorCode.Success;
				} catch (Exception e) {
					LOG.error(
							"SetUserAddress:processInternal ..... failed to update contact prefrences for customer id: {}",
							szCustomerId, e, e);
				}
			} else {
				LOG.warn("SetUserAddressNls:processInternal ..... no record found for customer id: {}, channel: {}",
						szCustomerId, szChannel);
				eReturnCode = ServiceAPIErrorCode.Success;
			}
		}

		request.setToResponse();
		request.setStatus(eReturnCode);
		return eReturnCode;
	}

}
