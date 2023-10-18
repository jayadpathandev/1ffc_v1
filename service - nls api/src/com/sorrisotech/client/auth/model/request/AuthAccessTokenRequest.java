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
package com.sorrisotech.client.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;

/******************************************************************************
 * Holds the information of the Access token request
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class AuthAccessTokenRequest {
	
	/**************************************************************************
	 * User name of user.
	 */
	@NotEmpty(message = "User name can not be null or empty")
	@JsonProperty("username")
	private String m_szUserName;
	
	/**************************************************************************
	 * Password of user.
	 */
	@NotEmpty(message = "User name can not be null or empty")
	@JsonProperty("password")
	private String m_szPassword;
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "AuthAccessTokenRequest [username=" + m_szUserName + ", password=" + m_szPassword + "]";
	}
	
	/**************************************************************************
	 * Default constructor
	 */
	public AuthAccessTokenRequest() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 */
	public AuthAccessTokenRequest(
	        @NotEmpty(message = "User name can not be null or empty") String username,
	        @NotEmpty(message = "User name can not be null or empty") String password) {
		super();
		this.m_szUserName = username;
		this.m_szPassword = password;
	}
	
	/**************************************************************************
	 * Getter method for user name.
	 * 
	 * @return String user name.
	 */
	public String getUsername() {
		return m_szUserName;
	}

	/**************************************************************************
	 * Getter method for password.
	 * 
	 * @return String password
	 */
	public String getPassword() {
		return m_szPassword;
	}
	
}
