useCase overview [
   /**
    *  author: Yvette
    *  created: June-07-2023
    *
    *  Primary Goal:
    *       1. start chid usecase to display overview page
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        1.0 07-June-2023 First Version Coded [Yvette]
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the menu option Overview.
        ]]
        postConditions: [[
            1. Primary -- Account overview page is displayed.
        ]]
    ]
	actors [
		view_overview		
	]

    actorRules [
        if sBillingType == "invoice" removeActor menu_view_bill     
    ]

    
	shortcut internal overviewJumpToPayment(jumpToPayment)[offset]
	shortcut internal overviewJumpToBillSummary(jumpToBillSummary) [offset]
	shortcut internal overviewJumpToAutoPay (jumpToAutoPay)  [offset]
	shortcut internal overviewJumpToViewDoc(jumpToViewDoc)  [offset]    

    startAt hasPortalAccess
    
    child utilImpersonationActive(utilImpersonationActive)        
    child manual multiple childAccountOverview(accountOverview)
    
    
                   
    /*************************
	* DATA ITEMS SECTION
	*************************/     	
	importJava Session(com.sorrisotech.app.utils.Session)
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)    
    importJava ParentOverview(com.sorrisotech.uc.overview.ParentOverview)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava TermsAndConditions(com.sorrisotech.fffc.user.TermsAndConditions)
	importJava UcHelper(com.sorrisotech.fffc.user.UcHelper)
    
    import billCommon.sBillAccountInternal
	import billCommon.sBillGroup
	import billCommon.sBillAccountExternal    
	import billCommon.sBillSelectedDate 
	import billCommon.sSelectedDropDown
	import billCommon.sPayAccountInternal
	import billCommon.sPayGroup
	import billCommon.sPayAccountExternal
	import billCommon.sPaySelectedDate 
	
	serviceStatus srGetOnlineAcctsStatus
    serviceParam(AccountStatus.GetEligibleAssignedAccounts) srGetOnlineAcctsReq
    serviceResult(AccountStatus.GetEligibleAssignedAccounts) srGetOnlineAcctsResult
    native string sStatusPaymentGroup = Config.get("1ffc.ignore.group")
    native string sBillPaymentGroup = Config.get("1ffc.bill.group")
    
    serviceStatus srHasPortalAccessStatus
    serviceParam(AccountStatus.HasPortalAccess) srHasPortalAccessReq
    serviceResult(AccountStatus.HasPortalAccess) srHasPortalAccessResult
    
    serviceStatus srEconsentNlsStatus
	serviceParam(FffcNotify.SetEconsentNls) srSetEconsentNlsReq
	
    serviceStatus srOverviewStatus	
    serviceParam(Documents.AccountOverview) srAcctsParam
    serviceResult(Documents.AccountOverview) srAcctsResult
    native string sUserId = Session.getUserId()    

    native string sBillingType = UcPaymentAction.getBillingBalanceType() 

	native volatile string childAccount  		= ParentOverview.account()			// Internal account number
	native volatile string childAccountDisplay 	= ParentOverview.accountDisplay()	// external account number
	native volatile string childPayGroup		= ParentOverview.payGroup()
	native volatile string childBillDate 		= ParentOverview.latestBillDate()
	native volatile string childBillCount 		= ParentOverview.billCount()
	native volatile string childDocDate 		= ParentOverview.latestDocDate()
	native volatile string childDocCount 		= ParentOverview.docCount()

	native string account
	native string payGroup
	native string offset
	
	native string sOrgId
    number eSignConsentLastUpdatedTimeStampProfile
    native string sIsConsentDataUpdatedRecently
    native string eSignConsentEnabled
    native volatile string sCurrentEpochTime = UcHelper.getCurrentEpochTime()
  
	string sPageHeader = "{Account overview}"
    
    // this is needed to force persona to generate i18n files
	string sChildId = ""

    structure(message) msgNoAccessToAccount [
    	string(title) sTitle = "{Access Denied}"
        string(body) sBody = "{You do not have access to any accounts on this portal, please contact your branch for further information.}"
    ]
    
    structure(message) msgAccessDenied [
        string(title) sTitle = "{Access Denied}"
        string(body) sBody = "{You do not have access to this application.}"
    ]
    
    string sPageName = "{E-SIGN Consent}"   
	
    tag hTermsText = TermsAndConditions.loadFile("terms_electronic_en_us.html")			       
        
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]
	       
	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	
	/* 0. System queries for user access to the portal */
	action hasPortalAccess [
		srHasPortalAccessReq.user = sUserId
		srHasPortalAccessReq.paymentGroup = sBillPaymentGroup
		switch apiCall AccountStatus.HasPortalAccess(srHasPortalAccessReq, 
													 srHasPortalAccessResult,
													 srHasPortalAccessStatus) [
			case apiSuccess testAccess
			default genericErrorMsg																							 	
		]
	]

	/* 0a. System checks result to see if user has access to portal */
	action testAccess [
		switch srHasPortalAccessResult.portalAccess [
			case enabled			getOnlineEligibleAccounts
			case disabledUser 		genericErrorMsg
			case disabledEconsent	compareEconsentLastUpdatedTime
			default 				genericErrorMsg
		]
	]
	
	/* 0b. Compare last updated time-stamp if disabledEconsent*/
	action compareEconsentLastUpdatedTime [
		loadProfile(
			eSignConsentEnabled: eSignConsentEnabled            
            eSignConsentLastUpdatedTimeStamp: eSignConsentLastUpdatedTimeStampProfile   
            )
        UcHelper.isConsentUpdatedRecently(eSignConsentLastUpdatedTimeStampProfile , srHasPortalAccessResult.lastUpdateTimestamp, sIsConsentDataUpdatedRecently)
        
        if sIsConsentDataUpdatedRecently == "true" then 
           checkEconsentStatusUserProfile
        else 
           regTermsAndConditionsScreen
	]
	
	/* 0c. Checks consent is enabled at NLS side or not.*/
	action checkEconsentStatusUserProfile [
		
		if eSignConsentEnabled == "false" then
		  regTermsAndConditionsScreen
		else
		  getOnlineEligibleAccounts
	]
	
	/* 0d. System retrieves the accounts this user is eligible to view online */
	action getOnlineEligibleAccounts [
    	srGetOnlineAcctsReq.user = sUserId
    	srGetOnlineAcctsReq.statusPaymentGroup = sStatusPaymentGroup
    	srGetOnlineAcctsReq.billPaymentGroup = sBillPaymentGroup
    	switch apiCall AccountStatus.GetEligibleAssignedAccounts(srGetOnlineAcctsReq, srGetOnlineAcctsResult, srGetOnlineAcctsStatus) [
           case apiSuccess start
           default start    // -- if this fails, do without the list --
       ]
		
	]
	/* 1. Start */
	action start	 [
		srAcctsParam.user = sUserId
		// -- passes the list of eligible accounts down as well --
		srAcctsParam.jsonAccountList = srGetOnlineAcctsResult.accountsAsJsonArray
		
		switch apiCall Documents.AccountOverview(srAcctsParam, srAcctsResult, srOverviewStatus ) [
           case apiSuccess init
           default init    
        ]   
	]


	/* 2. Init */	
	action init [		
    	switch ParentOverview.init(srAcctsResult.accounts) [
    		case "empty" genericErrorMsg
    		case "success" startAccountOverViewChild
    		default overviewScreen
    	]     	
    ]

	/* 2. Start Child AccountOverview */    
    action startAccountOverViewChild [  			

    	startUc(
    		childId: childAccountOverview 
    	    saveId: sChildId)[
 	  				sAccount			: childAccount
					sAccountDisplay 	: childAccountDisplay
					sPayGroup			: childPayGroup	
					sBillDate 			: childBillDate
					sBillCount			: childBillCount
					sDocDate			: childDocDate
					sDocCount			: childDocCount	   	  				 	  							
		]
		
		switch ParentOverview.next() [
			case "true" startAccountOverViewChild
			case "false" overviewScreen
			default overviewScreen
		]		
	]

	/* 3. Display Overview screen */
    xsltScreen overviewScreen("{Overview}") [
    	child utilImpersonationActive
    	
    	div pageHeader [
    		class: "st-summary-module-heading"	                		
    		display sPageHeader 
		] 
		
		div child1 [
			child childAccountOverview
		]
    ]

	/********************************************************
	* actions when users click on links on the overview page
	*********************************************************/
	/* action jumpToPayment */
	action jumpToPayment [
		sSelectedDropDown = offset
		
		Session.getAccount(sSelectedDropDown, sBillAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sBillGroup)
		Session.getAccountDisplay(sSelectedDropDown, sBillAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sBillSelectedDate)
		
		Session.getAccount(sSelectedDropDown, sPayAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sPayGroup)
		Session.getAccountDisplay(sSelectedDropDown, sPayAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sPaySelectedDate)
		
		gotoUc(startMakePayment)		
	]

	/* action jumpToBillSummary */	
	action jumpToBillSummary [
		sSelectedDropDown = offset 
		
		Session.getAccount(sSelectedDropDown, sBillAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sBillGroup)
		Session.getAccountDisplay(sSelectedDropDown, sBillAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sBillSelectedDate)
		
		Session.getAccount(sSelectedDropDown, sPayAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sPayGroup)
		Session.getAccountDisplay(sSelectedDropDown, sPayAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sPaySelectedDate)

		gotoUc(viewBillSummary)		
	]	

	/* action jumpToAutoPay */	
	action jumpToAutoPay [
		sSelectedDropDown = offset
		
		Session.getAccount(sSelectedDropDown, sBillAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sBillGroup)
		Session.getAccountDisplay(sSelectedDropDown, sBillAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sBillSelectedDate)
		
		Session.getAccount(sSelectedDropDown, sPayAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sPayGroup)
		Session.getAccountDisplay(sSelectedDropDown, sPayAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sPaySelectedDate)
		
		gotoUc(startAutomaticPayment)		
	]		

	/* action jumpToViewDocument */
	action jumpToViewDoc [
		sSelectedDropDown = offset 
		
		Session.getAccount(sSelectedDropDown, sBillAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sBillGroup)
		Session.getAccountDisplay(sSelectedDropDown, sBillAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sBillSelectedDate)
		
		Session.getAccount(sSelectedDropDown, sPayAccountInternal)
		Session.getPayGroup(sSelectedDropDown, sPayGroup)
		Session.getAccountDisplay(sSelectedDropDown, sPayAccountExternal)
		Session.getLatestBillDate(sSelectedDropDown, sPaySelectedDate)
		
		gotoUc(viewDocument)		
	]		
 
 	/* Generic error message */     
    action genericErrorMsg [ 
    	
        displayMessage(type: "danger" msg: msgNoAccessToAccount)     
        goto (overviewScreen)        
    ]
    
    /**************************************************************************
     * E-consent page if user disabled e-consent.
     **************************************************************************/ 
    noMenu xsltScreen regTermsAndConditionsScreen("{ESIGN Consent}") [
    	    	
        form regTermsAndConditionsForm [
	    	class: "st-login"
	            
	        div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
	            
    	            display sPageName
	            ]
			]
	    
	    	div content [
									
				div terms [
					display hTermsText    
				]
								
				div divider [
        			class: "border-top mt-3 pt-3"
					
				
            		display fCheckBoxes [
            			control_attr_tabindex: "1"
						control_attr_autofocus: ""
            		]
            		
				]
			]
			
			div buttons [
				class: "st-buttons"
				
				div row [
					class: "row"
					
					div col1 [
						class: "col-md-12"
						
						navigation termsConditionsSubmit(actionUpdateConsentFlag, "{Next}") [			             
		                	class: "btn btn-primary"  

		                	require: [
		                		fCheckBoxes
		                	]
		                	attr_tabindex: "3"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(actionAccessDenied, "{Cancel}") [			                
							class: "btn btn-secondary"
							attr_tabindex: "4"
						] 							
					]
				]					
			]			
        ]
    ]
    
    /* Update the consent flag in user profile table*/
    action actionUpdateConsentFlag [
     	updateProfile(
        	userId: sUserId        	
            eSignConsentEnabled: "true"
            eSignConsentLastUpdatedTimeStamp: sCurrentEpochTime   
            )
     	        
        if success then saveEconsentAtNls
        if failure then genericErrorMsg 
    ]
    
    /* Saves the consent data via NLS API service */
    action saveEconsentAtNls [
    	loadProfile(            
            fffcCustomerId: sOrgId   
            )
    	
    	srSetEconsentNlsReq.customerId = sOrgId
    	srSetEconsentNlsReq.sConsentActive = "true"
    	
    	
    	switch apiCall FffcNotify.SetEconsentNls(srSetEconsentNlsReq, srEconsentNlsStatus) [
    		case apiSuccess getOnlineEligibleAccounts
    		default genericErrorMsg
    	]
    ]
    
    /* Go to login screen.*/
    action actionAccessDenied [
        displayMessage(type: "danger" msg: msgAccessDenied)

        gotoModule(LOGIN)
    ]	 
      
]