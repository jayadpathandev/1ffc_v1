package com.sorrisotech.pdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sorrisotech.client.model.request.CreateSessionRequest.Party;
import com.sorrisotech.pdf.service.PDFEsignService;

public class Main {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("documentEsignServiceContext.xml");

		
		PDFEsignService service = context.getBean(PDFEsignService.class);
		
		var party = new Party(
				"Rohit Singh", 
				"rohits.sorriso@gmail.com", 
				"985499087", 
				"+91"
		);
		var accessToken = service.getAccessToken();
		var sessionId = service.createNewEsignSession(List.of(party), accessToken);
		
		var pdfDetails = getPDFDetails();
		boolean documentAdded = service.addDocumentToSession(
				pdfDetails, 
				sessionId, 
				accessToken,
				"Rohit Singh",
				"123456"
		);
		
		if (documentAdded) {
			var url = service.getEsignUrlForSession(
					sessionId, 
					accessToken,
					"Rohit Singh", 
					"rohits.sorriso@gmail.com"
			);
			
			System.out.println(url);
			
			var status = service.getEsignStatus(sessionId);
			
			System.out.println(status);
		}
	}
	
	public static Map<String, String> getPDFDetails() {

		String achPayment = "Off";
		String debitCardPayment = "Yes";

		Map<String, String> pdfDetails = new HashMap<String, String>();
		pdfDetails.put("ACH_CHECKBOX", achPayment);
		pdfDetails.put("DEBIT_CHECKBOX", debitCardPayment);
		pdfDetails.put("EFFECTIVE_DATE", "2024-02-04");
		pdfDetails.put("EXTERNAL_ACCT", "0123456789");
		pdfDetails.put("PAYMENT_DATE_RULE", "123456");
		pdfDetails.put("PAYMENT_AMOUNT_RULE", "789456");
		pdfDetails.put("PAYMENT_COUNT_RULE", "456123");
		pdfDetails.put("NAME_ON_PMT_ACCOUNT", "49723546");

		if (achPayment.equals("Yes")) {
			pdfDetails.put("BANK_NAME", "HDFC");
			pdfDetails.put("BANK_ROUTING_NUMBER", "1234567890");
			pdfDetails.put("BANK_ACCOUNT_MASKED", "7896321455");
		}

		if (debitCardPayment.equals("Yes")) {
			pdfDetails.put("DEBIT_CARD_MASKED", "45321365899");
			pdfDetails.put("DEBIT_CARD_EXPIRATION", "24/04");
		}
		
		return pdfDetails;
	}

}
