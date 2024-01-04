useCase contactPreferencesConsentEmail [
   /**
    *  author: James M. Looney
    *  created: 01-Jun-2023
    *
    *  Primary Goal:
    *       1. Display email consent notice and verify email validation regarding contact preferences topic.
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
            1. The user clicks the consent and validate email button from the contact preferences page.
        ]]
        postConditions: [[
            1. Primary -- User email updated successfully.
            2. Alternative 1 -- Contact details modification failed because of duplicate email.
            3. Alternative 2 -- Email validation failed.
            4. Alternative 3 -- Account locked.
        ]]
    ]
    
	startAt init[
		sNewEmail
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
    
	string sPopinTitle = "{E-mail validate}"                                 
    
    native string sAccountStatus                                              
    native string sNewEmail
    native string sEmailChannel = "email"                                                   
    native string sEmailValidationCode  
    native string sNameSpace = CreateSaml.getNameSpace()                                                                             
    
    native string sUserId            = Session.getUserId()
    native string sProfileUpdateFlag = UcNotificationsAction.isEmailNotifPrefEnabled(sUserId)
    native string sOrgId
    
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
        string(body) sBody = "{Your e-mail address has been updated successfully.}"
    ]
   
    structure(message) oMsgIncorrectAuthCode [
        string(title) sTitle = "{Incorrect validation code}"
        string(body) sBody = "{The provided validation code is incorrect. Please use the code from the received notification}"
    ]
    
    structure(message) oMsgEmailFailed [
        string(title) sTitle = "{E-mail failed}"
        string(body) sBody = "{An error occurred while trying to send an e-mail to the new address provided. Please try again later}"
    ]
    
    structure(message) oMsgDuplicateEmail [
        string(title) sTitle = "{Duplicate E-mail Address}"
        string(body) sBody = "{This e-mail address has been used. Please enter a different one.}"
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
        goto(verifyEmail)                    
    ]
    
	/* Final check that the email address is unique before processing,
	 * otherwise abort change email flow. */
    action verifyEmail [    
        switch UcProfileAction.isAddressAvailable (
        	sNameSpace, 
        	sEmailChannel, 
        	sNewEmail
        ) [        
            case "yes" sendEmailValidationCode
            case "no"  emailDuplicateMsg
            default genericErrorMsg
        ]
    ]
    
    /* System creates a validation code and sends the details.
     * Details are send either to the new email or to the phone 
     * based on the mode the user selected. */
    action sendEmailValidationCode [
        switch UcProfileAction.sendEmailValidationCode (
            sUserId,
            sNewEmail,
            sEmailValidationCode,
            sorrisoLanguage             
        ) [
            case "success" validationCodeSentMsg
            case "error" emailFailedMsg                        
        ]
    ]
    
    /* Validation popin is displayed with the message that the 
     * validation code details are send to the user.*/ 
    action validationCodeSentMsg [ 
        auditLog(audit_profile.notification_email_updated)
        
        displayMessage(type: "warning" msg: oMsgValidationCodeSent) 
        goto(emailValidationPopin)  
    ]
    
    /* Display the contact preferences email validation popin */
    xsltFragment emailValidationPopin [
    	
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
                        	
                        	navigation resendValidationCode(sendEmailValidationCode, "{Re-send verification code}") [
			                    class: "st-login-password-link"
			                    type: "popin"   
			                    attr_tabindex: "2"
			                    popin_size: "lg"
								attr_tabindex: "400"
			                ]
                        ]
					]
                ]
                
                div emailNotificationTerms [
									
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
     * E-MAIL VALIDATION ACTIONS
     *********************************************************************************************/
    
    /* User clicks the "Validate" button on the email validation popin.
     * System verifies the auth code entered with the auth code in the database.
     * If the auth code entered is correct, then email details are updated.
     * If the auth code entered is incorrect, then the failure method is called.*/
    action verifyAuthCode [
         if sEmailValidationCode == fvalidationCode.pInput then
            saveEmailAddress
         else
            processAuthCodeFailure     
    ] 
    
    /* Saves the new email address via the notification service. */
    action saveEmailAddress [
    	setData.userid = sUserId
    	setData.channel = sEmailChannel
    	setData.address = sNewEmail
        switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess saveEmailAddressAtNls
		    default saveContactDetailsError
		]
    ]
    
    /* Saves the new email address via NLS API service */
    action saveEmailAddressAtNls [
    	loadProfile(            
            fffcCustomerId: sOrgId   
            )
    	setDataFffc.customerId = sOrgId
    	setDataFffc.channel = sEmailChannel
    	setDataFffc.address = sNewEmail
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
    
    /* System sends the email update notification message to the user. */
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
    
    /* System displays the email validated successfully message on the main contact preferences page. */
    action validationCodeSuccessMsg [  
     	auditLog(audit_profile.notification_updated_success)
   	
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
    
    /* Display the account locked message on the contacts preferences email popin. */
    action accountLockedMsg [
        fvalidationCode.pInput = ""
        displayMessage(type: "danger" msg: oMsgAccountLocked)        
        gotoUc(contactPreferencesConsentEmail) 
    ]
    
    /* Displays a message that email sending failed. */
    action emailFailedMsg [
        displayMessage(type: "danger" msg: oMsgEmailFailed)            
        gotoUc(contactPreferences)
    ]
    
    /* Displays a message that the email address is already in use. */
    action emailDuplicateMsg [
        displayMessage(type: "danger" msg: oMsgDuplicateEmail)            
        gotoUc(contactPreferences)
    ]
    
    /* Calls the method to process the auth code failure. */
    action processAuthCodeFailure [
    	auditLog(audit_profile.email_validation_failure)
    	    	
        switch AuthUtil.processAuthCodeFailure(sUserId)[
            case "success" incorrectAuthCodeMsg
            case "failure.lockout" lockoutAuthCodeFailure
            case "error" genericErrorMsg
            default genericErrorMsg         
        ]
    ]
    
    /* Display the incorrect auth code message on the email validation popin. */ 
    action incorrectAuthCodeMsg [   	
        fvalidationCode.pInput = ""
        displayMessage(type: "danger" msg: oMsgIncorrectAuthCode)
        gotoUc(contactPreferencesConsentEmail) 
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