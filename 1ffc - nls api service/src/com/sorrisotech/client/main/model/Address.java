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

package com.sorrisotech.client.main.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * Holds the information of the address fields.
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class Address {
	
	/**************************************************************************
	 * Address line 1 of NLS branch.
	 */
	@JsonProperty("addressline1")
	private String m_szAddressLine1;
	
	/**************************************************************************
	 * Address line 2 of NLS branch.
	 */
	@JsonProperty("addressline2")
	private String m_szAddressLine2;
	
	/**************************************************************************
	 * City name of NLS branch.
	 */
	@JsonProperty("city")
	private String m_szCity;
	
	/**************************************************************************
	 * State name of NLS branch.
	 */
	@JsonProperty("state")
	private String m_szState;
	
	/**************************************************************************
	 * Zip Code of NLS branch.
	 */
	@JsonProperty("zipcode")
	private String m_szZipCode;
	
	/**************************************************************************
	 * Country name of NLS branch.
	 */
	@JsonProperty("county")
	private String county;
	
	/**********************************************************************************************
	 * Constructor for creating the object.
	 */
	public Address() {
		super();
	}
	
	/**********************************************************************************************
	 * Constructor for creating the object with all fields.
	 */
	public Address(
			String addressLine1, 
			String addressLine2, 
			String city, 
			String state,
	        String zipcode, 
	        String county) {
		this.m_szAddressLine1 = addressLine1;
		this.m_szAddressLine2 = addressLine2;
		this.m_szCity = city;
		this.m_szState = state;
		this.m_szZipCode = zipcode;
		this.county = county;
	}
	
	/**************************************************************************
	 * Getter for address line 1 of NLS branch.
	 * 
	 * @return String address line 1
	 */
	public String getAddressLine1() {
		return m_szAddressLine1;
	}
	
	/**************************************************************************
	 * Getter for address line 2 of NLS branch.
	 * 
	 * @return String address line 2
	 */
	public String getAddressLine2() {
		return m_szAddressLine2;
	}
	
	/**************************************************************************
	 * Getter for city name of NLS branch.
	 * 
	 * @return String city name
	 */
	public String getCity() {
		return m_szCity;
	}
	
	/**************************************************************************
	 * Getter for state name of NLS branch.
	 * 
	 * @return String state name
	 */
	public String getState() {
		return m_szState;
	}
	
	/**************************************************************************
	 * Getter for zip code of NLS branch.
	 * 
	 * @return String zip code
	 */
	public String getZipcode() {
		return m_szZipCode;
	}
	
	/**************************************************************************
	 * Getter for country name of NLS branch.
	 * 
	 * @return String country name
	 */
	public String getCounty() {
		return county;
	}
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "Address [addressLine1=" + m_szAddressLine1 + ", addressLine2=" + m_szAddressLine2
		        + ", city=" + m_szCity + ", state=" + m_szState + ", zipcode=" + m_szZipCode + ", county="
		        + county + "]";
	}
	
}
