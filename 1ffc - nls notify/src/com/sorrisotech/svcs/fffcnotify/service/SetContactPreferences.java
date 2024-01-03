/*
 * (c) Copyright 2017-2023 Sorriso Technologies, Inc(r), All Rights Reserved,
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.persona.notification.preferences.api.IPreferences;
import com.sorrisotech.persona.notification.preferences.api.PreferencesFactory;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.notifications.service.NotificationsDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/************************************************************************************************
 * This class sets the users notification channel setting (enable/disable
 * channel) and users notification channel addresses to database.
 * 
 * @author Asrar Saloda.
 */
public class SetContactPreferences extends SetContactPreferencesBase {
	
	/**********************************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = 4450142810218982382L;
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SetContactPreferences.class);
	
	/**************************************************************************
	 * JSON ObjectMapper.
	 */
	private static ObjectMapper m_cObjectMapper = new ObjectMapper();
	
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
		
		final String szUserId = request.getString(IApiFffcNotify.SetContactPreferences.userid);
		
		// TODO Need to confirm about validation of date.
		// As per discussion as of now we are not doing validation of date.
		@SuppressWarnings("unused")
		final String szDateTime = request.getString(IApiFffcNotify.SetContactPreferences.dateTime);
		
		final String szChannelAddresses = request
		        .getString(IApiFffcNotify.SetContactPreferences.channelAddresses);
		
		final String szTopicPreferences = request
		        .getString(IApiFffcNotify.SetContactPreferences.topicPrefrences);
		
		LOG.debug("SetContactPreferences:processInternal ..... entered method");
		
		request.setToResponse();
		
		try {
			
			// --------------------------------------------------------------------------------------
			// Converting userId into BigDecimal
			final BigDecimal cUserId = new BigDecimal(szUserId);
			
			// --------------------------------------------------------------------------------------
			// Getting instance of IPreferences interface.
			final IPreferences cPreferences = PreferencesFactory.getPreferences();
			
			// --------------------------------------------------------------------------------------
			// Converting channelAddress JSON into java objects.
			final ContactPrefrences.ChannelAddress[] cChannelAddressesList = m_cObjectMapper
			        .readValue(szChannelAddresses, ContactPrefrences.ChannelAddress[].class);
			
			// --------------------------------------------------------------------------------------
			// Converting topicPreferences JSON into java objects.
			final List<ContactPrefrences.TopicPreference> cTopicPreferencesList = m_cObjectMapper
			        .readValue(szTopicPreferences,
			                new TypeReference<List<ContactPrefrences.TopicPreference>>() {
			                });
			
			// Validating the topic preferences
			if (!validateRequest(cTopicPreferencesList)) {
				request.set(IApiFffcNotify.SetContactPreferences.status, "validationError");
				request.setStatus(ServiceAPIErrorCode.Success);
				return eReturnCode;
			}
			
			// --------------------------------------------------------------------------------------
			// Saving all channel addresses of user. If we receive null or empty channel
			// address we are deleting channel address and if we receive channel address
			// other then null or empty then we are adding/updating that channel address to
			// the database.
			saveUserAddresses(cUserId, cChannelAddressesList, cPreferences);
			
			// --------------------------------------------------------------------------------------
			// Saving all topic preferences of user to databse as per user setting received
			// from JSON request.
			saveUserTopicPreferences(cUserId, cTopicPreferencesList, cPreferences);
			
			request.set(IApiFffcNotify.SetContactPreferences.status, "good");
			
			eReturnCode = ServiceAPIErrorCode.Success;
			
			LOG.debug(
			        "SetContactPreferences:processInternal ..... success for userId: {}" + cUserId);
			
		} catch (Exception e) {
			request.set(IApiFffcNotify.SetContactPreferences.status, "error");
			LOG.error(
			        "SetContactPreferences:processInternal ..... An exception was thrown while setting contact preferences to database",
			        e, e);
		}
		
		request.setStatus(eReturnCode);
		
