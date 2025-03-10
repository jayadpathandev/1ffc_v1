/*
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.client.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/******************************************************************************
 * The request POJO for login endpoint.
 * 
 * @author Rohit Singh
 */
@JsonInclude(value = Include.NON_NULL)
public class LoginRequest{
	
	/******************************************************************************
	 * Host Financial Institution ID. This will be provided by IMM
	 */
    @JsonProperty("HostFIID") 
    public String m_szHostFIID;
    
    /******************************************************************************
	 * User existing in eSign. This will be provided by IMM
	 */
    @JsonProperty("UserID") 
    public String m_szUserID;
    
    /******************************************************************************
	 * Business system or Windows User. This will be provided by IMM.
	 */
    @JsonProperty("BusinessAppUserID") 
    public String m_szBusinessAppUserID;
    
    /******************************************************************************
	 * PartnerID will be provided by IMM
	 */
    @JsonProperty("PartnerID") 
    public String m_szPartnerID;
    
    /******************************************************************************
	 * Financial Institution Password set in eSign. This will be provided by IMM
	 */
    @JsonProperty("APIKey") 
    public String m_szAPIKey;
    
	public LoginRequest(
			String hostFIID, 
			String userID, 
			String businessAppUserID, 
			String partnerID, 
			String aPIKey) {
		this.m_szHostFIID = hostFIID;
		this.m_szUserID = userID;
		this.m_szBusinessAppUserID = businessAppUserID;
		this.m_szPartnerID = partnerID;
		this.m_szAPIKey = aPIKey;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LoginRequest [m_szHostFIID=");
		builder.append(m_szHostFIID);
		builder.append(", m_szUserID=");
		builder.append(m_szUserID);
		builder.append(", m_szBusinessAppUserID=");
		builder.append(m_szBusinessAppUserID);
		builder.append(", m_szPartnerID=");
		builder.append(m_szPartnerID);
		builder.append(", m_szAPIKey=");
		builder.append(m_szAPIKey);
		builder.append("]");
		return builder.toString();
	}
}
