package com.sorrisotech.client.util;

/******************************************************************************
 * End point enum of all API's.
 * 
 * @author Rohit Singh
 */
public enum EndPoints {
	
	LOGIN("/login"),
	CREATE_SESSION("/session/rts/create"),
	ADD_DOCUMENT("/session/{hostSessionId}/rts/document"),
	GET_REMOTE_SIGNING("/remote/{hostSessionId}"),
	GET_REMOTE_STATUS("/remote/{hostSessionId}/status"),
	CANCEL("/session/{hostSessionId}/cancel");
	
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
	 */
	public String buildURL(String szBaseUrl) {
		var szFinalUrl = szBaseUrl + m_szEndpoint;
		return szFinalUrl;
	}
}