		return eReturnCode;
	}
	
	/*********************************************************************************
	 * This method can be used to save users channel address in auth_user_address
	 * table based on userId
	 */
	private static void saveUserAddresses(final BigDecimal cUserId,
	        final ContactPrefrences.ChannelAddress[] cChannelAddressesList,
	        final IPreferences cPreferences) {
		
		for (ContactPrefrences.ChannelAddress channelAddress : cChannelAddressesList) {
			
			boolean bIsSuccess = false;
			
			if (channelAddress.getConsentAddress() == null
			        || channelAddress.getConsentAddress().isBlank()) {
				// -------------------------------------------------------------------------------
				// Deleting address form AUTH_USER_ADDRESS table if address id null or empty
				bIsSuccess = cPreferences.clearAddress(cUserId, channelAddress.getChannelName());
			} else {
				// -------------------------------------------------------------------------------
				// Adding address form AUTH_USER_ADDRESS table by doing '\n' encoding.
				bIsSuccess = cPreferences.setAddress(cUserId, channelAddress.getChannelName(),
				        channelAddress.getConsentAddress().replaceAll("\n", "\\n"));
			}
			
			if (!bIsSuccess) {
				LOG.error("Failed to update" + " notification channel consent address: "
				        + channelAddress.getConsentAddress() + " for channel: "
				        + channelAddress.getChannelName() + " for user: " + cUserId);
			} else {
				LOG.debug("Successfully updated" + " notification channel consent address: "
				        + channelAddress.getConsentAddress() + " for channel: "
				        + channelAddress.getChannelName() + " for user: " + cUserId);
			}
		}
		
	}
	
	/*************************************************************************************
	 * This method can be used to save users topic preferences in auth_user_channel
	 * table based on userId
	 */
	private static void saveUserTopicPreferences(final BigDecimal cUserId,
	        final List<ContactPrefrences.TopicPreference> cTopicPreferencesList,
	        final IPreferences cPreferences) {
		
		for (ContactPrefrences.TopicPreference topicPreferences : cTopicPreferencesList) {
			
			topicPreferences.getTopicChannels().forEach(topicChannel -> {
				
				boolean bIsSuccess = false;
				
				// --------------------------------------------------------------------------------------
				// enabling or disabling the channels based on user configurations(channel is
				// selected or not)
				bIsSuccess = topicChannel.isSelected()
				        ? cPreferences.enable(cUserId, topicPreferences.getTopicName(),
				                topicChannel.getChannelName())
				        : cPreferences.disable(cUserId, topicPreferences.getTopicName(),
				                topicChannel.getChannelName());
				if (!bIsSuccess) {
					LOG.error("Failed to " + (topicChannel.isSelected() ? "enable" : "disable")
					        + " notification channel: " + topicChannel.getChannelName()
					        + " for topic: " + topicPreferences.getTopicName() + " for user: "
					        + cUserId);
				} else {
					LOG.debug("Successfully " + (topicChannel.isSelected() ? "enabled" : "disabled")
					        + " notification channel: " + topicChannel.getChannelName()
					        + " for topic: " + topicPreferences.getTopicName() + " for user: "
					        + cUserId);
				}
			});
			
		}
		
	}
	
	private static boolean validateRequest(
	        List<ContactPrefrences.TopicPreference> szTopicPreferences) {
		
		// --------------------------------------------------------------------------------------
		// Fetching all the topic channels.
		final var cTopicChannelsList = m_cDao.queryTopicChannels();
		
		boolean isValidRequest = true;
		
		List<String> errorsList = new ArrayList<>();
		
		for (ContactPrefrences.TopicPreference topicPreference : szTopicPreferences) {
			
			for (ContactPrefrences.TopicChannel channel : topicPreference.getTopicChannels()) {
				
				boolean hasTopicChannelPair = cTopicChannelsList.stream().anyMatch(
				        topicChannel -> topicChannel.topic().equals(topicPreference.getTopicName())
				                && topicChannel.channel().equals(channel.getChannelName()));
				
				if (!hasTopicChannelPair) {
					isValidRequest = false;
					errorsList.add(new StringJoiner("/", "\n",
					        " - Topic or Channel name not found or is invalid")
					        .add(topicPreference.getTopicName()).add(channel.getChannelName())
					        .toString());
				} else {
					boolean isHardCodedViolation = cTopicChannelsList.stream()
					        .filter(topicChannel -> topicChannel.topic()
					                .equals(topicPreference.getTopicName())
					                && topicChannel.channel().equals(channel.getChannelName()))
					        .anyMatch(topicChannel -> {
						        try {
							        return !topicChannel.hasHardcoded()
							                || !(topicChannel.isHardcoded() ^ channel.isSelected());
						        } catch (Exception e) {
							        return false;
						        }
					        });
					
					if (!isHardCodedViolation) {
						isValidRequest = false;
						errorsList.add(new StringJoiner("/", "\n",
						        " - Not allowed to change hardcoded values")
						        .add(topicPreference.getTopicName()).add(channel.getChannelName())
						        .toString());
					}
				}
				
			}
			
		}
		
		if (!errorsList.isEmpty()) {
			LOG.error("Bad request for topic/channel: " + errorsList.toString());
		}
		
		return isValidRequest;
	}
	
}
