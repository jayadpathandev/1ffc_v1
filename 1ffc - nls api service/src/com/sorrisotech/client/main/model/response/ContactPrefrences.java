/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.client.main.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**************************************************************************
 * Class that holds contact preferences of user.
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class ContactPrefrences {
	
	/**************************************************************************
	 * Customer ID of user.
	 */
	@JsonProperty("customer_id")
	@NotEmpty(message = "Customer ID can not be null or empty")
	private String m_szCustomerId;
	
	/**************************************************************************
	 * Date and time.
	 */
	@JsonProperty("date_time")
	@NotEmpty(message = "Date and time can not be null or empty")
	private String m_szDateTime;
	
	/**************************************************************************
	 * List of all channel addresses.
	 */
	@JsonProperty("channel_addresses")
	@NotEmpty(message = "Channel addresses can not be null or empty")
	private List<ChannelAddress> m_szChannelAddresses;

	/**************************************************************************
	 * Econsent data.
	 */
	@JsonProperty("econsent")
	private Econsent econsent;
	
	/**************************************************************************
	 * List of all topic preferences.
	 */
	@JsonProperty("topic_preferences")
	@NotEmpty(message = "Topic prefrences can not be null or empty")
	private List<TopicPreference> m_szTopicPreferences;
	
	/**************************************************************************
	 * Getter for customer ID of user.
	 * 
	 * @return String customer id.
	 */
	public String getCustomerId() {
		return m_szCustomerId;
	}
	
	/**************************************************************************
	 * Getter for date and time.
	 * 
	 * @return String date and time.
	 */
	public String getDateTime() {
		return m_szDateTime;
	}
	
	/**************************************************************************
	 * Getter for list of channel addresses.
	 * 
	 * @return List of channel addresses.
	 */
	public List<ChannelAddress> getChannelAddresses() {
		return m_szChannelAddresses;
	}
	
	/**************************************************************************
	 * Getter for list of topic preferences.
	 * 
	 * @return List of topic preferences.
	 */
	public List<TopicPreference> getTopicPreferences() {
		return m_szTopicPreferences;
	}
	
	/**************************************************************************
	 * Getter for econsent.
	 *
	 * @return the econsent status along with last updated date.
	 */
	public Econsent getEconsent() {
		return econsent;
	}
	
	/**************************************************************************
	 * Setter for customer ID of user.
	 */
	public void setCustomerId(String m_szCustomerId) {
		this.m_szCustomerId = m_szCustomerId;
	}
	
	/**************************************************************************
	 * Setter for Date and time.
	 */
	public void setDateTime(String m_szDateTime) {
		this.m_szDateTime = m_szDateTime;
	}
	
	/**************************************************************************
	 * Setter for list of channel addresses.
	 */
	public void setChannelAddresses(List<ChannelAddress> m_szChannelAddresses) {
		this.m_szChannelAddresses = m_szChannelAddresses;
	}
	
	/**************************************************************************
	 * Setter for list of topic preferences.
	 */
	public void setTopicPreferences(List<TopicPreference> m_szTopicPreferences) {
		this.m_szTopicPreferences = m_szTopicPreferences;
	}
	
	/**************************************************************************
	 * Setter for e consent status along with updated date.
	 * 
	 * @param econsent the e consent to set
	 */
	public void setEconsent(Econsent econsent) {
		this.econsent = econsent;
	}

	@Override
	public String toString() {
	    return "{" +
	           "\"customer_id\": \"" + m_szCustomerId + "\"," +
	           "\"date_time\": \"" + m_szDateTime + "\"," +
	           "\"channel_addresses\": " + m_szChannelAddresses + "," +
	           "\"econsent\": " + econsent + "," +
	           "\"topic_preferences\": " + m_szTopicPreferences +
	           "}";
	}

	/**************************************************************************
	 * Default constructor
	 */
	public ContactPrefrences() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 */
	public ContactPrefrences(
	        @NotEmpty(message = "Customer ID can not be null or empty") String m_szCustomerId,
	        @NotEmpty(message = "Date and time can not be null or empty") String m_szDateTime,
	        @NotEmpty(message = "Channel addresses can not be null or empty") List<ChannelAddress> m_szChannelAddresses,
	        @NotEmpty(message = "Topic prefrences can not be null or empty") List<TopicPreference> m_szTopicPreferences,
	        @NotEmpty (message = "The consent data can not be null or empty") Econsent m_cEconsent) {
		this.m_szCustomerId = m_szCustomerId;
		this.m_szDateTime = m_szDateTime;
		this.m_szChannelAddresses = m_szChannelAddresses;
		this.m_szTopicPreferences = m_szTopicPreferences;
		this.econsent = m_cEconsent;
	}
	
	/**************************************************************************
	 * Inner class that holds Channel Address.
	 */
	@JsonInclude(Include.NON_NULL)
	public static class ChannelAddress {
		
		/**************************************************************************
		 * Channel name.
		 */
		@JsonProperty("channelname")
		@NotEmpty(message = "Channel name can not be null or empty")
		private String m_szChannelName;
		
		/**************************************************************************
		 * Consent address.
		 */
		@JsonProperty("consent_address")
		private String m_szConsentAddress;
		
		/**************************************************************************
		 * IP address of source.
		 */
		@JsonProperty("source_ip_address")
		private String m_szSourceIpAddress;
		
		/**************************************************************************
		 * Date of consent.
		 */
		@JsonProperty("consent_date")
		@NotEmpty(message = "Date and time can not be null or empty")
		private String m_szConsentDate;
		
		/**************************************************************************
		 * Latitude of location.
		 */
		@JsonProperty("latitude")
		private String m_szLatitude;
		
		/**************************************************************************
		 * Longitude of location.
		 */
		@JsonProperty("longitude")
		private String m_szLongitude;
		
		/**************************************************************************
		 * Getter for channel name.
		 * 
		 * @return String channel name.
		 */
		public String getChannelName() {
			return m_szChannelName;
		}
		
		/**************************************************************************
		 * Getter for Consent address.
		 * 
		 * @return String consent address.
		 */
		public String getConsentAddress() {
			return m_szConsentAddress;
		}
		
		/**************************************************************************
		 * Getter for IP address of source.
		 * 
		 * @return String Source IP address.
		 */
		public String getSourceIpAddress() {
			return m_szSourceIpAddress;
		}
		
		/**************************************************************************
		 * Getter for consent address.
		 * 
		 * @return String consent address.
		 */
		public String getConsentDate() {
			return m_szConsentDate;
		}
		
		/**************************************************************************
		 * Getter for latitude.
		 * 
		 * @return String latitude.
		 */
		public String getLatitude() {
			return m_szLatitude;
		}
		
		/**************************************************************************
		 * Getter for longitude.
		 * 
		 * @return String longitude.
		 */
		public String getLongitude() {
			return m_szLongitude;
		}
		
		/**************************************************************************
		 * Setter for channel name.
		 */
		public void setChannelName(String m_szChannelName) {
			this.m_szChannelName = m_szChannelName;
		}
		
		/**************************************************************************
		 * Setter for consent address.
		 */
		public void setConsentAddress(String m_szConsentAddress) {
			this.m_szConsentAddress = m_szConsentAddress;
		}
		
		/**************************************************************************
		 * Setter for source IP address.
		 */
		public void setSourceIpAddress(String m_szSourceIpAddress) {
			this.m_szSourceIpAddress = m_szSourceIpAddress;
		}
		
		/**************************************************************************
		 * Setter for consent date.
		 */
		public void setConsentDate(String m_szConsentDate) {
			this.m_szConsentDate = m_szConsentDate;
		}
		
		/**************************************************************************
		 * Setter for latitude.
		 */
		public void setLatitude(String m_szLatitude) {
			this.m_szLatitude = m_szLatitude;
		}
		
		/**************************************************************************
		 * Setter for longitude.
		 */
		public void setLongitude(String m_szLongitude) {
			this.m_szLongitude = m_szLongitude;
		}
		
		/**************************************************************************
		 * Overridden toString method.
		 * 
		 * @return a string representation of the object (JSON String).
		 */
		@Override
		public String toString() {
		    return "{" +
		           "\"channelname\": \"" + m_szChannelName + "\"," +
		           "\"consent_address\": \"" + m_szConsentAddress + "\"," +
		           "\"source_ip_address\": \"" + m_szSourceIpAddress + "\"," +
		           "\"consent_date\": \"" + m_szConsentDate + "\"," +
		           "\"latitude\": " + m_szLatitude + "," +
		           "\"longitude\": " + m_szLongitude +
		           "}";
		}
		
		/**************************************************************************
		 * Default constructor
		 */
		public ChannelAddress() {
		}
		
		/**************************************************************************
		 * All argument constructor.
		 */
		public ChannelAddress(
		        @NotEmpty(message = "Channel name can not be null or empty") String m_szChannelName,
		        String m_szConsentAddress, String m_szSourceIpAddress,
		        @NotEmpty(message = "Date and time can not be null or empty") String m_szConsentDate,
		        String m_szLatitude, String m_szLongitude) {
			super();
			this.m_szChannelName = m_szChannelName;
			this.m_szConsentAddress = m_szConsentAddress;
			this.m_szSourceIpAddress = m_szSourceIpAddress;
			this.m_szConsentDate = m_szConsentDate;
			this.m_szLatitude = m_szLatitude;
			this.m_szLongitude = m_szLongitude;
		}
		
	}
	
	/**************************************************************************
	 * Inner class that holds Topic preferences.
	 */
	@JsonInclude(Include.NON_NULL)
	public static class TopicPreference {
		
		/**************************************************************************
		 * Topic name.
		 */
		@JsonProperty("topicname")
		@NotNull(message = "Topic name can not be null.")
		private String m_szTopicName;
		
		/**************************************************************************
		 * List of all channels of that particular topic.
		 */
		@JsonProperty("topic_channels")
		@NotEmpty(message = "The topic channels can not be null or empty.")
		private List<TopicChannel> m_cTopicChannels;
		
		/**************************************************************************
		 * Getter for topic name.
		 * 
		 * @return String Topic name.
		 */
		public String getTopicName() {
			return m_szTopicName;
		}
		
		/**************************************************************************
		 * Getter for all list of channels for topic.
		 * 
		 * @return List of all channels of that particular topic.
		 */
		public List<TopicChannel> getTopicChannels() {
			return m_cTopicChannels;
		}
		
		/**************************************************************************
		 * Setter for topic name.
		 */
		public void setTopicName(String m_szTopicName) {
			this.m_szTopicName = m_szTopicName;
		}
		
		/**************************************************************************
		 * Setter for all list of channels for topic.
		 */
		public void setTopicChannels(List<TopicChannel> m_cTopicChannels) {
			this.m_cTopicChannels = m_cTopicChannels;
		}
		
		/**************************************************************************
		 * Overridden toString method.
		 * 
		 * @return a string representation of the object.
		 */
		@Override
		public String toString() {
		    return "{" +
		           "\"topicname\": \"" + m_szTopicName + "\"," +
		           "\"topic_channels\": " + m_cTopicChannels +
		           "}";
		}
		
		/**************************************************************************
		 * Default constructor
		 */
		public TopicPreference() {
		}
		
		/**************************************************************************
		 * All argument constructor.
		 */
		public TopicPreference(
		        @NotNull(message = "Topic name can not be null.") String m_szTopicName,
		        @NotEmpty(message = "The topic channels can not be null or empty.") List<TopicChannel> m_cTopicChannels) {
			super();
			this.m_szTopicName = m_szTopicName;
			this.m_cTopicChannels = m_cTopicChannels;
		}
	}
	
	/**************************************************************************
	 * Inner class that holds Topic channel.
	 */
	@JsonInclude(Include.NON_NULL)
	public static class TopicChannel {
		
		/**************************************************************************
		 * Channel name of that particular topic.
		 */
		@JsonProperty("channel_name")
		@NotNull(message = "The channel name can not be null")
		private String m_szChannelName;
		
		/**************************************************************************
		 * Boolean property for that channel is selected or not.
		 */
		@JsonProperty("selected")
		@NotNull(message = "The selected field can not be null or empty.")
		private boolean m_bSelected;
		
		/**************************************************************************
		 * Getter for channel name.
		 * 
		 * @return String channel name.
		 */
		public String getChannelName() {
			return m_szChannelName;
		}
		
		/**************************************************************************
		 * Getter for selected.
		 * 
		 * @return boolean channel is selected/not.
		 */
		public boolean isSelected() {
			return m_bSelected;
		}
		
		/**************************************************************************
		 * Overridden toString method.
		 * 
		 * @return a string representation of the object.
		 */
		@Override
		public String toString() {
		    return "{" +
		           "\"channel_name\": \"" + m_szChannelName + "\"," +
		           "\"selected\": " + m_bSelected +
		           "}";
		}
		
		/**************************************************************************
		 * Default constructor
		 */
		public TopicChannel() {
		}
		
		/**************************************************************************
		 * All argument constructor.
		 */
		public TopicChannel(
		        @NotNull(message = "The channel name can not be null") String m_szChannelName,
		        @NotNull(message = "The selected field can not be null or empty.") boolean m_bSelected) {
			super();
			this.m_szChannelName = m_szChannelName;
			this.m_bSelected = m_bSelected;
		}
		
		/**************************************************************************
		 * Setter for channel name.
		 */
		public void setChannelName(String m_szChannelName) {
			this.m_szChannelName = m_szChannelName;
		}
		
		/**************************************************************************
		 * Setter for selected.
		 */
		public void setSelected(boolean m_bSelected) {
			this.m_bSelected = m_bSelected;
		}
		
	}
	
	/**************************************************************************
	 * Inner class that holds e consent status.
	 */
	@JsonInclude(Include.NON_NULL)
	public static class Econsent {
		
		/**************************************************************************
		 * Boolean property for that consent is active or not.
		 */
		@JsonProperty("active")
		@NotNull(message = "The active field can not be null or empty.")
		private boolean active;
		
		/**************************************************************************
		 * Channel name of that particular topic.
		 */
		@JsonProperty("date_time")
		@NotNull(message = "The date time can not be null")
		private String dateTime;
		
		/**************************************************************************
		 * Default constructor
		 */
		public Econsent() {
			super();
		}
		
		/**************************************************************************
		 * All argument constructor.
		 * 
		 * @param active   boolean field for active status.
		 * @param dateTime the date and time to set.
		 */
		public Econsent(
		        @NotNull(message = "The active field can not be null or empty.") boolean active,
		        @NotNull(message = "The date time can not be null") String dateTime) {
			super();
			this.active = active;
			this.dateTime = dateTime;
		}
		
		/**************************************************************************
		 * Getter for channel name.
		 * 
		 * @return boolean the active status of e consent.
		 */
		public boolean isActive() {
			return active;
		}
		
		/**************************************************************************
		 * Setter for e consent active status.
		 * 
		 * @param active the active to set
		 */
		public void setActive(boolean active) {
			this.active = active;
		}
		
		/**************************************************************************
		 * Getter for date time.
		 * 
		 * @return the dateTime
		 */
		public String getDateTime() {
			return dateTime;
		}
		
		/**************************************************************************
		 * Setter for e consent date and time.
		 * 
		 * @param dateTime the dateTime to set
		 */
		public void setDateTime(String dateTime) {
			this.dateTime = dateTime;
		}
		
		/**************************************************************************
		 * Overridden toString method.
		 * 
		 * @return a string representation of the object.
		 */
		@Override
		public String toString() {
		    return "{" +
		           "\"active\": " + active + "," +
		           "\"date_time\": \"" + dateTime + "\"" +
		           "}";
		}
	}
}
