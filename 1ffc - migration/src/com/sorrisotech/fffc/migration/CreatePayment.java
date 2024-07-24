/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sorrisotech.fffc.migration.GetInternalAccountInfo.InternalAccts;
import com.sorrisotech.fffc.migration.PmtAcct.PayType;

/**
 *  Objec to sequence through the agent API and create either a 
 *  one time payment or recurring payment rule.
 */
public class CreatePayment {

	private static final Logger LOG = LoggerFactory.getLogger(CreatePayment.class);
	
	
	private enum TransactionType {oneTime, automatic};
	private String m_transactionId = null;

	public Boolean createScheduledPayment(final IScheduledPayment schedPayment) {
		Boolean lbRet= false;
		
		lbRet = apiStartPayment ( schedPayment.getCustomerId(),
								  schedPayment.getInternalAccount(),
								  schedPayment.getDisplayAccount(),
								  TransactionType.oneTime);
		
		if (lbRet) {
			lbRet = apiAddToken(schedPayment.getCustomerId(),
					  			schedPayment.getInternalAccount(),
					  			schedPayment.getPayAcct() );
		}
		
		return lbRet;
	}
	
	public Boolean createAutomaticPaymentRule(final IAutomaticPaymentRule autoPayRule) {
		Boolean lbRet = false;

		lbRet = apiStartPayment ( autoPayRule.getCustomerId(),
				autoPayRule.getInternalAccount(),
				autoPayRule.getDisplayAccount(),
				  TransactionType.automatic);

		if (lbRet) {
			lbRet = apiAddToken(autoPayRule.getCustomerId(),
								autoPayRule.getInternalAccount(),
								autoPayRule.getPayAcct() );
		}

		return lbRet;
		
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
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiStartPayment -- failed, catching error.", e);
			return lbRet;
		}

		lbRet = true;
		return lbRet;
		
	}
	
	private Boolean apiAddToken(final String cszCustomerId,
								final String cszInternalAcct,
								final PmtAcct pmtAccount) {
		Boolean lbRet = false;

		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.AddToken");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		try {
			RestTemplate restTemplate = new RestTemplate();
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

			data.put("accountHolder", "JohnK"/*pmtAccount.m_szAcctHolder*/);
			data.put("maskedNumber", pmtAccount.m_szMaskedName);
			data.put("expiration", pmtAccount.m_szExpiration);

			final RequestEntity<String> request = RequestEntity
					.post(new URI(lszURL + lszCallName))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.body(data.toString());
			// post request
			RestTemplate template = new RestTemplate();
			final ResponseEntity<String> response = template.postForEntity(
					lszURL + lszCallName, request, String.class
					);

			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiAddToken -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiAddToken -- error details error: {}, payload: {}", szErrorCode, szPayload);
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
	
	private Boolean apiScheduledPayment() {
		Boolean lbRet = false;
		
		return lbRet;
		
	}
	
	private  Boolean apiAutomaticPaymentRule() {
		Boolean lbRet = false;
		
		return lbRet;
		
	}
	
}
	

