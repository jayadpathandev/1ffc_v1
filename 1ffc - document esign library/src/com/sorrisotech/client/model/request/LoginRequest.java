package com.sorrisotech.client.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest{
    @JsonProperty("HostFIID") 
    public String m_szHostFIID;
    
    @JsonProperty("UserID") 
    public String m_szUserID;
    
    @JsonProperty("BusinessAppUserID") 
    public String m_szBusinessAppUserID;
    
    @JsonProperty("PartnerID") 
    public String m_szPartnerID;
    
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
}
