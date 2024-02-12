package com.sorrisotech.client;

import java.util.Arrays;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sorrisotech.client.model.request.AddDocumentRequest;
import com.sorrisotech.client.model.request.CreateSessionRequest;

public class TestApi {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		
		KinecticeEsignClient client = (KinecticeEsignClient) context.getBean("eSignClient");
		
		// Login goes here
		
		String accessToken = client.login();
		
		System.out.println("Login token : " + accessToken);
		
		// Session creation goes here
		
		CreateSessionRequest.Party party = new CreateSessionRequest.Party(
				"Brian Williams", 
				"kylet@immonline.com", 
				"551-577-0000", 
				"+1"
		);
		
		CreateSessionRequest request = new CreateSessionRequest(Arrays.asList(party));
		
		String hostSessionId = client.createNewSession(request, accessToken);
		
		System.out.println("Session Id : " + hostSessionId);
		
		// Adding document here
		
		String document = (String) context.getBean("documentBase64");
		
		AddDocumentRequest cAddDocRequest = new AddDocumentRequest(
				"IMM", 
				"IMMTemplate", 
				"IMM Template", 
				document
		);
		
		String response = client.addDocument(cAddDocRequest, accessToken, hostSessionId);
		
		System.out.println("Add document response : " + response);
		
		// Getting remote status here
		
		String status = client.getRemoteStatus(accessToken, hostSessionId);
		
		System.out.println("Remote Status : " + status);
		
		// Cancelling request here
		boolean cancelResponse = client.cancelSession(accessToken, hostSessionId);
		
		System.out.println("Cancel response : " + cancelResponse);
	}
}
