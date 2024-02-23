package com.sorrisotech.svcs.documentesign.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.documentesign.service.PDFEsignService;
import com.sorrisotech.svcs.documentesign.api.IApiDocumentEsign;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class GetDocumentEsignStatus extends GetDocumentEsignStatusBase {

	private static final long serialVersionUID = 8021554611451474002L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GetDocumentEsignStatus.class);

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		final String sessionId = request.getString(IApiDocumentEsign.GetDocumentEsignStatus.sessionId);
		
		request.setToResponse();
		
		final var status = PDFEsignService.getInstance().getEsignStatus(sessionId);
		
		if (status == null) {
			LOGGER.debug("Unable check status for the session : {}", sessionId);
			request.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		request.set(IApiDocumentEsign.GetDocumentEsignStatus.status, status);
		request.setRequestStatus(ServiceAPIErrorCode.Success);
		return ServiceAPIErrorCode.Success;
	}

}
