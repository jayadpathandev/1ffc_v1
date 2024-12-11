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
package com.sorrisotech.svcs.fffcnotify.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.client.main.model.response.ContactPrefrences.ChannelAddress;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicChannel;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicPreference;

/**************************************************************************
 * Utility class for contact preferences.
 * 
 * @author Asrar Saloda
 */
public class ContactPreferencesUtil {
	
	/******************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ContactPreferencesUtil.class);
	
	/************************************************************************************************
	 * Cleans the list of TopicPreference objects by removing: 1. TopicPreference
	 * objects with an empty or null topic name. 2. TopicChannel objects with an
	 * empty or null channel name within each TopicPreference. 3. ChannelAddress
	 * objects with an empty or null channel name within the list.
	 *
	 * @param ContactPrefrences Contact preferences setting List of TopicPreference
	 *                          objects to clean and List of ChannelAddress objects
	 *                          to clean.
	 * @param szCustomerId      ID of the customer for logging purposes.
	 */
	public static void cleanPayload(
	        final String szCustomerId,
	        ContactPrefrences contactPrefrences) {
		cleanTopicPreferences(szCustomerId, contactPrefrences.getTopicPreferences());
		cleanChannelAddresses(szCustomerId, contactPrefrences.getChannelAddresses());
	}
	
	/************************************************************************************************
	 * Cleans the list of TopicPreference objects by removing: 1. TopicPreference
	 * objects with an empty or null topic name. 2. TopicChannel objects with an
	 * empty or null channel name within each TopicPreference.
	 *
	 * @param szCustomerId      ID of the customer for logging purposes.
	 * @param cTopicPreferences List of TopicPreference objects to clean.
	 */
	private static void cleanTopicPreferences(
	        final String szCustomerId,
	        List<TopicPreference> cTopicPreferences) {
		if (null == cTopicPreferences || cTopicPreferences.isEmpty()) {
			LOG.info(
			        "ContactPreferencesUtil:cleanTopicPreferences .... TopicPreferences list is null and cannot be cleaned for customer: {}",
			        szCustomerId);
			return;
		}
		
		cTopicPreferences.removeIf(topicPreference -> {
			// ----------------------------------------------------------------------------------
			// Clean topic channels with empty or null channel names
			List<TopicChannel> channels = topicPreference.getTopicChannels();
			if (channels != null) {
				channels.removeIf(channel -> {
					if (isNullOrEmpty(channel.getChannelName())) {
						LOG.error(
						        "ContactPreferencesUtil:cleanTopicPreferences .... Removed TopicChannel with empty or null channel name: {}, for topic: {}, for customer: {}",
						        channel, topicPreference.getTopicName(), szCustomerId);
						return true;
					}
					return false;
				});
			}
			
			// ----------------------------------------------------------------------------------
			// Removing TopicPreference if topic name is null/empty or if all channels are
			// removed.
			if (isNullOrEmpty(topicPreference.getTopicName())) {
				LOG.error(
				        "ContactPreferencesUtil:cleanTopicPreferences .... Removed TopicPreference with empty or null topic name: {}, for customer: {}",
				        topicPreference, szCustomerId);
				return true;
			}
			
			if (channels != null && channels.isEmpty()) {
				LOG.error(
				        "ContactPreferencesUtil:cleanTopicPreferences .... Removed TopicPreference as all its TopicChannels were removed: {}, for customer: {}",
				        topicPreference, szCustomerId);
				return true;
			}
			
			return false;
		});
	}
	
	/************************************************************************************************
	 * Cleans the list of ChannelAddress objects by removing objects with an empty
	 * or null channel name.
	 *
	 * @param customerId        ID of the customer for logging purposes.
	 * @param cChannelAddresses List of ChannelAddress objects to clean.
	 */
	private static void cleanChannelAddresses(
	        final String szCustomerId,
	        List<ChannelAddress> channelAddresses) {
		if (null == channelAddresses || channelAddresses.isEmpty()) {
			LOG.info(
			        "ContactPreferencesUtil:cleanChannelAddresses .... ChannelAddresses list is null and cannot be cleaned for customer: {}",
			        szCustomerId);
			return;
		}
		
		channelAddresses.removeIf(channelAddress -> {
			if (isNullOrEmpty(channelAddress.getChannelName())) {
				LOG.error(
				        "ContactPreferencesUtil:cleanChannelAddresses .... Removed ChannelAddress with empty or null channel name: {}, for customer: {}",
				        channelAddress, szCustomerId);
				return true;
			}
			return false;
		});
	}
	
	/************************************************************************************************
	 * Helper method to check if a string is null or empty (after trimming).
	 *
	 * @param szInputString The string to check.
	 * @return true if the string is null or empty, false otherwise.
	 */
	private static boolean isNullOrEmpty(String szInputString) {
		return szInputString == null || szInputString.trim().isEmpty();
	}
}
