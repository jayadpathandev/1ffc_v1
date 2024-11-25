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
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.client.main.model.request.ContactPreferencesRequest;
import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicChannel;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicPreference;
import com.sorrisotech.client.main.model.response.CreateContactPrefrencesResponse;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.notifications.data.UserConfig;
import com.sorrisotech.svcs.notifications.service.NotificationsDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/************************************************************************************************
 * This class sets the users notification channel setting (enable/disable
 * channel) to NLS.
 * 
 * @author Asrar Saloda.
 */
public class SetContactSettingsNls extends SetContactSettingsNlsBase {

	/**********************************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = -1869814306839688997L;

	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SetContactSettingsNls.class);

	/**********************************************************************************************
	 * Dao instance for accessing the database.
	 */
	private static final NotificationsDao m_cDao = NotificationsDao.get();

	/**************************************************************************
	 * 1. Turn the request around. 2. Insert all the configuration parameters. 3.
	 * Return the request with success.
	 */
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;

		final String szCustomerId = request.getString(IApiFffcNotify.SetContactSettingsNls.customerId);

		final String szJsonConfig = request.getString(IApiFffcNotify.SetContactSettingsNls.jsonConfig);

		CreateContactPrefrencesResponse cContactPreferencesofUser = null;

		ContactPrefrences cContactPrefrencesRequest = null;

		LOG.debug("SetContactSettingsNls:processInternal ..... entered method for customer id: {}, JSON config: {}",
				szCustomerId, szJsonConfig);
		try {

			final String szDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());

			// --------------------------------------------------------------------------------------
			// Fetching all the topic channels.
			final var cTopicChannelsList = m_cDao.queryTopicChannels();

			// --------------------------------------------------------------------------------------
			// Passing the input (szJsonConfig) and result of queryTopicChannels to
			// UserConfig class to create user configuration.
			final var cUserConfig = new UserConfig(szJsonConfig, cTopicChannelsList);

			// --------------------------------------------------------------------------------------
			// Fetching users contact preferences by calling NLS API.
			cContactPreferencesofUser = NLSClient
					.getContactPreferencesofUser(new ContactPreferencesRequest(szCustomerId, szDateTime));

			var cTopicPreferenceRequest = new ArrayList<TopicPreference>();

			cUserConfig.topics().forEach(topic -> {
				// --------------------------------------------------------------------------------------
				// Creating map object for channel is selected or not for particular topic where
				// key is channel name and value is true/false based on users configuration.
				var cUserChannels = cUserConfig.usersTopicConfig(topic);

				var cTopicChannelsRequest = new ArrayList<ContactPrefrences.TopicChannel>();

				cUserChannels.entrySet().forEach((entry) -> {

					// --------------------------------------------------------------------------------------
					// adding channel in request to enable or disable the channels based on user
					// configurations(channel is selected or not)
					cTopicChannelsRequest.add(new TopicChannel(entry.getKey(), entry.getValue()));

				});
				cTopicPreferenceRequest.add(new TopicPreference(topic, cTopicChannelsRequest));
			});

			// --------------------------------------------------------------------------------------
			// Creating contact preference request with updated contact preference of user
			// (szJsonConfig) to send contact preferences setting to NLS.
			cContactPrefrencesRequest = new ContactPrefrences(szCustomerId, szDateTime,
			        cContactPreferencesofUser.getPayload().getChannelAddresses(),
			        cTopicPreferenceRequest, cContactPreferencesofUser.getPayload().getEconsent());
			
			// --------------------------------------------------------------------------------------
			// Sending notification preferences of user to NLS by calling NLS API with
			// updated topic preferences.
			var cUpdatePrefrencesResponse = NLSClient.updateContactPreferences(cContactPrefrencesRequest);

			if (cUpdatePrefrencesResponse.getSuccess()) {
				LOG.debug(
						"SetContactSettings:processInternal ..... success: " + cUpdatePrefrencesResponse.getPayload());
				eReturnCode = ServiceAPIErrorCode.Success;
			}

		} catch (Exception e) {
			LOG.error(
					"SetContactSettingsNls:processInternal ..... failed to update contact prefrences for customer id: {}, Request body: {}",
					szCustomerId, cContactPrefrencesRequest.toString(), e, e);
		}

		request.setToResponse();
		request.setStatus(eReturnCode);

		return eReturnCode;
	}

}
