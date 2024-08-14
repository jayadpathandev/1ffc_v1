useCase paymentAutomatic [
   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 23-Nov-2016
    *
    *  Primary Goal:
    *       1. Display automatic payment usecase.
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        1.0 23-Nov-2016 First Version Coded [Maybelle Johnsy Kanjirapallil]
    * 		 2.0 23-Feb-2022 payment harmonization
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the sub menu option Payment Automatic in the Payments page.
        ]]
        postConditions: [[
            1. Primary -- Payment automatic details are displayed.
        ]]
    ]
    actors [ 
        create_payment
    ]       
		   
    startAt getPayments[sGroupJson, status, futurePmtFlag] 
    
    child utilImpersonationActive(utilImpersonationActive)
    child paymentCommon(paymentCommon)
    child manual paymentUpdateAutomaticPayment(paymentUpdateAutomaticPayment)
    
    /*************************
	* DATA ITEMS SECTION
	*************************/ 
    
    importJava I18n(com.sorrisotech.app.common.utils.I18n)
    importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
    importJava Session(com.sorrisotech.app.utils.Session)
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
    importJava DateFormat(com.sorrisotech.common.DateFormat)
    importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)	
	importJava TermsAndConditions(com.sorrisotech.fffc.user.TermsAndConditions)
	importJava FffcAccountAction(com.sorrisotech.fffc.account.FffcAccountAction)
	importJava DisplayAccountMasked(com.sorrisotech.fffc.account.DisplayAccountMasked)
    
    import apiPayment.pmtRequest
    import apiPayment.getAutomaticPaymentByUserId
    import apiPayment.deleteAutomaticPayment
    import billCommon.sPayAccountInternal  
    import billCommon.sPayAccountExternal  
	import billCommon.sPayGroup		
	import paymentCommon.sPmtGroupConfigResult
	import paymentCommon.sDeleteAutomaticMsgFlag
	import paymentCommon.sShowNoChangeMsgFlag	
    import paymentCommon.sErrorAutomaticMsgFlag    
        
	native string sDateFormat = DateFormat.toJsonString()
	native string sFormat = LocalizedFormat.toJsonString()
	
	string sSelectedAutomaticId
	string sGroupJson
	string status
	string futurePmtFlag

	static msgPmtScheduled1_body = "{There are one or more payments scheduled for this account. Creating an recurring payment schedule will NOT override the upcoming payment(s). If you want to view or cancel the upcoming payments, click [a href='<1>']here[/a]}"
	static msgPmtScheduled2_body = "{There are one or more payments scheduled for this account. Editing this recurring payment schedule will NOT override the upcoming payment(s). If you want to view or cancel the upcoming payments, click [a href='<1>']here[/a]}"
	static msgPmtScheduled3_body = "{At least one payment is already scheduled for the account covered by this recurring payment schedule, which will not be affected by the new schedule.}"

    static sMultipleAccounts = "Multiple accounts"  
        
    string sAutomaticPaymentHeader    = "{Recurring Payments}"   
    string sLabelPaymentHistory       = "{Recurring payment history}" 
    string sLabelPaymentEdit          = "{Edit payment}"
    string sLabelPaymentCancel        = "{Cancel payment}"
    string szHistoryHeader            = "{Recurring payment history}"
    string sEmptyWalletMsg            = "{Payment wallet is empty.}"                    
    string sMessageCreate 		      = "{You have created a new recurring payment schedule using <1> to make the payments. This recurring payment schedule will apply to all payments due until cancelled, or until the next to last payment due.}"      
	string sMessageEdit	              = "{You have successfully modified this recurring payment of <1>. This recurring payment schedule will apply to all bills due from this day forward.}"
    string sMessageDelete             = "{Recurring payment has been successfully removed from <1>.}"  
    string sNoChangeMsg               = "{You didn't change any option. The system took no action as a result.}"
    string sErrorMsg                  = "{An error occurred while trying to fulfill your request. Please try again later}"
//  string sConfirmDeleteText         = "{Are you sure you want to delete the recurring payment from <1>?}"
    string sConfirmDeleteText         = "{You have canceled your recurring payment schedule. Please ensure that you also cancel any individually scheduled payments to avoid being charged.}"    
    string sImportantInfo 			  = "{Important Information: Please allow up to 20 minutes for payments made in your online services account to be reflected in 1st Franklin's loan servicing system.}"
    
    // Bring over from b2b    
    volatile string sSourceCreateMsg = I18n.translate ("paymentAutomatic_sMessageCreate", sNickName)
    volatile string sSourceEditMsg = I18n.translate ("paymentAutomatic_sMessageEdit", sNickName)  
    volatile string sSourceDeleteMsg = I18n.translate ("paymentAutomatic_sMessageDelete", sNickName) 

    native string sUserId     = Session.getUserId()   
    volatile string sIsB2b 	  = Session.isB2bAsString()     
    native string sSourceId   = ""        
    native string sWalletCount  = UcPaymentAction.getWalletCount(sUserId)
