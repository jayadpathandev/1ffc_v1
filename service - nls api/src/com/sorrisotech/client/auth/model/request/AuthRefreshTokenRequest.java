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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotEmpty;

/******************************************************************************
 * Holds the information of the Refresh token request
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.PUBLIC_ONLY)
public class AuthRefreshTokenRequest {
	
	/**************************************************************************
	 * Access token
	 */
	@NotEmpty(message = "Access token can not be null")
	public String accessToken;
	
	/**************************************************************************
	 * Refresh token
	 */
	@NotEmpty(message = "Refresh token can not be null or empty")
	public String refreshToken;
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "AuthRefreshTokenRequest [accessToken=" + accessToken + ", refreshToken="
		        + refreshToken + "]";
	}
	
	/**************************************************************************
	 * Default constructor
	 */
	public AuthRefreshTokenRequest() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 */
	public AuthRefreshTokenRequest(String accessToken, String refreshToken) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
}
