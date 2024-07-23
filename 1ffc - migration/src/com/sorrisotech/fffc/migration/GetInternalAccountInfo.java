package com.sorrisotech.fffc.migration;

import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implements web service call to get the internal account information
 * 
 */
public class GetInternalAccountInfo {
	
	private static final Logger LOG = LoggerFactory.getLogger(GetInternalAccountInfo.class);

	class InternalAccts {
		public String CustomerId;
		public String InternalAcctId;
	}
	
	/**
	 * Return the internall accounts given an external account
	 * 
	 * @param cszExternalAcct
	 * @return
	 * @throws RestClientException 
	 */
	public InternalAccts getAccts(final String cszExternalAcct) {

		final String cszCustomerId = "customerId";
		final String cszInternalAcctId = "internalAcctId";
		InternalAccts rVal = null;

		// -- build the url --
		String lszURL = Config.get("getAccounts.URL");
		String lszSecurityToken = Config.get("getAccounts.SecurityToken");
		String url = UriComponentsBuilder.fromHttpUrl(lszURL)
				.queryParam("securityToken", lszSecurityToken)
				.queryParam("externalAcctId", cszExternalAcct).toUriString();


		try {
			// -- post request for account status to application server --
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response;
				response =  restTemplate.getForEntity(url, String.class);

			JSONObject responseObject = new JSONObject(response.getBody());
			
			rVal = new InternalAccts();
	
			// -- pull status info out of the response and put it in a MonthlyStatusData object --
			rVal.CustomerId = Optional.ofNullable( responseObject.getString(cszCustomerId)).orElse("");
			rVal.InternalAcctId = Optional.ofNullable( responseObject.getString(cszInternalAcctId)).orElse("");
			
			if (rVal.CustomerId.isBlank() || rVal.InternalAcctId.isBlank()) rVal = null;
		
		} catch (JSONException | RestClientException e) {
			LOG.error("GetInternalAccountInfo:getAccts -- failed catching returned accts.", e);
			rVal = null;
		}
		return rVal;
			
	}
}
