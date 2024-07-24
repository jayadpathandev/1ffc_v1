/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 40 Nagog Park
 * Acton, MA 01720
 * +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona 
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition", 
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network", 
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay", 
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active 
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.svcs.agentpay.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sorrisotech.fffc.agent.pay.ApiPayDao;
import com.sorrisotech.fffc.agent.pay.Fffc;
import com.sorrisotech.fffc.agent.pay.data.ApiPayUser;
import com.sorrisotech.payment.api.ResponseCode;
import com.sorrisotech.svcs.agentpay.api.IApiAgentPay;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;
import com.sorrisotech.utils.AppConfig;
import com.sorrisotech.utils.Rest;

/************************************************************************
 * The implementation class of AddMigratedPaymentSource endpoint,
 * This will add the migrated payment source.
 * 
 * @author Rohit Singh
 * @since 19-July-2024
 */
public class AddMigratedPaymentSource extends AddMigratedPaymentSourceBase {
	
	/************************************************************************
	 * The generated serialversionUID for class.
	 */
	private static final long serialVersionUID = 779096163681383088L;

	/************************************************************************
	 * The default logger.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(AddMigratedPaymentSource.class);
	
	/************************************************************************
	 * The helper dao for getting user info.
	 */
	private ApiPayDao m_cApiPayDao = Fffc.apiPay();
	
	/************************************************************************
	 * The rest template object for calling external endpoints.
	 */
	private RestTemplate m_cRestTemplate = Rest.template();

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		final String customerId = request.getString(IApiAgentPay.AddMigratedPaymentSource.customerId);
		final String accountId  = request.getString(IApiAgentPay.AddMigratedPaymentSource.accountId);
		final String sourceType = request.getString(IApiAgentPay.AddMigratedPaymentSource.sourceType);
		final String sourceValue = request.getString(IApiAgentPay.AddMigratedPaymentSource.sourceValue);
		final String accountHolder = request.getString(IApiAgentPay.AddMigratedPaymentSource.accountHolder);
		final String maskedNumber = request.getString(IApiAgentPay.AddMigratedPaymentSource.maskedNumber);
		final String expiration = request.getString(IApiAgentPay.AddMigratedPaymentSource.expiration);
		
		request.setToResponse();

		ApiPayUser cUser = m_cApiPayDao.user(customerId);
		
		if (cUser == null) {
			m_cLog.info("Could not find user with customerId : {}", customerId);
			request.set(IApiAgentPay.AddMigratedPaymentSource.addResponseStatus, "400");
			request.setRequestStatus(ServiceAPIErrorCode.Success);
			return ServiceAPIErrorCode.Success;
		}
		
		final var account = m_cApiPayDao.lookupAccount(customerId, accountId);
		
		if (account == null) {
			m_cLog.info("Could not find account for customerId : {} and accountId : {}.", customerId, accountId);
			request.set(IApiAgentPay.AddMigratedPaymentSource.addResponseStatus, "400");
			request.setRequestStatus(ServiceAPIErrorCode.Success);
			return ServiceAPIErrorCode.Success;			
		}
		
		final String szUserID = cUser.userId.toPlainString();
		
		final String szPaymentBaseUrl = AppConfig.get("epayment.url");
		
		if (ObjectUtils.isEmpty(szPaymentBaseUrl)) {
			m_cLog.warn("Unable to find the base URL for payment");
			request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		final String szUrl = szPaymentBaseUrl + "addPaymentSource";
		
		 // Construct the JSON request body
        JSONObject requestBody = new JSONObject();
        JSONObject source = new JSONObject();
        JSONObject creditCard = new JSONObject();
        JSONObject accountHolderMap = new JSONObject();

        switch (sourceType) {
	        case "debit":
	        case "credit":
	            creditCard.put("accountType", sourceType);
	            creditCard.put("cardNumber", "ACITOKEN|" + sourceValue);
	            creditCard.put("cardType", "");
	            creditCard.put("cvvCode", "");
	            creditCard.put("expirationDate", expiration);
	            break;
	        case "bank":
	        case "sepa":
	        default:
				m_cLog.warn("Unsupported Payment type {}.", sourceType);
				request.setRequestStatus(ServiceAPIErrorCode.Failure);
				return ServiceAPIErrorCode.Failure;
        }

        accountHolderMap.put("name", accountHolder);
        accountHolderMap.put("street1", "");
        accountHolderMap.put("street2", "");
        accountHolderMap.put("city", "");
        accountHolderMap.put("state", "");
        accountHolderMap.put("postalCode", "");
        accountHolderMap.put("country", "");

        source.put("creditCard", creditCard);
        source.put("accountHolder", accountHolderMap);
        source.put("sourceType", sourceType);
        source.put("nickName", "Migrated token " + maskedNumber.substring(maskedNumber.length() - 4));
        source.put("default", false);
        source.put("internalToken", true);
        source.put("paymentGroup", "");
        source.put("hostedSourceCapture", true);

        requestBody.put("source", source);
        requestBody.put("token", "");
        requestBody.put("userId", szUserID);
        requestBody.put("saveSource", true);
        requestBody.put("appType", "b2c");
        requestBody.put("paymentGroup", "");

        try {
            RequestEntity<String> cRequestEntity = RequestEntity
                .post(szUrl)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody.toString());
            
            ResponseEntity<String> cResponse = m_cRestTemplate.exchange(cRequestEntity, String.class);
            
            if (cResponse.getStatusCode().is2xxSuccessful()) {
            	
            	JSONObject cResponseObject = new JSONObject(cResponse.getBody());
            	String szResponseCode = cResponseObject.getString("responseCode");
            	String szToken = cResponseObject.getString("token");
            	
            	if (ResponseCode.ADD_PAYMENT_SOURCE_SUCCESS.getCode().equals(szResponseCode)) {
            		m_cLog.info("Payment Source Added successfully.");
            		request.set(IApiAgentPay.AddMigratedPaymentSource.token, szToken);
            		request.set(IApiAgentPay.AddMigratedPaymentSource.addResponseStatus, "201");
            		request.setRequestStatus(ServiceAPIErrorCode.Success);
            		return ServiceAPIErrorCode.Success;
            	}
            }
        } catch (RestClientException ex) {
        	
        	if (ex instanceof HttpClientErrorException) {
            	m_cLog.info("Unable to add payment source invalid data procided");
    			request.set(IApiAgentPay.AddMigratedPaymentSource.addResponseStatus, "400");
    			request.setRequestStatus(ServiceAPIErrorCode.Success);
    			return ServiceAPIErrorCode.Success;
        	} else {
        		m_cLog.error("Exception occurred while adding payment source", ex);
        		request.setRequestStatus(ServiceAPIErrorCode.Failure);
        		return ServiceAPIErrorCode.Failure;
        	}
        }
        
        m_cLog.error("Unable to add payment source");
        request.setRequestStatus(ServiceAPIErrorCode.Failure);
        return ServiceAPIErrorCode.Failure;
	}

}
