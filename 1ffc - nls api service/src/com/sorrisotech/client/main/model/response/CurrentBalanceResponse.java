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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * Holds the information of the Current Balance Response
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class CurrentBalanceResponse {
	
	/**************************************************************************
	 * HTTP status code of response
	 */
	@JsonProperty("statuscode")
	private Integer m_iStatusCode;
	
	/**************************************************************************
	 * Boolean property to know 3d party rest call successful or not
	 */
	@JsonProperty("success")
	private Boolean m_bSuccess;
	
	/**************************************************************************
	 * Pay load of response
	 */
	@JsonProperty("payload")
	private Double m_cPayLoad;
	
	/**************************************************************************
	 * List of errors in response
	 */
	private ArrayList<String> errors;
	
	/**************************************************************************
	 * Getter method for pay load.
	 * 
	 * @return pay load of response.
	 */
	public Double getPayload() {
		return m_cPayLoad;
	}
	
	/**************************************************************************
	 * Getter method for status code.
	 * 
	 * @return Integer status code.
	 */
	public Integer getStatuscode() {
		return m_iStatusCode;
	}
	
	/**************************************************************************
	 * Getter method for success(true/false).
	 * 
	 * @return Boolean success status.
	 */
	public Boolean getSuccess() {
		return m_bSuccess;
	}
	
	/**************************************************************************
	 * Getter method for errors.
	 * 
	 * @return ArrayList<String> errors.
	 */
	public ArrayList<String> getErrors() {
		return errors;
	}
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "CurrentBalanceResponse [statuscode=" + m_iStatusCode + ", success=" + m_bSuccess
		        + ", payload=" + m_cPayLoad + ", errors=" + errors + "]";
	}
}
