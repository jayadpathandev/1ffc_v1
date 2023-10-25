useCase documentSearchChild [   
	/**
	* author: Gareth Lloyd
	* created: 15-Oct-2015
	*
	* Primary Goal:
	* To display a list of documents for a user's account in a tabular format. Allow the
	* user to search for documents in a certain date range, and then also allow the user 
	* to download the documents of their choosing from "Vault".
	*
	* Alternative Outcomes:
	* 1. Documents fail to load for some reason
	* 2. User tries to download documents but they haven't selected any to download
	* 3. There is some error connecting to the "Vault" database when trying to download the document
	*
	* Major Versions:
	* 1.0 15-Oct-2015 First Version Coded [Gareth Lloyd]
	* 2.0 23-Oct-2015 Alternative paths added [Gareth Lloyd]
	* 3.0 20-Mar-2018 Allow for columns in results table to be configured: https://sorriso.atlassian.net/browse/SSFB-523 [Gareth Lloyd]
	*/

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

		 
    startAt init
    
    /*************************
	* DATA ITEMS SECTION
	*************************/ 
    
    importJava UcVault(com.sorrisotech.uc.bill.UcVault)
    importJava UcVaultDownload(com.sorrisotech.uc.bill.UcVaultDownload)
    importJava UcUbfDownload(com.sorrisotech.uc.bill.UcUbfDownload)
    importJava UcDocFactoryMgr(com.sorrisotech.uc.documents.UcDocFactoryMgr)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava ExternalDocuments(com.sorrisotech.uc.bill.ExternalDocuments)
			
    import validation.dateValidation	    
    import billCommon.sBillAccountInternal
    import billCommon.sBillsFound
    
    serviceStatus srStatus	
    serviceResult(SearchSettings.GetVaultSearchResultColumns) srGetResponse
    
    serviceStatus srGetDocumentStatus
    serviceParam(ExtDocs.GetDocument)  srGetDocumentParam
    serviceResult(ExtDocs.GetDocument) srGetDocumentResponse
    
    serviceStatus srGetDocumentsStatus
    serviceParam(ExtDocs.GetDocuments)  srGetDocumentsParam
    serviceResult(ExtDocs.GetDocuments) srGetDocumentsResponse
    
    string sUserId = Session.getUserId()
    native string sDummy = ""
    string sHeading = "{Find document}"
    string sResultsHeading = "{Search results}"
    native string selectRow
    native string multiJson
    native string disposition = "inline"
    native string sFromDate = UcVault.getFromDate()
    native string sToDate = UcVault.getToDate()
    native string sHasContentFlag = UcDocFactoryMgr.getHasContentFlag()
    native volatile string sAuditInvoiceNumbers  = ExternalDocuments.multipleExternalDocIds(srGetDocumentsResponse.ARCHIVE)
    
    string sColumn1 = ""
    string sColumn2 = ""
    string sColumn3 = ""
    string sColumn4 = ""
    string sColumn5 = ""
    string sColumn6 = ""
    
    field fromDate [
        native string(label) sLabel = "From:"          
        date(control) aDate("yyyy-MM-dd", dateValidation)
    ]
    
    field toDate [
        native string(label) sLabel = "To:"          
        date(control) aDate("yyyy-MM-dd", dateValidation)         
    ]
       
    structure(message) msgVaultError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error while trying to download your document(s), please try again later.}"
    ] 
    structure(message) msgNoneSelected [
        string(title) sTitle = "{None Selected}"
        string(body) sBody = "{No documents selected for download.}"
    ] 
    
    structure(message) msgVaultNotConfigured [
        string(title) sTitle = "{Configuration Problem}"
        string(body) sBody = "{Document retrieval is not yet ready, please contact your System Administrator.}"
    ]
    
    checkBoxes(control) cSelect  
    
    native string sSelectedAccount       = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "ACCOUNT")
    native string sSelectedDate          = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "BILL_DATE_NUM")
    native string sSelectedLinkId        = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "LINK_ID")
    native string sSelectedLocation      = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "LOCATION")
    native string sSelectedPaymentGroup  = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "PAYMENT_GROUP")
    native string sSelectedStreamId      = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "BILL_STREAM")
    native string sSelectedDocId 		 = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "BILL_ID")
    native string sSelectedExtDocId		 = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "EXTERNAL_DOC_ID")
    native string sSelectedType          = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "DOCUMENT_TYPE")
    native string sSelectedInvoiceNumber = UcDocFactoryMgr.getValue(tBillSearch, selectRow, "INVOICE_NUMBER")
    
    table tBillSearch = UcDocFactoryMgr.getData("", "", "", sColumn1, sColumn2, sColumn3, sColumn4, sColumn5, sColumn6) [
        emptyMsg: "{There are no documents to display}"        
        
        "row"      			=> string sRow
        checkSelect  		cSelect
        "INVOICE_NUMBER"    => string sInvNum
        "DISPLAY_LAYOUT"    => string sInvType
        "DUE_DATE"      	=> string sDueDate
        "BILL_DATE"      	=> string sBillDate
        "DUE_DATE_NUM"     	=> number nDueDateNum
        "BILL_DATE_NUM"     => number nBillDateNum
        "AMOUNT_DUE"     	=> string sTotDue
        "AMOUNT_DUE_NUM"    => number nTotDueNum
        "BILL_AMOUNT"     	=> string sBillAmt
        "BILL_AMOUNT_NUM"   => number nBillAmtNum
        "MINIMUM_DUE"     	=> string sMinDueAmt
        "MINIMUM_DUE_NUM"   => number nMinDueAmtNum
	    "FLEX1" 			=> string sFLEX1
		"FLEX2" 			=> string sFLEX2
		"FLEX3" 			=> string sFLEX3
		"FLEX4" 			=> string sFLEX4
		"FLEX5" 			=> string sFLEX5
		"FLEX6" 			=> string sFLEX6
		"FLEX7" 			=> string sFLEX7
		"FLEX8" 			=> string sFLEX8
		"FLEX9" 			=> string sFLEX9
		"FLEX10" 			=> string sFLEX10 
		"FLEX11" 			=> string sFLEX11 
		"FLEX12" 			=> string sFLEX12 
		"FLEX13" 			=> string sFLEX13 
		"FLEX14" 			=> string sFLEX14 
		"FLEX15" 			=> string sFLEX15
		"FLEX16" 			=> string sFLEX16
		"FLEX17" 			=> string sFLEX17
		"FLEX18" 			=> string sFLEX18
		"FLEX19" 			=> string sFLEX19
		"FLEX20" 			=> string sFLEX20 
		"SELF_REG0" 		=> string sSELF_REG0
		"SELF_REG1" 		=> string sSELF_REG1
		"SELF_REG2" 		=> string sSELF_REG2
		"SELF_REG3" 		=> string sSELF_REG3
		"SELF_REG4" 		=> string sSELF_REG4
        "INVOICE_NUMBER" => link linkDownload(viewDocument) [
            selectRow: sRow  
        ]
        "NO_CHECKBOX"       => string sNoCheckbox
        
        column accSelectCol(checkbox) [
            elements: [ 
            	cSelect: [
	    			remove: sNoCheckbox
            	] 
            ]
        ]
		column flex2Col("{Document Type}") [
            elements: [ sFLEX2 ]
            sort: [ sFLEX2 ]
            tags: [ "visually-hidden" ]
        ]  
        column invNumCol("{Document Name}") [
            elements: [
            	linkDownload: [
            		attr_target: "_blank"
            	] 
            ]
            sort: [ sInvNum ]    
            tags: [ "visually-hidden" ]        
         ]
      
        descending column docDateCol("{Date Issued}") [
            elements: [ sBillDate ]
            sort: [ nBillDateNum ]
            tags: [ "visually-hidden" ]
        ]
        column dueDateCol("{Due date}") [
            elements: [ sDueDate ]
            sort: [ nDueDateNum ]
            tags: [ "visually-hidden" ]
        ]
        column totalDueCol("{Total due}") [
            elements: [ sTotDue ]
            sort: [ nTotDueNum ]
            tags: [ "visually-hidden",  "amount" ]
        ]  
        column billAmtCol("{Doc amount}") [
            elements: [ sBillAmt ]
            tags: [ "amount", "visually-hidden" ]
            sort: [ nBillAmtNum ]
        ]  
        column minDueAmtCol("{Min due}") [
            elements: [ sMinDueAmt ]
            tags: [ "amount", "visually-hidden" ]
            sort: [ nMinDueAmtNum ]
        ]  
        column docTypeCol("{Doc Format}") [
            elements: [ sInvType ]
            sort: [ sInvType ]
            tags: [ "visually-hidden" ]
        ]
        column flex1Col("{FLEX1}") [
            elements: [ sFLEX1 ]
            sort: [ sFLEX1 ]
            tags: [ "visually-hidden" ]
        ]
        column flex3Col("{FLEX3}") [
            elements: [ sFLEX3 ]
            sort: [ sFLEX3 ]
            tags: [ "visually-hidden" ]
        ]
        column flex4Col("{FLEX4}") [
            elements: [ sFLEX4 ]
            sort: [ sFLEX5 ]
            tags: [ "visually-hidden" ]
        ]
        column flex5Col("{FLEX5}") [
            elements: [ sFLEX5 ]
            sort: [ sFLEX5 ]
            tags: [ "visually-hidden" ]
            
        ]
        column flex6Col("{FLEX6}") [
            elements: [ sFLEX6 ]
            sort: [ sFLEX6 ]
            tags: [ "visually-hidden" ]
        ]
        column flex7Col("{FLEX7}") [
            elements: [ sFLEX7 ]
            sort: [ sFLEX7 ]
            tags: [ "visually-hidden" ]
        ]
        column flex8Col("{FLEX8}") [
            elements: [ sFLEX8 ]
            sort: [ sFLEX8 ]
            tags: [ "visually-hidden" ]
        ]
        column flex9Col("{FLEX9}") [
            elements: [ sFLEX9 ]
            sort: [ sFLEX9 ]
            tags: [ "visually-hidden" ]
        ]
        column flex10Col("{FLEX10}") [
            elements: [ sFLEX10 ]
            sort: [ sFLEX10 ]
            tags: [ "visually-hidden" ]
        ]
        column flex11Col("{FLEX11}") [
            elements: [ sFLEX11 ]
            sort: [ sFLEX11 ]
            tags: [ "visually-hidden" ]
        ]
        column flex12Col("{FLEX12}") [
            elements: [ sFLEX12 ]
            sort: [ sFLEX12 ]
            tags: [ "visually-hidden" ]
        ]
        column flex13Col("{FLEX13}") [
            elements: [ sFLEX13 ]
            sort: [ sFLEX13 ]
            tags: [ "visually-hidden" ]
        ]
        column flex14Col("{FLEX14}") [
            elements: [ sFLEX14 ]
            sort: [ sFLEX14 ]
            tags: [ "visually-hidden" ]
        ]
        column flex15Col("{FLEX15}") [
            elements: [ sFLEX15 ]
            sort: [ sFLEX15 ]
            tags: [ "visually-hidden" ]
        ]
        column flex16Col("{FLEX16}") [
            elements: [ sFLEX16 ]
            sort: [ sFLEX16 ]
            tags: [ "visually-hidden" ]
        ]
        column flex17Col("{FLEX17}") [
            elements: [ sFLEX17 ]
            sort: [ sFLEX17 ]
            tags: [ "visually-hidden" ]
        ]
        column flex18Col("{FLEX18}") [
            elements: [ sFLEX18 ]
            sort: [ sFLEX18 ]
            tags: [ "visually-hidden" ]
        ]
        column flex19Col("{FLEX19}") [
            elements: [ sFLEX19 ]
            sort: [ sFLEX19 ]
            tags: [ "visually-hidden" ]
        ]
        column flex20Col("{FLEX20}") [
            elements: [ sFLEX20 ]
            sort: [ sFLEX20 ]
            tags: [ "visually-hidden" ]
        ]
        column selfReg0Col("{SELF_REG0}") [
            elements: [ sSELF_REG0 ]
            sort: [ sSELF_REG0 ]
            tags: [ "visually-hidden" ]
        ]
        column selfReg1Col("{SELF_REG1}") [
            elements: [ sSELF_REG1 ]
            sort: [ sSELF_REG1 ]
            tags: [ "visually-hidden" ]
        ]
        column selfReg2Col("{SELF_REG2}") [
            elements: [ sSELF_REG2 ]
            sort: [ sSELF_REG2 ]
            tags: [ "visually-hidden" ]
        ]
        column selfReg3Col("{SELF_REG3}") [
            elements: [ sSELF_REG3 ]
            sort: [ sSELF_REG3 ]
            tags: [ "visually-hidden" ]
        ]
        column selfReg4Col("{SELF_REG4}") [
            elements: [ sSELF_REG4 ]
            sort: [ sSELF_REG4 ]
            tags: [ "visually-hidden" ]
        ]
    ] 
        
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	
	/* 1. System initializes userid for the filter and resets filter.*/
	action init [
		fromDate.aDate = sFromDate
		toDate.aDate = sToDate
		UcDocFactoryMgr.reset(sUserId, "vault", "D")
		//goto(setFilter)
		goto(retrieveSettings)
	]

	/* 2 RetrieveSettings */	
	action retrieveSettings [
		switch apiCall SearchSettings.GetVaultSearchResultColumns (srGetResponse, srStatus) [
		    case apiSuccess getResults
		    default setFilter
		]
	]
	
	/* 3. Get Results */
	action getResults [

		sColumn1 = srGetResponse.RSP_VAULT_COL1
		sColumn2 = srGetResponse.RSP_VAULT_COL2
		sColumn3 = srGetResponse.RSP_VAULT_COL3
		sColumn4 = srGetResponse.RSP_VAULT_COL4
		sColumn5 = srGetResponse.RSP_VAULT_COL5
		sColumn6 = srGetResponse.RSP_VAULT_COL6	
		
        goto(setFilter)                
	] 
	
    
     /* 5.  User views search results table. */ 
    xsltScreen screenShowHistoryTable("{Document History}") [
		    		
		div main [
			class: "row st-dashboard-search"

            div content [
            	class: "col-md-12"
                
				div header [
					class: "row"
					
					div headerCol [
						class: "col-auto"
						
	            		display sHeading [
	            			class: "h4 st-dashboard-search-heading"
	            		]
        		 	]
					div advancedSearchCol [
						class: "col text-end"
						navigation advancedSearchLink(actionAdvancedSearch, "{Advanced Search}") [
							class: "st-advanced-search-linksearch"
						]
					]
        		]
        		
                form row1 [
                	class: "row st-doc-search" 
                	
                	div range [
                		class: "col-8 col-sm-9 col-md-12 col-lg-9 row mb-md-3 mb-lg-0"
                		
	                	display fromDate [
	                		class: "col-md-4"
	                		control_attr_tabindex: "2"		
	                		control_attr_datepicker-options: "{showWeeks: false}"					
	                	]

	                	display toDate [
	                		class: "col-md-4"
	                		control_attr_tabindex: "3"
	                		control_attr_datepicker-options: "{showWeeks: false}"
	                	]
					]
										
                	div col3 [
                		class: "col-4 col-sm-3 col-md-12 col-lg-3 text-end text-lg-start d-flex align-items-end flex-column"

	                	navigation searchButton(setFilter, "{Search}") [
							class: " mt-auto btn btn-primary st-dashboard-search-button"
							
							obfuscate: FALSE
	           				data: [fromDate, toDate]
	           				require: [fromDate, toDate]
	           				attr_tabindex: "4"
						]
					]					
                ]
                
                form row2 [
                	class: "row"
                	
                	div col5 [
                	    display sResultsHeading [
		        			class: "h4 st-dashboard-search-heading"
		        		]
		        	]
		        	      	

                	div col6 [                		
                		logic: [                			
                			if sHasContentFlag == "false" then "hide"
                		]
                		
                		class: "col-md-2  col-6"
		        		
		        		div pdfButton [
		        			class: "col-12 d-none"
		        			
			                navigation downloadPdfs(checkDownload, "{DOWNLOAD SELECTED}") [			                
			                	class: "btn btn-primary" 		                			                		                	
			                	data: [tBillSearch,cSelect]		                				                	
			                	attr_tabindex: "5"	      			                	        			                    
			                ]
			                
                    		display sDummy [
                    			class: "glyphicon glyphicon-download-alt"
                    		]
		                ]
		        	]
		        	      		  		        			        	
		        	div col7 [
                		class: "col-md-12 d-none"
                		
		                display tBillSearch [
		                	class: "st-search-table"
		                ]
	                ]
        		]
                
                form row3 [
                	class: "row"
                	attr_target: "_blank"
                	
                	div col8 [
                		class: "col-md-12 visible-xs-block"
                		
		                display tBillSearch [
		                	class: "st-search-table"
		                ]
	                ]
	                
                	div col9 [
	            		class: "col-md-3 mb-5"
		        		
		        		logic: [
                			if sHasContentFlag == "false" then "hide"
                		]
                		
		        		div pdfButton1 [
		        			class: "col-12 visible-xs-block"
		        			
			                navigation downloadPdfs1(checkDownload, "{DOWNLOAD SELECTED}") [			                
			                	class: "btn btn-primary" 		                	
			                	data: [tBillSearch]	   		                	       			                    
			                ]
			                
                    		display sDummy [
                    			class: "glyphicon glyphicon-download-alt"
                    		]
                    	]
		           ]		           
	        	]
            ]
        ]
	]
    
	/* 3a 4. When user clicks Search, set the filter values and loads the results table.*/
	action setFilter [
		UcDocFactoryMgr.setStartDate("vault", fromDate.aDate)
		UcDocFactoryMgr.setEndDate("vault", toDate.aDate)
		goto(screenShowHistoryTable)
	]
	
	/* 6. When user chooses downloads, first check if Vault DB(s) are available.*/
    action checkDownload [   
    	 disposition = "attachment" 
		 switch ExternalDocuments.multipleDocuments( 
		 		tBillSearch,
		 		selectRow,
		 		multiJson
		 ) [		 
		 	case "single" 		viewExtDoc    
		 	case "multiple"		actionDoFilesDownload 
		 	case "error" 		actionVaultProblem	 							
			default 			screenShowHistoryTable
		]
	]

	/* 7. When user chooses Advanced Search link */
	action actionAdvancedSearch [
		gotoUc(documentAdvSearchChild)
	]	

	/*  If all Vault DB(s) are available, download the documents.*/
	action actionDoFilesDownload [
		auditLog(audit_bill.download_multiple_pdfs) [
			sAuditInvoiceNumbers
		]
		
		srGetDocumentsParam.docList = multiJson
		
		switch apiCall ExtDocs.GetDocuments(srGetDocumentsParam, srGetDocumentsResponse, srGetDocumentsStatus)	[
			case apiSuccess		downloadArchive     
			default				actionVaultProblem 
		]
	]

	action downloadArchive [
		auditLog(audit_bill.download_csv) [
			sSelectedAccount
			sSelectedDate
			sSelectedInvoiceNumber
			sSelectedType
		]
		
	    foreignHandler ExternalDocuments.downloadArchive(srGetDocumentsResponse.MIME_TYPE, srGetDocumentsResponse.ARCHIVE )  		
	]		
	
	/*==========================================
	 *  User selects a document to view.
	 */ 
	action viewDocument [
		disposition = "inline"
		switch sSelectedLocation [
			case "U" viewHtml
			case "D" viewExtDoc
			default viewHtml
		]
	]
	
	/*==========================================
	 *  User selects a PDF to view.
	 */ 
	action openExtDoc [
	    foreignHandler ExternalDocuments.open(sSelectedAccount, sSelectedDate, srGetDocumentResponse.MIME_TYPE, disposition, srGetDocumentResponse.DOCUMENT )  
	]
	
	action viewVault [
		UcVaultDownload.setVaultParams(sSelectedAccount, sSelectedDate, sSelectedLinkId)	    
	    foreignHandler UcVaultDownload.open()
	]
	
	action viewHtml [
		UcUbfDownload.setUbfParams(sSelectedAccount, sSelectedDate, sSelectedPaymentGroup, sorrisoLanguage, sorrisoCountry)	    
	    foreignHandler UcUbfDownload.openUbf()  
	]

	action viewExtDoc [
		
		srGetDocumentParam.STREAM_ID    = sSelectedStreamId
		srGetDocumentParam.DOC_ID 		= sSelectedDocId
		srGetDocumentParam.EXT_DOC_ID 	= sSelectedExtDocId
		srGetDocumentParam.EXT_DOC_PART = ""
		
		switch apiCall ExtDocs.GetDocument(srGetDocumentParam, srGetDocumentResponse, srGetDocumentStatus ) [
           case apiSuccess openExtDoc
           default actionVaultProblem       
		]
	]	
	
    /*  User selects a single invoice to download.*/ 
	action downloadFile [
		auditLog(audit_bill.download_single_pdf) [
			sSelectedAccount
			sSelectedDate
			sSelectedInvoiceNumber
		]
			    
		foreignHandler UcVault.downloadFile()
	] 
	

	
	/*************************
	* EXTENSION SCENARIOS
	*************************/
	
	/* 4A. There was some error connecting to Vault */ 
	 action actionVaultProblem [
		displayMessage(type: "danger" msg: msgVaultError)

        goto(screenShowHistoryTable) 
	] 
	
	/* 4B. User tries to download but makes no selection */ 
	 action actionNoneSelected [
		displayMessage(type: "warning" msg: msgNoneSelected)

        goto(screenShowHistoryTable) 
	]
]