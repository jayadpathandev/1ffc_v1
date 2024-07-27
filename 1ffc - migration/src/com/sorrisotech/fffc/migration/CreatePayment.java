/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
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
	 * reports on an automatic payment rule
	 * 
	 * @param cszCustomerId
	 * @param cszExternalAcct
	 * @param cszInternalAcct
	 * @return
	 */
	public WebSvcReturnCode getAutomaticPaymentRule(
			final String cszCustomerId,
			final String cszExternalAcct,			
			final String cszInternalAcct) {

		m_retCode = null;
		
		Boolean lbRet = apiGetAutomaticPaymentForAccount(cszCustomerId, cszExternalAcct, cszInternalAcct);
		LOG.info("CreatePayment:getAutomaticPaymentRule -- satus on automatic payment for external account {} is {}.",
					cszExternalAcct, lbRet.toString());
		return m_retCode;
	}
	
	/**
	 * reports on a scheduled payment rule
	 * 
	 * @param cszCustomerId
	 * @param cszExternalAcct
	 * @param cszInternalAcct
	 * @param cszPaymentId
	 * @return
	 */
	public WebSvcReturnCode getScheduledPayment(
			final String cszCustomerId,
			final String cszExternalAcct,			
			final String cszInternalAcct,
			final String cszPaymentId ) {

		m_retCode = null;
		
		Boolean lbRet = apiGetScheduledPaymentForAccount(
									cszCustomerId,
									cszExternalAcct,
									cszInternalAcct,
									cszPaymentId);
		LOG.info("CreatePayment:getScheduledPayment -- status on scehduled payment {} for external account {} is {}.",
				cszPaymentId, cszExternalAcct, lbRet.toString());
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
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = "--";
			m_retCode.error = "400";
			m_retCode.payload = "JSON RestClientException on call";
			return lbRet;
		} catch (URISyntaxException e) {
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = "--";
			m_retCode.error = "400";
			m_retCode.payload = "URISyntaxException on call";
			e.printStackTrace();
			return lbRet;
		}

		lbRet = true;
		
		return lbRet;
		
	}
	
	/**
	 * Creates a scheduled payment
	 * 
	 * @param pmt
	 * @return
	 */
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
			JSONObject responseObject = new JSONObject(response.getBody());
			// -- grab the transaction for moving forward
			String m_szPmtId = Optional.ofNullable( responseObject.getString("paymentId")).orElse("");
			String m_szPmtStatus = Optional.ofNullable( responseObject.getString("status")).orElse("");
			
			
			if (m_szPmtId.isBlank() || m_szPmtStatus.isBlank()) {
				LOG.error("CreatePayment:apiScheduledPayment -- error returned empty paymentId or payment status");
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = pmt.getDisplayAccount();
				m_retCode.error = "200";
				m_retCode.payload = "Returned success but with empty payment information";
				 
				return lbRet;
			}

			pmt.setPayTransactionInfo(m_szPmtId, m_szPmtStatus);
		
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiScheduledPayment -- failed, catching errro.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = pmt.getDisplayAccount();
			m_retCode.error = "400";
			m_retCode.payload = "JSON RestClientException on call";
			return lbRet;
		} catch (URISyntaxException e) {
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = pmt.getDisplayAccount();
			m_retCode.error = "400";
			m_retCode.payload = "URISyntaxException on call";
			return lbRet;
		}

		lbRet = true;
		
		return lbRet;
		
	}
	
	/**
	 * Creates an automatic payment rule
	 * 
	 * @param pmt
	 * @return
	 */
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
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = pmt.getDisplayAccount();
			m_retCode.error = "400";
			m_retCode.payload = "JSON RestClientException on call";
			return lbRet;
		} catch (URISyntaxException e) {
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = pmt.getDisplayAccount();
			m_retCode.error = "400";
			m_retCode.payload = "URISyntaxException on call";
			e.printStackTrace();
		}

		lbRet = true;
		return lbRet;
		
	}
	
	/**
	 * returns information about an automatic payment rule for an account
	 * 
	 * @param cszCustomerId
	 * @param cszExternalAcct
	 * @param cszInternalAcct
	 * @return
	 */
	private Boolean apiGetAutomaticPaymentForAccount (
												final String cszCustomerId,
			   									final String cszExternalAcct,			
												final String cszInternalAcct) {
		Boolean lbRet = null;
		
		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.GetAutomaticPaymentRule");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");
		String url = UriComponentsBuilder.fromHttpUrl(lszURL + lszCallName)
				.queryParam("securityToken", lszSecurityToken)
				.queryParam("customerId", cszCustomerId)
				.queryParam("accountId", cszInternalAcct)
				.toUriString();

		try {
			// -- post request for account status to application server --
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response;
				response =  restTemplate.getForEntity(url, String.class);

			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:apiGetAutomaticPaymentForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:apiGetAutomaticPaymentForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
			
			JSONObject responseObject = new JSONObject(response.getBody());
	
			// -- do we have a rule? --
			Optional<Boolean> bAutoPayEnabled = Optional.ofNullable( responseObject.getBoolean("hasRule"));
			
			if (bAutoPayEnabled.get() == true) {
				
				// -- check return values --
				String lszNickname = Optional.ofNullable( responseObject.getString("nickName")).orElse("");
				String lszPaymentAccount = Optional.ofNullable( responseObject.getString("paymentAccount")).orElse("");
				String lszPaymentAccountType = Optional.ofNullable( responseObject.getString("paymentAcctType")).orElse("");
				String lszPaymentDateRule = Optional.ofNullable( responseObject.getString("paymentDateRule")).orElse("");
				String lszPaymentAmountRule = Optional.ofNullable( responseObject.getString("paymentAmountRule")).orElse("");
				String lszPaymentCountRule = Optional.ofNullable( responseObject.getString("paymentCountRule")).orElse("");
				LOG.info("CreatePayment:getAutomaticPaymentForAccount -- " +
						"Autopayment rule for customerId {}, external account {}, and internal account {} -- " +
						"payAcct: {}, payType: {}, dateRule: {}, amtRule: {}, countRule().", 
						cszCustomerId, cszExternalAcct, cszInternalAcct,
						lszPaymentAccount, lszPaymentAccountType, lszPaymentDateRule,
						lszPaymentAmountRule, lszPaymentCountRule);
				lbRet = true;
			
			}
			else {
				LOG.info("CreatePayment:getAutomaticPaymentForAccount -- " +
						"No autopayment rule for customerId {}, external account {}, and internal account {}", 
						cszCustomerId, cszExternalAcct, cszInternalAcct );
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "No Automatic payment exists.";
				
			}
			
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:apiGetAutomaticPaymentForAccount -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "Invalid customer id or accountId.";
		}
		return lbRet;
	}
	
	private Boolean apiGetScheduledPaymentForAccount(
							final String cszCustomerId,
							final String cszExternalAcct,			
							final String cszInternalAcct,
							final String cszPaymentId) {
		Boolean lbRet = false;
		
		// -- build the url --
		String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.GetScheduledPayments");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");
		String url = UriComponentsBuilder.fromHttpUrl(lszURL + lszCallName)
				.queryParam("securityToken", lszSecurityToken)
				.queryParam("customerId", cszCustomerId)
				.queryParam("accountId", cszInternalAcct)
				.toUriString();

		try {
			// -- post request for scheduled payments to application server --
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response;
				response =  restTemplate.getForEntity(url, String.class);

			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.isError()) {
				LOG.error("CreatePayment:getScheduledPaymentForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("CreatePayment:getScheduledPaymentForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
			
			JSONObject responseObject = new JSONObject(response.getBody());
			List<String> paymentIds = getValuesInObject(responseObject, "paymentId");
			
			boolean bFoundPayment = false;
			if (null != paymentIds) {
				for (String pmtid: paymentIds ) {
					if (cszPaymentId.equals(pmtid)) {
						bFoundPayment = true;
						break;
					}
				}
			}
			
			if (bFoundPayment == true) {
				
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "Scheduled Payment " + cszPaymentId + " for account " + cszExternalAcct + " is validated.";
				lbRet = true;
			}
			else {
				LOG.info("CreatePayment:getScheduledPaymentForAccount -- " +
						"No autopayment rule for customerId {}, external account {}, and internal account {}", 
						cszCustomerId, cszExternalAcct, cszInternalAcct );
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "Scheduled Payment " + cszPaymentId + " for account " + cszExternalAcct + " does not exist.";
			}
			
		} catch (JSONException | RestClientException e) {
			LOG.error("CreatePayment:getScheduledPaymentForAccount -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "Invalid customer id or accountId.";
		}
		
		return lbRet;
		
	}
	
	/**
	 * given a JSONobject, returns List of values for a given key
	 * 
	 * @param jsonObject
	 * @param key
	 * @return
	 */
    public List<String> getValuesInObject(JSONObject jsonObject, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (String currentKey : jsonObject.keySet()) {
            Object value = jsonObject.get(currentKey);
            if (currentKey.equals(key)) {
                accumulatedValues.add(value.toString());
            }

            if (value instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInObject((JSONObject) value, key));
            } else if (value instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInArray((JSONArray) value, key));
            }
        }

        return accumulatedValues;
    }

    /**
     * give JSONArray, returns a list of all objects contained therein
     * 
     * @param jsonArray
     * @param key
     * @return
     */
    public List<String> getValuesInArray(JSONArray jsonArray, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInArray((JSONArray) obj, key));
            } else if (obj instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInObject((JSONObject) obj, key));
            }
        }

        return accumulatedValues;
    }
}
	

