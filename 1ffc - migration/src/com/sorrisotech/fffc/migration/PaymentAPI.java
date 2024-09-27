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
 *  Object uses agent api to:
 *  	a. create a one time payment or recurring payment rule.
 *  	b. perform other maintenance functions on payments.
 *  
 *  There are settings in the properties file the provide the address
 *  and name of each api call as well as security.  They are documented
 *  there.  
 *  
 *  In addition, there are property file settings that can drive how this
 *  API creates recurring payments:
 *  
 *  	DeleteExistingAutoPayRule - if set to true will delete an 
 *  		existing autopay rule before creating this one if 
 *  		a rule does exist.
 *  
 *  	DeleteExistingScheduledPayment - if set to true, and 
 *  		DeleteExistingAutoPayRule is true, any existing scheduled
 *  		payments with the attribute "automatic" will be deleted before 
 *  		creating the new autopay rule.
 *  			
 *  
 *  
 */
public class PaymentAPI {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentAPI.class);
	
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
			lbRet = apiCreateScheduledPayment(schedPayment);
		}
		
		return m_retCode;
	}
	
	/**
	 * Creates an automatic payment rule
	 * 
	 * @param autoPayRule
	 * @return
	 */
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
			lbRet = apiCreateAutomaticPaymentRule(autoPayRule);
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
			final String cszInternalAcct,			
			final String cszExternalAcct) {

		m_retCode = null;
		
		Boolean lbRet = apiIsThereAnAutomaticPaymentForAccount(cszCustomerId, cszInternalAcct, cszExternalAcct);
		LOG.info("PaymentAPI:getAutomaticPaymentRule -- satus on automatic payment for external account {} is {}.",
					cszExternalAcct, lbRet.toString());
		return m_retCode;
	}
	
	/**
	 * reports on a scheduled payment rule
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @param cszPaymentId
	 * @return
	 */
	public WebSvcReturnCode getScheduledPayment(
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct,
			final String cszPaymentId ) {

		m_retCode = null;
		
		Boolean lbRet = apiIsThereAScheduledPaymentForPmtId(
									cszCustomerId,
									cszInternalAcct,
									cszExternalAcct,
									cszPaymentId );
		LOG.info("PaymentAPI:getScheduledPayment -- status on scheduled payment {} for external account {} is {}.",
				cszPaymentId, cszExternalAcct, lbRet.toString());
		return m_retCode;
	}
	
	/**
	 * deletes the recurring payment rule associated with an account if it exists.
	 * 
	 * @param IAutomaticPaymentRule
	 *
	 * @return
	 */
	public WebSvcReturnCode deleteAutomaticPaymentRuleAndPmts (final IAutomaticPaymentRule autoPayRule) {
		
		m_retCode = null;
		
		// -- get rid of old autopay if configured to do that --
		Boolean lbRet = cleanupOldAutoPay( autoPayRule.getCustomerId(),
								   autoPayRule.getInternalAccount(),
								   autoPayRule.getDisplayAccount());	
		
		LOG.info("PaymentAPI:deleteRecurringPaymentRuleAndPayments -- returned for external account {}, value: {}",
				autoPayRule.getDisplayAccount(), lbRet.toString());
				
		
		return m_retCode;
	
	}
	
	/**
	 * deletes a scheduled payment for an account, given the paymentId
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAccount
	 * @param cszExternalAccount
	 * @param cszPaymentId
	 * @return
	 */
	public WebSvcReturnCode deleteScheduledPayment (
		final String cszCustomerId,
		final String cszInternalAccount,
		final String cszExternalAccount,
		final String cszPaymentId) {

		m_retCode = null;
		Boolean lbret = apiDeleteScheduledPaymentForAccount(
									cszCustomerId,
									cszInternalAccount,
									cszExternalAccount,
									cszPaymentId);
		LOG.info("PaymentAPI:deleteScheduledPayment -- returned for external account {}, value: {}",
				cszExternalAccount, lbret);
		
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
				LOG.error("PaymentAPI:apiStartPayment -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiStartPayment -- error details error: {}, payload: {}", szErrorCode, szPayload);
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
				LOG.error("PaymentAPI:apiStartPayment -- no transaction id for" +
						" customerId: {}, external account: {}.",
						cszCustomerId, cszExternalAcct);
			}
			
			// -- if this is autopay request and an automatic payment exists.. get out now --
			Optional<Boolean> bAutoPayEnabled = Optional.ofNullable( responseObject.getBoolean("automaticPaymentRuleEnabled"));
			if ((bAutoPayEnabled.get() == true) && (ceTransType.equals(TransactionType.automatic))) {
				LOG.error("PaymentAPI:apiStartPayment -- automatic payment already exists for" + 
						" customerId: {}, external account: {}.",
						cszCustomerId, cszExternalAcct);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "Automatic payment alread exists";
				
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:apiStartPayment -- failed, catching error.", e);
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
				LOG.error("PaymentAPI:apiAddToken -- invalid source type for paymnent {}.",
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
				LOG.error("PaymentAPI:apiAddToken -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiAddToken -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = "--";
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:apiAddToken -- failed, catching errro.", e);
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
	private Boolean apiCreateScheduledPayment(final IScheduledPayment pmt) {
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
				LOG.error("PaymentAPI:apiScheduledPayment -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiScheduledPayment -- error details error: {}, payload: {}", szErrorCode, szPayload);
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
				LOG.error("PaymentAPI:apiScheduledPayment -- error returned empty paymentId or payment status");
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = pmt.getDisplayAccount();
				m_retCode.error = "200";
				m_retCode.payload = "Returned success but with empty payment information";
				 
				return lbRet;
			}

			pmt.setPayTransactionInfo(m_szPmtId, m_szPmtStatus);
		
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:apiScheduledPayment -- failed, catching errro.", e);
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
	private  Boolean apiCreateAutomaticPaymentRule(IAutomaticPaymentRule pmt) {
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
				LOG.error("PaymentAPI:apiAutomaticPaymentRule -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiAutomaticPaymentRule -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = pmt.getDisplayAccount();
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:apiAutomaticPaymentRule -- failed, catching error.", e);
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
	 * cleans up (deletes) old automatic payments if configured to do
	 * so
	 * 
	 * @param cszCustomerId
	 * @param cszExternalAcct
	 * @param cszInternalAcct
	 * @return
	 */
	private Boolean cleanupOldAutoPay(
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct) {
		Boolean bRetAutopay = false;
		Boolean bRetSched = false;
		String szAutopayMessage = null;
		String szSchedMessage = null;
		
		final String cszDeleteAutoPayFirst = "DeleteExistingAutoPayRule";
		final String cszDeleteScheduledPay = "DeleteExistingScheduledPayment";
		
		// -- if configured to delete automatic payment rule, delete it
		final Boolean bDeleteRule = Boolean.parseBoolean(Config.get(cszDeleteAutoPayFirst));
		final Boolean bDeleteScheduled = Boolean.parseBoolean(Config.get(cszDeleteScheduledPay));
		if (bDeleteRule) {
			bRetAutopay = apiIsThereAnAutomaticPaymentForAccount(cszCustomerId, cszInternalAcct, cszExternalAcct);

			if (bRetAutopay) { 
				bRetAutopay = apiDeleteAutomaticPaymentRuleForAccount(cszCustomerId, cszInternalAcct, cszExternalAcct);
				// -- got one then delete it --	
				LOG.info("PaymentAPI:cleanupOldAutoPay -- removed autopay for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
				if (bRetAutopay) {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- removed autopay for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
					szAutopayMessage = "Autopay rule deleted.";
				} else {
					LOG.error("PaymentAPI:cleanupOldAutoPay -- failed to remove autopay for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
				}
				
			} else {
				if (null != m_retCode && m_retCode.success) {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- no autopay found for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
					bRetAutopay = true;
					szAutopayMessage = "No Autopay found.";
				} else {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- error deleting autopay for customerId {}, externalAcct {} " + 
							"(if 400 error probably no account yet).",
							cszCustomerId, cszExternalAcct);
					if (null != m_retCode && m_retCode.error.equals("400")) {
						LOG.info("PaymentAPI:cleanupOldAutoPay -- error deleting autopay for customerId {}, externalAcct {} " + 
								"(no account yet).",
								cszCustomerId, cszExternalAcct);
						bRetAutopay = true;
						szAutopayMessage = "No portal account yet.";
						
					} else {
						LOG.info("PaymentAPI:cleanupOldAutoPay -- error deleting autopay for customerId {}, externalAcct {}.",
								cszCustomerId, cszExternalAcct);
						bRetAutopay = false;
					}
				}
			}
		} else {
			LOG.info("PaymentAPI:cleanupOldAutoPay -- delete autopay not selected for customerId {}, externalAcct {}.",
					cszCustomerId, cszExternalAcct);
			bRetAutopay = true;
			szAutopayMessage = "Delete autopay option not selected.";
		}

		// -- even if we don't have a current autopay we may have a scheduled payment for that around 
		//		somewhere --
		if (bDeleteScheduled && bRetAutopay) {
			// -- if configured to delete scheduled payments nuke them too --
			String szPmtId = apiGetPaymentIdForAutoScheduledPaymentForAccount(cszCustomerId, cszInternalAcct, cszExternalAcct);
			if (null != szPmtId) {
				bRetSched = apiDeleteScheduledPaymentForAccount(cszCustomerId, cszInternalAcct, cszExternalAcct, szPmtId);
				if (bRetSched) {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- removed scheduled pmt created by autopay for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
					bRetSched = true;
					szSchedMessage = "Scheduled autopay deleted.";
				}
			} else {
				if (null != m_retCode && m_retCode.error.equals("400")) {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- error deleting existing pmt for customerId {}, externalAcct {} " + 
							"(no account yet).",
							cszCustomerId, cszExternalAcct);
					bRetSched = true;
					szSchedMessage = "No portal account yet.";
				} else if (null != m_retCode && m_retCode.success) {
					LOG.info("PaymentAPI:cleanupOldAutoPay -- no scheduled pmt created by autopay for customerId {}, externalAcct {}.",
						cszCustomerId, cszExternalAcct);
					bRetSched = true;
					szSchedMessage = "No scheduled autopay to delete.";
				} else {
					LOG.error("PaymentAPI:cleanupOldAutoPay -- failed call to delete scheduled pmt created by autopay for customerId {}, externalAcct {}.",
							cszCustomerId, cszExternalAcct);
				}
			}
		} else {
			LOG.info("PaymentAPI:cleanupOldAutoPay -- delete scheduled pmt by autopay not selected for customerId {}, externalAcct {}.",
					cszCustomerId, cszExternalAcct);
			szSchedMessage = "Delete sched autopay not selected.";
			bRetSched = true;
		}
		
		if (bRetAutopay && bRetSched) {
			m_retCode = new WebSvcReturnCode(true);
			m_retCode.displayName = cszExternalAcct;
			m_retCode.payload = szAutopayMessage + " | " + szSchedMessage;
			m_retCode.error = "200";
		}
		return (bRetAutopay && bRetSched);
	}
	
	/**
	 * returns information about an automatic payment rule for an account
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @return
	 */
	private Boolean apiIsThereAnAutomaticPaymentForAccount (
												final String cszCustomerId,
			   									final String cszInternalAcct,			
												final String cszExternalAcct) {
		Boolean lbRet = false;
		
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
				LOG.error("PaymentAPI:apiIsThereAnAutomaticPaymentForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiIsThereAnAutomaticPaymentForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
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
				LOG.info("PaymentAPI:apiIsThereAnAutomaticPaymentForAccount -- " +
						"Autopayment rule for customerId {}, external account {}, and internal account {} -- " +
						"payAcct: {}, payType: {}, dateRule: {}, amtRule: {}, countRule().", 
						cszCustomerId, cszExternalAcct, cszInternalAcct,
						lszPaymentAccount, lszPaymentAccountType, lszPaymentDateRule,
						lszPaymentAmountRule, lszPaymentCountRule);
				lbRet = true;
			
			}
			else {
				LOG.info("PaymentAPI:apiIsThereAnAutomaticPaymentForAccount -- " +
						"No autopayment rule for customerId {}, external account {}, and internal account {}", 
						cszCustomerId, cszExternalAcct, cszInternalAcct );
				m_retCode = new WebSvcReturnCode(true);
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = "200";
				m_retCode.payload = "No Automatic payment exists.";
				lbRet = true;
				
			}
			
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:apiIsThereAnAutomaticPaymentForAccount -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.statusCode = "400";
			m_retCode.error = "400";
			m_retCode.payload = "Invalid customer id or accountId.";
		}
		return lbRet;
	}
	
	/**
	 * returns true if there is a scheduled payment with the specified paymentID for an account
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @param cszPaymentId
	 * @return
	 */
	private Boolean apiIsThereAScheduledPaymentForPmtId(
							final String cszCustomerId,
							final String cszInternalAcct,			
							final String cszExternalAcct,
							final String cszPaymentId) {
		Boolean lbRet = false;
		
		JSONArray scheduledPayments = apiGetScheduledPaymentsForAccount(
				cszCustomerId,
				cszInternalAcct,
				cszExternalAcct);
		
		boolean bFoundPayment = false;
		if (null != scheduledPayments) {
			for (int i = 0;  i< scheduledPayments.length(); i++) {
				JSONObject currObject = scheduledPayments.getJSONObject(i);
				if (null != currObject) {
					String pmtId = currObject.getString("paymentId");
					if ((null != pmtId) && (pmtId.equals(cszPaymentId))) {
						bFoundPayment = true;
						break;
					}
				} 
			}
		} else {
			// -- a null scheduled payments object means we had an error in the 
			//		lower level api call.
			return lbRet;
		}
		
		
			
		if (bFoundPayment == true) {
			
			m_retCode = new WebSvcReturnCode(true);
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "200";
			m_retCode.payload = "Scheduled Payment " + cszPaymentId + " for account " + cszExternalAcct + " is validated.";
			lbRet = true;
		}
		else {
			LOG.info("PaymentAPI:apiIsThereAScheduledPaymentForPmtId -- " +
					"No autopayment rule for customerId {}, external account {}, and internal account {}", 
					cszCustomerId, cszExternalAcct, cszInternalAcct );
			m_retCode = new WebSvcReturnCode(true);
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "200";
			m_retCode.payload = "Scheduled Payment " + cszPaymentId + " for account " + cszExternalAcct + " does not exist.";
		}
			
		return lbRet;
		
	}
	
	/**
	 * Returns the paymentId of a scheduled payment created by an automatic payment rule on an account if
	 * 	one exists, otherwise null.
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @return
	 */
	private String apiGetPaymentIdForAutoScheduledPaymentForAccount(
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct) {
		String szRetValue = null;
		
		JSONArray scheduledPayments = apiGetScheduledPaymentsForAccount(
				cszCustomerId,
				cszInternalAcct,
				cszExternalAcct);

		boolean bFoundPayment = false;
		if (null != scheduledPayments) {
			for (int i = 0;  i< scheduledPayments.length(); i++) {
				JSONObject currObject = scheduledPayments.getJSONObject(i);
				if (null != currObject) {
					String pmtCategory = currObject.getString("paymentCategory");
					if ((null != pmtCategory) && (pmtCategory.equals("automatic"))) {
						String pmtId = currObject.getString("paymentId");
						if (null != pmtId) {
							bFoundPayment = true;
							szRetValue = pmtId;
							break;
						}
					}
				} 
			}
		} else {
			// -- a null scheduled payments object means we had an error in the 
			//		lower level api call.
			return szRetValue;
		}
		
		
		if (bFoundPayment == true) {
			
			m_retCode = new WebSvcReturnCode(true);
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "200";
			m_retCode.payload = "Scheduled Payment in automatic category found:" + szRetValue + " for account " + cszExternalAcct + ".";
		}
		else {
			LOG.info("PaymentAPI:getPaymentIdForAutoScheduledPaymentForAccount -- " +
					"No autopay payment scheduled for customerId {}, external account {}, and internal account {}", 
					cszCustomerId, cszExternalAcct, cszInternalAcct );
			m_retCode = new WebSvcReturnCode(true);
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "200";
			m_retCode.payload = "There is no autopay scheduled payment for account " + cszExternalAcct + ".";
		}
		
		return szRetValue;
	}
	
	/**
	 * Returns an array of scheduled payments for an account
	 * 
	 * @param cszCustomerId
	 * @param cszInternalAcct
	 * @param cszExternalAcct
	 * @return
	 */
	private JSONArray apiGetScheduledPaymentsForAccount(
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct) {
		JSONArray aReturn = null;

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
				LOG.error("PaymentAPI:getScheduledPaymentForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:getScheduledPaymentForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return aReturn;
			}
			
			JSONObject responseObject = new JSONObject(response.getBody());
			JSONArray  scheduledPayments = responseObject.getJSONArray("scheduledPayments");
			
			if (null == scheduledPayments) {
				LOG.info("PaymentAPI:apiGetScheduledPaymentForAccounts -- no scheduled payments for " +
								"customer id {}, externalAccountId{}", cszCustomerId, cszExternalAcct);
				aReturn = new JSONArray(); // return an empty JSONArray --
			}else {
				LOG.info("PaymentAPI:apiGetScheduledPaymentForAccounts -- returning scheduled payments for " +
						"customer id {}, externalAccountId{}", cszCustomerId, cszExternalAcct);
				aReturn = scheduledPayments; // -- return the scheduled payments --
			}
			
		} catch (JSONException | RestClientException e) {
			LOG.error("PaymentAPI:getScheduledPaymentForAccount -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "Invalid customer id or accountId.";
		}

		return aReturn;
	}
	
    /** deletes an automatic payment rule
     * 
     * @param cszCustomerId
     * @param cszInternalAcct
     * @param cszExternalAcct
     * @return
     */
    private Boolean apiDeleteAutomaticPaymentRuleForAccount(
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct) {
    	Boolean lbRet = false;

    	// -- get configured parameters -- 
    	String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.DeleteAutomatiPaymentRule");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		try {
			final JSONObject data = new JSONObject();
			
			// -- base requirements --
			data.put("securityToken", lszSecurityToken);
			data.put("customerId", cszCustomerId);
			data.put("accountId", cszInternalAcct);

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
				LOG.error("PaymentAPI:apiDeleteAutomaticPaymentRuleForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiDeleteAutomaticPaymentRuleForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException | URISyntaxException e) {
			LOG.error("PaymentAPI:apiDeleteAutomaticPaymentRuleForAccount -- failed, catching errro.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "JSON RestClientException on call";
			return lbRet;
		}
		
		lbRet = true;
    	
    	return lbRet;
    }

    /**
     * Deletes a scheduled payment for an account, given the payment id
     * 
     * @param cszCustomerId
     * @param cszInternalAcct
     * @param cszExternalAcct
     * @param cszPaymentId
     * @return
     */
    private Boolean apiDeleteScheduledPaymentForAccount (
			final String cszCustomerId,
			final String cszInternalAcct,			
			final String cszExternalAcct,
			final String cszPaymentId) {
		Boolean lbRet = false;
	
    	// -- get configured parameters -- 
    	String lszURL = Config.get("apiPay.URLBase");
		String lszCallName = Config.get("apiPay.DeleteScheduledPayment");
		String lszSecurityToken = Config.get("apiPay.SecurityToken");

		try {
			final JSONObject data = new JSONObject();
			
			// -- base requirements --
			data.put("securityToken", lszSecurityToken);
			data.put("customerId", cszCustomerId);
			data.put("accountId", cszInternalAcct);
			data.put("paymentId", cszPaymentId);
			
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
				LOG.error("PaymentAPI:apiDeleteScheduledPaymentForAccount -- call failed with error {}", statusCode.value());
				JSONObject responseObject = new JSONObject(response.getBody());
				String szErrorCode = Optional.ofNullable( responseObject.getString("error")).orElse("");
				String szPayload = Optional.ofNullable( responseObject.getString("payload")).orElse("");
				LOG.error("PaymentAPI:apiDeleteScheduledPaymentForAccount -- error details error: {}, payload: {}", szErrorCode, szPayload);
				m_retCode = new WebSvcReturnCode();
				m_retCode.displayName = cszExternalAcct;
				m_retCode.error = szErrorCode;
				m_retCode.payload = szPayload;
				return lbRet;
			}
		
		} catch (JSONException | RestClientException | URISyntaxException e) {
			LOG.error("PaymentAPI:apiDeleteScheduledPaymentForAccount -- failed, catching error.", e);
			m_retCode = new WebSvcReturnCode();
			m_retCode.displayName = cszExternalAcct;
			m_retCode.error = "400";
			m_retCode.payload = "JSON RestClientException on call";
			return lbRet;
		}
		
		lbRet = true;
	
		return lbRet;
	}
    
}
	

