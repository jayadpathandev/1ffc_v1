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
package com.sorrisotech.client.auth.model.response;

/******************************************************************************
 * Holds the information of the Authorization response.
 * 
 * @author Asrar Saloda
 */
public class AuthPayload {
	
	/**************************************************************************
	 * Access token.
	 */
	public String accessToken;
	
	/**************************************************************************
	 * Refresh token.
	 */
	public String refreshToken;

	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "AuthPayload [accessToken=" + accessToken + ", refrershToken=" + refreshToken + "]";
	}
	
	/**************************************************************************
	 * Getter method of access token
	 * 
	 * @return String access token.
	 */
	public String getAccessToken() {
		return accessToken;
	}
	
	/**************************************************************************
	 * Getter method of refresh token
	 * 
	 * @return String refresh token.
	 */
	public String getRefrershToken() {
		return refreshToken;
	}
}
