useCase contactPreferencesConsentSms [
   /**
    *  author: James M. Looney
    *  created: 01-Jun-2023
    *
    *  Primary Goal:
    *       1. Display sms consent notice and verify sms validation regarding contact preferences topic.
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        1.0 01-Jun-2023 First Version Coded [James M. Looney]
    * 		 2.0 01-Nov-2023 Updated Version Coded [James M. Looney]
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the consent and validate sms button from the contact preferences page.
        ]]
        postConditions: [[
            1. Primary -- User sms updated successfully.
            2. Alternative 1 -- Contact details modification failed because of duplicate sms.
            3. Alternative 2 -- sms validation failed.
            4. Alternative 3 -- Account locked.
        ]]
    ]
    
	startAt init[
		sNewSms
		sCurrentEmail
	]
	
	importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil)
	importJava CreateSaml(com.sorrisotech.app.^library.saml.CreateSaml)
	importJava LoginUtil(com.sorrisotech.app.common.utils.LoginUtil)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava UcNotificationsAction(com.sorrisotech.app.notifications.UcNotificationsAction)
	importJava UcProfileAction(com.sorrisotech.app.profile.UcProfileAction)
	importJava UcTermsConditions(com.sorrisotech.uc.termsconditions.UcTermsConditions)
	
	serviceStatus status
	serviceParam(Notifications.SetUserAddress) setData
	serviceParam(FffcNotify.SetUserAddressNls) setDataFffc
	
    import validation.validationCodeRegex
    import profile.sAppName
	
	/**********************************************************************************************
	 * DATA ITEMS SECTION
	 *********************************************************************************************/		
    
	string sPopinTitle = "{SMS validate}"                                     
    
    native string sAccountStatus                                              
    native string sNewSms 
    native string sCurrentEmail   
    native string sSmsChannel = "sms"                                                  
    native string sSmsValidationCode  
    native string sOrgId
    native string sNameSpace = CreateSaml.getNameSpace()                                                                                 
    
    native string sUserId            = Session.getUserId()
    native string sProfileUpdateFlag = UcNotificationsAction.isEmailNotifPrefEnabled(sUserId)
    
    tag hTermsText = UcTermsConditions.getTermsConditions(sorrisoLanguage, sorrisoCountry)
    
    field fvalidationCode [                                                   
        string(label) sLabel = "{* Validation code:}" 
        input(control) pInput(validationCodeRegex, fvalidationCode.sValidation) 
        string(validation) sValidation = "{Validation code must be 6 digits.}"      
        string(required) sRequired = "{This field is required.}"                
    ]
    
    field fCheckBoxes [        												  
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]
    
	structure(message) oMsgValidationCodeSent [    
        string(title) sTitle = "{Validation code was sent}"
        string(body) sBody = "{We sent you the validation code using the specified method. Use it to proceed further.}"
    ]
    
    structure(message) oMsgValidationCodeSuccess [    
        string(title) sTitle = "{Update complete.}"
        string(body) sBody = "{Your sms mobile has been updated successfully.}"
    ]
   
    structure(message) oMsgIncorrectAuthCode [
        string(title) sTitle = "{Incorrect validation code}"
        string(body) sBody = "{The provided validation code is incorrect. Please use the code from the received notification}"
    ]
    
    structure(message) oMsgSmsFailed [
        string(title) sTitle = "{SMS failed}"
        string(body) sBody = "{An error occurred while trying to send a sms to the new mobile provided. Please try again later}"
    ]
    
    structure(message) oMsgDuplicateSms [
        string(title) sTitle = "{Duplicate SMS Address}"
        string(body) sBody = "{This sms address has been used. Please enter a different one.}"
    ]
    
    structure(message) oMsgAccountLocked [    
        string(title) sTitle = "{Account locked}"
        string(body) sBody = "{Your account is locked. Please try again after 10 minutes.}"
    ]
    
    structure(message) oMsgGenericError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later}"
    ]
    
    structure(message) oMsgSaveContactDetailsError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to save contact details. Please try again later}"
    ]
    
   /***********************************************************************************************
     * MAIN SUCCESS SCENARIOS
     *********************************************************************************************/   
    
    /* Loads user profile and get user details. */       
    action init [  
    	loadProfile(            
            accountStatus: sAccountStatus   
            ) 
        goto(verifySms)                    
    ]
    
    /* Final check that the sms address is unique before processing,
	 * otherwise abort change sms flow. */
    action verifySms [    
        switch UcProfileAction.isAddressAvailable (
        	sNameSpace, 
        	sSmsChannel, 
        	sNewSms
        ) [        
            case "yes" sendSmsValidationCode
            case "no"  smsDuplicateMsg
            default genericErrorMsg
        ]
    ]
    
    /* System creates a validation code and sends the details.
     * Details are sent either to the new email or to the phone 
     * based on the mode the user selected. */
    action sendSmsValidationCode [
        switch UcProfileAction.sendEmailValidationCode (
            sUserId,
            sCurrentEmail,
            sSmsValidationCode,
            sorrisoLanguage             
        ) [
            case "success" validationCodeSentMsg
            case "error" smsFailedMsg                        
        ]
    ]
    
    /* Validation popin is displayed with the message that the 
     * validation code details are sent to the user.*/ 
    action validationCodeSentMsg [ 
        auditLog(audit_profile.notification_sms_updated)
        
        displayMessage(type: "warning" msg: oMsgValidationCodeSent) 
        goto(smsValidationPopin)  
    ]
    
    /* Display the contact preferences sms validation popin */
    xsltFragment smsValidationPopin [
    	
    	form content [
    		class: "modal-content"
    	
	        h4 heading [
	            class: "modal-header"
				display sPopinTitle 
	        ]
			
            div body [ 
				class: "modal-body"
				messages: "top"
				                               
                div validationFields [
                	div row1 [
                        class: "row"
                        
                        div fieldsCol1 [
                        	class: "col-md-3"
                        	
                        	display fvalidationCode [
	                    		control_attr_tabindex: "1"
								control_attr_autofocus: ""
	                    	]
                        ]
                        
                        div fieldsCol2 [
                        	class: "col-md-9"
                        	
                        	navigation resendValidationCode(sendSmsValidationCode, "{Re-send verification code}") [
			                    class: "st-login-password-link"
			                    type: "popin"   
			                    attr_tabindex: "2"
			                    popin_size: "lg"
								attr_tabindex: "400"
			                ]
                        ]
					]
                ]
                
                div smsNotificationTerms [
									
					div row2 [
						class: "row"
						
						div col2 [
							class: "col-md-12"
			
							display hTermsText    
						]
					]
					
					div row3 [
						class: "row"
	                
						div col3 [
							class: "col-md-12"
	                
	                		display fCheckBoxes [
	                			class: "st-spacing-top"
	                			control_attr_tabindex: "1"
								control_attr_autofocus: ""
	                		]
						]
					]
				]
			]
			
            div buttons [
                class: "modal-footer"
                                   	
                navigation confirm (verifyAuthCode, "{Confirm}") [
                    class: "btn btn-primary"
                    require: [
                    	fvalidationCode
                    	fCheckBoxes
                    ]
                    type: "popin"
                    logic: [
                        if sAccountStatus != "open" then "remove"
                    ]
                    attr_tabindex: "2"
                    popin_size: "lg"
					attr_tabindex: "400"
                ]
                navigation cancel (gotoContactPreferences, "{Cancel}") [
                    class: "btn btn-secondary"
                    attr_tabindex: "3"
                ]
            ]
        ]
    ]
    
    /**********************************************************************************************
     * SMS VALIDATION ACTIONS
     *********************************************************************************************/
    
    /* User clicks the "Validate" button on the sms validation popin.
     * System verifies the auth code entered with the auth code in the database.
     * If the auth code entered is correct, then sms details are updated.
     * If the auth code entered is incorrect, then the failure method is called.*/
    action verifyAuthCode [
         if sSmsValidationCode == fvalidationCode.pInput then
            saveSmsAddress
         else
            processAuthCodeFailure     
    ]
    
    /* Saves the new sms address via the notification service. */
    action saveSmsAddress [
    	setData.userid = sUserId
    	setData.channel = sSmsChannel
    	setData.address = sNewSms
        switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess saveSmsAddressAtNls
		    default saveContactDetailsError
		]
    ]
	
	/* Saves the new sms address via NLS API service */
    action saveSmsAddressAtNls [
    	loadProfile(            
            fffcCustomerId: sOrgId   
            )
    	setDataFffc.customerId = sOrgId
    	setDataFffc.channel = sSmsChannel
    	setDataFffc.address = sNewSms
    	switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, status) [
    		case apiSuccess checkProfileUpdateNotificationFlag
    		default saveContactDetailsError
    	]
    ]
    
    /* Could not save contact details. */
    action saveContactDetailsError [
    	displayMessage(type: "danger" msg: oMsgSaveContactDetailsError)
    	gotoUc(contactPreferences)
    ]
    
    /* System checks the profile update notification flag. */
    action checkProfileUpdateNotificationFlag [
        if  sProfileUpdateFlag == "true"   then
            sendUpdateNotification
        else
            validationCodeSuccessMsg   
    ]
    
    /* System sends the sms update notification message to the user. */
    action sendUpdateNotification [
        switch UcProfileAction.sendUpdateNotification (
            sUserId,
            sAppName             
        ) [
            case "success" validationCodeSuccessMsg
            case "error" genericErrorMsg 
            default genericErrorMsg                       
        ]
    ]
    
    /* System displays the sms validated successfully message on the main contact preferences page. */
    action validationCodeSuccessMsg [  
     	auditLog(audit_profile.notification_sms_updated_success)
   	
        displayMessage(type: "success" msg: oMsgValidationCodeSuccess)
        gotoUc(contactPreferences) 
    ]
    
    /* User clicks the "Cancel" button or "Later" button 
     * which closes the popin and shows the contact preferences page.
     */
    action gotoContactPreferences [  	
        gotoUc(contactPreferences)
    ] 
    
    /**********************************************************************************************
     * EXTENSION SCENARIOS
     *********************************************************************************************/
    
    /* Display the account locked message on the contacts preferences sms popin. */
    action accountLockedMsg [
        fvalidationCode.pInput = ""
        displayMessage(type: "danger" msg: oMsgAccountLocked)        
        gotoUc(contactPreferencesConsentSms) 
    ]
    
    /* Displays a message that sms sending failed. */
    action smsFailedMsg [
        displayMessage(type: "danger" msg: oMsgSmsFailed)            
        gotoUc(contactPreferences)
    ]
    
    /* Displays a message that the sms address is already in use. */
    action smsDuplicateMsg [
        displayMessage(type: "danger" msg: oMsgDuplicateSms)            
        gotoUc(contactPreferences)
    ]
    
    /* Calls the method to process the auth code failure. */
    action processAuthCodeFailure [
    	auditLog(audit_profile.sms_validation_failure)
    	    	
        switch AuthUtil.processAuthCodeFailure(sUserId)[
            case "success" incorrectAuthCodeMsg
            case "failure.lockout" lockoutAuthCodeFailure
            case "error" genericErrorMsg
            default genericErrorMsg         
        ]
    ]
    
    /* Display the incorrect auth code message on the sms validation popin. */ 
    action incorrectAuthCodeMsg [   	
        fvalidationCode.pInput = ""
        displayMessage(type: "danger" msg: oMsgIncorrectAuthCode)
        gotoUc(contactPreferencesConsentSms) 
    ]
    
    /* Updates the account status as locked. */  
    action lockoutAuthCodeFailure [
         sAccountStatus = "lockedAuthCodeFailure"
         updateProfile(
            userId: sUserId     
            accountStatus: "lockedAuthCodeFailure"                          
            )
         switch LoginUtil.setTempLockoutTime()[
            case "success" accountLockedMsg
            case "error" genericErrorMsg
            default genericErrorMsg
         ]   
    ]
    
    /* Display a generic error message. */ 
    action genericErrorMsg [
    	auditLog(audit_profile.notification_failure)
    	
        displayMessage(type: "danger" msg: oMsgGenericError)            
        gotoUc(contactPreferences)
    ]
]