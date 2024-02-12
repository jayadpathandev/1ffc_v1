package com.sorrisotech.client.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateSessionRequest {
    @JsonProperty("Parties") 
    public List<Party> m_cParties;

	public CreateSessionRequest(List<Party> parties) {
		this.m_cParties = parties;
	}

    public static class Party{
        @JsonProperty("FullName") 
        public String m_szFullName;
        
        @JsonProperty("Email") 
        public String m_szEmail;
        
        @JsonProperty("PhoneNumber") 
        public String m_szPhoneNumber;
        
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
        
    }
}