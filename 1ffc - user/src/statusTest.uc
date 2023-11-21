useCase statusTest [
  
  /**
	* author: John Kowalonek
	* created: 2023-Nov-11
	*
	* Primary Goal:
	*  Single page that tests the Status APi used to get business rules that govern
	*  the behavior of the application
	*
	* Alternative Outcomes:
	* 1. Page displays with list of accounts associated with current user, user selects an account 
	*	  and status for that account is displayed.
	* 2. Page displays with list of accounts associated with current user, user selects an account
	* 		and one or more status calls fail.
	* 3. Call to get accounts for user fails and page displays error message with empty table.
	*
	* Major Versions:
	* 	2023-Nov-12 -- first version for 1FFC -- jak
	*/
	
	   documentation [
        preConditions: [[
            1. The endUser can successfully log into the system.
           
        ]]
        triggers: [[
			1. The enduser selects this test on the menu.
        ]]
        postConditions: [[
            1. Screen shows test results
            2. Screen shows something fucked up if status call fails
        ]]
    ]
    
    
    
    startAt start
    
    importJava Session(com.sorrisotech.app.utils.Session)

	/**
	 * Status calls for testing. There's the "everything" call
	 * and then there are the various calls that are designed
	 * to be used in specific places in the application that will
	 * just return a single value
	 * 
	 */    
    
    // -- GetStatus returns the status for all major conditions --
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetStatus) srGetStatusParams
    serviceResult (AccountStatus.GetStatus) srGetStatusResult
    
    // -- GetBasicStatus returns the status for just account itself --
    serviceParam (AccountStatus.GetBasicStatus) srGetBasicStatusParams
    serviceResult (AccountStatus.GetBasicStatus) srGetBasicStatusResult
    
    // -- IsPaymentEnabled --
    serviceParam (AccountStatus.IsPaymentEnabled) srIsPaymentEnabledParams
    serviceResult (AccountStatus.IsPaymentEnabled) srIsPaymentEnabledResult
    
    // -- IsAchEnabled --
    serviceParam (AccountStatus.IsAchEnabled) srIsAchEnabledParams
    serviceResult (AccountStatus.IsAchEnabled) srIsAchEnabledResult

    // -- IsMinimumPaymentRequired --
    serviceParam (AccountStatus.IsMinimumPaymentRequired) srIsMinRequiredParams
    serviceResult (AccountStatus.IsMinimumPaymentRequired) srIsMinRequiredResult
    
    // -- HasAccountAccess --
    serviceParam (AccountStatus.HasAccountAccess) srHasAccountAccessParams
    serviceResult (AccountStatus.HasAccountAccess) srHasAccountAccessResult
    
    // -- HasAccountAccess --
    serviceParam (AccountStatus.HasPortalAccess) srHasPortalAccessParams
    serviceResult (AccountStatus.HasPortalAccess) srHasPortalAccessResult

    native string sAccountIdStatus = ""
    native string sPaymentGroupStatus = ""
    
    /**
     * The fields here are for displaying the status elements returned from
     * each status call 
     */
     
    field fUserId [
    	string(label) label = "{User ID:}"
		string(control) control
    ]
    field fPaymentGroup [
    	string(label) label = "{Payment Group:}"
    	string(control) control
    ]
    
    field fAccountId [
    	string(label) label = "{Account ID:}"
    	string(control) control 
    ]
    
    field fAccountStatus [
    	string(label) label = "{Account Status:}"
    	string(control) control
    ]
    
    field fPaymentEnabled [
    	string(label) label = "{Payment Enabled:}"
    	string(control) control
    ]  
    
    field fAchEnabled [
    	string(label) label = "{ACH Enabled:}"
    	string(control) control
    ]  

    field fViewAccount [
    	string(label) label = "{View Account:}"
    	string(control) control
    ]  
    
    field fBasicAccountStatus [
    	string(label) label = "{Account Status:}"
    	string(control) control
    ]
    
    field fIsPaymentEnabled [
    	string(label) label = "{Payment Enabled:}"
    	string(control) control
    ]
 
    field fIsAchEnabled [
    	string(label) label = "{ACH Enabled:}"
    	string(control) control
    ]

    field fIsMinimumPaymentRequired[
    	string(label) label = "{Min Payment:}"
    	string(control) control
    ]
    
    field fMinimumPaymentRequiredAmount[
    	string(label) label = "{Min Payment Amt:}"
    	string(control) control
    ]
    
    field fHasAccountAccess[
    	string(label) label = "{Account Access:}"
    	string(control) control
    ]

   field fHasPortalAccess[
    	string(label) label = "{Portal Access:}"
    	string(control) control
    ]
     
    

	/**
	 * API Call to get a list of accounts assigned to a user and
	 *  the important attributes of those accounts
	 */
    serviceStatus srOverviewStatus	
    serviceParam(Documents.AccountOverview) srAcctsParam
    serviceResult(Documents.AccountOverview) srAcctsResult
    
    /**
     * Table fo displaying the list of accounts and important
     * attributes of those accounts
     */ 
   	table tTable [
	    emptyMsg: "{No Accounts Configured}"
	 
	 	"account"    		=> link linkStatus(viewStatus) [  
	 		sAccountIdStatus : sAccountId
			sPaymentGroupStatus : sPaymentGroup
	 	]    
		"account"			=> string sAccountId
		"accountDisplay" 	=> string sAccountDisplayName
		"payGroup" 			=> string sPaymentGroup
		"latestBill"		=> string sLatestBillDate
		"billCount" 		=> string sNumberOfBills
		"latestDoc"			=> string sLatestDocDate
		"docCount"			=> string sNumberOfDocs
		
	    column acctCol("{Account Id}") [
	        elements: [ linkStatus ]
	        
	    ]
	    
        column acctDisplayCol("{Account Display Id}") [
	        elements: [sAccountDisplayName]                        
	    ]
	    
	    column amountCol("{Payment Group}") [
	        elements: [sPaymentGroup]  
	    ] 
	    
	    column billDate("{Bill Date}") [
	        elements: [sLatestBillDate]  
	    ] 
	    
	    column numBills("{# Bills}") [
	        elements: [sNumberOfBills]  
	    ] 
	    
	    column docDate("{DocDate}") [
	        elements: [sLatestDocDate]  
	    ] 
	    
	    column numDocs("{# Docs}") [
	    	elements: [sNumberOfDocs]
	    ]
	    
	 ]	

	/**
	 * Generic information used on the page
	 */
 	string sPageHeader = "{Status Test}"
    
    structure msgGenericError [
        static sBody = "{Yup... something's fucked up!}"
    ]

	native string szUId = Session.getUserId()
    native string bStatusCalled = "false"	
    string szAccountListHeader = "{Account List for Current User}"
    string szParametersHeader = "{Parameters for Status Call(s)}"
    string szGetStatusHeader = "{Returned from GetStatus}"
    string szGetBasicStatusHeader = "{Returned from GetBasicStatus, HasAccountAccess, HasPortalAccess}"
    string szGetPaymentStatusHeader = "{Returned from IsPaymentEnabled, IsAchEnabled, IsMinimumPaymentRequired}"

	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/    
    
    /** 1. Get account information for the current user
     */
    action start [
		// -- give a value to all the controls in case of error
		//		so they don't show up vertically flattened --
		fUserId.control = "empty"
		fAccountId.control = "empty"
		fPaymentGroup.control = "empty"
		fAccountStatus.control = "empty"
		fPaymentEnabled.control = "empty"
		fAchEnabled.control = "empty"
		fViewAccount.control = "empty"
		fBasicAccountStatus.control = "empty"
		fIsAchEnabled.control = "empty"
		fHasAccountAccess.control = "empty"
		fHasPortalAccess.control = "empty"
		fIsMinimumPaymentRequired.control = "empty"
		fIsPaymentEnabled.control = "empty"
		fMinimumPaymentRequiredAmount.control ="empty"
		
		srAcctsParam.user = szUId
		switch apiCall Documents.AccountOverview(srAcctsParam, srAcctsResult, srOverviewStatus ) [
           case apiSuccess init
           default genericErrorMsg    
        ]
    ]
    
    /** 2. Move that information into a table to be displayed. Note this
     * 		is also where the page gets redisplayed when an account is selected
     *		and we've retrieved the status for that account
     */
    action init [
     	tTable = srAcctsResult.accounts
     	goto (accountStatusScreen)
    ]
    
    /** 3. Show the screen with table and optionally status results
     * 
     */
    xsltScreen accountStatusScreen [
    	div pageHeader [
     		class: "st-summary-module-heading"	                		
    		display sPageHeader    		
    	]
    	
 		    	
    	// -- table of accounts for this user --
    	div cancelAutomaticBody [
           class: "container mb-2"

    	   // -- table of accounts for this user --
		   div tablerow [
		   		class: "row mt-4"
	            div tableHeader [
	            	class: "col-md-12 h2"
	            	display szAccountListHeader
	            ]    
	            div tableRow [                	
	                class: "row"
	                display tTable [
	                	class: "col-md-12"    
	                ]                   																				
				] 
			] // -- end of table of acocounts -- 
			      
			// -- arguments for the calls --
 	        div rowargs [
 	        	class: "row mt-4"	
 	        	div arguments [
		  	      	logic: [if bStatusCalled == "false" then "remove" ]
		        	div header [
		        		class: "col-md-12 h2"
		        		display szParametersHeader
		        	]
		        	div row1 [
		        		class: "row"
			        	div userIdentifier [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fUserId
				        	]
				 		]         	
			        	div payGroup [
				        	class: "col-md-4"
				        	div details [
				        		class: "disabled"
				        		display fPaymentGroup
				        	]
				 		]         	
				 		div accountId [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fAccountId
				        	]
				 		]
					]	        		
	        	]
	        ] // -- end of arguments for calls --
        
        	// -- GetStatus Results
	        div rowStatus [
	        	class: "row mt-4"
		        div status [
		        	logic: [if bStatusCalled == "false" then "remove" ]
		        	div header [
		        		class: "col-md-12 h2"
		        		display szGetStatusHeader
		        	]
		        	div row1 [
		        		class: "row"
			   			div accountStatus [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fAccountStatus
				        	]
			        	]
			       		div accountPayEnabled [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fPaymentEnabled
				        	]
				 		]
			      		div accountAchEnabled [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fAchEnabled
				        	]
				 		]       	
			      		div accountView [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fViewAccount
				        	]
				 		]  
			 		] 
			 		
			 	]    	
	        ] // -- end of GetStatus
        
	        // -- Conditionally show GetBasicStatus, HasAccountAccess, and HasPortalAccess information --
	      	div basicstatus [
	        	logic: [if bStatusCalled == "false" then "remove" ]
	        	class: "row mt-4"
	        	div header [
	        		class: "col-md-12 h2"
	        		display szGetBasicStatusHeader
	        	]
	        	div rowB1 [
	        		class: "row"
		   			div accountStatus [
			        	class: "col-md-2"
			        	div details [
			        		class: "disabled"
			        		display fBasicAccountStatus
			        	]
		        	]
		   			div hasAccountAccess [
			        	class: "col-md-2"
			        	div details [
			        		class: "disabled"
			        		display fHasAccountAccess
			        	]
		        	]
		   			div hasPortalAccess [
			        	class: "col-md-2"
			        	div details [
			        		class: "disabled"
			        		display fHasPortalAccess
			        	]
		        	]
	         	]
         	]  /** end of GetBasicStatus, etc. */
 
        	// -- IsPaymentEnabled, IsAchEnabled, IsMinimumPaymentRequired Results
	        div rowPayment [
	        	class: "row mt-4"
		        	div status [
		        	logic: [if bStatusCalled == "false" then "remove" ]
		        	div header [
		        		class: "col-md-12 h2"
		        		display szGetPaymentStatusHeader
		        	]
		        	div row1 [
		        		class: "row"
			   			div PaymentEnabled [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fIsPaymentEnabled
				        	]
			        	]
			       		div ACHEnabled [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fIsAchEnabled
				        	]
				 		]
			      		div minRequired [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fIsMinimumPaymentRequired
				        	]
				 		]       	
			      		div minRequiredAmt [
				        	class: "col-md-2"
				        	div details [
				        		class: "disabled"
				        		display fMinimumPaymentRequiredAmount
				        	]
				 		]  
			 		] 
			 		
			 	]    	
	        ] // -- end of IsPaymentEnabled, etc.
         
         ]
    ]
    
    /**
     * 4. User clicks on link associated with an account, call GetStats API for that 
     *		account and all other APIs in subsequent actions 
     */
    action viewStatus [
    	fUserId.control = szUId
    	fAccountId.control = sAccountIdStatus
    	fPaymentGroup.control = sPaymentGroupStatus
    	srGetStatusParams.user = szUId
    	srGetStatusParams.paymentGroup = sPaymentGroupStatus
    	srGetStatusParams.account = sAccountIdStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.GetStatus(srGetStatusParams, srGetStatusResult, srAccountStatusCode) [
    		case apiSuccess assignStatus
    		default genericErrorMsg
    	]
    ]
    
    /** 5. Assign the status information to the screen variables and go to next call     */
    action assignStatus [
		bStatusCalled = "true"
		fAccountStatus.control = srGetStatusResult.accountStatus
		fPaymentEnabled.control = srGetStatusResult.paymentEnabled
		fAchEnabled.control = srGetStatusResult.achEnabled
		fViewAccount.control = srGetStatusResult.viewAccount
    
    	goto (basicStatus)
    ]
    
    /** 6. Retrieve the basic status information (account status only) */
    action basicStatus [
    	srGetBasicStatusParams.user = szUId
    	srGetBasicStatusParams.paymentGroup = sPaymentGroupStatus
    	srGetBasicStatusParams.account = sAccountIdStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.GetBasicStatus(srGetBasicStatusParams, srGetBasicStatusResult, srAccountStatusCode) [
    		case apiSuccess assignBasicStatus
    		default genericErrorMsg
    	]
    ]

    /** 7. Assign the basic status information to the screen variables and go back 
     * 		to show the screen with latest status informatiuon
     */
    action assignBasicStatus [
		fBasicAccountStatus.control = srGetBasicStatusResult.accountStatus
    	goto (isPaymentEnabled)
    ]

    /** 8. Retrieve IsPaymentEnabled information */
    action isPaymentEnabled [
    	srIsPaymentEnabledParams.user = szUId
    	srIsPaymentEnabledParams.paymentGroup = sPaymentGroupStatus
    	srIsPaymentEnabledParams.account = sAccountIdStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.IsPaymentEnabled(srIsPaymentEnabledParams, srIsPaymentEnabledResult, srAccountStatusCode) [
    		case apiSuccess assignIsPaymentEnabled
    		default genericErrorMsg
    	]
    ]

    /** 9. Assign IsPaymentEnabled information to the screen variables
     */
    action assignIsPaymentEnabled [
		fIsPaymentEnabled.control = srIsPaymentEnabledResult.bPaymentEnabled
    	goto (isAchEnabled)
    ]

    /** 10. Retrieve IsAchEnabled information */
    action isAchEnabled [
    	srIsAchEnabledParams.user = szUId
    	srIsAchEnabledParams.paymentGroup = sPaymentGroupStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.IsAchEnabled(srIsAchEnabledParams, srIsAchEnabledResult, srAccountStatusCode) [
    		case apiSuccess assignIsAchEnabled
    		default genericErrorMsg
    	]
    ]

    /** 11. Assign IsAchEnabled information to the screen variables
     */
    action assignIsAchEnabled [
		fIsAchEnabled.control = srIsAchEnabledResult.bAchEnabled
    	goto (isMinimumPaymentRequired)
    ]
    
   /** 12. Retrieve IsMnimumPaymentRequired information */
    action isMinimumPaymentRequired [
    	srIsMinRequiredParams.user = szUId
    	srIsMinRequiredParams.paymentGroup = sPaymentGroupStatus
    	srIsMinRequiredParams.account = sAccountIdStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.IsMinimumPaymentRequired(srIsMinRequiredParams, srIsMinRequiredResult, srAccountStatusCode) [
    		case apiSuccess assignIsMinimumPaymentRequired
    		default genericErrorMsg
    	]
    ]

    /** 13. Assign IsMnimumPaymentRequired information to the screen variables
     */
    action assignIsMinimumPaymentRequired [
		fIsMinimumPaymentRequired.control = srIsMinRequiredResult.bMinimumRequired
		fMinimumPaymentRequiredAmount.control = srIsMinRequiredResult.sAmountRequired
    	goto (hasAccountAccess)
    ]
    
    
    /** 14. Retrieve HasAccountAccess information */
    action hasAccountAccess [
    	srHasAccountAccessParams.user = szUId
    	srHasAccountAccessParams.paymentGroup = sPaymentGroupStatus
    	srHasAccountAccessParams.account = sAccountIdStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.HasAccountAccess(srHasAccountAccessParams, srHasAccountAccessResult, srAccountStatusCode) [
    		case apiSuccess assignHasAccountAccess
    		default genericErrorMsg
    	]
    ]

    /** 15. Assign HasAccountAccess information to the screen variables
     */
    action assignHasAccountAccess [
		fHasAccountAccess.control = srHasAccountAccessResult.bAccessEnabled
    	goto (hasPortalAccess)
    ]
  
    /** 16. Retrieve HasPortalAccess information */
    action hasPortalAccess [
    	srHasPortalAccessParams.user = szUId
    	srHasPortalAccessParams.paymentGroup = sPaymentGroupStatus
    	// -- call the api for status --
    	switch apiCall AccountStatus.HasPortalAccess(srHasPortalAccessParams, srHasPortalAccessResult, srAccountStatusCode) [
    		case apiSuccess assignHasPortalAccess
    		default genericErrorMsg
    	]
    ]

    /** 17. Assign HasPortalAccess information to the screen variables
     */
    action assignHasPortalAccess [
		fHasPortalAccess.control = srHasPortalAccessResult.bAccessEnabled
    	goto (init)
    ]
    
	/*****************************************
	* ALTERNATIVE PATH SCENARIOS (ERRORS)
	******************************************/    

    /* 1. Generic error message used during testing
     * 		assumption is that there's someone at a
     *		debug console that can get the details
     *		associated with the problem
     */     
    action genericErrorMsg [ 
    	
        displayMessage(type: "danger" msg: msgGenericError.sBody)     
        goto (accountStatusScreen)        
    ]
    
    	
    
]
