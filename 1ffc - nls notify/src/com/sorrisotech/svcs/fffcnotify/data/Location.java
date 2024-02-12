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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
			LOG.error("Location(String, String) ......  Latitude and longitude are null.");
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
	
}
