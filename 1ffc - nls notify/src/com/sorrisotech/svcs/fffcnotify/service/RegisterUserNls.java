package com.sorrisotech.svcs.fffcnotify.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.client.main.model.response.ContactPrefrences;
import com.sorrisotech.client.main.model.response.ContactPrefrences.ChannelAddress;
import com.sorrisotech.client.main.model.response.ContactPrefrences.Econsent;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicChannel;
import com.sorrisotech.client.main.model.response.ContactPrefrences.TopicPreference;
import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.fffcnotify.data.Location;
import com.sorrisotech.svcs.notifications.data.UserConfig;
import com.sorrisotech.svcs.notifications.service.NotificationsDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class RegisterUserNls extends RegisterUserNlsBase {
	
	/**********************************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = -7658046393147052756L;
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RegisterUserNls.class);
	
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
		
		final String szCustomerId = request.getString(IApiFffcNotify.RegisterUserNls.customerId);
		
		final String szChannelAddrJsonConfig = request
		        .getString(IApiFffcNotify.RegisterUserNls.channelAddrJsonConfig);
		
		final String szContactPrefsJsonConfig = request
		        .getString(IApiFffcNotify.RegisterUserNls.contactPrefsJsonConfig);
		
		final String szBrowserGeo = request.getString(IApiFffcNotify.RegisterUserNls.browserGeo);
		
		final String szIpGeolocation = request.getString(IApiFffcNotify.RegisterUserNls.ipGeo);
		
		final String szIpAddress = request.getString(IApiFffcNotify.RegisterUserNls.ipAddress);
		
		final String szIsConsentActive = request
		        .getString(IApiFffcNotify.RegisterUserNls.sConsentActive);
		
		ContactPrefrences cContactPrefrencesRequest = null;
		
		final Location cLocation = Location.getLocation(szBrowserGeo, szIpGeolocation);
		
		String latitude = null;
		
		String longitude = null;
		
		// --------------------------------------------------------------------------------------
		// Log error if failed to get Location.
		if (null == cLocation) {
			LOG.error("RegisterUserNls:processInternal .... failed to get location for user");
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
			LOG.error("RegisterUserNls:processInternal ..... Failed to get current date", e, e);
			request.setToResponse();
			request.setStatus(eReturnCode);
			return eReturnCode;
		}
		
		try {
			
			final List<ChannelAddress> channelAddressesList = getChannelAddressesList(szDateTime,
			        szIpAddress, latitude, longitude, szChannelAddrJsonConfig);
			
			final List<TopicPreference> cTopicPreferencesList = getTopicPreferencesList(
			        szCustomerId, szDateTime, szContactPrefsJsonConfig);
			
			if (channelAddressesList.isEmpty() || cTopicPreferencesList.isEmpty()) {
				LOG.error(
				        "RegisterUserNls:processInternal ..... Failed to get list of channel addresses or topic preferences");
				request.setToResponse();
				request.setStatus(eReturnCode);
				return eReturnCode;
			}
			
			// --------------------------------------------------------------------------------------
			// Creating contact preference request with updated contact preference of user
			// (szContactPrefsJsonConfig) and channel addresses (szChannelAddrJsonConfig) to
			// send contact preferences setting to NLS.
			cContactPrefrencesRequest = new ContactPrefrences(szCustomerId, szDateTime,
			        channelAddressesList, cTopicPreferencesList,
			        new Econsent(Boolean.parseBoolean(szIsConsentActive), szDateTime));
			
			// --------------------------------------------------------------------------------------
			// Sending default contact preferences setting to NLS
			NLSClient.updateContactPreferences(cContactPrefrencesRequest);
			
			eReturnCode = ServiceAPIErrorCode.Success;
			
		} catch (Exception e) {
			LOG.error(
			        "RegisterUserNls:processInternal ..... failed to update contact prefrences for customer id: {}",
			        szCustomerId, e, e);
		}
		
		request.setToResponse();
		request.setStatus(eReturnCode);
		
		return eReturnCode;
	}
	
	private List<TopicPreference> getTopicPreferencesList(String szCustomerId, String szDateTime,
	        String szContactPrefsJsonConfig) {
		
		var cTopicPreferenceRequest = new ArrayList<TopicPreference>();
		
		try {
			// --------------------------------------------------------------------------------------
			// Fetching all the topic channels.
			final var cTopicChannelsList = m_cDao.queryTopicChannels();
			
			// --------------------------------------------------------------------------------------
			// Passing the input (szJsonConfig) and result of queryTopicChannels to
			// UserConfig class to create user configuration.
			final var cUserConfig = new UserConfig(szContactPrefsJsonConfig, cTopicChannelsList);
			
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
		} catch (Exception e) {
			LOG.error(
			        "RegisterUserNls:getTopicPreferencesList ..... failed to get topic prefrences for customer id: {}",
			        szCustomerId, e, e);
		}
		
		return cTopicPreferenceRequest;
	}
	
	private List<ChannelAddress> getChannelAddressesList(final String szDateTime,
	        final String szIpAddress, final String latitude, final String longitude,
	        final String szChannelAddrJsonConfig) {
		
		var cChannelAddressesList = new ArrayList<ChannelAddress>();
		
		try {
			ChannelAddressesConfig[] channelAddressConfigList = m_cObjectMapper
			        .readValue(szChannelAddrJsonConfig, ChannelAddressesConfig[].class);
			
			Arrays.stream(channelAddressConfigList).forEach(value -> {
				cChannelAddressesList.add(new ChannelAddress(value.channel, value.address,
				        szIpAddress, szDateTime, latitude, longitude));
			});
			
		} catch (IOException e) {
			LOG.error(
			        "RegisterUserNls:getChannelAddressesList() .. Error parsing channel addresses from JSON: {}",
			        e);
		}
		
		return cChannelAddressesList;
	}
	
	@SuppressWarnings("unused")
	private static class ChannelAddressesConfig {
		private String  channel;
		private boolean simple;
		private String  address;
		
		@JsonCreator
		public ChannelAddressesConfig(@JsonProperty("channel") String channel,
		        @JsonProperty("simple") boolean simple, @JsonProperty("address") Object address) {
			this.channel = channel;
			this.simple = simple;
			if (address instanceof String) {
				this.address = (String) address;
			} else if (address instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> parts = (List<String>) address;
				this.address = String.join(", ", parts);
			} 
		}
		
		public String getChannel() {
			return channel;
		}
		
		public boolean isSimple() {
			return simple;
		}
		
		public String getAddress() {
			return address;
		}
	}
	
}
