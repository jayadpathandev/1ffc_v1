package com.sorrisotech.client.utils;

import java.util.Map;

/******************************************************************************
 * End point enum of all API's.
 * 
 * @author Asrar Saloda
 */
public enum EndPoints {
	
	AUTH_GET_TOKEN("/api/v{version}/Auth/getToken"), AUTH_REFRESH("/api/v{version}/Auth/refresh"),
    // PAYMENT_RECONCILIATION("/api/v{version}/Main/PaymentReconciliation"),
    // ACCOUNT_FEED("/api/v{version}/Main/AccountFeed"),
    // CONTACT_PREFERENCES("/api/v{version}/Main/ContactPreferences"),
    // PAYMENT_HISTORY("/api/v{version}/Main/PaymentHistory"),
    // CONTACT_PREFERENCE_UPDATES("/api/v{version}/Main/ContactPreferenceUpdates"),
    // TRANSACTIONS("/api/v{version}/Main/Transactions"),
	DOCUMENT("/api/v{version}/Main/Document"),
	// CURRENT_BALANCE("/api/v{version}/Main/{loanid}/CurrentBalance"),
	// BORROWER_NICKNAME("/api/v{version}/Main/{loanid}/BorrowerNickname"),
	// CONVENIENCE_FEE("/api/v{version}/Main/{loanid}/ConvenienceFee")
	;
	
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
	
	/**********************************************************************************************
	 * This method is used to build URL using base URL and end points.
	 * 
	 * @param szBaseUrl      Base URL of server.
	 * @param cPathVariables Path variable of end point.
	 * @param cQueryParams   Query parameters of URL
	 */
	public String buildURL(
	        String szBaseUrl,
	        Map<String, String> cPathVariables,
	        Map<String, String> cQueryParams) {
		var szFinalUrl = szBaseUrl + m_szEndpoint;
		for (var entry : cPathVariables.entrySet()) {
			szFinalUrl = szFinalUrl.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		
		if (cQueryParams != null && !cQueryParams.isEmpty()) {
			StringBuilder cQueryParamBuilder = new StringBuilder("?");
			
			boolean bIsFirst = true;
			
			for (var entry : cQueryParams.entrySet()) {
				if (!bIsFirst) {
					cQueryParamBuilder.append("&");
				} else {
					bIsFirst = false;
				}
				cQueryParamBuilder.append(entry.getKey()).append("=").append(entry.getValue());
			}
			
			szFinalUrl += cQueryParamBuilder.toString();
		}
		return szFinalUrl;
	}
}
