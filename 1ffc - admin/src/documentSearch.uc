useCase documentSearch [
	
   /**
    *  author: Yvette
    *  created: 07-April-2017
    *
    *  Primary Goal:
    *       1. Allow customer agents to search and display any document in the system based upon a mix of fixed and flexible fields in the bill data table, 
    *       regardless of whether the customer is registered or not.
    *       
    *  Alternative Outcomes:
    *       1. 
    *       3. 
    *       4. 
    *       5. 
    *                     
    *   Major Versions:
    *        1.0 07-April-2017 First Version Coded 
    */
        

    documentation [
        preConditions: [[
            1. CSR agent successfully logged in.
        ]]
        triggers: [[
            1. CSR agent selects the Find documents menu. Agent will enter input from the form and click search
        ]]
        postConditions: [[
            1. Primary -- System displays the document details table based on the agent search
        ]]
    ]
	startAt actionInit
	
	/*******************************************************************************************************************
	 * Only business admin and business users are able to see assist customers tab. Sys Admin wont be able to see this 
	 *******************************************************************************************************************/
	 
	actors[
		view_doc_search
		assist_document_controller
	]
	
    /**************************
     * Strings used by the JAVA
     **************************/
              
    static tdocList_accName = "Account name"
    static tdocList_accNum = "Account number"
    static tdocList_amtDue = "Amount due"
    static tdocList_docDate = "Document date"
    static tdocList_docNum = "Document name"
    static tdocList_docFormat = "Document format"
    static tdocList_dontShow = "Don't Show"
    static tdocList_flex1 = "Flex 1"
    static tdocList_flex10 = "Flex 10"
    static tdocList_flex11 = "Flex 11"
    static tdocList_flex12 = "Flex 12"
    static tdocList_flex13 = "Flex 13"
    static tdocList_flex14 = "Flex 14"
    static tdocList_flex15 = "Flex 15"
    static tdocList_flex16 = "Flex 16"
    static tdocList_flex17 = "Flex 17"
    static tdocList_flex18 = "Flex 18"
    static tdocList_flex19 = "Flex 19"
    static tdocList_flex2  = "Document type"
    static tdocList_flex20 = "Flex 20"
    static tdocList_flex3  = "Flex 3"
    static tdocList_flex4  = "Flex 4"
    static tdocList_flex5  = "Flex 5"
    static tdocList_flex6  = "Flex 6"
    static tdocList_flex7  = "Flex 7"
    static tdocList_flex8  = "Flex 8"
    static tdocList_flex9  = "Flex 9"
	
	/**************************
     * DATA ITEMS SECTION
     **************************/    
    // -- import the request client for this --

	importJava GetColumnNames(com.sorrisotech.uc.documents.GetColumnNames)
	importJava Dates(com.sorrisotech.uc.documents.Dates)
	importJava UcUbfDownload(com.sorrisotech.uc.bill.UcUbfDownload)
	importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)
	importJava ExternalDocuments(com.sorrisotech.uc.bill.ExternalDocuments)
	importJava DocumentSearch(com.sorrisotech.uc.documents.DocumentSearch)
	
    import validation.dateValidation
	
    serviceStatus srStatus			
    serviceResult(SearchSettings.GetSearchFields) srGetResponse
    serviceParam(SearchSettings.GetSearchFields) srGetResponseParam
    
    serviceParam(AgentFindDocuments.GetDocuments) srFindRequest
    serviceResult(AgentFindDocuments.GetDocuments) srFindResponse
    
   	serviceStatus srGetDocumentStatus
    serviceParam(ExtDocs.GetDocument)  srGetDocumentParam
    serviceResult(ExtDocs.GetDocument) srGetDocumentResponse     
    		
    native string sFormat     = LocalizedFormat.toJsonString()
	
	// -- Page name and header for find documents page --		
	string sPageName = "{Find Documents}" //Page name.
	string (p) sMsgHeader = "{Enter one or more document attributes to search on. When you have entered the attributes you want to search on, choose SEARCH.}"
	string (p) sMsgHeaderSub = "{Enter one or more document attributes to search on. A document attribute may use an asterisk (*) as a wild card for one or more characters in any attribute except date. When you have entered the attributes you want to search on, choose SEARCH}"

	// -- Page name and header for search results page --
	string sResultsHeading = "{Search results}" //Search results heading
	string (p) sMsgResultsHeader = "Results for query: "
	
	string sMsgSearchTitle = "{Search criteria}"
	string sMsgOptional = "{Document date range (optional)}"
		             
	field fromDate [
        string(label) sLabel = "{From:}"          
        date(control) aDate("yyyy-MM-dd", dateValidation)         
        string(error) sRequired = "Both from and to dates must be provided."           
    ]
 	
    field toDate [       	
    	string(label) slabel = "{To:}"
        date(control) aDate("yyyy-MM-dd", dateValidation)         
        string(error) sRequired = "Both from and to dates must be provided."       
    ]
    
    static validRegex = "^[\\p{L}\\d.\\-_, '&$#@!]{3,}[\\p{L}\\d.\\-_, '&$#@!*]{0,28}$"
    static validSubRefex = "^[\\p{L}\\d.\\-_, '&$#@!*]{0,29}[\\p{L}\\d.\\-_, '&$#@!]{3,}[\\p{L}\\d.\\-_, '&$#@!*]{0,29}$"
     
	field fAccountNumber [                              // Input field for Account number
	    string(label) sLabel = "{Loan number:}" 
        input (control) pInput ("(^\\d{3,9}\\*|^\\d{10}$)$", fAccountNumber.sValidation)
		string(validation) sValidation = "{The Loan number search criteria must be at least 3 digits plus a wildcard (*) or maximum of 10 digits. For example: 897*, 89790003*, or 8979000382.}"
		string(help) sHelp = "{Please enter a customers' Loan Number. Search criteria must include at least 3 digits and a wild card (*) up to a full 10 digit loan number.}"
    ]
 
 	field fDocIdentifier [                              // Input field for Document Identifier
	    string(label) sLabel = "{Document identifier:}" 
        input (control) pInput (validRegex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{The Document Identifier must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]

	field fCustomerId [                              // Input field for Account number
	    string(label) sLabel = "{Customer id:}" 
        input (control) pInput ("(^\\d{3,6}\\*|^\\d{7}$)$", fCustomerId.sValidation)
        string(validation) sValidation = "{Customer identification number search criteria must be at least 3 digits plus a wildcard (*) or maximum of 7 digits. For example: 207*, 2071*, or 2071978.}"
        string(help) sHelp = "{Please enter a customers' Customer Identification Number. Search criteria must include at least 3 digits and a wild card (*) up to a full 7 digit customer id.}"       
    ]

	field fAccountName [                              // Input field for Account number
	    string(label) sLabel = "{Account name:}" 
        input (control) pInput (validRegex, fAccountName.sValidation)
        string(validation) sValidation = "{The Account name must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.  For example: EDN*, EDNA*, or EDNA Iona Durham}"
        string(help) sHelp = "{Please enter a customers' Account Name. Search criteria must include at least 3 non wildcard characters and a wild card (*) up to 28 characters.}"        
        
    ]

	field fFlex1 [                      // Branch Id is for Flex1
	    string(label) sLabel = "{Branch id:}" 
        input (control) pInput (validRegex, fFlex1.sValidation)
        string(validation) sValidation = "{The Branch id must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character. For example: 896*, 8962*, or 8962-TEMPLE}"
       string(help) sHelp = "{Please enter a customers' Branch Id. Search criteria must include at least 3 non wildcard characters and a wild card (*) up to 28 characters.}"
    ]
    
	field fFlex2 [						// Document type is for Flex2
	    string(label) sLabel = "{Document type}" 
        dropDown (control) pInput [
        	None: ""
	        Letter: "{Letter}"
	        Statement: "{Statement}"
	    ]    
    ]  

  	field fFlex3 [						// Document display for Flex3
	    string(label) sLabel = "{Document display:}" 
        dropDown (control) pInput [
        	None: ""
	        Internal: "{Internal}"
	        External: "{External}"       	
	   ]
    ]  

 	field fFlex3A [						// Document display for Flex3
	    string(label) sLabel = "{Document display:}" 
        dropDown (control) pInput [
	        External: "{External}"       	
	   ]
    ]  

	field fFlex4 [										// Input field for Flex4
	    string(label) sLabel = "{Flex 4:}" 
        input (control) pInput (validRegex, fFlex4.sValidation)
        string(validation) sValidation = "{The Flex 4 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
 	field fFlex5 [										// Input field for Flex5
	    string(label) sLabel = "{Flex 5:}" 
        input (control) pInput (validRegex, fFlex5.sValidation)
        string(validation) sValidation = "{The Flex 7 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
  	field fFlex6 [										// State is for Flex6
	    string(label) sLabel = "{State:}" 
        dropDown (control) pInput [
            None: ""
	  		State1 : "{AL}"
	  		State2 : "{AK}"
	  		State3 : "{AZ}"
	  		State4 : "{AR}"
	  		State5 : "{CA}"
	  		State6 : "{CO}"
	  		State7 : "{CT}"
	  		State8 : "{DE}"
	  		State9 : "{DC}"
	  		State10: "{FL}"
	  		State11: "{GA}"
	  		State12: "{HI}"
	  		State13: "{ID}"
	  		State14: "{IL}"
	  		State15: "{IN}"
	  		State16: "{IA}"
	  		State17: "{KS}"
	  		State18: "{KY}"
	  		State19: "{LA}"
	  		State20: "{ME}"
	  		State21: "{MD}"
	  		State22: "{MA}"
	  		State23: "{MI}"
	  		State24: "{MN}"
	  		State25: "{MS}"
	  		State26: "{MO}"
	  		State27: "{MT}"
	  		State28: "{NE}"
	  		State29: "{NV}"
	  		State30: "{NH}"
	  		State31: "{NJ}"
	  		State32: "{NM}"
	  		State33: "{NY}"
	  		State34: "{NC}"
	  		State35: "{ND}"
	  		State36: "{OH}"
	  		State37: "{OK}"
	  		State38: "{OR}"
	  		State39: "{PA}"
	  		State40: "{RI}"
	  		State41: "{SC}"
	  		State42: "{SD}"
	  		State43: "{TN}"
	  		State44: "{TX}"
	  		State45: "{UT}"
	  		State46: "{VT}"
	  		State47: "{VA}"
	  		State48: "{WA}"
	  		State49: "{WV}"
	  		State50: "{WI}"
	  		State51: "{WY}"
        ]
    ]  

	field fFlex7 [										// Input field for Flex7
	    string(label) sLabel = "{Flex 7:}" 
        input (control) pInput (validRegex, fFlex7.sValidation)
        string(validation) sValidation = "{The Flex 7 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  

	field fFlex8 [										// Input field for Flex8
	    string(label) sLabel = "{Flex 8:}" 
        input (control) pInput (validRegex, fFlex8.sValidation)
        string(validation) sValidation = "{The Flex 8 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex9 [										// Input field for Flex9
	    string(label) sLabel = "{Flex 9:}" 
        input (control) pInput (validRegex, fFlex9.sValidation)
        string(validation) sValidation = "{The Flex 9 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex10 [										// Input field for Flex10
	    string(label) sLabel = "{Flex 10:}" 
        input (control) pInput (validRegex, fFlex10.sValidation)
        string(validation) sValidation = "{The Flex 10 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex11 [										// Input field for Flex11
	    string(label) sLabel = "{Flex 11:}" 
        input (control) pInput (validRegex, fFlex11.sValidation)
        string(validation) sValidation = "{The Flex 11 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]       
    
	field fFlex12 [										// Input field for Flex12
	    string(label) sLabel = "{Flex 12:}" 
        input (control) pInput (validRegex, fFlex12.sValidation)
        string(validation) sValidation = "{The Flex 12 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex13 [										// Input field for Flex13
	    string(label) sLabel = "{Flex 13:}" 
        input (control) pInput (validRegex, fFlex13.sValidation)
        string(validation) sValidation = "{The Flex 13 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex14 [										// Input field for Flex14
	    string(label) sLabel = "{Flex 14:}" 
        input (control) pInput (validRegex, fFlex14.sValidation)
        string(validation) sValidation = "{The Flex 14 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex15 [										// Input field for Flex15
	    string(label) sLabel = "{Flex 15:}" 
        input (control) pInput (validRegex, fFlex15.sValidation)
        string(validation) sValidation = "{The Flex 15 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex16 [										// Input field for Flex16
	    string(label) sLabel = "{Flex 16:}" 
        input (control) pInput (validRegex, fFlex16.sValidation)
        string(validation) sValidation = "{The Flex 16 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex17 [										// Input field for Flex17
	    string(label) sLabel = "{Flex 17:}" 
        input (control) pInput (validRegex, fFlex17.sValidation)
        string(validation) sValidation = "{The Flex 17 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex18 [										// Input field for Flex18
	    string(label) sLabel = "{Flex 18:}" 
        input (control) pInput (validRegex, fFlex18.sValidation)
        string(validation) sValidation = "{The Flex 18 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex19 [										// Input field for Flex19
	    string(label) sLabel = "{Flex 19:}" 
        input (control) pInput (validRegex, fFlex19.sValidation)
        string(validation) sValidation = "{The Flex 19 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]  
    
	field fFlex20 [										// Input field for Flex20
	    string(label) sLabel = "{Flex 20:}" 
        input (control) pInput (validRegex, fFlex20.sValidation)
        string(validation) sValidation = "{The Flex 20 must contain 3 non wildcard characters followed by up to 28 characters including the wildcard(*) character.}"
    ]                                      
	
	field fAccountNumberSub [                              // Input field for Account number
	    string(label) sLabel = "{Account Number:}" 
        input (control) pInput (validSubRefex, fAccountNumber.sValidation)
        string(validation) sValidation = "{Account Number must contain at least 3 sequential non wildcard(*) characters.}"
    ]
 
 	field fDocIdentifierSub [                              // Input field for Document Identifier
	    string(label) sLabel = "{Document Identifier:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Document Identifier must contain at least 3 sequential non wildcard(*) characters.}"
    ]
    
	field fFlex1Sub [                                     // Input field for Flex1
	    string(label) sLabel = "{Flex 1:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 1 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex2Sub [										// Input field for Flex2
	    string(label) sLabel = "{Flex 2:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 2 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
                 
	field fFlex3Sub [										// Input field for Flex3
	    string(label) sLabel = "{Flex 3:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 3 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  

	field fFlex4Sub [										// Input field for Flex4
	    string(label) sLabel = "{Flex 4:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 4 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
 	field fFlex5Sub [										// Input field for Flex5
	    string(label) sLabel = "{Flex 5:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 5 must contain at least 3 sequential non wildcard(*) characters.}"
    ]     

	field fFlex6Sub [										// Input field for Flex6
	    string(label) sLabel = "{Flex 6:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 6 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  

	field fFlex7Sub [										// Input field for Flex7
	    string(label) sLabel = "{Flex 7:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 7 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  

	field fFlex8Sub [										// Input field for Flex8
	    string(label) sLabel = "{Flex 8:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 8 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex9Sub [										// Input field for Flex9
	    string(label) sLabel = "{Flex 9:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 9 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex10Sub [										// Input field for Flex10
	    string(label) sLabel = "{Flex 10:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 10 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex11Sub [										// Input field for Flex11
	    string(label) sLabel = "{Flex 11:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 11 must contain at least 3 sequential non wildcard(*) characters.}"
    ]       
    
	field fFlex12Sub [										// Input field for Flex12
	    string(label) sLabel = "{Flex 12:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 12 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex13Sub [										// Input field for Flex13
	    string(label) sLabel = "{Flex 13:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 13 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex14Sub [										// Input field for Flex14
	    string(label) sLabel = "{Flex 14:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 14 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex15Sub [										// Input field for Flex15
	    string(label) sLabel = "{Flex 15:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 15 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex16Sub [										// Input field for Flex16
	    string(label) sLabel = "{Flex 16:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 16 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex17Sub [										// Input field for Flex17
	    string(label) sLabel = "{Flex 17:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 17 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex18Sub [										// Input field for Flex18
	    string(label) sLabel = "{Flex 18:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 18 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex19Sub [										// Input field for Flex19
	    string(label) sLabel = "{Flex 19:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 19 must contain at least 3 sequential non wildcard(*) characters.}"
    ]  
    
	field fFlex20Sub [										// Input field for Flex20
	    string(label) sLabel = "{Flex 20:}" 
        input (control) pInput (validSubRefex, fDocIdentifier.sValidation)
        string(validation) sValidation = "{Flex 20 must contain at least 3 sequential non wildcard(*) characters.}"
    ]               
   
    string sColName1 = GetColumnNames.getColumnName("col1")
    string sColName2 = GetColumnNames.getColumnName("col2")
    string sColName3 = GetColumnNames.getColumnName("col3")
    string sColName4 = GetColumnNames.getColumnName("col4")
    string sColName5 = GetColumnNames.getColumnName("col5")
    string sColName6 = GetColumnNames.getColumnName("col6")
    string sColName7 = GetColumnNames.getColumnName("col7")
    string sColName8 = GetColumnNames.getColumnName("col8")
    
    native string selectRow
	native string sSelectedAccount      = DocumentSearch.selectedValue(tdocList, selectRow, "accountNumberInt")
    native string sSelectedDate         = DocumentSearch.selectedValue(tdocList, selectRow, "docDateNum")
    native string sSelectedPaymentGroup = DocumentSearch.selectedValue(tdocList, selectRow, "paymentGroup")
    native string sSelectedType         = DocumentSearch.selectedValue(tdocList, selectRow, "location")
    native string sSelectedStreamId     = DocumentSearch.selectedValue(tdocList, selectRow, "billStream")
    native string sSelectedDocId        = DocumentSearch.selectedValue(tdocList, selectRow, "billId")
    native string sSelectedExtDocId     = DocumentSearch.selectedValue(tdocList, selectRow, "extDocId")
    
	native string hasActorDocController = "false"
    
     table tdocList [
    	
        emptyMsg: "{There are no documents to display}"
         checkSelect cSelect
        "row"      			            => string sRow
        "col1"      		        	=> string sCol1
        "col2"      		        	=> string sCol2
        "col3"      		        	=> string sCol3
        "col4" => link linkDownload(viewDocument) [
            selectRow: sRow  
        ]        
        "col5"      		        	=> string sCol5
        "col6"      		        	=> string sCol6
        "col7"      		        	=> string sCol7
        "col8"      		        	=> string sCol8    
        "accountNumber"      			=> string sAccountNumber    
        "accountNumberInt"      		=> string sAccountNumberInt        
        "paymentGroup"					=> string sPaymentGroup
        "billStream"					=> string sBillStream        
        "location"						=> string sDocType        
        "extDocId"                      => string sExtDocId
        "docDateNum"				    => number nDocDateNum
        "billId"						=> string sBillId
        "docFormat"						=> string sDocFormat
        

        column column1(sColName1) [
        	tags: [ "column1" ]
            elements: [ sCol1 ]                        
        ]
        
         column column2(sColName2) [
         	tags: [ "column2" ]       
            elements: [ sCol2 ]                 
        ]
        
        column column3(sColName3) [
        	tags: [ "column3" ]   
            elements: [ sCol3 ]                     
        ]
               
        column column4(sColName4) [
        	tags: [ "column4" ] 
            elements: [ linkDownload ]                       
        ]
        
        column column5(sColName5) [
        	tags: [ "column5" ]   
            elements: [ sCol5 ]                     
        ]   

		column column6(sColName6) [
			tags: [ "column6" ]
            elements: [ sCol6 ]                        
        ]
        
        column column7(sColName7) [
        	tags: [ "column7" ]  
            elements: [ 
            	sCol7 : [		            
		            ^class: sCol7
		        ]
            ]                      
        ]
        
        column column8(sColName8) [        	
        	tags: [ "column8" ]     
            elements: [ 
            	sCol8 : [		            
		            ^class: sCol8
		        ]
            ]                           
        ]        
    ]
     	
	structure (message) msgFailed [
		string (title) sTitle = "{Call Failed}"
		volatile string (body) sMessage = "{There was a problem querying the database.}"
	]

   structure(message) msgError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error while trying to download your document(s), please try again later.}"
    ]     
    		            
	structure (message) msgOverLimit [
		string (title) sTitle = "{More results}"
		string(body) sBody = "{The search returned more results than are displayed. To reduce the number of matches, improve the search parameters to narrow the search.}"
	]
	
   structure(message) msgInputError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{Please provide additional search criteria for the documents you are looking for.}"
    ]    	
          
    /*************************
     * MAIN SUCCESS SCENARIO
     *************************/
	
    /*==================================================================================================
     * 1. System loads the name of the current user and call SearchConfig api to get the current 
     *    setting for the FLEX fields
     */ 
	action actionInit [
		GetColumnNames.init()
		srGetResponseParam.REQ_APP = "csr"
		switch apiCall SearchSettings.GetSearchFields (srGetResponseParam,srGetResponse, srStatus) [
		    case apiSuccess setDocumentController
		    default errorReport
		]	    
	]

	action setDocumentController [
		switch on actors [
            has assist_document_controller actionEnableDocController
            default documentSearchScreen
        ]
	]	

	action actionEnableDocController [
		hasActorDocController = "true"
		goto (documentSearchScreen)
	]	
			
	/*=============================================================================================
	 * 2. User provides search criteria to find a user.
	 */ 
	xsltScreen documentSearchScreen("{Document Search}") [
		
       form main [
            class: "st-document-search"

		    // Display the header
            div headerPage [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"               
	                display sPageName
				]
            ]
            
                  
            // display the message and search button                           
            div row1 [
                class: "row"
                
                div col1 [                  
					div headerRow [
						logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]   
	  					class: "col-12"
	  					display sMsgHeader
	  				]	  				
                    
                    div headerRowSub [
                    	logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"]   
	  					class: "col-12"
	  					display sMsgHeaderSub
	  				]
	  			]	  				
			] 	
			
			// Display all content
			div content [
				class : "row"	

			    div col1Search [
					class: "col-6"

					h4 rowTitle [     
						class: "st-padding-bottom"          
		                display sMsgSearchTitle
					]					

	  				display fCustomerId [
	               		class: "row"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]
	  				
	  				display fFlex1 [
	               		class: "row"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]
	  				
	  								 
	  				display fAccountNumber [
	               		class: "row"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]
	  				
	  				display fAccountName [
	               		class: "row"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]	  				

	  				display fFlex6 [
	               		class: "row st-padding-bottom"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]

	  				display fFlex2 [
	               		class: "row st-padding-bottom"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"]        		  					
	  				]

	  				display fFlex3 [
	               		class: "row st-padding-bottom"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	                	
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	               			     if hasActorDocController == "false" then "remove"
	               		]        		  					
	  				]

	  				display fFlex3A [
	               		class: "row st-padding-bottom"    
	               		label_class: "col-4"
	                	field_class: "col-6"     
	               		logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	               			     if hasActorDocController == "true" then "remove"
	               		]        		  					
	  				]
		  		   	
 		  			display fAccountNumberSub [
    					class: "row"
	               		label_class: "col-4"
	                	field_class: "col-6"  
	                	logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"]  
		  			]
		  		    	
		  			display fDocIdentifierSub [
	               		class: "row"    
	               		label_class: "col-4"
	                	field_class: "col-6"  
	                	logic: [ if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"]  		  				
		  		    ]

	  				display fFlex1Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX1_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]	  							  						
	  				]
	  		   						
	  				display fFlex2Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX2_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]	  		  					
	  				]

	  				display fFlex3Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX3_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  					
	  				]
	  		   		
 	  				display fFlex4 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX4_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]	  			  					
	  				]

	  				display fFlex4Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX4_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  					
	  				]
	  		   		
	  				display fFlex5 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX5_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex5Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX5_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]

	  				display fFlex6Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX6_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  		   		
	  		   
 	  				display fFlex7 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX7_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex7Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX7_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  	
	  		   
 	  				display fFlex8 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX8_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex8Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX8_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  	
	  		   						
 	  				display fFlex9 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX9_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex9Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX9_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  	
	  		   		
 	  				display fFlex10 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX10_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex10Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX10_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  	
	  		   		
	  				display fFlex11 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX11_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex11Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX11_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  		  		   
 
 	  				display fFlex12 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX12_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex12Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX12_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	  		  		   
 	  		   
  	  				display fFlex13 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX13_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex13Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX13_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	 

  	  				display fFlex14 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX14_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex14Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX14_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	 
	  					  				
  	  				display fFlex15 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX15_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex15Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX15_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	 
	  								
  	  				display fFlex16 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX16_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex16Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX16_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	 

  	  				display fFlex17 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX17_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex17Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX17_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	
	  				
  	  				display fFlex18 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX18_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex18Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX18_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	
	  		   
   	  				display fFlex19 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX19_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex19Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX19_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]	

   	  				display fFlex20 [
              			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX20_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then "remove"
	  					]		  					
	  				]

	  				display fFlex20Sub [
             			class: "row" 	               		
               			label_class: "col-4"
	                	field_class: "col-6"                    
	  					logic: [ 
	  						if srGetResponse.RSP_FLEX20_ENABLE != "true" then "remove"
	  						if srGetResponse.RSP_SUBSTRING_ENABLE != "true" then "remove"
	  					]		  	  					
	  				]		  		   						  		  			
			     ]						  		   			  		   

			    div col2Date [
			   		class: "col-6"
			   		
			   		h4 rowDateTitle [        
			   			class: "st-padding-bottom"       
		                display sMsgOptional
					]	
			   		
	  				display fromDate [	  					
	  					class: "from row"
	  					label_class: "col-2"
                		field_class: "col-6"
                		
	  					control_ng-required: "form['toDate']"
	  					control_attr_datepicker-options: "{showWeeks: false}"	
	  					sRequired_ng-show: "!!main['fromDate.aDate'].$error.required"
	  				]	
	  			
	  				display toDate [
	  					class: "to row"
	  					label_class: "col-2"
                		field_class: "col-6"
	  					
	  					control_ng-required: "form['fromDate']"
	  					control_attr_datepicker-options: "{showWeeks: false}"	
	  					sRequired_ng-show: "!!main['toDate.aDate'].$error.required"
	  				]	  					
			   	]		   					  		   	  		   	  		   		  		   	  		   	  		   				
			]

			div buttons [
				class: "row mb-5"
				
				div col2 [  
						class: "col-12"
				        navigation searchaction(checkifSubstringEnabled, "{SEARCH}") [
				            class: "btn btn-primary st-search-button"
				            data: [fAccountNumber,fDocIdentifier, fCustomerId, fAccountName, fromDate, toDate, 
				            	   fFlex1, fFlex2, fFlex3, fFlex4, fFlex5, fFlex6, 
				            	   fFlex7, fFlex8, fFlex9, fFlex10, fFlex11, fFlex12, fFlex13, fFlex14, fFlex15,
				            	   fFlex16, fFlex17, fFlex18, fFlex19, fFlex20,
				            	   fAccountNumberSub,fDocIdentifierSub, fFlex1Sub, fFlex2Sub, fFlex3Sub, fFlex4Sub, fFlex5Sub, fFlex6Sub, 
				            	   fFlex7Sub, fFlex8Sub, fFlex9Sub, fFlex10Sub, fFlex11Sub, fFlex12Sub, fFlex13Sub, fFlex14Sub, fFlex15Sub,
				            	   fFlex16Sub, fFlex17Sub, fFlex18Sub, fFlex19Sub, fFlex20Sub]   
				            attr_tabindex: "3"
				        ]
				]				
			]
		]                    
	 ]
	
	 /*=============================================================================================
     * 3. Checks if the substring search option is enabled.
     */
	action checkifSubstringEnabled [
		
		if srGetResponse.RSP_SUBSTRING_ENABLE == "true" then
			assignSubStringFields
		else
			assignFields
	]
	
	 /*=============================================================================================
     * 4. Assign substring search fields to the request. 
     */
	action assignSubStringFields [
		
 		srFindRequest.REQ_ACCOUNT_NUMBER  	= fAccountNumberSub.pInput
 		srFindRequest.REQ_DOCUMENT_NUMBER 	= fDocIdentifierSub.pInput 		
		srFindRequest.REQ_FLEX1 			= fFlex1Sub.pInput
		srFindRequest.REQ_FLEX2				= fFlex2Sub.pInput
		srFindRequest.REQ_FLEX3				= fFlex3Sub.pInput		
		srFindRequest.REQ_FLEX4				= fFlex4Sub.pInput
		srFindRequest.REQ_FLEX5				= fFlex5Sub.pInput
		srFindRequest.REQ_FLEX6				= fFlex6Sub.pInput
		srFindRequest.REQ_FLEX7				= fFlex7Sub.pInput
		srFindRequest.REQ_FLEX8				= fFlex8Sub.pInput
		srFindRequest.REQ_FLEX9				= fFlex9Sub.pInput
		srFindRequest.REQ_FLEX10			= fFlex10Sub.pInput
		srFindRequest.REQ_FLEX11			= fFlex11Sub.pInput
		srFindRequest.REQ_FLEX12			= fFlex12Sub.pInput
		srFindRequest.REQ_FLEX13			= fFlex13Sub.pInput
		srFindRequest.REQ_FLEX14			= fFlex14Sub.pInput
		srFindRequest.REQ_FLEX15			= fFlex15Sub.pInput
		srFindRequest.REQ_FLEX16			= fFlex16Sub.pInput
		srFindRequest.REQ_FLEX17			= fFlex17Sub.pInput
		srFindRequest.REQ_FLEX18			= fFlex18Sub.pInput
		srFindRequest.REQ_FLEX19			= fFlex19Sub.pInput
		srFindRequest.REQ_FLEX20			= fFlex20Sub.pInput
	
 		goto(getDocuments)   	               
	]
	
    /*=============================================================================================
     * 5. Assign string search fields to the request. 
     */ 
 	action assignFields [
 		 	
 		srFindRequest.REQ_ACCOUNT_NUMBER  	= fAccountNumber.pInput
 		srFindRequest.REQ_DOCUMENT_NUMBER 	= fDocIdentifier.pInput 
 		srFindRequest.REQ_ACCOUNT_NAME      = fAccountName.pInput 	
 		srFindRequest.REQ_PAYMENT_GROUP     = "1FFC-ea0f1923-255f-4f12-a603-16a1ed4f950c"
		srFindRequest.REQ_ORG_ID 	        = fCustomerId.pInput
 			
		srFindRequest.REQ_FLEX1 			= fFlex1.pInput
		srFindRequest.REQ_FLEX2				= fFlex2.pInput
		srFindRequest.REQ_FLEX3				= fFlex3.pInput		
		srFindRequest.REQ_FLEX4				= fFlex4.pInput
		srFindRequest.REQ_FLEX5				= fFlex5.pInput
		srFindRequest.REQ_FLEX6				= fFlex6.pInput
		srFindRequest.REQ_FLEX7				= fFlex7.pInput
		srFindRequest.REQ_FLEX8				= fFlex8.pInput
		srFindRequest.REQ_FLEX9				= fFlex9.pInput
		srFindRequest.REQ_FLEX10			= fFlex10.pInput
		srFindRequest.REQ_FLEX11			= fFlex11.pInput
		srFindRequest.REQ_FLEX12			= fFlex12.pInput
		srFindRequest.REQ_FLEX13			= fFlex13.pInput
		srFindRequest.REQ_FLEX14			= fFlex14.pInput
		srFindRequest.REQ_FLEX15			= fFlex15.pInput
		srFindRequest.REQ_FLEX16			= fFlex16.pInput
		srFindRequest.REQ_FLEX17			= fFlex17.pInput
		srFindRequest.REQ_FLEX18			= fFlex18.pInput
		srFindRequest.REQ_FLEX19			= fFlex19.pInput
		srFindRequest.REQ_FLEX20			= fFlex20.pInput
		
		goto(isFlexField2Empty)               
	]

	action isFlexField2Empty [
		if srFindRequest.REQ_FLEX2 == "None" then
		   resetFlex2
		else
		   isFlexField3Empty
	]

	action resetFlex2 [
		srFindRequest.REQ_FLEX2 = ""
		goto (isFlexField3Empty)
	]	
	
	action isFlexField3Empty [
		if srFindRequest.REQ_FLEX3 == "None" then
		   resetFlex3
		else
		   isDocumentController
	]

	action resetFlex3 [
		srFindRequest.REQ_FLEX3 = ""
		goto (isFlexField6Empty)
	]		

	action isDocumentController [
		if hasActorDocController == "false" then
		   setFlex3
		else
		   isFlexField6Empty
	]

	action setFlex3 [
		srFindRequest.REQ_FLEX3= "External"
		goto (isFlexField6Empty)
	]
	
	action isFlexField6Empty [
		if srFindRequest.REQ_FLEX6 == "None" then
		   resetFlex6
		else
		   checkAccountNumIsSet
	]

	action resetFlex6 [
		srFindRequest.REQ_FLEX6 = ""
		goto (checkAccountNumIsSet)
	]	
		
	action checkAccountNumIsSet [
		if srFindRequest.REQ_ACCOUNT_NUMBER != "" then 
			getDocuments
		else
			checkDocIdIsSet
	]

	action checkDocIdIsSet [
		if srFindRequest.REQ_DOCUMENT_NUMBER != "" then 
			getDocuments
		else
			checkFlex1IsSet
	]	

	action checkFlex1IsSet [
		if srFindRequest.REQ_FLEX1 != "" then 
			getDocuments
		else
			checkFlex2IsSet
	]	
	
	action checkFlex2IsSet [
		if srFindRequest.REQ_FLEX2 != "" then 
			getDocuments
		else
			checkFlex3IsSet
	]	
	
	action checkFlex3IsSet [
		if srFindRequest.REQ_FLEX3 != "" then 
			getDocuments
		else
			checkFlex4IsSet
	]

	action checkFlex4IsSet [
		if srFindRequest.REQ_FLEX4 != "" then 
			getDocuments
		else
			checkFlex5IsSet
	]
	
	action checkFlex5IsSet [
		if srFindRequest.REQ_FLEX5 != "" then 
			getDocuments
		else
			checkFlex6IsSet
	]

	action checkFlex6IsSet [
		if srFindRequest.REQ_FLEX6 != "" then 
			getDocuments
		else
			checkFlex7IsSet
	]
	
	action checkFlex7IsSet [
		if srFindRequest.REQ_FLEX7 != "" then 
			getDocuments
		else
			checkFlex8IsSet
	]
	
	action checkFlex8IsSet [
		if srFindRequest.REQ_FLEX8 != "" then 
			getDocuments
		else
			checkFlex9IsSet
	]
	
	action checkFlex9IsSet [
		if srFindRequest.REQ_FLEX9 != "" then 
			getDocuments
		else
			checkFlex10IsSet
	]
	
	action checkFlex10IsSet [
		if srFindRequest.REQ_FLEX10 != "" then 
			getDocuments
		else
			checkFlex11IsSet
	]
	
	action checkFlex11IsSet [
		if srFindRequest.REQ_FLEX11 != "" then 
			getDocuments
		else
			checkFlex12IsSet
	]
	
	action checkFlex12IsSet [
		if srFindRequest.REQ_FLEX12 != "" then 
			getDocuments
		else
			checkFlex13IsSet
	]	
	
	action checkFlex13IsSet [
		if srFindRequest.REQ_FLEX13 != "" then 
			getDocuments
		else
			checkFlex14IsSet
	]
	
	action checkFlex14IsSet [
		if srFindRequest.REQ_FLEX14 != "" then 
			getDocuments
		else
			checkFlex15IsSet
	]	
	
	action checkFlex15IsSet [
		if srFindRequest.REQ_FLEX15 != "" then 
			getDocuments
		else
			checkFlex16IsSet
	]
	
	action checkFlex16IsSet [
		if srFindRequest.REQ_FLEX16 != "" then 
			getDocuments
		else
			checkFlex17IsSet
	]	
	
	action checkFlex17IsSet [
		if srFindRequest.REQ_FLEX17 != "" then 
			getDocuments
		else
			checkFlex18IsSet
	]
	
	action checkFlex18IsSet [
		if srFindRequest.REQ_FLEX18 != "" then 
			getDocuments
		else
			checkFlex19IsSet
	]	
	
	action checkFlex19IsSet [
		if srFindRequest.REQ_FLEX19 != "" then 
			getDocuments
		else
			checkFlex20IsSet
	]
	
	action checkFlex20IsSet [
		goto (getDocuments)
/* 		if srFindRequest.REQ_FLEX20 != "" then 
			getDocuments
		else
			inputError
*/
	]	
	
     /*=============================================================================================
     * 6. Get the document details. 
     */
    action getDocuments [
    	Dates.fixRange(fromDate.aDate, toDate.aDate)    	
    	srFindRequest.REQ_START_DATE 		= fromDate.aDate
 		srFindRequest.REQ_END_DATE 			= toDate.aDate
 		srFindRequest.FORMAT_JSON           = sFormat
 		
    	switch apiCall AgentFindDocuments.GetDocuments (srFindRequest, srFindResponse, srStatus) [
		    case apiSuccess getResultsAction
		    default errorReport
		]	
    ]
    
    /*=============================================================================================
     * 7 . Based on user input for search, system will call agentFindDocument api to get documents
     *     populate into tdocList
     */
	action getResultsAction [
		tdocList = srFindResponse.RSP_RESULTS
		
		switch srFindResponse.RSP_OVERLIMIT [
			case "true" showOverLimitMessage
			case "false" documentSearchResultsScreen
			default documentSearchResultsScreen
		]
	]
	
	/*=============================================================================================
	 * 8. System display the search results 
	 */ 
	xsltScreen documentSearchResultsScreen("{Search Results}") [
		
       form main [
            class: "st-document-search"

		    // Display the header
            div headerPage [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"               
	                display sResultsHeading
				]
            ]
                  
            // display the message and search button                           
            div row1 [
                class: "row"
                
                  
				div headerRow [
	  				class: "col-sm-10"
	  				display sMsgResultsHeader   
	  			]	  				
				
				div col2 [
					class: "col-sm-2"
		            navigation search(searchAgainAction, "{SEARCH AGAIN}") [  
		                  class: "btn btn-primary st-search-button"
		                  data: [tdocList]   
		                  attr_tabindex: "3"					
				    ]
				]						
			] 	
			
			// Display all content
			div content [
				div col1 [
					class : "row st-padding-top"
					display  tdocList [
						class: "st-doc-search"
					]						
				]	
			]                    
	    ]
	]

	/*==========================================
	 * 9. User clicks Search again button
	 */ 
	action searchAgainAction [         
        gotoUc (documentSearch)
    ]

	/*==========================================
	 * 10. Search query results in more than max limit.
	 */ 
	action showOverLimitMessage [
        displayMessage(type: "warning" msg: msgOverLimit)         
        goto (documentSearchResultsScreen)
	]
	
	/*==========================================
	 * 11. User selects a document to view.
	 */ 
	action viewDocument [
		switch sSelectedType [
			case "U" viewHtml
			case "D" viewExtDoc
		]	]
	
	/*==========================================
	 * 13. User selects a UBF to view.
	 */ 
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
	           default viewError       
			]
		]
		
	action openExtDoc [
	    foreignHandler ExternalDocuments.open(sSelectedAccount, sSelectedDate, srGetDocumentResponse.MIME_TYPE, "inline", srGetDocumentResponse.DOCUMENT )  
	]
			
    /*************************
     * EXTENSION SCENARIOS
     *************************/

    /**************************************************************************
     *  1.2 System determined that the request failed. 
     * 		System formats and sets the display message indicating the failure.
     * 		System then returns to main path 1.
     */   
	action errorReport [
		displayMessage (msg: msgFailed type: "warning")
		goto (documentSearchScreen)
	]
	
	 action viewError [
		displayMessage(type: "danger" msg: msgError)
        goto(documentSearchScreen) 
	] 		
	
	 action inputError [
		displayMessage(type: "danger" msg: msgInputError)
        goto(documentSearchScreen) 
	] 		
]