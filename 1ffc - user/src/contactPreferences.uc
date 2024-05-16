useCase contactPreferences [
   /**
    *  author: James M. Looney
    *  created: 01-Jun-2023
    *
    *  Primary Goal:
    *       1. Modify contact preferences.
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        1.0 01-Jun-2023 First Version Coded [James M. Looney]
    * 		 2.0 01-Nov-2023 Updated Version Coded [James M. Looney]
    * 		 3.0 01-Dec-2023 Updated Version Coded [James M. Looney]
    */
    
    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the notifications menu option.
        ]]
        postConditions: [[
            1. Primary -- Contact preferences topic(s) displayed.
        ]]
    ]
    
    actors [
        ACTOR_SAAS_USER_PROFILE
    ]
	
	shortcut appContactPreferences(getUserDetails) 
	startAt getUserDetails

	child utilImpersonationActive(utilImpersonationActive)
             
    importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil)
    importJava ContactPrefs(com.sorrisotech.app.profile.ContactPrefs)
    importJava LoginUtil(com.sorrisotech.app.common.utils.LoginUtil)
    importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
    importJava PersonaData(com.sorrisotech.app.utils.PersonaData)
    importJava Session(com.sorrisotech.app.utils.Session)	     
    importJava UcProfileAction(com.sorrisotech.app.profile.UcProfileAction)
    import validation.emailRegex
    
    // -- handling impersonation --
 	import utilImpersonationActive.sImpersonationActive	
 	native string bImpersonateActive
    
    
    serviceStatus status
	serviceParam(Notifications.GetUserAddresses) getData
	serviceResult(Notifications.GetUserAddresses) getResponse
	serviceParam(Notifications.SetContactSettings) channels
	serviceParam(FffcNotify.SetContactSettingsNls) setDataFffc
	
	serviceParam (Profile.AddLocationTrackedEvent) setLocationData
	serviceResult (Profile.AddLocationTrackedEvent) setLocationResp
     
    /**********************************************************************************************
     * DATA ITEMS SECTION
     *********************************************************************************************/ 
     
    static sMenuLink       = "{Contact preferences}"
    string sHeader1        = "{Please select the new notification(s) that you would like to receive.}"
    string sPageHeader     = "{Contact Preferences}"
    
    string(p) sIconMailTitle = "{Mail}"
	image sIconMail = "img/mail.svg"
    string(p) sIconEmailTitle = "{Email}"
    image sIconEmail = "img/email.svg"
    string(p) sIconTextTitle   = "{Text}"
    image sIconText = "img/mobile.svg"
    
    string sConsentMessage = "{E-Sign consent: By signing this form electronically, you are agreeing to the terms and conditions.}"
    
    string reactJsTopics = "JavaScript error, topics cannot be displayed."
    
    native string sCurrentEmail
    native string sCurrentSms
    native string sMaskedEmail = NotifUtil.getMaskedEmail(sCurrentEmail)
    
	native string sAccountStatus                                              
    native string sNewEmail
    native string sEmailChannel = "email"                                                   
    native string sUnverifiedEmailAddress                                         
	native string sNewSms
	native string sSmsChannel = "sms"
	native string sOrgId
	
	native string sUserId            = Session.getUserId()
    native string sNameSpace         = AuthUtil.getAppNameSpace()
    native string sIpAddress         = Session.getExternalIpAddress()
    native string sCategory          = "topic"
    native string sType
    native string sOperation
	
	input sGeolocation
	
    field fUserEmail [  													                                             
	    string(label) sLabel = "{Update E-Mail message alert}"
	    input(control) pInput(emailRegex, fUserEmail.sValidation)
        string(validation) sValidation = "{Please provide a valid e-mail address. Your e-mail address may contain up to 50 characters and must appear in the standard e-mail address format: name@example.com.}"
        string(required) sRequired = "{This field is required.}"     
        string(error) sError = "{This e-mail address is in use. Please provide a different e-mail address.}" 
	]
		
	field fUserMobile [               										                              
	    string(label) sLabel = "{Update Text message alert}"	        
	    input(control) pInput("^1?[\\s-]?\\(?(\\d{3})\\)?[\\s-]?\\d{3}[\\s-]?\\d{4}$", fUserMobile.sValidation)        
        string(validation) sValidation = "{Entry must be numeric and appear in the standard telephone number format: XXX-XXX-XXXX.}" 
        string(required) sRequired = "{This field is required.}"
	]
	
	native string sESignPrevious
	native string sESignCurrent
    checkBoxes cEnableESignConsent [
		true : "{I consent to E-Sign documents.}"	
	]
    
    structure(message) oMsgNoEmailChangeMade [    
        string(title) sTitle = "{No change was made}"
        string(body) sBody = "{There was no change in the e-mail address. The system took no action as a result.}"
    ]
    
    structure(message) oMsgNoSmsChangeMade [    
        string(title) sTitle = "{No change was made}"
        string(body) sBody = "{There was no change in the mobile number. The system took no action as a result.}"
    ]
    
    structure(message) oMsgDuplicateEmail [    
        string(title) sTitle = "{E-mail error}"
        string(body) sBody = "{This e-mail address is in use. Please provide a different e-mail address.}"
    ]
    
    structure(message) oMsgDuplicateSms [    
        string(title) sTitle = "{Mobile error}"
        string(body) sBody = "{This mobile number is in use. Please provide a different mobile number.}"
    ]
    
    structure(message) oMsgAccountLocked [    
        string(title) sTitle = "{Account locked}"
        string(body) sBody = "{Your account is locked. Please try again after 10 minutes.}"
    ]
    
    structure(message) oMsgGenericError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later}"
    ]
    
    structure(message) oMsgSaveNotificationSuccess [    
        string(title) sTitle = "{Update complete.}"
        string(body) sBody = "{Your notification preferences have been updated successfully.}"
    ]
    
    structure(message) oMsgNoChangesMade [    
        string(title) sTitle = "{No changes made.}"
        string(body) sBody = "{There were no notification changes made to the system.}"
    ]
    
    structure(message) oMsgRetrieveContactDetailsError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to retrieve contact details. Please try again later}"
    ]
    
    structure(message) oMsgESignConsentEnabled [
        string(title) sTitle = "{E-Sign consent update complete.}"
        string(body) sBody = "{Your E-Sign consent has been enabled successfully.}"
    ]
    
    structure(message) oMsgESignConsentDisabled [
        string(title) sTitle = "{E-Sign consent update complete.}"
        string(body) sBody = "{Your E-Sign consent has been disabled successfully.}"
    ]
    
    structure(message) oMsgESignConsentNoChangesMade [
        string(title) sTitle = "{No changes made.}"
        string(body) sBody = "{There were no E-Sign consent changes made to the system.}"
    ]
    
    /**********************************************************************************************
	 * MAIN SUCCESS SCENARIOS
     *********************************************************************************************/
	
	/* Loads user profile account and consent status and json object. */       
    action getUserDetails [   
    	bImpersonateActive = sImpersonationActive
    	
        loadProfile(            
            accountStatus: sAccountStatus
            eSignConsentEnabled: sESignPrevious
            )
		PersonaData.selectItem(cEnableESignConsent, sESignPrevious)
        getData.userid = sUserId
        switch apiCall Notifications.GetUserAddresses(getData, getResponse, status) [
		    case apiSuccess checkUserDetailsResult
		    default retrieveContactDetailsError
		]
    ]
    
    /* Verify json response. */
    action checkUserDetailsResult [
		if getResponse.jsonAddresses == "" then 
			retrieveContactDetailsError 
		else 
			extractEmailFromAddresses    	
    ]
    
    /* Could not retrieve contact details. */
    action retrieveContactDetailsError [
    	sCurrentEmail = ""
    	sCurrentSms = ""
    	displayMessage(type: "danger" msg: oMsgRetrieveContactDetailsError)
    	goto(assignValuesToFields)
    ]
    
    /* Loads current email from database via the notifications service. */
    action extractEmailFromAddresses [
    	UcProfileAction.getAddress (
            getResponse.jsonAddresses,
            sEmailChannel,
            sCurrentEmail             
        ) 
        goto(extractSmsFromAddresses)
    ]
    
    /* Loads current sms from database via the notifications service. */
     action extractSmsFromAddresses [
    	UcProfileAction.getAddress (
            getResponse.jsonAddresses,
            sSmsChannel,
            sCurrentSms             
        ) 
        goto(assignValuesToFields)
    ]
    
     /* Assign user profile values to the usecase fields. */
    action assignValuesToFields [       
        fUserEmail.pInput = sMaskedEmail
        sNewEmail = sUnverifiedEmailAddress      
		fUserMobile.pInput = sCurrentSms 
        goto(checkAccountStatus)
    ]
    
    /* Verify the account status. */
    action checkAccountStatus [           
        switch sAccountStatus [         
            case "open"  notificationsScreen
            case "lockedPasswordFailure"  checkTempLockoutOver                          
            case "lockedAuthCodeFailure"  checkTempLockoutOver            
            default genericErrorMsg
        ]               
    ]
    
    /* Display the notification preferences screen. */
    xsltScreen notificationsScreen("{Contact Preferences}") [
    	
    	child utilImpersonationActive
        
        div contactPreferences [
        	class: "st-notifications"
			
	        div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
	            
		            display sPageHeader
				]
				
				h4 info [
					class: "col-md-12"
					display sHeader1 
				]
			]
			          
	        div contactPreferencesForm [
	                      
	            div content [
	                class: "st-notifications-body"
	                
	                div contactPreferences [
	                	
	                	class: "row st-border-bottom pb-4 mb-4"
	                	
	                	form contactLeft [
	                		class: "row col-md-4"
	                		
	                		div contactLeftRow1 [
								class: "row"
								
		                		display fUserEmail [
									class: "st-toggle"
									label_class: "col-10"
									field_class: "col-8"
									control_attr_tabindex: "1"
									control_attr_autofocus: ""
									pInput_attr_new-email-address: ""
									pInput_attr_st-new-email: ""
									sError_ng-show: "main['fUserEmail.pInput'].$invalid && !!main['fUserEmail.pInput'].$error.email && main['fUserEmail.pInput'].$dirty"
									sError_attr_sorriso-error: "new-email"
								]							
							]
							
							div buttonsLeftRow1 [ 
								class: "row"
								
								div buttonsCol1 [
									class: "col-md-12"
									navigation consentValidateEmail(verifyEmail, "{Consent and Validate}") [
										logic: [
											if bImpersonateActive == "true" then "remove"
									     ]	
										type: "popin"
										class: "btn btn-primary"
										popin_controller: "ChangeAuthCtl"
										popin_size: "lg"
										attr_tabindex: "400"
										require: [
											fUserEmail
										]
									]
									navigation consentValidateEmailDisable(verifyEmail, "{Consent and Validate Disabled}") [
										logic: [
											if bImpersonateActive != "true" then "remove"
									     ]	
										type: "popin"
										class: "btn btn-primary disabled"
										popin_controller: "ChangeAuthCtl"
										popin_size: "lg"
										attr_tabindex: "400"
										require: [
											fUserEmail
										]
									]
									
									
								]
							]
	                	]
	                	
	                	form contactMiddle [
	                		class: "row col-md-4"
	                		
	                		div contactMiddleRow1 [
								class: "row"
								
								display fUserMobile [
									class: "st-toggle"
									label_class: "col-10"
									field_class: "col-8"
									control_attr_tabindex: "2"
								]
							]
							
							div buttonsMiddleRow1 [ 
								class: "row"
								
								div buttonsCol2 [
									class: "col-md-12"
									navigation consentValidateSms(verifySms, "{Consent and Validate}") [
										logic: [
											if bImpersonateActive == "true" then "remove"
									     ]	
										type: "popin"
										class: "btn btn-primary"
										popin_controller: "ChangeAuthCtl"
										popin_size: "lg"
										attr_tabindex: "400"
										require: [
											fUserMobile
										]
									]
									navigation consentValidateSmsDisable(verifySms, "{Consent and Validate Disabled}") [
										logic: [
											if bImpersonateActive != "true" then "remove"
									     ]	
										type: "popin"
										class: "btn btn-primary disabled"
										popin_controller: "ChangeAuthCtl"
										popin_size: "lg"
										attr_tabindex: "400"
										require: [
											fUserMobile
										]
									]
									
								]
							]
	                	]
	                	
	                	form contactRight [
	                		class: "col-md-4"
	                		
	                		div contactRightRow1 [
								class: "mt-3"
								display sConsentMessage
							]
							
							div contactRightRow2 [
	                			display cEnableESignConsent [
	                				control_attr_tabindex: "1"
									control_attr_autofocus: ""
	                			]
							]
							
		                	div buttonsRightRow [
				                class: "row"
				                                   	
				                div buttonsCol3 [
									class: "col-md-12 mt-4"
									navigation confirmESign (confirmESign, "{Submit}") [
										logic: [
											if bImpersonateActive == "true" then "remove"
										]										
										class: "btn btn-primary"
										attr_tabindex: "400"
										data: [cEnableESignConsent]
									]
									navigation confirmESignDisable (confirmESign, "{Submit Disabled}") [
										logic: [
											if bImpersonateActive != "true" then "remove"
										]										
										class: "btn btn-primary disabled"
										attr_tabindex: "400"
										data: [cEnableESignConsent]
									]
								]
				            ]
	                	]
	                ]
	                
					div contactPreferencesIconsRow [
						class: "row st-border-bottom"
						
						div mailIconCol [
							class: "offset-sm-6 col-4 col-sm-2"
							display sIconMail
							display sIconMailTitle 
						]
						
						div emailIconCol [
							class: "col-4 col-sm-2"
							display sIconEmail
							display sIconEmailTitle 
						]
						
						div textIconCol [
							class: "col-4 col-sm-2"
							display sIconText
							display sIconTextTitle 
						]
					]
	                
	                div contactPreferencesTopic [
	                	class: "mb-3"
	                	display reactJsTopics [
						     attr_sorriso: "element-topics-config"
						]
	                ]
	                
	                form contactPreferencesSaveAndCancel [
		            	class: "st-buttons"
		            	
		            	display sGeolocation [
		                	 control_attr_sorriso-geo: ""
		                	 logic: [
		                		if "true" == "true" then "hide"
		               		]
		               ]
		                
		                div buttonsRow1 [
		                    class: "row"
		                    
		                    div buttonsCol1 [
		                    	class: "col-md-12 st-padding-top30"
		                    	
		                        navigation save (hasNotificationChanges, "{Save}") [
				                     logic: [
				                         if bImpersonateActive == "true" then "remove"
				                    ]		                        	
		                            class: "btn btn-primary"
		                            attr_tabindex: "9"
		                            data: [
		                            	sGeolocation
		                            ]
		                        ]

		                        navigation saveDisable (hasNotificationChanges, "{Save Disabled}") [
				                     logic: [
				                         if bImpersonateActive != "true" then "remove"
				                    ]		                        	
		                            class: "btn btn-primary disabled"
		                            attr_tabindex: "9"
		                            data: [
		                            	sGeolocation
		                            ]
		                        ]
		                        
								navigation cancel (cancelNotification, "{Cancel}") [
		        					class: "btn btn-secondary"
		        					attr_tabindex: "10"
								]
							]
						]
		            ]
	            ]                       
	        ]
        ]
    ]
    
    /**********************************************************************************************
     * E-MAIL NOTIFICATION PREFERENCES ACTIONS
     *********************************************************************************************/
    
    /* User clicks the "Consent and Validate" button. System first verifies if the new email provided 
     * already exists. */
    action verifyEmail [
    	if sMaskedEmail == fUserEmail.pInput then
    		duplicateEmailMsg
    	else
    		verifyNewEmail
    ]
    
    /* Again, if the new email provided already exists, a duplicate email message is displayed.
     * If there is no change with the email, a no changes made message is displayed. */
    action verifyNewEmail [
        switch UcProfileAction.verifyEmail (
        	sNameSpace,
        	sEmailChannel,
            sCurrentEmail, 
            fUserEmail.pInput             
        ) [
            case "success" consentValidateEmail
            case "duplicate" duplicateEmailMsg            
            case "no_change" noEmailChangeMadeMsg
        ]
    ]
    
    /* Email Validation popin is displayed where user can provide the 
     * validation code for confirming the new email change. */
    action consentValidateEmail [
    	sNewEmail = fUserEmail.pInput
		gotoUc(contactPreferencesConsentEmail) [
			sNewEmail:sNewEmail
		]
	]
	
	/**********************************************************************************************
     * MOBILE NOTIFICATION PREFERENCES ACTIONS
     *********************************************************************************************/
	
	/* User clicks the "Consent and Validate" button. System verifies the sms entered. 
     * If the new sms provided already exists, a duplicate sms message is displayed.
     * If there is no change with the sms, a no changes made message is displayed. */
    action verifySms [
    	switch UcProfileAction.verifySms (
    		sNameSpace,
        	sSmsChannel,
        	sCurrentSms,
        	fUserMobile.pInput            
        ) [
            case "success" consentValidateSms
            case "duplicate" duplicateSmsMsg
            case "no_change" noSmsChangeMadeMsg
        ]
    ]
    
	/* Sms Validation popin is displayed where user can provide the 
     * validation code for confirming the new sms change.
     */
	action consentValidateSms [
		sNewSms = fUserMobile.pInput
		gotoUc(contactPreferencesConsentSms) [
			sNewSms:sNewSms
			sCurrentEmail:sCurrentEmail
		]
	]
	
	/**********************************************************************************************
     * E-SIGN CONSENT NOTIFICATION PREFERENCES ACTIONS
     *********************************************************************************************/
    
    /* User clicks the "Submit" button. System first verifies if e-sign enabled or disabled.*/
    action confirmESign [
		PersonaData.getSelectedValue(cEnableESignConsent, sESignCurrent)
		if sESignCurrent == "true" then eSignEnable else eSignDisable
	]
	
	/* System is enabled and determines what messages should be displayed. */
	action eSignEnable [
		if sESignPrevious == "true" then consentNotChanged else showESignEnabled
	]
	
	/* Display an e-sign consent enabled message. */
	action showESignEnabled [
		auditLog(audit_profile.notification_esign_enabled)
		
		displayMessage(type: "success" msg: oMsgESignConsentEnabled)
		goto(saveESign)
	]
	
	/* System is disabled and determines what messages should be displayed. */
	action eSignDisable [
		if sESignPrevious == "" then consentNotChanged else showESignDisabled
	]
	
	/* Display an e-sign consent disabled message. */
	action showESignDisabled [
		auditLog(audit_profile.notification_esign_disabled)
		
		displayMessage(type: "success" msg: oMsgESignConsentDisabled)
		goto(saveESign)
	]
	
	/* System saves and updates the user profile's e-sign consent attribute. */
	action saveESign [
		PersonaData.getSelectedValue(cEnableESignConsent, sESignPrevious)
		updateProfile(            
            eSignConsentEnabled: sESignPrevious 
            )
		
		goto(notificationsScreen)
	]
	
	/* Display an e-sign consent no changes message. */
	action consentNotChanged [
		auditLog(audit_profile.notification_esign_no_changes)
		
		displayMessage(type: "success" msg: oMsgESignConsentNoChangesMade)
		goto(notificationsScreen)
	]
	
	/**********************************************************************************************
     * TOPICS AND FEATURES NOTIFICATION PREFERENCES ACTIONS
     *********************************************************************************************/
  
  	/* Determine if the user has made notification changes. */
  	action hasNotificationChanges [
  		switch ContactPrefs.hasChanges() [
  			case "true" saveNotificationSuccess
  			case "false" noNotificationChangesMadeMsg
  			default genericErrorMsg
  		]
  	]
  
    /* Load json into notifications. */    
    action saveNotificationSuccess [ 
		channels.userid = sUserId
		ContactPrefs.toJson(channels.jsonConfig)
        switch apiCall Notifications.SetContactSettings(channels, status) [
		    case apiSuccess addLocationTrackedEvent
		    default genericErrorMsg
		]
    ]
    
    /* Adding geolocation track event */
    action addLocationTrackedEvent [
    	ContactPrefs.getConsentTypeAndOperation(sType, sOperation)
		setLocationData.sUser = sUserId
    	setLocationData.sCategory = sCategory
    	setLocationData.sType = sType
    	setLocationData.sIpAddress = sIpAddress
    	setLocationData.sBrowserGeo = sGeolocation
    	setLocationData.sOperation = sOperation
    	
    	switch apiCall Profile.AddLocationTrackedEvent(setLocationData, setLocationResp, status) [
    		case apiSuccess saveNotificationAtNls
    		default genericErrorMsg
    	]
    ]
    
    /* Fetch the orgID from the user profile and send contact preferences setting to NLS */
    action saveNotificationAtNls [
 		loadProfile(            
            fffcCustomerId: sOrgId   
            )
    	setDataFffc.customerId = sOrgId
    	ContactPrefs.toJson(setDataFffc.jsonConfig)
    	switch apiCall FffcNotify.SetContactSettingsNls(setDataFffc, status) [
    		case apiSuccess genericSuccessMsg
    		default genericErrorMsg
    	]
    ]
 
    /* User clicks the "Cancel" button. */    
    action cancelNotification [                
        goto(notificationsScreen)
    ] 
   
    /**********************************************************************************************
     * EXTENSION SCENARIOS
     *********************************************************************************************/
    
    /* Password failure - System checks if the lock out time is over. 
     * Auth code failure - System checks if the lock out time is over.
     */
    action checkTempLockoutOver [
        switch LoginUtil.isTempLockoutOver(sUserId) [        
          case "yes" clearFailure
          case "no"  accountLockedMsg                 
          default genericErrorMsg
        ]       
    ]
   
   /* Lock out time is over. Reset the fields. */
    action clearFailure [    
        sAccountStatus = "open"        
        updateProfile(
            userId: sUserId     
            accountStatus: "open"                  
            lockoutTime: "0"
            passwordRetryCount: "0"
            passwordFailTime: "-1"             
            authCodeRetryCount: "0"
            authCodeFailTime: "-1"          
            )
        gotoUc(contactPreferences)   
    ]
    
   /* Display the account locked message on the profile contact edit popin. */
    action accountLockedMsg [
        displayMessage(type: "danger" msg: oMsgAccountLocked)        
        goto(notificationsScreen)
    ]
   
    /* Displays a message that the email entered is a duplicate 
     * that already exists in the database.
     */  
    action duplicateEmailMsg [       
        displayMessage(type: "danger" msg: oMsgDuplicateEmail)                    
        goto(notificationsScreen)
    ]
    
    /* Displays a message that the sms entered is a duplicate 
     * that already exists in the database.
     */  
    action duplicateSmsMsg [       
        displayMessage(type: "danger" msg: oMsgDuplicateSms)                    
        goto(notificationsScreen)
    ]
    
    /* Displays a message that no changes were made to the email address. */
    action noEmailChangeMadeMsg [    
        displayMessage(type: "warning" msg: oMsgNoEmailChangeMade)                           
         goto(notificationsScreen)          
    ]
    
    /* Displays a message that no changes were made to the mobile number. */
    action noSmsChangeMadeMsg [    
        displayMessage(type: "warning" msg: oMsgNoSmsChangeMade)                           
         goto(notificationsScreen)          
    ]
    
    /* Display a no changes message. */
    action noNotificationChangesMadeMsg [
    	auditLog(audit_profile.notification_no_changes)
    	
        displayMessage(type: "success" msg: oMsgNoChangesMade)               
        goto(notificationsScreen)
    ]
    
    /* Display a generic success message. */
    action genericSuccessMsg [
    	auditLog(audit_profile.notification_saved_success)
    	
        displayMessage(type: "success" msg: oMsgSaveNotificationSuccess)               
        goto(notificationsScreen)
    ]
    
    /* Display a generic error message. */
    action genericErrorMsg [
    	auditLog(audit_profile.notification_failure)
    	
        displayMessage(type: "danger" msg: oMsgGenericError)            
        goto(notificationsScreen)
    ]
]