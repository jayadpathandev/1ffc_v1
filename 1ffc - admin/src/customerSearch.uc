useCase customerSearch [
	
   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 23-Nov-2015
    *
    *  Primary Goal:
    *       1. Search users by username and password.
    *       
    *  Alternative Outcomes:
    *       1. CSR impersonates Correspondence page for customer.
    *       2. CSR impersonates Notifications page for customer.
    *       3. CSR impersonates Profile page for customer.
    *       4. CSR resends the enrollment confirmation email.
    *       5. CSR resets the user password and secret questions.
    *                     
    *   Major Versions:
    *        1.0 21-Nov-2015 First Version Coded [Maybelle Johnsy Kanjirapallil]
    * 		 1.1 2024-Jan-16 jak customization for 1st Franklin
    *        1.2 2024-Jan-24 YN Removed temporary password
    */
        

    documentation [
        preConditions: [[
            1. CSR successfully logged in.
        ]]
        triggers: [[
            1. CSR selects the Customer search menu. Enters username or user email or both and click Search button.
        ]]
        postConditions: [[
            1. Primary -- System displays the user details table.
            2. Alternative 1 -- CSR selects Correspondence link, System take the CSR to the user's Correspondence page.
            3. Alternative 2 -- CSR selects Notifications link, System take the CSR to the user's notification page.
            4. Alternative 3 -- CSR selects Profile link, System take the CSR to the user's profile page.
            5. Alternative 4 -- CSR selects resend confirmation and enrollment email, System sends enrollment confirmation email to the user.
            6. Alternative 5 -- CSR selects reset password and secret questions link, System resets the user's passowrd and secret question and sends the email.
        ]]
    ]
	startAt actionInit
	
	// Only business admin and business users are able to see assist customers tab. Sys Admin wont be able to see this 
	actors[
		edit_b2c_customer
	]
	
    /**************************
     * Strings used by the JAVA
     **************************/            
    static billingImpersonateLink = "{Billing & Usage}"
    static correspondenceImpersonateLink = "{Correspondence}"
    static notificationsImpersonateLink = "{Notifications}"
    static paymentImpersonateLink = "{Payment}"
    static profileImpersonateLink = "{Profile}"
    
	/**************************
     * DATA ITEMS SECTION
     **************************/    
    import validation.emailRegex
     
    importJava AppName(com.sorrisotech.utils.AppName) 
    importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil) 
	importJava CookieUtil(com.sorrisotech.app.^library.^login.cookie.CookieUtil)
	importJava ForeignProcessor(com.sorrisotech.app.common.ForeignProcessor)
	importJava I18n(com.sorrisotech.app.common.utils.I18n)
	importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
	// -- extends the standard consumer search to search for information needed by 1FFC --
	importJava UcConsumerSearchAction(com.sorrisotech.uc.admin1ffc.Uc1FFCSearchByCustomerId)
	importJava UcProfile2FAAction(com.sorrisotech.app.profile.UcProfile2FAAction)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava Initialize(com.sorrisotech.uc.admin1ffc.Initialize)
	
	serviceStatus status
	serviceParam(Notifications.SetUserAddress) setData
	serviceParam(Notifications.GetContactSettings) getContactData
	serviceResult(Notifications.GetContactSettings) getContactResponse
	serviceParam(FffcNotify.SetUserAddressNls) setDataFffc
	serviceParam(FffcNotify.SetContactSettingsNls) setTopicFffc
	serviceParam (Profile.AddLocationTrackedEvent) setLocationData
	serviceResult (Profile.AddLocationTrackedEvent) setLocationResp
	
	string sPageName = "{Customer Search}"
	string sResultsHeading = "{Search results}"
	string sPopinTitle1 = "{Resend confirmation & enrollment e-mail}"
	string sPopinTitle2 = "{Reset secret questions & password}"
	string sPopinTitle3 = "{Reset Multi-Factor Authentication for Login option}"
	
	// *-- Important : Do not  delete the 4 messages below. The use case uses it !!
	string msgNewEmailSuccess = "{E-mail address changed from <1> to <2>. We sent you the validation code to the new e-mail. Use it to login.}"
	string msgSameEmailSuccess = "{We sent you the validation code to the new e-mail <1>. Use it to login.}"
	string msgResetNewEmailSuccess = "{E-mail address changed from <1> to <2>. We sent you the new validation code to the new e-mail. Use it to login.}"
	string msgResetSameEmailSuccess = "{We sent the new validation code to the e-mail <1>. Use it to login.}"
		
	native string sAppNameSpace = AuthUtil.getAppNameSpace()	
	native string sAppName = AppName.getAppName("b2c")
	native string sFlag = "false"
	native string sUserName
	native string sImpersonateUrl
	native string sRowId         
	native string sSelectedUserId
	native string sEmailAddress  	
	native string sOldEmailAddress
	native string sAuthCode
	native string sCurrentTime
	native string sFirstName
    native string sLastName
    native string sMaskedEmailId
    native string sSelectedUserName
    native string sMaskedOldEmailId = NotifUtil.getMaskedEmail(sOldEmailAddress)
    native string sMaskedNewEmailId = NotifUtil.getMaskedEmail(sEmailAddress)    
    native string sNotificationFirstName
    native string sNotificationLastName
    
    // -- used to hide non-1stFranklinSearchFields and to show the 1st Franklin style search
    native string bHideProductSearch = "true"
    native string bShow1stFranklinSearch = "true"
    
    native string sIpAddress         = Session.getExternalIpAddress()
    native string sEmailChannel      = "email"
    native string sCategory          = "address"
    native string sOperation         = "The agent successfully updated users email address."
    native string sSelectedCustomerId
    native volatile string sCurrentEpochTime = Initialize.getCurrentEpochTime()
    
    input sGeolocation
    			
	field fUserName [   
        string(label) sLabel = "{Username}"      
        input (control) pInput ("(^[\\p{L}\\d_\\.\\-]{3,}\\*?$)|(^[\\p{L}\\d_\\.\\-]+$)", fUserName.sValidation)
        string(validation) sValidation = "{Username may contain letters, numbers, hyphens (-), underscores (_), and a wildcard *.  At least three characters are needed before the wildcard, such as 'abc*'.}"
        string(help) sHelp = "{Please enter a username. Search criteria may consist of one or more characters and followed by an asterisk (*) and click Search button.}"   
    ]
    
    field fEmailAddress [     
        string(label) sLabel = "{E-mail address}"
        input (control) pInput ("(^[\\p{L}\\d_\\.\\-\\@\\+]{3,}\\*?$)|(^[\\p{L}\\d_\\.\\-\\@\\+]+$)", fEmailAddress.sValidation)
        string(validation) sValidation = "{Username may contain letters, numbers, hyphens (-), underscores (_), at sign (@), pluses (+), and a wildcard *.  At least three characters are needed before the wildcard, such as 'abc*'.}"
        string(help) sHelp = "{Please enter an e-mail. Search criteria may consist of one or more characters and followed by an asterisk (*) and click Search button.}"       
    ]
    
    field fCustomerId [
        string(label) sLabel = "{Customer Identification Number}"
        input (control) pInput ("(^\\d{3,6}\\*|^\\d{7}$)$", fCustomerId.sValidation)
        string(validation) sValidation = "{Customer identification number search criteria must be at least 3 digits plus a wildcard (*) or maximum of 7 digits. For example: 207*, 2071*, or 2071978.}"
        string(help) sHelp = "{Please enter a customers' Customer Identification Number. Search criteria must include at least 3 digits and a wild card (*) up to a full 7 digit customer id.}"       
    ]
   
    field fUserEmail [	
        string(label) sLabel = "{* E-mail address:}"
        input(control) pInput(emailRegex, fUserEmail.sValidation)
        string(validation) sValidation = "{Please provide a valid e-mail address. Your e-mail address may contain up to 50 characters and must appear in the standard e-mail address format: name@example.com.}"
        string(required) sRequired = "{This field is required.}" 
        string(error) sError = "{This e-mail address is already used by another user, please use different one.}" 
    ]
 
    field fUserEmailRetype [
        string(label) sLabel = "{* Retype e-mail address:}"
        input(control) pInput = ""              
        string(required) sRequired = "{This field is required.}"  
        string(error) sError = "{E-mail and retype e-mail address fields should match}"      
    ]
    
    // -- message strings for display when use case completes.      
    structure(message) oMsgInvalidInput [
        string(title) sTitle = "{Invalid input}"
        string(body) sBody = "{Please provide a username or e-mail and click Search button.}"
    ]
    
    structure(message) oMsgTooManyResults [
        string(title) sTitle = "{Too many results}"
        string(body) sBody = "{You have entered a query that returned too many results, please refine your query and search again.}"
    ]    
    
	structure(message) oMsgResendSameEmailNotifSuccess [
        string(title) sTitle = "{Confirmation and enrollment e-mail was sent successfully.}"
        volatile string (body) sBody = I18n.translate ("customerSearch_msgSameEmailSuccess", sMaskedOldEmailId)
    ]
    
	structure(message) oMsgResendNewEmailNotifSuccess [        
        string(title) sTitle = "{Confirmation and enrollment e-mail was sent successfully.}"
        volatile string (body) sBody = I18n.translate ("customerSearch_msgNewEmailSuccess", sMaskedOldEmailId, sMaskedNewEmailId)
    ]
    
    structure(message) oMsgResendConfirmationNotifError [
        string(title) sTitle = "{E-mail failed.}"
        string(body) sBody = "{There was a problem in re-sending the confirmation e-mail. Please try again later.}"
    ]
    
    structure(message) oMsgResetSameEmailNotifSuccess [
        string(title) sTitle = "{The password and secret answer reset success.}"
        volatile string (body) sBody = I18n.translate ("customerSearch_msgResetSameEmailSuccess", sMaskedOldEmailId)
    ]
    
    structure(message) oMsgResetNewEmailNotifSuccess [
        string(title) sTitle = "{The password and secret answer reset success.}"
        volatile string (body) sBody = I18n.translate ("customerSearch_msgResetNewEmailSuccess", sMaskedOldEmailId, sMaskedNewEmailId)
    ]
    
    structure(message) oMsgResetPasswordNotifError [
        string(title) sTitle = "{Cannot send new password}"
        string(body) sBody = "{Cannot send the notification with the new password. Please try again later.}"
    ]
    
    structure(message) oMsgEmailRegexError [
        string(title) sTitle = "{Invalid email}"
        string(body) sBody = "{The email you entered is not valid.}"
    ]
    
    structure(message) oMsgGenericError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
    ]
    
    structure(message) oMsgReset2FANotifError [
        string(title) sTitle = "{Error while sending Notification email to the user.}"
        string(body) sBody = "{Cannot send Multi-Factor Authentication for Login settings update notification to the user. Please try again later.}"
    ]
    
    structure(message) oMsg2FADeRegError [
        string(title) sTitle = "{Invalid email}"
        string(body) sBody = "{The email you entered is not valid.}"
    ]
    
    structure(message) oMsgReset2FANotifSuccess [
        string(title) sTitle = "{Multi-Factor Authentication for Login reset operation is successful.}"
        volatile string (body) sBody = "{Multi-Factor Authentication for Login security option is disabled for the selected user.}"
    ]
    
    string sReset2FAMessageQue = "{Are you sure that you want to disable Multi-Factor Authentication for Login for this user?}"
    string sReset2FAMessageDesc = "{Please press 'Submit' button to disable Multi-Factor Authentication for Login Security option, press 'Cancel' to go back to search screen.}"
    
    structure(message) oMsgResetTopicPreferencesAtNlsSuccess [
        string(title) sTitle = "{The topic preferences reset at NLS success.}"
        volatile string (body) sBody = "{The topic preferences have been reset successfully in the NLS.}"
    ]
    
    string resetPasswordFlag = "false"
        
    table tUserList = UcConsumerSearchAction.getTable() [
        emptyMsg: "{There are no registered users with the Customer Identifier you entered.}"
         
        "id" => string sId
        "userid" => string sUserId
        "username" => string sUsername
        "name" => string sName
        "email" => string sEmail
        "emailMasked" => string sEmailMasked
        "billingUrl" => string sBillingUrl  
        "correspondenceUrl" => string sCorrespondenceUrl
        "notificationsUrl" => string sNotificationsUrl
        "profileUrl" => string sProfileUrl
        "paymentUrl" => string sPaymentUrl
        "billClass" => string sBillClass
        "docClass" => string sDocClass
        "paymentClass" => string sPaymentClass
        "impersonate_url" => string sImpersonateUrl
        // - specific to 1st Franklin, their customer id -
        "customerId" => string sCustomerId
        
        link "{Billing & Usage}"  billingImpersonateLink (impersonateUserBilling) [
            sRowId: sId            
        ]
        
        link "{Correspondence}"  correspondenceImpersonateLink (impersonateUserCorrespondence) [
            sRowId: sId            
        ]
        
        link "{Payment}"  paymentImpersonateLink (impersonateUserPayment) [
            sRowId: sId            
        ]
        
        link "{Notifications}" notificationsImpersonateLink(impersonateUserNotifications)[
            sRowId: sId      
        ]
        
        link "{Profile}" profileImpersonateLink(impersonateUserProfile) [
            sRowId: sId      
        ]
        
        link  "{Impersonate}" impersonateLink(impersonateAction) [
             sRowId: sId
        ]
        
        link "{Resend confirmation & enrollment e-mail}" resendEmail(assignFieldsResendEmailPopin) [            
            sSelectedUserId: sUserId       
            sSelectedUserName: sUsername   
            sEmailAddress: sEmail
            sOldEmailAddress: sEmail   
            sMaskedEmailId: sEmailMasked
            sSelectedCustomerId: sCustomerId       
        ]
        
        link "{Reset secret questions & password}" resetUserDetails(assignFieldsResetPwdPopin) [
            sSelectedUserId: sUserId                          
            sEmailAddress: sEmail
            sOldEmailAddress: sEmail      
            sMaskedEmailId: sEmailMasked
            sSelectedCustomerId: sCustomerId
        ]

        link "{View Audit Logs}" viewAuditLogs(viewAuditLogs) [
            sSelectedUserId: sUserId                
        ]
        
        link "{Reset Multi-Factor Authentication for Login Security Option}" reset2FAOption(reset2FAOptionPopin) [
            sSelectedUserId: sUserId                
        ]
        
        link "{Reset Topic Preferences}" resetTopicPreferences(resetTopicPreference) [
            sSelectedUserId: sUserId                          
            sSelectedCustomerId: sCustomerId               
        ]

        column customerIdColumn("{Customer Id}") [
            elements: [sCustomerId ]
            sort    : [sCustomerId ]
        ]
                
        column nameColumn("{Customer Name}") [
            elements: [sName ]
            sort    : [sName ]
        ]
        
        column usernameColumn("{Username}") [
            elements: [sUsername ]
            sort    : [sUsername ]            
        ]
            
        column behalfColumn("{On behalf of}") [
            tags: [ "st-vertical-links" ]
            elements: [
                /*billingImpersonateLink: [
                    attr_target: "_blank"
                    attr_class: sBillClass
                ]  
                correspondenceImpersonateLink: [
                    attr_target: "_blank"
                    attr_class: sDocClass
                ]
                paymentImpersonateLink: [
                    attr_target: "_blank"
                    attr_class: sPaymentClass
                ]  
                notificationsImpersonateLink: [
                    attr_target: "_blank"
                ]
                
                profileImpersonateLink: [
                    attr_target: "_blank"
                ]*/
                impersonateLink : [
    				attr_target : "_blank"
    			]               
            ]
        ]    
   
        column actionsColumn("{Actions}") [
           tags: [ "st-vertical-links" ]
           elements: [
                viewAuditLogs
               
           		resendEmail: [
           			^type: "popin"
           		], 
           		resetUserDetails: [
           			^type: "popin"
           		],
           		reset2FAOption: [
           			^type: "popin"
           		]
           		
           		resetTopicPreferences
           ]
       ]  
    ]
    
    /*************************
     * MAIN SUCCESS SCENARIO
     *************************/
	
    /* 1. System loads the name of the current user. */ 
	action actionInit [
	    sUserName = getUserName()
	    Initialize.init()
	    goto(customerSearchScreen)
	]
	
	/* 2. User provides search criteria to find a user. */ 
	xsltScreen customerSearchScreen("{Customer Search}") [
		
       form main [
            class: "st-customer-search"

            div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
                
	                display sPageName
				]
            ]
                                      
            div content [
                class: "st-search-body st-field-row"
                
                div row1 [    
                	class: "row"
                  
					div col1 [  
						class: "col-md-3"
					    logic: [ if bHideProductSearch == "true" then "remove"]
		                display fUserName [
		                	control_attr_tabindex: "1"
						    control_attr_autofocus: ""
						    logic: [ if bHideProductSearch == "true" then "remove"]
		                ]
					]
                    
					div col2 [  
						class: "col-md-3"
					    logic: [ if bHideProductSearch == "true" then "remove"]
                		display fEmailAddress [
                			control_attr_tabindex: "2"
                			logic: [ if bHideProductSearch == "true" then "remove"]
                		]
                	]
                	
 					div col3 [  
						class: "col-md-3"
					    logic: [ if bHideProductSearch == "true" then "remove"]
		                navigation search(verifyInputData, "{SEARCH}") [
		                    class: "btn btn-primary st-search-button"
		                    data: [fUserName, fEmailAddress]
		                    attr_tabindex: "3"
		                    logic:[ if bHideProductSearch == "true" then "remove"]
		                ]
					]
 
  					/** Begin 1st Franklin search columns */	
  	            	div col1_1ffc [
                		class: "col-md-3"
               			logic: [if bShow1stFranklinSearch != "true" then "remove"]
                		display fCustomerId [
                			control_attr_tabindex: "1"
                			logic: [if bShow1stFranklinSearch != "true" then "remove"]
                		]
                		
                	]

 					div col2_1ffc [  
						class: "col-md-3"
               			logic: [if bShow1stFranklinSearch != "true" then "remove"]
		                navigation search_1ffc (verifyInputData, "{SEARCH}") [
		                    class: "btn btn-primary st-search-button"
		                    data: [fCustomerId]
		                    attr_tabindex: "2"
                			logic: [if bShow1stFranklinSearch != "true" then "remove"]
		                ]
					]
					/** end 1st Franklin search columns */
  				]
            ]                    
                        
            div results [
            	class: "st-search-results"
            	
                div row4 [    
                	class: "row"
                    
					h2 col4 [  
						class: "col-md-12"
						
		                display sResultsHeading [
		                    logic: [
		                         if sFlag  == "false" then "hide"
		                    ]
		                ]
					]
				]
            	
                div row5 [    
                	class: "row"
                    
					div col5 [  
						class: "col-md-12"
						
		                display tUserList [
		                    logic: [
		                        if sFlag  == "false" then "remove"
		                    ]
		                ]
					]
				]
            ]
	    ]
	]
    	
    /* 3. System verifies the input data for user search. */ 
