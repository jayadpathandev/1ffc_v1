/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 *  Objec to sequence through the agent API and create either a 
 *  one time payment or recurring payment rule.
 */
public class CreatePayment {

	private static final Logger LOG = LoggerFactory.getLogger(CreatePayment.class);
	
	private WebSvcReturnCode m_retCode = null;
	private enum TransactionType {oneTime, automatic};
	private String m_transactionId = null;

	public WebSvcReturnCode createScheduledPayment(final IScheduledPayment schedPayment) {
		Boolean lbRet= false;
		m_retCode = null;
		lbRet = apiStartPayment ( schedPayment.getCustomerId(),
								  schedPayment.getInternalAccount(),
								  schedPayment.getDisplayAccount(),
								  TransactionType.oneTime);
		
		if (lbRet) {
			lbRet = apiAddToken(schedPayment.getCustomerId(),
					  			schedPayment.getInternalAccount(),
					  			schedPayment.getPayAcct() );
		}
		
		if (lbRet) {
			lbRet = apiScheduledPayment(schedPayment);
		}
		
		return m_retCode;
	}
	
	public WebSvcReturnCode createAutomaticPaymentRule(final IAutomaticPaymentRule autoPayRule) {
		Boolean lbRet = false;
		m_retCode = null;

		lbRet = apiStartPayment ( autoPayRule.getCustomerId(),
				autoPayRule.getInternalAccount(),
				autoPayRule.getDisplayAccount(),
				  TransactionType.automatic);

		if (lbRet) {
			lbRet = apiAddToken(autoPayRule.getCustomerId(),
								autoPayRule.getInternalAccount(),
								autoPayRule.getPayAcct() );
		}
		
		if (lbRet) {
			lbRet = apiAutomaticPaymentRule(autoPayRule);
		}

		return m_retCode;
		
	}
	
	/**
	 * Starts the agent payment process 
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @param ceTransType
	 * @return
	 */
	private Boolean apiStartPayment( final String cszCustomerId, 
									 final String cszInternalAcct,
									 final String cszExternalAcct,
									 final TransactionType ceTransType) {
		Boolean lbRet = false;

		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.Start");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");
		String url = UriComponentsBuilder.fromHttpUrl(lszURL + lszCallName)
				.queryParam("securityToken", lszSecurityToken)
				.queryParam("customerId", cszCustomerId)
				.queryParam("accountId", cszInternalAcct)
				.queryParam("paymentTransactionType", ceTransType.toString())
				.toUriString();

		try {
			// -- post request for account status to application server --
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response;
				response =  restTemplate.getForEntity(url, String.class);

			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiStartPayment -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiStartPayment -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
			
			JSONObject responseObject = new JSONObject(response.getBody());
	
			// -- grab the transaction for moving forward
			m_transactionId = Optional.ofNullable( responseObject.getString("transactionId")).orElse("");
			if (m_transactionId.isBlank()) {
				// -- no transaction id -- 
				LOG.error("CreatePayment:apiStartPayment -- no transaction id for" +
						" customerId: {}, external account: {}.",
						cszCustomerId, cszExternalAcct);
			}
			
			// -- if this is autopay request and an automatic payment exists.. get out now --
			Optional<Boolean> bAutoPayEnabled = Optional.ofNullable( responseObject.getBoolean("automaticPaymentRuleEnabled"));
			if ((bAutoPayEnabled.get() == true) && (ceTransType.equals(TransactionType.automatic))) {
				LOG.error("CreatePayment:apiStartPayment -- automatic payment already exists for" + 
						" customerId: {}, external account: {}.",
						cszCustomerId, cszExternalAcct);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "Automatic payment alread exists";
				
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiStartPayment -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "Invalid customer id or accountId.";
			return lbRet;
		}

		lbRet = true;
		return lbRet;
		
	}
	
