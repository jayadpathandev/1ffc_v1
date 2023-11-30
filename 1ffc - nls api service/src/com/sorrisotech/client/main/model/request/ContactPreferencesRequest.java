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
package com.sorrisotech.client.main.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;

/**************************************************************
 * Holds the information of the Contact Preferences request
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class ContactPreferencesRequest {
	
	/**************************************************************************
	 * Customer id of user.
	 */
	@NotEmpty(message = "Customer id can not be null or empty")
	@JsonProperty("customer_id")
	private String szCustomerId;
	
	/**************************************************************************
	 * Date and time.
	 */
	@NotEmpty(message = "Date and time can not be null or empty")
	@JsonProperty("date_time")
	private String szDateTime;
	
	/**************************************************************************
	 * Default constructor
	 */
	public ContactPreferencesRequest() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 */
	public ContactPreferencesRequest(
	        @NotEmpty(message = "Customer id can not be null or empty") String szCustomerId,
	        @NotEmpty(message = "Date and time can not be null or empty") String szDateTime) {
		this.szCustomerId = szCustomerId;
		this.szDateTime = szDateTime;
	}
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "ContactPreferencesRequest [szCustomerId=" + szCustomerId + ", szDateTime="
		        + szDateTime + "]";
	}
	
	/**************************************************************************
	 * Getter method for customer id.
	 * 
	 * @return String customer id.
	 */
	public String getCustomerId() {
		return szCustomerId;
	}
	
	/**************************************************************************
	 * Date and time.
	 * 
	 * @return String Date and time.
	 */
	public String getDateTime() {
		return szDateTime;
	}
}
