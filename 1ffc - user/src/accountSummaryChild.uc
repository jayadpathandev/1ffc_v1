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

    startAt selectAccount[sParent]   

    /*************************
	* DATA ITEMS SECTION
	*************************/     
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava UcPaymentAccountBalance(com.sorrisotech.uc.payment.UcPaymentAccountBalance)
	importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
	importJava Format(com.sorrisotech.common.app.Format)
	importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)	
	importJava UcBillingAction(com.sorrisotech.uc.bill.UcBillingAction)
	importJava FlexFieldInfo(com.sorrisotech.fffc.user.FlexFieldInformation)
				
    import billCommon.sBillAccountInternal
    import billCommon.sBillAccountExternal
    import billCommon.sBillSelectedDate
	import billCommon.sBillGroup
	import billCommon.sBillStream
	import billCommon.sBillVersion
	import billCommon.sBillingPeriod
	import billCommon.sBillsFound
	import billCommon.sSelectedDropDown
	
	import billCommon.sPayAccountInternal
	import billCommon.sPayAccountExternal	
	import billCommon.sPayGroup
	import billCommon.sPayIsBill
	import billCommon.sPaySelectedDate

    // -- GetStatus returns the status for all major conditions --
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetStatus) srGetStatusParams
    serviceResult (AccountStatus.GetStatus) srGetStatusResult
    native string sAccountState = "accountClosed"
	native string sPaymentButtonOn = "false"
	 
    
	serviceStatus srStatus	
    serviceResult(SystemConfig.GetCurrentBalanceConfig) srGetComm
    
    serviceStatus srBillOverviewStatus
    serviceParam(Documents.BillOverview) 	srBillOverviewParam
    serviceResult(Documents.BillOverview) 	srBillOverviewResult    
    
    native string maxAge            = AppConfig.get("recent.number.of.months", "3")
    native string sBillDate         = Format.formatDateNumeric(srBillOverviewResult.docDate)
    native string sInvDueDateValue  = Format.formatDateNumeric(srBillOverviewResult.dueDate)
    native string sAccountBalance   = LocalizedFormat.formatAmount(srBillOverviewParam.payGroup, srBillOverviewResult.totalDue)
    
	string sUserId = Session.getUserId()
    string sAccNumLabel             = "{Account Number:}"
    string sTotDueHead      		= "{Statement Amount due}"   
	string sInvDueDateLabel			= "{Monthly payment due date}"		
	string sLoanAmountLabel         = "{Personal loan amount}"
	
	native string sCurrBalDisplay
	native string sCurrentBalance
	native string sCurrentBalanceFlag
	native string sTotalAmountDue
                
	native string sBalanceType	 
	native string sPaymentFlag      = Session.arePaymentsEnabled()
	native string sMultipleAccounts = Session.multipleAccounts()
	native string cszFlexNumber = "9"
	string cszGetFlexAsAmountFailed = "failed"
	native string sLoanAmount 	    = FlexFieldInfo.getFlexFieldAsAmount (sUserId, srBillOverviewParam.account, 
		                                                  sBillSelectedDate, srBillOverviewParam.payGroup,
		                                                  cszFlexNumber, cszGetFlexAsAmountFailed)
	
	volatile string sOlderThanXMonths = UcBillingAction.checkBillAge(maxAge, srBillOverviewParam.billDate)
	
	native string sAccountDisplay
	native string sIsBill
	
	persistent native string sParent	
		
	auto dropDown dAccounts 		[ Session.getAccounts() ]
	    
    structure(message) msgBillError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error while trying to access your most recent bill, please try again later.}"
    ]
    
    structure(message) msgStatusError [
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There was an error retrieving status information for your account, please try again later.}"
    ]
    
	
	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

	/* 1. System retrieves the account selected.*/	
	action selectAccount[
		dAccounts = sSelectedDropDown
		Session.getAccount(dAccounts, srBillOverviewParam.account)
		Session.getAccountDisplay(dAccounts, sAccountDisplay)
		Session.getPayGroup(dAccounts, srBillOverviewParam.payGroup)
		Session.getIsBill(dAccounts, srBillOverviewParam.isBill)
		Session.getLatestBillDate(dAccounts, srBillOverviewParam.billDate)
		goto (getBillOverview)
	]

	
	/* 2. System retrieves the most recent document.*/			
	action getBillOverview [		
		srBillOverviewParam.user = sUserId
		
	    switch apiCall Documents.BillOverview(srBillOverviewParam, srBillOverviewResult, srBillOverviewStatus) [
		   case apiSuccess getAccountStatus
	       default actionBillProblem
	    ]			
	]	

	/**************************************************************************************
	 * BEGIN 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	 
	/**
	 *	2a. Retrieve status information associated with this account. This status information
	 * 		will be used to drive the behavior of this sues case and the behavior of 
	 * 		the screen that's displayed.  The status information is specific to 1st 
	 * 		Franklin.
	 */ 
	action getAccountStatus [
		
		srGetStatusParams.user = sUserId
		srGetStatusParams.paymentGroup = srBillOverviewParam.payGroup
		srGetStatusParams.account = srBillOverviewParam.account
		// -- retrieve the status information --
   		switch apiCall AccountStatus.GetStatus(srGetStatusParams, srGetStatusResult, srAccountStatusCode) [
    		case apiSuccess checkStatusOfAccount
    		default errorRetrievingStatus
    	]
 	]
 	
 	/**
 	 *  2b. What's the result. Our basic account state?  That determines
 	 * 		what we need going forward.
 	 */
 	action checkStatusOfAccount [
 		sAccountState = srGetStatusResult.accountStatus
 		switch sAccountState [
 			// -- we might get a bill before status update --
 			case "newAccount" checkForNewAccountTransition 
 			// -- we know its an active account so we should have a bill --
 			case "activeAccount" checkPaymentState
 			// -- its closed, the screen knows! :-) --
 			case "closedAccount" screenShowInfo
 			default	errorRetrievingStatus
 		]
 	]
 	
 	/**
 	 *  2c. Currently "newAccount" status. But it might be in that transition
 	 * 		where we've received a bill and the status is not yet up to date.
 	 * 		If we've got a bill it isn't new anymore
 	 */
 	 action checkForNewAccountTransition [
 		if "noDocs" != srBillOverviewResult.result then 
 			setActiveState // -- got a bill, then we are really active
 		else
 			screenShowInfo  // -- just opened... show welcome
 	]
 	
 	/**
 	 *  2d. Action above determined  we've changed state from newAccount but status feed
 	 * 		has not seen that yet.. change state to active.
 	 */
 	action setActiveState [
 		sAccountState = "activeAccount"
 		goto (checkPaymentState)
 	]
 	

 	/**
 	 *  2e. Got here because we are an activeAccount, need to see
 	 * 			if payment button is enabled
 	 */
 	 action checkPaymentState [
 		sPaymentButtonOn = "true"
 		switch srGetStatusResult.paymentEnabled [
 			case "enabled"	checkResult
 			case "disabledDelinquent" checkResult // -- delinquent isn't really turned off, just limits on what we can pay
 			default turnOffPaymentButton
 		]
 	]
 	
 	/**
 	 *  2f. Got here because payment enabled status says we need
 	 * 		 to turn off payment
 	 */
 	action turnOffPaymentButton [
 		sPaymentButtonOn = "false"
 		goto(checkResult)
 	]

	/**************************************************************************************
	 * END 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */

	/* 3. Get results from BillOverview call */
	action checkResult [
	    switch srBillOverviewResult.result [
	        case "singleDoc"    loadLatestBill
	        case "noDocs"       hidePdfLink
	        case "multipleDocs" hidePdfLink
	    ]
	]
	
	/* 4. Load the latest bill data into screen elements. */
    action loadLatestBill [    
        sBillAccountInternal         = srBillOverviewParam.account   // internalAccount
        sBillAccountExternal 		 = sAccountDisplay               // externalAccount
        sBillGroup         			 = srBillOverviewParam.payGroup
        sBillingPeriod               = sBillDate
        sCurrBalDisplay              = sAccountBalance        
        sBillStream 				 = srBillOverviewResult.docStream
        sBillVersion 				 = srBillOverviewResult.docVersion 
        sIsBill						 = srBillOverviewParam.isBill
        
        sPayAccountInternal          = srBillOverviewParam.account   
        sPayAccountExternal 		 = sAccountDisplay               
        sPayGroup         		     = srBillOverviewParam.payGroup
        sPaySelectedDate			 = srBillOverviewResult.docDate
        			
		goto(checkPaymentFlag)
	]
	
	
	/* 5. Checks if the payment flag is enabled.*/
	action checkPaymentFlag [
		if sPaymentFlag == "true" then 
			retrieveSettings
		else
			screenShowInfo
	]
	
	/* 6. System retrieves the current settings.*/
	action retrieveSettings [		
		switch apiCall SystemConfig.GetCurrentBalanceConfig (srGetComm, srStatus) [
		    case apiSuccess getResults
		    default screenShowInfo
		]
	]
	
	/* 7. System gets the value and check the balance type.*/
	action getResults [		
		sBalanceType = srGetComm.RSP_CURBALTYPE	    
	    switch sBalanceType [	    	
	    	case "I" getCurrentBalanceForInternal
	    	case "F" getCurrentBalance
	    	case "R" screenShowInfo
	    	default screenShowInfo
	    ]
	]  

	/* 8. Call getDocumentCurrentBalance to get the most recent current balance */
	action getCurrentBalanceForInternal [
	  	UcPaymentAction.getDocumentCurrentBalance(
									srBillOverviewParam.account, 
									srBillOverviewResult.docNumber, 
									srBillOverviewParam.payGroup, 
									srBillOverviewResult.docAmount, 
									sCurrentBalance, 
									sCurrentBalanceFlag)		
												  
		goto(getTotalAmountDue)
		
	]
			
	/* 8B. Gets the current balance from the PMT_ACCOUNT_BALANCE table.*/
	action getCurrentBalance [
		UcPaymentAccountBalance.init(sUserId, srBillOverviewParam.account, srBillOverviewParam.payGroup)
		UcPaymentAccountBalance.getAmountDisplay(sCurrBalDisplay)
		
		goto(getTotalAmountDue)
		
	]

	/* 9. Get total amount due to display */
	action getTotalAmountDue [
		UcPaymentAction.getAmountDisplay (srBillOverviewParam.payGroup, srBillOverviewResult.totalDue, sTotalAmountDue)
		
		goto(screenShowInfo)
	]
	
    /* 10. Show the user's greeting message and latest bill information (if found). */
    xsltScreen screenShowInfo("{Greeting and recent information}") [

    form accountSummaryForm [
       	class: "st-account-filter"    
       		
       div summary [
            class: "row st-dashboard-summary"
//	        logic: [		                	
//				if sIsBill != "true" then "hide"
//	        ]	
            
            div info [
            	class: "col-8 col-sm-9 col-lg-10 row"

	            div accountCol [
	            	class: "col-12 col-lg-3 st-summary-account"	            	
 
 	            	display sAccNumLabel [
	                	class: "st-dashboard-summary-label"
	            		append_space: "true"
	            	] 
	            	
                	display sBillAccountExternal [
                		class: "st-dashboard-summary-value"
                		logic: [ if sMultipleAccounts == "true" then "remove" ]		                	
                	]
                	
	    		    display dAccounts [
						class: "d-inline form-control-sm form-select-sm st-dropdown-control"
						ontrol_attr_autofocus: ""
						auto_submit: "submit"
	            		logic: [ if sMultipleAccounts == "false" then "remove" ]		                	
	            	]
	            	navigation submit(submitSelectedAccount, "{Submit}") [
		                class:    "btn btn-primary d-none"
		                validate: "false"
			            data: [ dAccounts ]
	    		    ]	       		                	                		            	
	            ]
	            
	            div loanState [
	            	class: "row col-12 col-lg-3 st-summary-loan-state text-lg-center"
					logic: [ if "activeAccount" != sAccountState then "remove"]
	            	display sLoanAmount [
	            		class: "h3 st-dashboard-summary-value"
	                ]
	            	
	            	display sLoanAmountLabel [
	                	class: "st-dashboard-summary-label"
	            	] 	            	
	            ]
				    
				div payDate [
					class: "row col-12 col-lg-3 st-summary-date text-lg-center"
					logic: [ if "activeAccount" != sAccountState then "remove"]

					display sInvDueDateValue [
	            		class: "h3 st-dashboard-summary-value d-lg-block"
	                ]				
						            	
	                display sInvDueDateLabel [			              			 				                				                	
	                	class: "st-dashboard-summary-label d-lg-block"
	                ]
	
				]        
	
				div payAmount [
					class: "row col-12 col-lg-3 st-summary-amount text-lg-center"
					logic: [ if "activeAccount" != sAccountState then "remove"]

	                display  sTotalAmountDue [
	            		class: "h3 st-dashboard-summary-value d-lg-block"
	                ]
	            	
	                display sTotDueHead [
	                	class: "st-dashboard-summary-label d-lg-block"
	                ]
				]
				
				
			]  
			      
			div payNow [
				class: "col-4 col-sm-3 col-lg-2 st-summary-pay-now float-end"
 
				logic: [
                	if sParent == "payment_onetime" then "hide"			                	
                ]
            	display sPaymentButtonOn
        		navigation payNowLink(gotoPaymentOnetime, "{Pay Now}") [
					class: "btn btn-primary"
													
					logic: [
						if sPaymentButtonOn == "false" then "disabled"
//	                	if sOlderThanXMonths == "true"  then "hide"
//	                	if sPaymentFlag      != "true"  then "hide"	                	
	                ]
	                attr_tabindex: "2"
				]
			]  // -- end div payNow --      
        ] // -- end div summary
	 ]
    ]		    

   /* 11. Action to submit the date and redirect to parent screen.*/
    action submitSelectedAccount [    	    
    	sSelectedDropDown = dAccounts  	
		Session.getAccount(dAccounts, sPayAccountInternal)
		Session.getAccountDisplay(dAccounts, sPayAccountExternal)
		Session.getPayGroup(dAccounts, sPayGroup)
		Session.getIsBill(dAccounts, sPayIsBill)
		Session.getLatestBillDate(dAccounts, sPaySelectedDate)    	

   	switch sParent [
            case "bill" gotoBillSummary
            case "usage" gotoBillUsage
            case "document" gotoDocument
            case "payment_onetime" gotoPayment
            case "payment_automatic" gotoPayment
            case "payment_wallet" gotoPayment 
            case "payment_history" gotoPayment
            case "payment_document" gotoDocument
            default gotoBillSummary
    	]
    ]
    
    /* 12. Action to go to bill summary.*/
    action gotoBillSummary [    
    	gotoUc(billSummary)
    ]
    
    /* 12A. Action to go to bill usage.*/
    action gotoBillUsage [
    	gotoUc(billUsage)
    ] 
    
    /* 12B. Action to go to document.*/
    action gotoDocument [
    	gotoUc(document)
    ] 

	/* 12C. Action to go to payment.*/
    action gotoPayment [
    	gotoModule(PAYMENT)[sActive:sParent]
    ] 
    
	action gotoPaymentOnetime [
		sParent = "payment_onetime"
		goto (gotoPayment)
	]    

	/*************************
	* EXTENSION SCENARIOS
	*************************/
	/* 3A. There was either no Vault document found for the bill, or more than 1 was found. In that
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
	
	action errorRetrievingStatus [
		displayMessage (type: "danger" msg: msgStatusError)
		goto(screenShowInfo)
	]
]