	/**
	 * Adds the migrated token as part of the payment process. Must call start first
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param pmtAccount
	 * @return
	 */
	private Boolean apiAddToken(final String cszCustomerId,
								final String cszInternalAcct,
								final PmtAcct pmtAccount) {
		Boolean lbRet = false;

		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.AddToken");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		
		try {
			final JSONObject data = new JSONObject();
			
			// -- base requirements --
			data.put("securityToken", lszSecurityToken);
			data.put("customerId", cszCustomerId);
			data.put("accountId", cszInternalAcct);
			data.put("transactionId", m_transactionId);

			// -- add call specific data --
			data.put("sourceType", pmtAccount.m_payType.toString());

			switch (pmtAccount.m_payType) {
			case debit:	
			case credit:
				data.put("sourceValue", pmtAccount.m_szTokenId);
				break;
			case bank:
			case sepa:
				data.put("soruceValue", pmtAccount.m_szBankRouting + "|" + pmtAccount.m_szBankAcct);
				break;
			default:
				LOG.error("CreatePayment:apiAddToken -- invalid source type for paymnent {}.",
						pmtAccount.m_payType.toString());;
			}

			data.put("accountHolder", pmtAccount.m_szAcctHolder);
			data.put("maskedNumber", pmtAccount.m_szMaskedName);
			data.put("expiration", pmtAccount.m_szExpiration);

			// -- create the request --
			final RequestEntity<String> request = RequestEntity
					.post(new URI(lszURL + lszCallName))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.body(data.toString());

			// -- post the request
			RestTemplate template = new RestTemplate();
			final ResponseEntity<String> response = template.postForEntity(
					lszURL + lszCallName, request, String.class
					);
			
			// -- process the response --
			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiAddToken -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiAddToken -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = "--";
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiAddToken -- failed, catching errro.", e);
			return lbRet;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lbRet = true;
		
		return lbRet;
		
	}
	
	private Boolean apiScheduledPayment(final IScheduledPayment pmt) {
		Boolean lbRet = false;

		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.OneTimePayment");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		
		try {
			final JSONObject data = new JSONObject();
			
			// -- base requirements --
			data.put("securityToken", lszSecurityToken);
			data.put("customerId", pmt.getCustomerId());
			data.put("accountId", pmt.getInternalAccount());
			data.put("transactionId", m_transactionId);

			// -- add call specific data --
			data.put("paymentDate", pmt.getPayDate());
			data.put("paymentAmount", pmt.getPayAmount());

			// -- create the request --
			final RequestEntity<String> request = RequestEntity
					.post(new URI(lszURL + lszCallName))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.body(data.toString());

			// -- post the request
			RestTemplate template = new RestTemplate();
			final ResponseEntity<String> response = template.postForEntity(
					lszURL + lszCallName, request, String.class
					);
			
			// -- process the response --
			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiScheduledPayment -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiScheduledPayment -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = pmt.getDisplayAccount();
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiScheduledPayment -- failed, catching errro.", e);
			return lbRet;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lbRet = true;
		
		return lbRet;
		
	}
	
	private  Boolean apiAutomaticPaymentRule(IAutomaticPaymentRule pmt) {
		Boolean lbRet = false;
		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.AutomaticPaymentRule");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		
		try {
			final JSONObject data = new JSONObject();
			
			// -- base requirements --
			data.put("securityToken", lszSecurityToken);
			data.put("customerId", pmt.getCustomerId());
			data.put("accountId", pmt.getInternalAccount());
			data.put("transactionId", m_transactionId);

			// -- add call specific data --
			data.put("paymentDateRule", "dayOfMonth=" + pmt.getPayDay());
			data.put("paymentAmountRule", "billAmount");
			data.put("paymentCountRule", "untilCanceled");

			// -- create the request --
			final RequestEntity<String> request = RequestEntity
					.post(new URI(lszURL + lszCallName))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.body(data.toString());

			// -- post the request
			RestTemplate template = new RestTemplate();
			final ResponseEntity<String> response = template.postForEntity(
					lszURL + lszCallName, request, String.class
					);
			
			// -- process the response --
			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiAutomaticPaymentRule -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiAutomaticPaymentRule -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = pmt.getDisplayAccount();
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiAutomaticPaymentRule -- failed, catching errro.", e);
			return lbRet;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lbRet = true;
		
		return lbRet;
		
	}
	
}
	

