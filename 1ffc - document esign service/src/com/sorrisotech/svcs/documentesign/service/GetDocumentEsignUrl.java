package com.sorrisotech.svcs.documentesign.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.client.model.request.CreateSessionRequest.Party;
import com.sorrisotech.fffc.documentesign.service.Dao;
import com.sorrisotech.fffc.documentesign.service.PDFEsignService;
import com.sorrisotech.fffc.documentesign.service.WalletInfo;
import com.sorrisotech.svcs.documentesign.api.IApiDocumentEsign;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;
import com.sorrisotech.utils.Rest;

public class GetDocumentEsignUrl extends GetDocumentEsignUrlBase{

	private static final long serialVersionUID = -1133389163946546889L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GetDocumentEsignUrl.class);
	
	private static final Properties config = new Properties();
	
	private static SimpleDateFormat dateFormatter = null;
	
	static {
		try (var inputStream = GetDocumentEsignUrl.class.getClassLoader().getResourceAsStream("app-config.properties")) {
			
			if (inputStream != null) {
				config.load(inputStream);
				dateFormatter = new SimpleDateFormat(config.getProperty("pdf.effective.date.format"), Locale.US);
			}
		} catch (Exception e) {
			LOGGER.error("Could not load the properties file. {}", e);
		}
	}

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		final String monthlyContractedAmount = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.monthlyContractedAmount);
		final String displayAccount = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.displayAccount);
		final String internalAccount = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.internalAccount);
		final String customerId = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.customerId);
		final String extDocId = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.extDocId);
    	final String dateRule = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.dateRule);
    	final String sourceId = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.sourceId);
    	final String fullName = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.fullName);
    	final String email = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.email);
    	final String phone = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.phone);
    	final String flex1 = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.flex1);
    	final String flex2 = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.flex2);
    	final String flex3 = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.flex3);
    	final String flex4 = request.getString(IApiDocumentEsign.GetDocumentEsignUrl.flex4);
    	
    	request.setToResponse();
    	
    	final WalletInfo walletInfo = Dao.getInstance().getWalletInfo(sourceId);
    	
    	if (walletInfo == null) {
    		LOGGER.warn("Wallet info not found for source : {}", sourceId);
    		request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
    	}
    	
    	final Map<String, String> sourceMap = getSourceDetailsMap(sourceId);
    	final String monthlyPaymentAmount = monthlyContractedAmount;
    	
    	if (null == sourceMap || sourceMap.isEmpty() ) {
    		LOGGER.warn("Unable to finds the source for sourceId : {}", sourceId);
			request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
    	}
    	
    	if (null == monthlyPaymentAmount) {
    		LOGGER.warn("Unable to find the monthly payment for intenal account : {}", internalAccount);
			request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
    	}

    	final Map<String, String> pdfDetailsMap = new HashMap<>();
    	
    	if ( !"bank".equals( walletInfo.getSourceType()) ) {
    		pdfDetailsMap.put("EFFECTIVE_DATE", dateRule);
    		pdfDetailsMap.put("DATE_1", dateConverterToTimeZone(new Date(),
					ZoneId.of(config.getProperty("application.locale.time.zone.id"))));
    		pdfDetailsMap.put("FIRST_PMT_AMT", monthlyPaymentAmount);
    		pdfDetailsMap.put("MONTHLY_PMT_AMT", monthlyPaymentAmount);
    		pdfDetailsMap.put("EXTERNAL_ACCT", displayAccount);
    		pdfDetailsMap.put("NAME_ON_PMT_ACCOUNT", Optional.ofNullable(sourceMap.get("accountHolderName")).orElse(""));
    		
    		pdfDetailsMap.put("DEBIT_CARD_MASKED", walletInfo.getSourceNum());
    		pdfDetailsMap.put("DEBIT_CARD_EXPIRATION", walletInfo.getSourceExpiry());

    		pdfDetailsMap.put("BANK_ACCOUNT_MASKED", "");
    		pdfDetailsMap.put("BANK_NAME", "");
    		pdfDetailsMap.put("BANK_ROUTING_NUMBER", "");
    		pdfDetailsMap.put("CHECKING_SAVINGS", "");

    	} else {
    		
    		pdfDetailsMap.put("EFFECTIVE_DATE", dateRule);
    		pdfDetailsMap.put("DATE_1", dateConverterToTimeZone(new Date(),
					ZoneId.of(config.getProperty("application.locale.time.zone.id"))));
    		pdfDetailsMap.put("FIRST_PMT_AMT", monthlyPaymentAmount);
    		pdfDetailsMap.put("MONTHLY_PMT_AMT", monthlyPaymentAmount);
    		pdfDetailsMap.put("EXTERNAL_ACCT", displayAccount);
    		pdfDetailsMap.put("NAME_ON_PMT_ACCOUNT", Optional.ofNullable(sourceMap.get("accountHolderName")).orElse(""));
    		
    		pdfDetailsMap.put("DEBIT_CARD_MASKED", "");
    		pdfDetailsMap.put("DEBIT_CARD_EXPIRATION", "");

    		pdfDetailsMap.put("BANK_ACCOUNT_MASKED", walletInfo.getSourceNum());
    		pdfDetailsMap.put("BANK_NAME", Optional.ofNullable(sourceMap.get("bankName")).orElse(""));
    		pdfDetailsMap.put("BANK_ROUTING_NUMBER", Optional.ofNullable(sourceMap.get("routingNumber")).orElse(""));
    		pdfDetailsMap.put("CHECKING_SAVINGS", Optional.ofNullable(sourceMap.get("accountType")).orElse(""));
    	}
    	
		
		final var party = new Party(fullName, email, phone, null);
		
		final var accessToken = PDFEsignService.getInstance().getAccessToken();
		
		if (accessToken == null) {
			LOGGER.debug("Unable to get access token for esign");
    		request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		final var sessionId = PDFEsignService.getInstance().createNewEsignSession(
				List.of(party), 
				accessToken
		);
		
		if (sessionId == null) {
			LOGGER.debug("Unable to create host session id for esign");
    		request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		final Map<String, String> metadata = new HashMap<>();
		metadata.put("account", customerId);
		metadata.put("date", dateConverterToTimeZone(new Date(),
				ZoneId.of(config.getProperty("application.locale.time.zone.id"))));
		metadata.put("intAccId", internalAccount);
		metadata.put("extAccId", displayAccount);
		metadata.put("extDocId", extDocId);
		metadata.put("flex1", flex1);
		metadata.put("flex2", flex2);
		metadata.put("flex3", flex3);
		metadata.put("flex4", flex4);
		
		if (displayAccount != null && displayAccount.length() >= 4) {
			metadata.put("billId",  "EFT form - " + displayAccount.substring(displayAccount.length() - 4));
		} else {
			metadata.put("billId",  "EFT form - XXXX");
		}

		final boolean documentAdded = PDFEsignService.getInstance().addDocumentToSession(
				pdfDetailsMap, 
				sessionId, 
				accessToken,
				fullName,
				metadata
		);
		
		if (!documentAdded) {
			LOGGER.debug("Unable to add document to session: {}", sessionId);
    		request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		final var url = PDFEsignService.getInstance().getEsignUrlForSession(
				sessionId, 
				accessToken,
				fullName, 
				email
		);
		
		if (url == null) {
			LOGGER.debug("Unable to get the esign URL for session: {}", sessionId);
    		request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		request.set(IApiDocumentEsign.GetDocumentEsignUrl.SESSION_ID, sessionId);
		request.set(IApiDocumentEsign.GetDocumentEsignUrl.URL, url);
		request.setRequestStatus(ServiceAPIErrorCode.Success);
		return ServiceAPIErrorCode.Success;

	}
	
	private Map<String, String> getSourceDetailsMap(String sourceId) {
		final var restTemplate = Rest.template();

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final var requestBodyMap = Map.of("token", sourceId);

        final var objectMapper = new ObjectMapper();
        String requestBodyJson;
        
        try {
            requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);
        } catch (JsonProcessingException ex) {
        	LOGGER.error("Error converting Map to JSON: {}", ex);
            return null;
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson, headers);
        
        final var url = config.getProperty("epayment.url") + "getIIN";
        
        try {
	        ResponseEntity<String> responseEntity = restTemplate.exchange(
	        		url, 
	        		HttpMethod.POST, 
	        		requestEntity, 
	        		String.class
	        );
	
	        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
	        	LOGGER.error(
	        			"Error occurred while detokenizing source: {}, responseCode: {}, response: {}", 
	        			sourceId, 
	        			responseEntity.getStatusCode(), 
	        			responseEntity.getBody()
	        	);
	        	
	        	return null;
	        }
	        
        	final var data = objectMapper.readTree(responseEntity.getBody()).get("iin").asText();
        	
            if (data == null || data.isBlank()) return null;
            
            return Arrays.asList(data.split("\\|")).stream()
	            .map(val -> val.split("="))
	            .filter(val -> val.length == 2)
	            .collect(Collectors.toMap(val -> val[0], val -> val[1]));

        } catch (Exception ex) {
        	LOGGER.error("Error occurred while detokenizing source: {}, exception: {}", sourceId, ex);
		}
        
        return null; 
	}
    
	private String dateConverterToTimeZone(Date dateToConvert, ZoneId zoneId) {
		
		ZonedDateTime zonedDateTime = null;
		
		String convertedDate = null;
		
		try {
			zonedDateTime = dateToConvert.toInstant().atZone(zoneId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		
		convertedDate = zonedDateTime.toLocalDate().format(dateFormatter);
		
		return convertedDate;
	}
}
