useCase accountSummaryChild [
	/**
	* author: Maybelle Johnsy Kanjirapallil
	* created: 10-Feb-2016
	*
	* Primary Goal:
	* To display summary of the account.
	*
	* Alternative Outcomes:
	* 1. No recent bill is available
	*
	* Major Versions:
	* 1.0 10-Feb-2016 First Version Coded [Maybelle Johnsy Kanjirapallil]
	*/

    documentation [
        preConditions: [[
            1. The endUser can successfully log into the system.
        ]]
        triggers: [[
            1. The endUser successfully logs in and lands on the overview as a default.
            2. The endUser has clicked the "Corresponded" menu.
        ]]
        postConditions: [[
            1. The endUser can view the account summary.
        ]]
    ]
    actors [
        view_document
        view_bill
    ]
	    
    startAt checkIfBillsEnabled[sParent]
    

    /*************************
	* DATA ITEMS SECTION
	*************************/     
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava UcPaymentAccountBalance(com.sorrisotech.uc.payment.UcPaymentAccountBalance)
	importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
	importJava Format(com.sorrisotech.common.app.Format)
	importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)
	
    import billCommon.sBillAccountInternal
    import billCommon.sBillAccountExternal
	import billCommon.sBillGroup
	import billCommon.sBillStream
	import billCommon.sBillVersion
	import billCommon.sBillingPeriod
	import billCommon.sBillsFound
    
	serviceStatus srStatus	
    serviceResult(SystemConfig.GetCurrentBalanceConfig) srGetComm
    serviceParam(Documents.MostRecent) srLatestReq
    serviceResult(Documents.MostRecent) srLatestResult
    
    native string maxAge            = AppConfig.get("recent.number.of.months", "3")
    native string sBillDate         = Format.formatDateNumeric(srLatestResult.docDate)
    native string sInvDueDateValue  = Format.formatDateNumeric(srLatestResult.dueDate)
    native string sAccountBalance   = LocalizedFormat.formatAmount(srLatestResult.payGroup, srLatestResult.totalDue)
    
	string sUserId = Session.getUserId()
    string sAccNumLabel             = "{Account number}"
    string sTotDueHead      		= "{Monthly payment amount}"   
	string sInvDueDateLabel			= "{Monthly payment due date}"		
	string sLoanAmountLabel         = "{Personal loan amount}"
	
	native string sCurrBalDisplay
	native string sCurrentBalance
	native string sCurrentBalanceFlag
	native string sTotalAmountDue
	native string sLoanAmount = "$10,000"
                
	native string sBalanceType
	native string sOlderThanXMonths = "true" 
	native string sPaymentFlag      = Session.arePaymentsEnabled()
	native string sBillsEnabledFlag = Session.areBillsEnabled()
	native string sParent = ""
	    
    structure(message) msgBillError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error while trying to access your most recent bill, please try again later.}"
    ]
	
	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

    /* 1.  Check whether SmartBill is enabled */
	action checkIfBillsEnabled [
		if sBillsEnabledFlag == "true" then 
			setUseUBF
		else
			setDontUseUBF
	]	

	/* 2. Set Use UBF */
	action setUseUBF [
	   srLatestReq.useDocument = "true"
	   goto (loadMostRecent)	
	]	
	
	/* 2A. Set Use UBF */
	action setDontUseUBF [
	   srLatestReq.useDocument = "false"

	   goto (loadMostRecent)   
	]		
	
	/* 3. Initialize the latest bill.*/
	action loadMostRecent [
        srLatestReq.user = sUserId
        srLatestReq.olderThan = maxAge
        
        switch apiCall Documents.MostRecent(srLatestReq, srLatestResult, srStatus) [
           case apiSuccess checkResult
           default actionBillProblem
        ]
	]

	/* 4. Get results from MostRecent call */
	action checkResult [
	    switch srLatestResult.result [
	        case "singleDoc"    loadLatestBill
	        case "noDocs"       hidePdfLink
	        case "multipleDocs" hidePdfLink
	    ]
	]
	
	/* 5. Load the latest bill data into screen elements. */
    action loadLatestBill [    
        sBillAccountInternal = srLatestResult.internalAccount
        sBillAccountExternal = srLatestResult.externalAccount
        sBillGroup           = srLatestResult.payGroup
        sBillingPeriod       = sBillDate
        sCurrBalDisplay      = sAccountBalance
        sOlderThanXMonths    = srLatestResult.oldBill
        
        sBillStream = srLatestResult.docStream
        sBillVersion = srLatestResult.docVersion 
        sOlderThanXMonths = srLatestResult.oldBill
        			
		goto(checkPaymentFlag)
	]
	
	
	/* 6. Checks if the payment flag is enabled.*/
	action checkPaymentFlag [
		if sPaymentFlag == "true" then 
			retrieveSettings
		else
			screenShowInfo
	]
	
	/* 7. System retrieves the current settings.*/
	action retrieveSettings [		
		switch apiCall SystemConfig.GetCurrentBalanceConfig (srGetComm, srStatus) [
		    case apiSuccess getResults
		    default screenShowInfo
		]
	]
	
	/* 8. System gets the value and check the balance type.*/
	action getResults [		
		sBalanceType = srGetComm.RSP_CURBALTYPE	    
	    switch sBalanceType [	    	
	    	case "I" getCurrentBalanceForInternal
	    	case "F" getCurrentBalance
	    	case "R" screenShowInfo
	    	default screenShowInfo
	    ]
	]  
	
	/* 9. Gets the current balance from the PMT_ACCOUNT_BALANCE table.*/
	action getCurrentBalance [
		UcPaymentAccountBalance.init(sUserId)
		UcPaymentAccountBalance.getAmountDisplay(sCurrBalDisplay)
		
		goto(getTotalAmountDue)
		
	]

	/* 10. Get total amount due to display */
	action getTotalAmountDue [
		UcPaymentAction.getAmountDisplay (srLatestResult.payGroup, srLatestResult.totalDue, sTotalAmountDue)
		
		goto(screenShowInfo)
	]
	
	/* 9A. Call getDocumentCurrentBalance to get the most recent current balance */
	action getCurrentBalanceForInternal [
	  	UcPaymentAction.getDocumentCurrentBalance(
									srLatestResult.internalAccount, 
									srLatestResult.docNumber, 
									srLatestResult.payGroup, 
									srLatestResult.docAmount, 
									sCurrentBalance, 
									sCurrBalDisplay,
									sCurrentBalanceFlag)		
												  
		goto(getTotalAmountDue)
		
	]
	
    /* 11. Show the user's greeting message and latest bill information (if found). */
    xsltScreen screenShowInfo("{Greeting and recent information}") [
    	
        div summary [
            class: "row st-dashboard-summary"
            
            div account [
            	class: "col-6 col-lg-3 d-flex flex-column justify-content-center st-summary-account"
            	
            	div accountFlex [
	            	display sAccNumLabel [
	                	class: "st-dashboard-summary-label"
	            		append_space: "true"
	            	] 
	            	display sBillAccountExternal [
	            		class: "st-dashboard-summary-value"
	                ]
                ]            	
            ]

			div payNowNarrow [
				class: "col-6 d-lg-none d-flex flex-column justify-content-center st-summary-pay-now"
            	
            	div payFlexN [
	        		navigation payNowLinkN(gotoPayment, "{Pay Now}") [
						class: "btn btn-primary float-end"
														
						logic: [
		                	if sOlderThanXMonths == "true"  then "hide"
		                	if sPaymentFlag      != "true"  then "hide"
		                	
		                ]
		                attr_tabindex: "2"
					]
				]
			]        
            
            div info [
            	class: "col-12 col-lg-6 col-lg-6 col-xl-7 mt-4 mt-lg-0 text-center"
	            
	            h2 values [
	            	class: "row"
	            	
	            	div loanValue [
	            		class: "col"
	            		display sLoanAmount
	            	]

	            	div dateValue [
	            		class: "col"
	            		display sInvDueDateValue
	            	]

	            	div dueValue [
	            		class: "col"
	            		display sTotalAmountDue
	            	]
	            ]
	            div labels [
	            	class: "row st-dashboard-summary-label"
	            	
	            	div loanLabel [
	            		class: "col"
	            		display sLoanAmountLabel	
	            	]

	            	div dateLabel [
	            		class: "col"
	            		display sInvDueDateLabel	
	            	]

	            	div dueLabel [
	            		class: "col"
	            		display sTotDueHead	
	            	]
	            ]
			]
			        
			div payNowWide [
				class: "d-none d-lg-flex col-lg-3 col-xl-2 d-flex flex-column justify-content-center st-summary-pay-now"
            	
            	div payFlexW [
	        		navigation payNowLinkW(gotoPayment, "{Pay Now}") [
						class: "btn btn-primary float-end"
														
						logic: [
		                	if sOlderThanXMonths == "true"  then "hide"
		                	if sPaymentFlag      != "true"  then "hide"
		                	
		                ]
		                attr_tabindex: "2"
					]
				]
			]        
        ]
    ]		    

	/* 12. Go to the payment page. */
    action gotoPayment [    
    	
        gotoModule(PAYMENT)
    ]
    
	/*************************
	* EXTENSION SCENARIOS
	*************************/
	/* 5A. There was either no Vault document found for the bill, or more than 1 was found. In that
	 * case we will hide the "View pdf" link. */ 
	action hidePdfLink [
		sBillsFound = "false"
		
		goto (loadLatestBill)
	]
	
	/* 1A. There was a problem trying to get the initialize the bill data. */
	 action actionBillProblem [
		displayMessage(type: "danger" msg: msgBillError)

        goto(screenShowInfo) 
	]	
]