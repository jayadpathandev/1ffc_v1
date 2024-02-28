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
package com.sorrisotech.svcs.fffcnotify.data;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/******************************************************************************
 * This class is used to parse the JSON of Ip Geolocation and fetch latitude and
 * longitude.
 * 
 * @author Asrar Saloda
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
	
	/******************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Location.class);
	
	/**************************************************************************
	 * JSON ObjectMapper.
	 */
	private static ObjectMapper m_cObjectMapper = new ObjectMapper();
	
	/**************************************************************************
	 * Pattern of browser geolocation e.g. latitude,longitude.
	 */
	private static final Pattern m_cBrowserGeoPattern = Pattern
	        .compile("(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)");
	
	/**********************************************************************************************
	 * The latitude of geolocation.
	 */
	@JsonProperty("latitude")
	private String latitude;
	
	/**********************************************************************************************
	 * The longitude of geolocation.
	 */
	@JsonProperty("longitude")
	private String longitude;
	
	/**********************************************************************************************
	 * Default constructor.
	 */
	public Location() {
		super();
	}
	
	/**********************************************************************************************
	 * ALL argument constructor.
	 *
	 * @param latitude
	 * @param longitude
	 */
	public Location(String latitude, String longitude) {
		if (latitude == null || longitude == null) {
			LOG.error("Location(String, String) ......  latitude and longitude are null.");
		}
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**********************************************************************************************
	 * Getter method for latitude.
	 * 
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}
	
	/**********************************************************************************************
	 * Setter method for latitude.
	 * 
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**********************************************************************************************
	 * Getter method for longitude.
	 * 
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}
	
	/**********************************************************************************************
	 * Setter method for longitude.
	 * 
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**********************************************************************************************
	 * This method is used to get geolocation of user either of using browserGeo or
	 * ipGeo.
	 * 
	 * @param browserGeo Browser geolocation.
	 * @param ipGeo      IP geolocation .
	 * 
	 * @return Geolocation of user.
	 */
	public static Location getLocation(final String browserGeo, final String ipGeo) {
		if (!browserGeo.isBlank()) {
			return parseLocationFromBrowserGeo(browserGeo);
		} else if (!ipGeo.isBlank()) {
			return parseLocationFromIpGeo(ipGeo);
		} else {
			LOG.error("Location:getLocation() ... Both browserGeo and ipGeo are blank.");
			return null;
		}
	}
	
	/**********************************************************************************************
	 * This method is used to get geolocation of user sing browserGeo.
	 * 
	 * @param browserGeo Browser geolocation.
	 * 
	 * @return Geolocation of user.
	 */
	private static Location parseLocationFromBrowserGeo(final String browserGeo) {
		final Matcher matcher = m_cBrowserGeoPattern.matcher(browserGeo);
		
		if (matcher.matches()) {
			// latitude, longitude
			return new Location(matcher.group(1), matcher.group(3));
		} else {
			LOG.error(
			        "Location:parseLocationFromBrowserGeo() .. Failed to extract latitude and longitude from browser geolocation: {}",
			        browserGeo);
			return null;
		}
	}
	
	/**********************************************************************************************
	 * This method is used to get geolocation of user either of using ipGeo.
	 * 
	 * @param ipGeo IP geolocation .
	 * 
	 * @return Geolocation of user.
	 */
	private static Location parseLocationFromIpGeo(final String ipGeo) {
		try {
			return m_cObjectMapper.readValue(ipGeo, Location.class);
		} catch (IOException e) {
			LOG.error(
			        "Location:parseLocationFromIpGeo() .. Error parsing location from IP geolocation: {}",
			        e);
			return null;
		}
	}
}