/*	action verifyInputData [
	   switch UcConsumerSearchAction.verifyInputData(fUserName.pInput, fEmailAddress.pInput) [
            case "success" performSearch
            case "error" invalidInputMsg 
            default genericErrorMsg           
        ]	          
	]	
*/
	/* 3. System there's something in the input data request for 1st Franklin  */
	action verifyInputData [
	   switch UcConsumerSearchAction.verifyInputData(fCustomerId.pInput) [
            case "success" performSearch
            case "error" invalidInputMsg 
            default genericErrorMsg           
       ]	          
	]	
	
	
    /* 4. System performs the user search. */ 
/*	action performSearch [    
	    sFlag = "true"
	    UcConsumerSearchAction.setAdminDetails(sUserName, sAppNameSpace)
	    switch UcConsumerSearchAction.performSearch(fUserName.pInput, fEmailAddress.pInput) [       
            case "success" customerSearchScreen
            case "noResults" customerSearchScreen
            case "tooManyResults" tooManyResultsMsg                               
            default genericErrorMsg
        ]        
    ]
 */
 
 	/* 4. System performs Customer Id based search for 1st Franklin */
 	action performSearch [    
	    sFlag = "true"
	    UcConsumerSearchAction.setAdminDetails(sUserName, sAppNameSpace)
	    switch UcConsumerSearchAction.performSearch(fCustomerId.pInput) [       
            case "success" customerSearchScreen
            case "noResults" customerSearchScreen
            case "tooManyResults" tooManyResultsMsg                               
            default genericErrorMsg
        ]        
    ] 

    /* 5. Gets the billing page impersonate url. */
    action impersonateUserBilling [
        switch UcConsumerSearchAction.getBillingUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
    ] 
    
    /* 6. Gets the correspondence page impersonate url. */
    action impersonateUserCorrespondence [
        switch UcConsumerSearchAction.getCorrespondenceUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
    ]  
    
    /* 7. Gets the payment page impersonate url. */
    action impersonateUserPayment [
        switch UcConsumerSearchAction.getPaymentUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
    ]   
    
    /* 8. Gets the notification page impersonate url. */
    action impersonateUserNotifications [
        switch UcConsumerSearchAction.getNotificationsUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
    ]   
    
    /* 9. Gets the profile page impersonate url. */
    action impersonateUserProfile [
        switch UcConsumerSearchAction.getProfileUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
    ]
    
    /* 10. Gets the impersonate url. */
	action impersonateAction [
		switch UcConsumerSearchAction.getImpersonateUrl(sRowId, sImpersonateUrl) [
            case "success" impersonateUser
            case "error" genericErrorMsg
            default genericErrorMsg
        ]        
	]
   
    /* 11. System redirects to the user application page. */
    action impersonateUser [    
        foreignHandler ForeignProcessor.writeResponse(sImpersonateUrl)
    ]
    
    /* 12. Assign values to the popin fields. */
	action assignFieldsResendEmailPopin [
    	 fUserEmail.pInput = sMaskedEmailId
		 fUserEmailRetype.pInput = sMaskedEmailId
		 goto(resendEmailPopin)
    ]
     
    /* 13. Resend confirmation & enrollment e-mail popin. */    
    xsltFragment resendEmailPopin [
        
        form content [
        	class: "modal-content"
        	
	        div resendEmailheading [
	            class: "modal-header"
	            
	            div resendEmailheadingRow [
	            	class: "row"
	            	
	            	h4 resendEmailheadingCol [
	            		class: "col-md-12"
	            		
			            display sPopinTitle1 
					]
				]
	        ]
	         
            div resendEmailemailbody [
               class: "modal-body"
               messages: "top"
                                   
                div resendEmailfields [
                	div resendEmailfieldsRow1 [
                        class: "row"
                        
                        div resendEmailfieldsCol1 [
                        	class: "col-md-12"
                    
		                    display fUserEmail [
		                    	control_attr_tabindex: "10"
		                    	control_attr_autofocus: ""
		                    ]
						]
						
						div resendEmailfieldsCol2 [
                        	class: "col-md-12"
                    
		                    display fUserEmailRetype [			                    	
			                    pInput_attr_st-compare-to: "form.fUserEmail"
			                    sError_ng-show: "!content.$error.stPattern && !!content.$error.stCompareTo"
			                    control_attr_tabindex: "11"
			                    sError_attr_sorriso-error: 'same-as'
								pInput_attr_st-same-as: 'fUserEmail'	
			                ]		                    	
						]											
					]
                ]
            ]
                       
            div resendEmailbuttons [
                class: "modal-footer"
                
                 display sGeolocation [ 
                   control_attr_sorriso-geo: ""
                      logic: [ 
                	      if "true" == "true" then "hide" 
                      ]  
                ] 
                
                navigation resendEmailsubmit (verifyResendEmailPopinFields, "{Submit}") [
                    class: "btn btn-primary"
                    data: [fUserEmail, fUserEmailRetype, sGeolocation]
                    require: [fUserEmail, fUserEmailRetype] 
                    type: "popin"
                    attr_tabindex: "12"		                    
                ]
		                
                navigation resendEmailcancel (customerSearchScreen, "{Cancel}") [
                    type: "cancel"
                    class: "btn btn-secondary"
                    attr_tabindex: "13"
                ]
            ]
        ]
    ]
    
    /* 14. Verify field values. */
	action verifyResendEmailPopinFields [
		resetPasswordFlag = "false"
		
    	switch NotifUtil.verifyEmailFields(sEmailAddress, fUserEmail.pInput, sMaskedEmailId) [
    		case "equal" updateUserDetails
    		case "success" updateUserDetails
            case "error"  emailRegexErrorMsg
            default resendConfirmationNotifErrorMsg
    	]
    ]
    
    /* 15. System updates the email address. */ 
    action updateUserDetails [       	
		 loadProfile(        	
	    	userId: sSelectedUserId    
	    	firstName: sFirstName
        	lastName: sLastName
	        )  
		setData.userid = sSelectedUserId
    	setData.channel = sEmailChannel
    	setData.address = sEmailAddress
        switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess addLocationTrackedEvent
		    default genericErrorMsg
		]
    ]
    
     /* 16. Adding geolocation track event */
    action addLocationTrackedEvent [
    	setLocationData.sUser = sSelectedUserId
    	setLocationData.sCategory = sCategory
    	setLocationData.sType = sEmailChannel
    	setLocationData.sData = sEmailAddress
    	setLocationData.sIpAddress = sIpAddress
    	setLocationData.sBrowserGeo = sGeolocation
    	setLocationData.sOperation = sOperation
    	
    	switch apiCall Profile.AddLocationTrackedEvent(setLocationData, setLocationResp, status) [
    		case apiSuccess saveEmailAddressAtNls
    		default genericErrorMsg
    	]
    ]
    
    /* 17. Saves the new email address via NLS API service */
    action saveEmailAddressAtNls [
    	setDataFffc.customerId = sSelectedCustomerId
    	setDataFffc.channel = sEmailChannel
    	setDataFffc.address = sEmailAddress
    	setDataFffc.browserGeo = sGeolocation
    	setDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDataFffc.ipAddress = sIpAddress
    	// - FFFC-771 Disabling econsent at NLS here.
    	setDataFffc.sConsentActive = "false"
    	switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, status) [
    		case apiSuccess saveEconsentDetails
    		default genericErrorMsg
    	]
    ]
    
    /* 17a. Save e-consent details.(attribbute at auth_user_profile table) */
    action saveEconsentDetails [
    	 updateProfile(        	
        	userId: sSelectedUserId    
        	eSignConsentEnabled: "false"
            eSignConsentLastUpdatedTimeStamp: sCurrentEpochTime 	
            )
        if success then generateAuthCode        
        if failure then genericErrorMsg  
    ]
    
    /* 18. Create Authorization code. */
    action generateAuthCode [    	  	   	
        switch AuthUtil.generateAuthCode(sAuthCode) [        
            case "success" getCurrentTimeStamp
            case "error"  genericErrorMsg
            default genericErrorMsg
        ]   
    ] 
    
    /* 19. Get current time. */
    action getCurrentTimeStamp [    
        switch AuthUtil.getCurrentTime(sCurrentTime) [        
            case "success" saveAuthCodeDetails
            case "error"  genericErrorMsg
            default genericErrorMsg
        ]   
    ]  
    
    /* 20. Save auth code details. */
    action saveAuthCodeDetails [
    	 updateProfile(        	
        	userId: sSelectedUserId    
        	authCode: sAuthCode     
        	authCodeCreationTimestamp: sCurrentTime  	
            )
        if success then resetAttributes        
        if failure then genericErrorMsg  
    ]    
    
    /* 21. Updates the user's profile attributes. */    
    action resetAttributes [
        updateProfile(
            userId: sSelectedUserId    
            accountStatus: "open"            
            lockoutTime: "0"
            passwordRetryCount: "0"
            passwordFailTime: "-1"            
            authCodeRetryCount: "0"
            authCodeFailTime: "-1"                                  
        )
        goto(sendNotificationEmail)     
    ]  

	/* 22. Send email notification */    
	action sendNotificationEmail [
		if resetPasswordFlag == "false" then
			sendRegistrationEmail
		else
			clearRecognizedPCs
	]    
        
    /* 23. Send email. */
    action sendRegistrationEmail [    
//    	switch NotifUtil.sendOrgAdminRegistration(sSelectedUserId, sSelectedUserName, sAuthCode, sAuthCode, sFirstName, sLastName) [    
		switch NotifUtil.sendAuthCode(sSelectedUserId, sAppNameSpace, "b2c", sUserName, sAuthCode, sFirstName, sLastName) [     
           case "success" resendSuccess
            case "error"  resendConfirmationNotifErrorMsg
            default resendConfirmationNotifErrorMsg
        ]   
    ]   

    /* 24. Checks if email address has changed. */   
    action resendSuccess [
    	if sOldEmailAddress == sEmailAddress then
    		resendSameEmailSuccessMsg
    	else 
    		resendNewEmailSuccessMsg	
    ]
    
    /* 25. Resend to the same email success message. */
    action resendSameEmailSuccessMsg [
        displayMessage(type: "success" msg: oMsgResendSameEmailNotifSuccess)
        goto(customerSearchScreen)
    ] 
    
    /* 26. Resend to the new email success message. */
    action resendNewEmailSuccessMsg [
        displayMessage(type: "success" msg: oMsgResendNewEmailNotifSuccess)
        goto(verifyInputData)
    ] 
   
    /* 27. Assign values to the popin fields. */    
	action assignFieldsResetPwdPopin [
    	 fUserEmail.pInput = sMaskedEmailId
		 fUserEmailRetype.pInput = sMaskedEmailId
		 resetPasswordFlag = "true"
		 
		 goto(resetPwdPopin)
    ]
    
    /* 28. Reset password & secret questions popin. */
    xsltFragment resetPwdPopin [
        
        form content [
        	class: "modal-content"

	        div resetPwdheading [
	            class: "modal-header"
	            
	            div resetPwdheadingRow [
	            	class: "row"
	            	
	            	h4 resetPwdheadingCol [
	            		class: "col-md-12"
	            		
			            display sPopinTitle2 
					]
				]
	        ]
            div resetPwdemailbody [
               class: "modal-body"
               messages: "top"
                                   
                div resetPwdfields [
                	div resetPwdfieldsRow [
                        class: "row"
                        
                        div resetPwdfieldsCol1 [
                        	class: "col-md-12"
                    
		                    display fUserEmail [
		                    	control_attr_tabindex: "10"
		                    	control_attr_autofocus: ""
		                    ]
						]
						
						div resetPwdfieldsCol2 [
                        	class: "col-md-12"
                    
		                    display fUserEmailRetype [		                    			                    	
			                    pInput_attr_st-compare-to: "form.fUserEmail"
			                    sError_ng-show: "!content.$error.stPattern && !!content.$error.stCompareTo"
			                    control_attr_tabindex: "11"		
			                    sError_attr_sorriso-error: 'same-as'
								pInput_attr_st-same-as: 'fUserEmail'	                
		                    ]
						]											
					]
                ]
            ]
                       
            div resetPwdbuttons [
                class: "modal-footer"
                
                display sGeolocation [ 
                   control_attr_sorriso-geo: ""
                      logic: [ 
                	      if "true" == "true" then "hide" 
                      ]  
                ] 
                
                navigation resetPwdSubmit (verifyResetPwdPopinFields, "{Submit}") [
                    class: "btn btn-primary"
                    data: [fUserEmail, fUserEmailRetype, sGeolocation]
                    require: [fUserEmail, fUserEmailRetype] 
                    type: "popin"
                    attr_tabindex: "12"		                    
                ]
                 navigation resetPwdCancel (customerSearchScreen, "{Cancel}") [
                    type: "cancel"
                    class: "btn btn-secondary"
                    attr_tabindex: "13"
                ]
            ]
        ]
	]
    
    /* 29. Verify field values. */
	action verifyResetPwdPopinFields [
    	switch NotifUtil.verifyEmailFields(sEmailAddress, fUserEmail.pInput, sMaskedEmailId) [
    		case "equal" getUserDetails
    		case "success" getUserDetails
            case "error"  emailRegexErrorMsg
            default resetPasswordNotifErrorMsg
    	]
    ]
    
    /* 30. System updated the email address. */ 
    action getUserDetails [       	 
		setData.userid = sSelectedUserId
    	setData.channel = sEmailChannel
    	setData.address = sEmailAddress
        switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess addLocationTrackedEventResetPwd
		    default genericErrorMsg
		]                	
    ]
    
     /* 31. Adding geolocation track event */
    action addLocationTrackedEventResetPwd [
    	setLocationData.sUser = sSelectedUserId
    	setLocationData.sCategory = sCategory
    	setLocationData.sType = sEmailChannel
    	setLocationData.sData = sEmailAddress
    	setLocationData.sIpAddress = sIpAddress
    	setLocationData.sBrowserGeo = sGeolocation
    	setLocationData.sOperation = sOperation
    	
    	switch apiCall Profile.AddLocationTrackedEvent(setLocationData, setLocationResp, status) [
    		case apiSuccess checkEmaiUpdated
    		default genericErrorMsg
    	]
    ]
    
    /* 31a. Checks if email address has changed or not. */   
    action checkEmaiUpdated [
    	if sOldEmailAddress == sEmailAddress then
    		saveSameEmailAddressAtNlsResetPwd
    	else 
    		saveNewEmailAddressAtNlsResetPwd	
    ]
    
    /* 32. Saves the same email address via NLS API service */
    action saveSameEmailAddressAtNlsResetPwd [
    	setDataFffc.customerId = sSelectedCustomerId
    	setDataFffc.channel = sEmailChannel
    	setDataFffc.address = sEmailAddress
    	setDataFffc.browserGeo = sGeolocation
    	setDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDataFffc.ipAddress = sIpAddress
    	switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, status) [
    		case apiSuccess resetCsrPasswordFlag
    		default genericErrorMsg
    	]
    ]
    
    /* 32a. Saves the new email address and disable e-consent via NLS API service*/
    action saveNewEmailAddressAtNlsResetPwd [
    	setDataFffc.customerId = sSelectedCustomerId
    	setDataFffc.channel = sEmailChannel
    	setDataFffc.address = sEmailAddress
    	setDataFffc.browserGeo = sGeolocation
    	setDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDataFffc.ipAddress = sIpAddress
    	// - FFFC-771 Disabling e-consent at NLS here.
    	setDataFffc.sConsentActive = "false"
    	switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, status) [
    		case apiSuccess resetCsrPasswordFlagAndDisableEconsent
    		default genericErrorMsg
    	]
    ]
    
    /* 33. Updates the user's profile attributes. */    
    action resetCsrPasswordFlag [
        updateProfile(
            userId: sSelectedUserId    
            accountStatus: "open"            
            lockoutTime: "0"
            passwordRetryCount: "0"
            passwordFailTime: "-1"            
            authCodeRetryCount: "0"
            authCodeFailTime: "-1"       
            csrResetPasswordFlag: "true"                   
        )     
        goto(generateAuthCode)        
    ]
    
    /* 33a. Updates the user's profile attributes and disable e-consent. */  
    action resetCsrPasswordFlagAndDisableEconsent [
    	updateProfile(
            userId: sSelectedUserId    
            accountStatus: "open"            
            lockoutTime: "0"
            passwordRetryCount: "0"
            passwordFailTime: "-1"            
            authCodeRetryCount: "0"
            authCodeFailTime: "-1"       
            csrResetPasswordFlag: "true"
            eSignConsentEnabled: "false"
            eSignConsentLastUpdatedTimeStamp: sCurrentEpochTime                    
        )
        goto(generateAuthCode) 
    ]  
        
    /* 34. Resets the cookies. */
    action clearRecognizedPCs [
        switch CookieUtil.clearRecognizedPCs(sSelectedUserId) [      
            case "success" sendResetNotification           
            case "error" sendResetNotification                                    
            default sendResetNotification
        ]
    ]
    
    /* 35. Sends an email to the user with the new password and secret question answer. */
    action sendResetNotification [
        switch NotifUtil.sendResetPasswordAuthCode(sSelectedUserId, sAuthCode, sAppName) [
            case "success" resetNotifSuccess
            case "failure" resetPasswordNotifErrorMsg
            case "error" genericErrorMsg
            default genericErrorMsg
        ]
    ]
    
    /* 36. Checks if the email has changed. */
    action resetNotifSuccess [
    	if sOldEmailAddress == sEmailAddress then
    		resetSameEmailSuccessMsg
    	else 
    		resetNewEmailSuccessMsg	
    ]
    
    /* 37. Reset success message. */
    action resetSameEmailSuccessMsg [
        displayMessage(type: "success" msg: oMsgResetSameEmailNotifSuccess)
        goto(customerSearchScreen)
    ] 
    
    /* 38. Reset success message. */ 
    action resetNewEmailSuccessMsg [
        displayMessage(type: "success" msg: oMsgResetNewEmailNotifSuccess)
        goto(verifyInputData)
    ] 
    
    /* 39. System displays audit logs for the user. */
    action viewAuditLogs [
        gotoUc(auditView) [
            sArgUserId: sSelectedUserId
        ]
    ]
    
    /* 40. Reset 2FA Popin screen. */    
	action reset2FAOptionPopin [
    	 	
		 goto(reset2FAPopin)
    ]
    
    /* 41. Reset 2FA Security Option popin. */
    xsltFragment reset2FAPopin [
        
        form content [
        	class: "modal-content"
        
	        div reset2FAheading [
	            class: "modal-header"
	            
	            div reset2FAheadingRow [
	            	class: "row"
	            	
	            	h4 reset2FAheadingCol [
	            		class: "col-md-12"
	            		
			            display sPopinTitle3 
					]
				]
	        ]
	         
			div reset2FAbody [
				class: "modal-body"
				messages: "top"
				
				div reset2FAMessageQueRow [
				    class: "row"
				    
				    div reset2FA2FAMessageCol [
				    	class: "col-md-12"
				
				        display sReset2FAMessageQue
					]											
				]
				
				div reset2FAMessageDescRow [
				    class: "row"
				    
				    div reset2FA2FAMessageCol [
				    	class: "col-md-12"
				
				        display sReset2FAMessageDesc
					]											
				]
			]
                       
            div reset2FAbuttons [
                class: "modal-footer"
                
                navigation reset2FASubmit (submit2FAResetRequest, "{Submit}") [
                    class: "btn btn-primary" 
                    attr_tabindex: "12"		                    
                ]
                 navigation reset2FACancel (customerSearchScreen, "{Cancel}") [
                    type: "cancel"
                    class: "btn btn-secondary"
                    attr_tabindex: "13"
                ]
			]
        ]
	]
    
    /* 42. Submit 2FA de-registration request. */
	action submit2FAResetRequest [
    	switch UcProfile2FAAction.deRegister2FAMethod(sSelectedUserId) [
    		case "success" send2FAResetNotification
            case "error"  twoFADeRegErrorMsg
            default reset2FANotifErrorMsg
    	]
    ]
    
    /* 43. Sends an email to the user to notify changes in their 2FA login option. */
    action send2FAResetNotification [
    	
    	loadProfile(        	
	    	userId: sSelectedUserId
	    	firstName: sNotificationFirstName
        	lastName: sNotificationLastName
	    )
        switch NotifUtil.send2FADeRegistrationNotification(sSelectedUserId, sNotificationFirstName, sNotificationLastName, sAppName) [
            case "success" reset2FADeRegSuccessMsg
            case "failure" reset2FANotifErrorMsg
            case "error" genericErrorMsg
            default genericErrorMsg
        ]
    ]
    
    /* 44. Reset topic preference. */
    action resetTopicPreference [
    	goto (getDefaultContactPrefSetting)
    ]
    
	/* 45. Getting default contact preferences setting from database. */
    action getDefaultContactPrefSetting [ 
    	getContactData.userid = sSelectedUserId
    	switch apiCall Notifications.GetContactSettings(getContactData, getContactResponse, status) [
		    case apiSuccess saveNotificationAtNls
		    default genericErrorMsg
		]   
    ]
    
    /* 46. Send contact preferences setting to NLS. */
    action saveNotificationAtNls [
    	setTopicFffc.customerId = sSelectedCustomerId
    	setTopicFffc.jsonConfig = getContactResponse.jsonConfig
    	switch apiCall FffcNotify.SetContactSettingsNls(setTopicFffc, status) [
    		case apiSuccess resetTpoicAtNlsSuccessMsg
    		default genericErrorMsg
    	]
    ]
    
    /* 47. Send contact preferences update success message. */
    action resetTpoicAtNlsSuccessMsg [
        displayMessage(type: "success" msg: oMsgResetTopicPreferencesAtNlsSuccess)
        goto(customerSearchScreen)
    ] 
    
    /*************************
     * EXTENSION SCENARIOS
     *************************/
    
    /* 3A. System displays the invalid input message on the screen. */ 
    action invalidInputMsg [
        displayMessage(type: "danger" msg: oMsgInvalidInput)         
        goto(customerSearchScreen)
    ]
    
    /* 4A. System displays too many results message on the screen. */
    action tooManyResultsMsg [
        displayMessage(type: "danger" msg: oMsgTooManyResults)         
        goto(customerSearchScreen)
    ]
    
    /* 13A. System displays the email regex error message on the screen. 
     * 24A. System displays the email regex error message on the screen. */
    action emailRegexErrorMsg [
        displayMessage(type: "danger" msg: oMsgEmailRegexError)         
        goto(customerSearchScreen)
    ] 
    
    /* 13A. System displays the resend enrollment confirmation email failed message on the screen.
     * 18A. System displays the resend enrollment confirmation email failed message on the screen. */
    action resendConfirmationNotifErrorMsg [
        displayMessage(type: "danger" msg: oMsgResendConfirmationNotifError)         
        goto(customerSearchScreen)
    ] 
    
    /* 24A. System displays the reset password email failed message on the screen.
     * 31A. System displays the reset password email failed message on the screen. */ 
    action resetPasswordNotifErrorMsg [
        displayMessage(type: "danger" msg: oMsgResetPasswordNotifError)         
        goto(customerSearchScreen)
    ]       
    
    /* 41B. System displays 2FA de-registration failed message on the screen. */
    action twoFADeRegErrorMsg [
    	auditLog(audit_update.update_others_2fa_error) [
            secondary: sSelectedUserId
        ]
        displayMessage(type: "danger" msg: oMsg2FADeRegError)         
        goto(customerSearchScreen)
    ]
    
    /* 42B. System displays 2FA de-registration success message on the screen. */
    action reset2FADeRegSuccessMsg [
    	auditLog(audit_update.update_others_2fa) [
            secondary: sSelectedUserId
        ]
        displayMessage(type: "success" msg: oMsgReset2FANotifSuccess)
        goto(customerSearchScreen)
    ]
    
    /* 42C. System displays 2FA de-registration user notification failed message on the screen. */
    action reset2FANotifErrorMsg [
        displayMessage(type: "danger" msg: oMsgReset2FANotifError)         
        goto(customerSearchScreen)
    ]
    
    /* 3B. There was an unknown problem verifying user search input data.
     * 4B. There was an unknown problem performing user search.
     * 5A. There was an error getting the billing page impersonate url.
     * 5B. There was an unknown problem getting the billing page impersonate url.     
     * 6A. There was an error getting the correspondence page impersonate url.
     * 6B. There was an unknown problem getting the correspondence page impersonate url.
     * 7A. There was an error getting the payment page impersonate url.
     * 7B. There was an unknown problem getting the payment page impersonate url.     
     * 8A. There was an error getting the notifications page impersonate url.
     * 8B. There was an unknown problem getting the notifications page impersonate url.
     * 9A. There was an error getting the profile page impersonate url.
     * 9B. There was an unknown problem getting the profile page impersonate url.
     * 13A. There was an error generating authorization code.
     * 13B. There was an unknown problem generating authorization code.
     * 14A. There was an error generating time stamp.
     * 14B. There was an unknown problem generating time stamp.
     * 15A. There was a failure saving authorization code details.
     * 22A. There was a failure saving password because of duplicate username.
     * 22B. There was an unknown failure saving password.
     * 24A. There was an error generating new secret question answer for the user to login.
     * 24B. There was an unknown problem generating new secret question answer for the user to login. 
     * 27B. There was an error sending new secret question answer email to the user.
     * 27C. There was an unknown problem sending new secret question answer email to the user. 
     */     
    action genericErrorMsg [
        displayMessage(type: "danger" msg: oMsgGenericError)        
        goto(customerSearchScreen)
    ] 
]