//  native string sAutoPayCount = UcPaymentAction.getAutoPayCount(sUserId)
    native string sAutoPayCount = UcPaymentAction.getAutoPayCount(sUserId, sPayAccountInternal, sPayGroup, sIsB2b, sDeleteAutomaticHistoryText)
    native string sNtfParams = ""
    native string sShowCreateFlag  = "false"
    native string scheduleFoundWithAccount = "false"
    native string sSendDeleteEmailFlag = NotifUtil.isNotificationEnabled(sUserId, "payment_automatic_delete_success")
    volatile native string sDisplayAccountNickname = DisplayAccountMasked.displayAccountLookup(sUserId, sPayAccountInternal, sPayGroup)
    native string sPmtScheduledFlag = UcPaymentAction.getPaymentScheduledFlag(
        sUserId,
		sPayAccountInternal,
		"",
        "automatic"
    )   
    native string sCurrentAccounts = UcPaymentAction.getCurrentAccounts()    
    
    persistent native string sNickName   = ""
    persistent native string sPmtSourceId = ""
    
    string sDeleteSourceTitle   = "{CONFIRM REMOVE RECURRING PAYMENT}"
 	volatile string sDeleteSourceText1 = I18n.translate ("paymentAutomatic_sConfirmDeleteText", sNickName)     
    string sDeleteSourceText2 = "{This action cannot be undone.}" 
    native string sIpAddress         = Session.getExternalIpAddress()
    native string sCategory          = "terms_and_conditions"
    native string sType              = "recurring_payment"
    native string sOperation         = "The user has successfully set up an automatic (recurring) payment"
    native string sPortalChannel 	 = "portal"
    native string sOrgId
    native string sDeleteAutomaticHistoryText = "Recurring payment deleted."
    native string bIsConsentActive = "true"
    
    persistent input sGeolocation
    
    structure(message) msgPmtScheduledMsg1 [
		string(title) sTitle = "{Payment warning}"
		volatile native string (body) sBody = UcPaymentAction.getAutomaticWarningMsg("create")   
	]
	
	structure(message) msgPmtScheduledMsg2 [
		string(title) sTitle = "{Payment warning}"
		volatile native string (body) sBody = UcPaymentAction.getAutomaticWarningMsg("edit")
	]

	structure(message) msgPmtScheduledMsg3 [
		string(title) sTitle = "{Payment warning}"
		string (body) sBody = "{At least one payment is already scheduled for the account covered by this recurring payment schedule, which will not be affected by the new schedule.}"
	]

	structure(message) msgNoPmtGroupError [
		string(title) sTitle = "{Configuration problem}"
		string(body) sBody = "{There is no payment group configured to your account. Please contact your System Administrator.}"
	]
	
    structure(message) msgMultiplePmtGroupError [
		string(title) sTitle = "{Not supported}"
		string(body) sBody = "{There are more than one payment group configured to your account. We currently do not support multiple payment groups. Please contact your System Administrator.}"
	]
	
    structure msgAutoAccAlreadyFound [
        static sBody = "{One or more of the accounts selected have been removed since they are found in one of the recurring schedules for this user.}"
    ]

    structure msgAutoUnableComplete [
        static sBody = "{There are no accounts selected so a schedule cannot be created}"
    ]

    serviceStatus ssStatus
	        
    serviceParam(Payment.GetAutomaticPaymentByUserId)  srGetAutomaticParam
    serviceResult(Payment.GetAutomaticPaymentByUserId) srGetAutomaticResult
    
    serviceParam(Payment.GetAutomaticPaymentForAccount)  srGetAutomaticForAcctParam
    serviceResult(Payment.GetAutomaticPaymentForAccount) srGetAutomaticForAcctResult        
    
    serviceParam(Payment.DeleteAutomaticPayment)  srDeleteAutomaticParam
    serviceResult(Payment.DeleteAutomaticPayment) srDeleteAutomaticResult
    
    serviceParam(Payment.GetAutomaticPaymentHistory)  srGetAutomaticHistoryParam
    serviceResult(Payment.GetAutomaticPaymentHistory) srGetAutomaticHistoryResult
    
    serviceParam(Payment.DeleteAutomaticPaymentHistory)  srDeleteAutomaticHistoryParam
    serviceResult(Payment.DeleteAutomaticPaymentHistory) srDeleteAutomaticHistoryResult
    
    serviceParam (Profile.AddLocationTrackedEvent) setLocationData
	serviceResult (Profile.AddLocationTrackedEvent) setLocationResp
	
	serviceParam(FffcNotify.SetUserAddressNls) setDataFffc
      
    table tAutomaticPaymentsTable [
        emptyMsg: "{There are no details to display}"
 
// 		"BILLING_ACCOUNT_ID"  => string sAccountId
// 		"INTERNAL_ACCOUNT_ID" => string sIntAccountId            
//        "PMT_GROUP_ID"       => string sGroupId
//        "SOURCE_ID"          => string sSourceId
//        "SOURCE_NAME"        => string sSourceName
//        "PAY_TRIGGER"        => string sPayTrigger
//        "PAY_COUNT"          => string sPayCount
//        "PAY_EXPIRY"         => string sPayExpiry
//        "PAY_AMOUNT"         => string sPayAmount           
//        "EDIT_DATE"          => string sLastEditDate
//        "EDIT_DATE_NUM"      => number nLastEditDate
//        "AUTO_STATUS"        => string sAutoStatus
//        "USER_ID"            => string sUserId

 		"AUTOMATIC_ID"       => string sAutomaticId
 		"GROUPING_JSON" 	 => string sGroupingJson
        "PMT_GROUP_ID"       => string sGroupId
        "SOURCE_ID"          => string sSourceId
        "SOURCE_NAME"        => string sSourceName
        "PAY_TRIGGER"        => string sPayTrigger
        "PAY_COUNT"          => string sPayCount
        "PAY_EXPIRY"         => string sPayExpiry
        "PAY_AMOUNT"         => string sPayAmount           
        "EDIT_DATE"          => string sLastEditDate
        "EDIT_DATE_NUM"      => number nLastEditDate
        "AUTO_STATUS"        => string sAutoStatus
        "USER_ID"            => string sUserId
        "GRP_DISP_ACC"		 => string sAccountNumber
        
        "" => string  sAutoExpired  = "Card expired" 
        
        link "" automaticPaymentHistory(getAutomaticPaymentHistory) [            
			sSelectedAutomaticId: sAutomaticId
        ]
 
 /**
  *		Removed for 1st Franklin Explicit requirement to not support
  * 	edit of automatic payment
  *
  *              
  *      link "" automaticPaymentEdit(updateAutomaticPaymentAction) [            
  *			sSelectedAutomaticId: sAutomaticId
  *			sPmtSourceId: sSourceId
  *      ]
  */       
        link "" automaticPaymentDelete(cancelAutomaticPaymentPopin) [            
			sSelectedAutomaticId: sAutomaticId
        ]
 
         column payForScheduledCol("{Account number}") [
            elements: [sAccountNumber]   
            sort: [sAccountNumber]         
        ]
 		             
        column payFromCol("{Payment method}") [
            elements: [sSourceName]   
            sort: [sSourceName]         
        ]
 
        column payAmountCol("{Pay amount}") [
            elements: [sPayAmount]   
            sort: [sPayAmount]
            tags: [ "d-none", "d-sm-none" ]
        ]  
 
       column payTriggerCol("{Recurring payment date}") [
            elements: [sPayTrigger]   
            sort: [sPayTrigger]                     
        ]
 
        column payCountCol("{Pay count}") [
            elements: [sPayCount]   
            sort: [sPayCount]
            tags: [ "d-none", "d-sm-none" ]
        ] 

       column expiryCol("{Debit card expiration date}") [
            elements: [sPayExpiry]   
            sort: [sPayExpiry]
            tags: [ "d-none", "d-sm-none" ]
        ] 
                              
        column lastEditDateCol("{Last edit date}") [
            elements: [sLastEditDate]  
            sort: [nLastEditDate]
            tags: [ "d-none", "d-sm-none" ]
        ] 
                
        column actionsCol("{Actions}") [
        	tags: ["payment-automatic-status"]
           elements: [       
           		automaticPaymentHistory: [
           			^type: "popin"      
           			attr_class: "payment-history-auto-img st-left-space"     			
           		],        
/**  
 *			REMOVED SPECIFICALLY FOR 1ST FRANKLIN BASED ON THEIR REQUIREMENTS 
           	automaticPaymentEdit: [           			    			
           			attr_class: "payment-edit-img st-left-space"
           		],
*/            	
            	automaticPaymentDelete: [
           			^type: "popin"    
           			attr_class: "payment-cancel-img st-left-space"       			
           		],
           		
           		sAutoExpired: [
            		^class: 'pay-auto-expired'
            		^class: sAutoStatus
        		]  
           ]
       ]                   
    ]
    
    table tAutomaticPaymentsHistoryTable [
        emptyMsg: "{There are no details to display}"
 
 		"CHANGE_DATE"    => string sChangeDate       
        "USERNAME"       => string sUserName
        "CONFIG_CHANGE"  => string sConfigChange
           
        column changeDateCol("{Change date}") [
            elements: [sChangeDate]   
        ]
 
        column changeByCol("{Change by}") [
            elements: [sUserName]   
            sort: [sUserName] 
            tags: [ "d-none", "d-sm-none" ]
        ]  
 
       column congigChangeCol("{Configuration change}") [
            elements: [sConfigChange]   
        ]
     ]
    
	// -- handling impersonation --
 	import utilImpersonationActive.sImpersonationActive
 	native string bImpersonateActive
    
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	/* 1. Initialize the usecase. Get automatic payment details. */
	
	action getPayments [
		bImpersonateActive = sImpersonationActive
		if sIsB2b == "true" then 
			getPaymentsByUserId
		else
			getPaymentsForAccount	
	]

	/* get payment by user id */
	action getPaymentsByUserId [
		
		srGetAutomaticParam.USER_ID     = sUserId		
		srGetAutomaticParam.FORMAT_JSON = sFormat	
		stopUc(childId: paymentUpdateAutomaticPayment)	
		
		switch apiCall Payment.GetAutomaticPaymentByUserId(srGetAutomaticParam, srGetAutomaticResult, ssStatus) [
		    case apiSuccess setAutomaticResults
		    default gotoPaymentAutomaticScreen
		]
    ]

	/* get payment by user id, account and payment group */    
	action getPaymentsForAccount [
				
		srGetAutomaticForAcctParam.USER_ID     = sUserId		
		srGetAutomaticForAcctParam.FORMAT_JSON = sFormat
		srGetAutomaticForAcctParam.INTERNAL_ACCOUNT_ID = sPayAccountInternal	 
		srGetAutomaticForAcctParam.PMT_GROUP_ID = sPayGroup
		srGetAutomaticForAcctParam.CONFIG_CHANGE = sDeleteAutomaticHistoryText
		
		stopUc(childId: paymentUpdateAutomaticPayment)	
		
		switch apiCall Payment.GetAutomaticPaymentForAccount(srGetAutomaticForAcctParam, srGetAutomaticForAcctResult, ssStatus) [
		    case apiSuccess setAutomaticResults
		    default gotoPaymentAutomaticScreen
		]
    ]

	/* 2. set automatic details  */    
    action setAutomaticResults [      
    	if sIsB2b == "true"  then
    		setAutomaticResultsB2b
    	else
    		setAutomaticResultsB2c       	
    ]

	/* set automatic results for b2b */    
    action setAutomaticResultsB2b [
    	tAutomaticPaymentsTable = srGetAutomaticResult.automatic
    	switch status [
			case "addSuccess" addLocationTrackedEvent
			case "editSuccess" addLocationTrackedEvent			
			default checkAutomaticPaymentMethods
		]
    ]

	/* set automatic results for b2c */    
    action setAutomaticResultsB2c [
    	FffcAccountAction.setAutomaticPaymentsTableFromPaymentAutomaticData(srGetAutomaticForAcctResult.automatic, tAutomaticPaymentsTable)    	
    	 switch status [
			case "addSuccess" addLocationTrackedEvent
			case "editSuccess" addLocationTrackedEvent			
			default checkAutomaticPaymentMethods
		]
    ]
    
    
	 /**************************************************************************
	 *  Adding user geolocation tracked events.
	 */
    action addLocationTrackedEvent [
    	setLocationData.sUser = sUserId
    	setLocationData.sCategory = sCategory
    	setLocationData.sType = sType
    	setLocationData.sData = sPayAccountInternal
    	setLocationData.sIpAddress = sIpAddress
    	setLocationData.sBrowserGeo = sGeolocation
    	setLocationData.sOperation = sOperation
    	
    	switch apiCall Profile.AddLocationTrackedEvent(setLocationData, setLocationResp, ssStatus) [
    		case apiSuccess saveConsentAtNls
    		default genericErrorMsg
    	]
    ]
    
	/**************************************************************************
     * Save user's recurring payment consent. (NLS side).
     * As per discussion saving channel name as "portal" and channel 
     * address as internal account number.
     */
    action saveConsentAtNls [
    	loadProfile(            
            fffcCustomerId: sOrgId   
            )
    	setDataFffc.customerId = sOrgId
    	setDataFffc.channel = sPortalChannel
    	setDataFffc.address = sPayAccountInternal
    	setDataFffc.browserGeo = sGeolocation
    	setDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDataFffc.ipAddress = sIpAddress
    	setDataFffc.sConsentActive = bIsConsentActive
    	
    	switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, ssStatus) [
    		case apiSuccess checkAutomaticPaymentMethods
    		default genericErrorMsg
    	]
    ]  

    /* 3. Check automatic payment methods */
    action checkAutomaticPaymentMethods [        
        switch UcPaymentAction.checkAutomaticPaymentMethods(sUserId, tAutomaticPaymentsTable) [
    		case "success" gotoPaymentAutomaticScreen
    		case "error" gotoPaymentAutomaticScreen
    		default gotoPaymentAutomaticScreen
    	]      
    ]

	/*-------------------------------------------------------------
	 * 4. payment automatic screen 
	 * The button create payment automatic applies to B2C only  
	 -------------------------------------------------------------*/
	action gotoPaymentAutomaticScreen [
		if sIsB2b == "true" then
			isGroupingJson
		else			
			setShowCreateFlag
	]	

	/* 4.1 Set ShowCreateFlag to display the create automatic button */	
	action setShowCreateFlag [
		sShowCreateFlag = "true"		
		goto(isGroupingJson)
	]
	
	/* 5. Check whether is GroupingJson is null */
	action isGroupingJson [   		
 		if sGroupJson != "" then
			isSelectedAutomaticId
		else
			setsGroupJsonAction		
	]
	
	/* 6. Check to see if there are existing automatic payments */
	action isSelectedAutomaticId [
		if sSelectedAutomaticId == "" then
			hasExistingAutomaticPayments
		else
			setsGroupJsonAction                    // case for edit a scheduled payment
	]

	/* 6.1 Set GroupJson */	
	action setsGroupJsonAction [
		sGroupJson			     = "[{\"internalAccountNumber\":\"" + sPayAccountInternal + "\", \"displayAccountNumber\":\"" + sPayAccountExternal + "\", \"paymentGroup\":\"" + sPayGroup + "\"}]"
		goto(paymentAutomaticScreen)
	]   
	
	/* 7. Has existing automatic payments */
	action hasExistingAutomaticPayments [
        switch UcPaymentAction.hasExistingAutomaticPayments(sUserId, sGroupJson) [
	        case "ok" createAutomaticPaymentAction
	        case "update" setCurrentAccountsForUpdate
	        case "removeAll" displayRemoveAllMessages	
	        default getPayments
        ]    
	]

	/*  Gets automatic payment history details. */
    action getAutomaticPaymentHistory [
    	tAutomaticPaymentsTable = srGetAutomaticResult.automatic    
    	srGetAutomaticHistoryParam.USER_ID     = sUserId
    	srGetAutomaticHistoryParam.AUTOMATIC_ID = sSelectedAutomaticId
		srGetAutomaticHistoryParam.DATE_FORMAT = sDateFormat
		srGetAutomaticHistoryParam.CONFIG_CHANGE = sDeleteAutomaticHistoryText
		
    	switch apiCall Payment.GetAutomaticPaymentHistory(srGetAutomaticHistoryParam, srGetAutomaticHistoryResult, ssStatus) [
		    case apiSuccess getAutomaticResults
		    default automaticPaymentHistoryPopin
		]
    ]
    
    /*  Populate the automatic payments table. */
	action getAutomaticResults [        
        tAutomaticPaymentsHistoryTable = srGetAutomaticHistoryResult.history    
        goto(automaticPaymentHistoryPopin)   
    ]
    
    /* 9. Shows the payment automatic details. */
    xsltScreen paymentAutomaticScreen("{Payment}") [
		
		child utilImpersonationActive
		
		div payAutomaticContainerContent [
			div payAutomaticContainerContentHolder [
				class: "row st-padding-top st-padding-bottom"
				
				div sMessageAutomaticCreate [
					class: "alert alert-success addSuccess"
					
					logic: [	
					        if status == "" then "remove"
							if status == "editSuccess" then "remove"					
					] 	    	    		
	    	    	display sSourceCreateMsg   	    		
				]
				
				div sMessageAutomaticEdit [
					class: "alert alert-success editSuccess"
					
					logic: [	
					        if status == "" then "remove"
							if status == "addSuccess" then "remove"					
					] 	    	    		
	    	    	display sSourceEditMsg   	    		
				]
				
				div sMessageAutomaticDelete [
					class: "alert alert-success deleteSuccess"
					
    	    		logic: [
						if sDeleteAutomaticMsgFlag == "false" then "remove"						
					] 	  	    		
	    	    	display sSourceDeleteMsg  			    	    	
 				]	
 					
		        div message1 [
					logic: [						
						if sPmtGroupConfigResult != "nopaygroup" then "remove"						
					]				
					class: "alert alert-danger"
					attr_role: "alert"				
					display msgNoPmtGroupError 
				]
 
		 		div message2 [
					logic: [						
						if sPmtGroupConfigResult != "manypaygroups" then "hide"						
					]				
					class: "alert alert-danger"
					attr_role: "alert"				
					display msgMultiplePmtGroupError 
				]
				
				div emptyWalletText [
					class: "alert alert-warning warning"
    	    		logic: [
						if sWalletCount > "0" then "remove"						
					] 	    	    		
					
	    	    	display sEmptyWalletMsg
				]
				
				div paymentNoChangeText [
					class: "alert alert-warning warning"
    	    		logic: [
						if sShowNoChangeMsgFlag == "false" then "remove"						
					] 	    	    		
					
	    	    	display sNoChangeMsg 	    		
				]
				
				div errorMsg [
					class: "alert alert-danger error"
    	    		logic: [
						if sErrorAutomaticMsgFlag == "false" then "remove"						
					] 	    	    		
					
					display sErrorMsg 	    		
				]
        
		    	div paymentAutomatic [  
		    		class: "col-md-12 st-padding-top st-padding-bottom"  	    
		    	    logic: [
						if sPmtGroupConfigResult == "nopaygroup" then "remove"
						if sPmtGroupConfigResult == "manypaygroups" then "remove"					
					]
					
		    	    div automaticPaymentsBlock [		    	    	
		    	    	div message5 [
							class: "alert alert-warning"		    	    		
		    	    		
							logic: [	
								if sIsB2b == "true" then "remove"																	
								if sPmtScheduledFlag != "true" then "remove"			
								if sAutoPayCount > "0" then "remove"	
								if status == "addSuccess" || status == "editSuccess" then "remove"															
							]				
					
							display msgPmtScheduledMsg1         // have payment schedules create message
						]

						
						div message6 [
							class: "alert alert-warning"
	
							logic: [	
								if sIsB2b == "true" then "remove"																
								if sPmtScheduledFlag != "true" then "remove"	
								if sAutoPayCount == "0" then "remove"		
								if status == "addSuccess" || status == "editSuccess" then "remove"				
							]															
							display msgPmtScheduledMsg2             // have payment schedules edit message
						]

 
 						// b2c : Display this message after successfully add or edit an automatic payment. 
 						// The message warns users that they have future scheduled payments configured
						div message7 [
							class: "alert alert-warning"
							logic: [	
								if sIsB2b == "true" then "remove"			
								if sPmtScheduledFlag == "true" && status == "" then "remove"
								if sAutoPayCount == "0" && status == "" then "remove"		
								if sAutoPayCount > "0" && sPmtScheduledFlag == "false" then "remove"															
							]				

							display msgPmtScheduledMsg3
						]


						// b2b : Display this message after successfully add or edit an automatic payment. 
						// The message warns users that they have future scheduled payments configured.
						div message7b [
							class: "alert alert-warning"
							logic: [	
								if sIsB2b == "false" then "remove"
								if status == "" then "remove"	
								if futurePmtFlag == "false" then "remove"
							]				

							display msgPmtScheduledMsg3
						]

			    	    div automaticHeaderRow [
			    	    	class: "row st-padding-bottom"
			    	    	
			    	    	h4 automaticPaymentHeader [
								class: "col-12 col-md-6"
								display sAutomaticPaymentHeader 
							]
							
							div automaticHeaderButton [
								class: "col-12 col-md-6"
								
								logic: [
									if sWalletCount == "0" then "remove"
									if sAutoPayCount > "0" then "remove"
									if sShowCreateFlag == "false" then "remove"
								]
								
				    	    	navigation createAutomaticPaymentLink(termsAndConditions, "{CREATE NEW RECURRING PAYMENT}") [
					                class: "btn btn-primary float-md-end"
					                type: "popin"
					                attr_st-pop-in-size: "lg"
					                attr_tabindex: "1"
					            ]
				            ]		            		            
			    	    ]		
											       			
						div automaticPaymentsTable [
							class: "col-md-12 st-padding-top"
							display tAutomaticPaymentsTable
						] 
										
						div automaticActionIcons [
							class: "col-md-12 st-padding-bottom"
							
							div automaticActionIconsRow [
								class: "row st-payment-help-icons"
								
								div historyIconCol [
									class: "col-md-6 text-center"
									
									div historyIcon [
										class: "st-payment-view-details-auto"
										display sLabelPaymentHistory 
									]
								]
								
								div cancelIconCol [
									class: "col-md-6 text-center text-md-start"
									
									div cancelIcon [
										class: "st-payment-cancel-details"
										display sLabelPaymentCancel 
									]
								]
							]
						]
						div children [
							class: "col-md-12 st-padding-top st-padding-bottom"
							child paymentUpdateAutomaticPayment
						]						
					]
		        ] 
          ]			
			
			div impInfo [
				class: "row"
				display sImportantInfo
			]
       ]
    ]
    
    string sTncTitle = "{ESIGN Consent}"
    tag    tTncHtml  = TermsAndConditions.loadFile("terms_electronic_en_us.html")
    field  fTncConsent [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]
    
    /*---------------------------------------
     *   Automatic payment Cancel popin.
     ----------------------------------------*/         
    xsltFragment termsAndConditions [
        
        form content [
        	class: "modal-content"
        	
        	display sGeolocation [
		       control_attr_sorriso-geo: ""
		       logic: [
		      		if "true" == "true" then "hide"
		       ]
		    ]
        
	        div heading [
	            class: "modal-header"
	            
	            h4 title [
	            	display sTncTitle
	            ]		             
	        ]
	        
            div body [
               class: "modal-body"
               messages: "top"
                    
				div terms [
					display tTncHtml    
				]
									
				div divider [
        			class: "border-top mt-3 pt-3"
					
				
            		display fTncConsent [
            			control_attr_tabindex: "2"
						control_attr_autofocus: ""
            		]
				]				
            ]
                       
            div cancelAutomaticButtons [
                class: "modal-footer"
                
				navigation tncConfirm(createAutomaticPaymentAction, "{Confirm}") [			                
                	class: "btn btn-primary"
                	require: [ fTncConsent ]
                	data: [sGeolocation]
                	attr_tabindex: "3"                			                    
                ]        			
				
                navigation tncCancel(paymentAutomaticScreen, "{Cancel}") [			                
					class: "btn btn-secondary"
					type:  "cancel"
					attr_tabindex: "4"
				] 							
            ]
        ]        
    ]
    
    /*---------------------------------------
     *   Automatic payment Cancel popin.
     ----------------------------------------*/         
    xsltFragment cancelAutomaticPaymentPopin [
        
        form content [
        	class: "modal-content"
        
	        div cancelAutomaticHeading [
	            class: "modal-header"
	            
	            div cancelAutomaticRow [
	            	class: "row text-center"
	            	
	            	h4 cancelAutomaticCol [
	            		class: "col-md-12"
	            		
			            display sDeleteSourceTitle 
					]
				]
	        ]
	        
            div cancelAutomaticBody [
               class: "modal-body"
               messages: "top"
                                   
                div cancelAutomaticRow [                	
                    class: "row text-center"
                   	                
                    display sDeleteSourceText1 [
                    	class: "col-md-12"    
                    ]
                    display sDeleteSourceText2 [
                    	class: "col-md-12 text-danger"    
                    ]																					
				]                
            ]
                       
            div cancelAutomaticButtons [
                class: "modal-footer"
                
    			navigation yesButton (deleteAutomaticPayment, "{YES}") [
                	class: "btn btn-primary"
                	attr_tabindex: "10"		                   		                                    
                	// -- disabled button shows if agent is impersonating --
					logic: [if bImpersonateActive == "true" then "remove"]
            	]

    			navigation yesButtonDisabled (deleteAutomaticPayment, "{DISABLED FOR AGENT}") [
                	class: "btn btn-primary disabled"
                	attr_tabindex: "10"		                   		                                    
                	// -- disabled button shows if agent is impersonating --
					logic: [if bImpersonateActive != "true" then "remove"]
            	]

    			navigation noButton (resetFlags, "{NO}") [
    				attr_tabindex: "11"
				]
            ]
        ]        
    ]
    
    /*-------------------------------------
     *  Automatic payment history popin.
     --------------------------------------*/    
    xsltFragment automaticPaymentHistoryPopin [
        
        div content [
        	class: "modal-content"
        
	        div historyHeading [
	            class: "modal-header"
	            
	            div historyHeadingRow [
	            	class: "row text-center"
	            	
	            	div historyHeadingCol [
	            		class: "col-md-12"
	            		
			            display szHistoryHeader [
			                type : "h3"
			            ]
					]
				]
	        ]
	        	
	        div cancelAutomaticBody [
	           class: "modal-body"
	                
	            div tableRow [                	
	                class: "row"
	               	                
	                display tAutomaticPaymentsHistoryTable [
	                	class: "col-md-12"    
	                ]                   																				
				]                
	        ]  
	        
	        div historyButton [
	            class: "modal-footer"
	            
	            div historyButtonRow [
	            	class: "row text-center"
	            	
	            	div closeButton [
	            		class: "col-md-12"
	            			
//	        			navigation close (checkAutoPaymentMethod, "{CLOSE}") [
	        			navigation close (getPayments, "{CLOSE}") [	
	                    	class: "btn btn-primary"
	                    	attr_tabindex: "10"		                   		                                    
	                	]            		                           		  		                		                
					]
				]
	        ]                                                 
		]
    ]  
    
    /* 8 and 10. Create automatic payment action.*/
    action createAutomaticPaymentAction [	
    	sShowCreateFlag          = "false"    	
    	sDeleteAutomaticMsgFlag  = "false"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "false"

		stopUc(childId: paymentUpdateAutomaticPayment)		
        startUc(childId: paymentUpdateAutomaticPayment) [ 
        	sGroupJson  : sGroupJson
        	scheduleFoundWithAccount : scheduleFoundWithAccount

        ]
		goto(paymentAutomaticScreen)
	]
	
	/* 8.1 Set the new current accounts to GroupJson after removing the existing automatic payment accounts */
	action setCurrentAccountsForUpdate [
		scheduleFoundWithAccount = "true"
		sGroupJson = sCurrentAccounts
		
		goto(createAutomaticPaymentAction)
	]
		
    /* 8.2 Start child usecase only to display messages.*/
    action displayRemoveAllMessages [	
    	sShowCreateFlag          = "false"    	
    	sDeleteAutomaticMsgFlag  = "false"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "false"
		gotoUc(paymentAutomaticMessages)
	]	
	
	/* 10.1. Update automatic payment action.*/
	action updateAutomaticPaymentAction [	    
		sShowCreateFlag          = "false" 	
    	sDeleteAutomaticMsgFlag  = "false"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "false"
		stopUc(childId: paymentUpdateAutomaticPayment)		
        startUc(childId: paymentUpdateAutomaticPayment) [
        	sSelectedAutomaticId  : sSelectedAutomaticId 
        ]
		goto(gotoPaymentAutomaticScreen)
	]
			
	/* 11. Delete automatic payment. */	
	action deleteAutomaticPayment [
		srDeleteAutomaticParam.USER_ID      = sUserId
		srDeleteAutomaticParam.AUTOMATIC_ID = sSelectedAutomaticId
		switch apiCall Payment.DeleteAutomaticPayment(srDeleteAutomaticParam, srDeleteAutomaticResult, ssStatus) [
            case apiSuccess deleteAutomaticPaymentHistory
            default genericErrorMsg
        ]	
    ]
    
    /* 12. Delete automatic payment history. */	
	action deleteAutomaticPaymentHistory [
		srDeleteAutomaticHistoryParam.AUTOMATIC_ID = sSelectedAutomaticId
		srDeleteAutomaticHistoryParam.CONFIG_CHANGE = sDeleteAutomaticHistoryText
		switch apiCall Payment.DeleteAutomaticPaymentHistory(srDeleteAutomaticHistoryParam, srDeleteAutomaticHistoryResult, ssStatus) [
            case apiSuccess checkDeleteResult
            default deleteErrorMsg
        ]	
    ]
    
    /* 13. Check delete automatic payment result. */
	action checkDeleteResult [		
		if srDeleteAutomaticHistoryResult.RESULT == "success" then
    		setDeleteAutomaticMsgFlag
    	else 
    		genericErrorMsg	
	]
	
	/* 14. Sets delete automatic payment success flag to true, audit logs, send email. */
	action 	setDeleteAutomaticMsgFlag [
    	sDeleteAutomaticMsgFlag  = "true"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "false"		
    	status = ""
    	
    	auditLog(audit_payment.automatic_payment_delete_success) [
    		primary  : sUserId
            secondary: sUserId
    		sSourceId
    	 ]
    	
    	switch sSendDeleteEmailFlag [
			case "true" sendDeleteSuccessEmail
			case "false" getPayments			
			default getPayments
		]		
	]    	    
	
	/* 16. Sends wallet delete success email. */
	action sendDeleteSuccessEmail [
		sNtfParams = "accountNumber=" + sDisplayAccountNickname
		NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_automatic_delete_success")		
		goto(getPayments)
	]
	
	/*  Delete audit logs. */
	action 	deleteErrorMsg [    	
    	auditLog(audit_payment.automatic_payment_delete_failure) [
    		primary  : sUserId
            secondary: sUserId
    		sSourceId
    	 ]
    				
		goto(genericErrorMsg)
	]    	
	
	/* Generic error message. */
	action genericErrorMsg [		
    	sDeleteAutomaticMsgFlag  = "false"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "true"			
    	status = ""
        goto(paymentAutomaticScreen)
	]	

	/* Need to call checkAutoPaymentMethods before reset to display the table correctly
	 *  especially case that we want to display multiple accounts */
	action checkAutoPaymentMethod [
        switch UcPaymentAction.checkAutomaticPaymentMethods(sUserId, tAutomaticPaymentsTable) [
    		case "success" resetFlags
    		case "error" resetFlags
    		default resetFlags
		]	
	]

	/* Resets automatic payment flags. */
	action resetFlags [
    	sDeleteAutomaticMsgFlag  = "false"
    	sShowNoChangeMsgFlag     = "false"
    	sErrorAutomaticMsgFlag   = "false"	
    	status = ""
    	goto(gotoPaymentAutomaticScreen)	
	]
]