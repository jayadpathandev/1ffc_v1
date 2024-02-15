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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/******************************************************************************
 * The request POJO for creating new esign session endpoint.
 * 
 * @author Rohit Singh
 */
@JsonInclude(value = Include.NON_NULL)
public class CreateSessionRequest {
	
	/******************************************************************************
	 * The list of the parties.
	 */
    @JsonProperty("Parties") 
    public List<Party> m_cParties;

	public CreateSessionRequest(List<Party> parties) {
		this.m_cParties = parties;
	}

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CreateSessionRequest [m_cParties=");
		builder.append(m_cParties);
		builder.append("]");
		return builder.toString();
	}

	public static class Party{
		
		/******************************************************************************
		 * Party's Full Name
		 */
        @JsonProperty("FullName") 
        public String m_szFullName;
        
        /******************************************************************************
    	 * Party's Full Email
    	 */
        @JsonProperty("Email") 
        public String m_szEmail;
        
        /******************************************************************************
    	 * Party's Phome number
    	 */
        @JsonProperty("PhoneNumber") 
        public String m_szPhoneNumber;
        
        /******************************************************************************
    	 * Party's Country code
    	 */
        @JsonProperty("PhoneCountryCode") 
        public String m_szPhoneCountryCode;

		public Party(
				String fullName, 
				String email, 
				String phoneNumber, 
				String phoneCountryCode) {
			this.m_szFullName = fullName;
			this.m_szEmail = email;
			this.m_szPhoneNumber = phoneNumber;
			this.m_szPhoneCountryCode = phoneCountryCode;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Party [m_szFullName=");
			builder.append(m_szFullName);
			builder.append(", m_szEmail=");
			builder.append(m_szEmail);
			builder.append(", m_szPhoneNumber=");
			builder.append(m_szPhoneNumber);
			builder.append(", m_szPhoneCountryCode=");
			builder.append(m_szPhoneCountryCode);
			builder.append("]");
			return builder.toString();
		}
        
    }
}