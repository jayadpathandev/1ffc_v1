useCase fffcViewDocument [   
    documentation [
        preConditions: [[
            1. The endUser can successfully log into the system.
            2. The endUser has an account that has documents (e.g. invoices) associated with it.
        ]]
        triggers: [[
            1. The endUser successfully logs in and lands on the dashboard as a default.
            2. The endUser has clicked the "Overview" menu.
        ]]
        postConditions: [[
            1. The endUser can search for documents in a date range of their choosing.
            2. The endUser can select one or more documents to download from "Vault"
        ]]
    ]
    
    actors [
        view_document
    ]
		 
    startAt actionLoad
    shortcut fffcViewDoc(actionLoad) [
    	sAccount, sDate, sStreamId, sDocId, sExtDocId
    ]
	    
    /*************************
	* DATA ITEMS SECTION
	*************************/ 
	importJava ExternalDocuments(com.sorrisotech.uc.bill.ExternalDocuments)
    
    serviceStatus srGetDocumentStatus
    serviceParam(ExtDocs.GetDocument)  srGetDocumentParam
    serviceResult(ExtDocs.GetDocument) srGetDocumentResponse
        
    structure(message) msgError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error while trying to download your document(s), please try again later.}"
    ] 
    
    native string sAccount
    native string sDate
    native string sStreamId
    native string sDocId
    native string sExtDocId
            
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	
	/* 1. System retrieves the document.*/
	action actionLoad [
		
		srGetDocumentParam.STREAM_ID    = sStreamId
		srGetDocumentParam.DOC_ID 		= sDocId
		srGetDocumentParam.EXT_DOC_ID 	= sExtDocId
		srGetDocumentParam.EXT_DOC_PART = ""
		
		switch apiCall ExtDocs.GetDocument(srGetDocumentParam, srGetDocumentResponse, srGetDocumentStatus ) [
           case apiSuccess actionOpen
           default actionIssue       
		]
	]	
	
	/* 2a. System displays the document. */
	action actionOpen [
	    foreignHandler ExternalDocuments.open(sAccount, sDate, srGetDocumentResponse.MIME_TYPE, "inline", srGetDocumentResponse.DOCUMENT )  
	]
	
	/* 2a. System displays an error message. */
	 action actionIssue [
		displayMessage(type: "danger" msg: msgError)

        gotoUc(overview) 
	] 
]