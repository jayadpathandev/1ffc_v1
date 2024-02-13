package com.sorrisotech.client.utils;

import java.util.Map;

/******************************************************************************
 * End point enum of all API's.
 * 
 * @author Asrar Saloda
 */
public enum EndPoints {
	
	AUTH_GET_TOKEN("/api/v{version}/Auth/getToken"), AUTH_REFRESH("/api/v{version}/Auth/refresh"),
	CONTACT_PREFERENCES("/api/v{version}/Main/ContactPreferences"),
	CONTACT_PREFERENCE_UPDATES("/api/v{version}/Main/UpdatedPreferences"),
	DOCUMENT("/api/v{version}/Main/Document"),
	CURRENT_BALANCE("/api/v{version}/Main/CurrentBalance"),
	BORROWER_NICKNAME("/api/v{version}/Main/LoanNickname"),
	CONVENIENCE_FEE("/api/v{version}/Main/ConvenienceFee"),
	BRANCHES("/api/v{version}/Main/Branches"),
	FILEAVAILABLE("/api/v{version}/Main/PaymentReconciliation");
	
	private String m_szEndpoint;
	
	/**********************************************************************************************
	 * Initializing member variable.
	 * 
	 * @param szEndpoint End point.
	 */
	EndPoints(String szEndpoint) {
		this.m_szEndpoint = szEndpoint;
	}
	
	/**********************************************************************************************
	 * This method is used to build URL using base URL and end points.
	 * 
	 * @param szBaseUrl      Base URL of server.
	 * @param cPathVariables Path variable of end point.
	 */
	public String buildURL(String szBaseUrl, Map<String, String> cPathVariables) {
		var szFinalUrl = szBaseUrl + m_szEndpoint;
		for (var entry : cPathVariables.entrySet()) {
			szFinalUrl = szFinalUrl.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		return szFinalUrl;
	}
}
