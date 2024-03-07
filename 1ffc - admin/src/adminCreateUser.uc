useCase adminCreateUser [

	/**********************************************************************************************
	 *  File:               adminCreateUser.uc
	 *  Author:             Yvette Nguyen
	 *  Created:            2015-September-9
	 *  
	 *  Primary Goal:
	 *		User with System Administrator or Business Unit Administrator privileges wants to create a new user.
	 *
	 *  Alternative Outcomes:
	 *    	1. User entered incorrect information, or information that conflicts with another user. User is prompted
	 *         to enter correct information
	 *      2. User cancels the information and returns to the same page 
	 *        
	 *  Child use cases are:
	 *          None
	 * 
	 *  Minimal success guarantees:
	 *      The user has submitted valid data.
	 * 
	 *  Major Versions:
	 *  	2015-September-09 -- first version.
	 * 
	 **********************************************************************************************/
	

    documentation [
        preConditions: [[
            1. A System Administrator or Business Unit Administrator have logged into the system.
        ]]
        triggers: [[
            1. If no users exist in the system, System Administrator is brought to this page when
            ;   they log in.
            2. User chooses enroll from the main menu.
        ]]
        postConditions: [[
            1. Primary -- A new user has been created.
            2. Alternative 1 -- the transaction was abandoned or failed.
        ]]
    ]
	startAt actionInit												   
	
	/**************************
	 * DATA ITEMS SECTION
	 **************************/
 	actors [
	    create_user, 
	    create_admin,
	    create_org_admin
	]
 	           		
	importJava UserAccess(com.sorrisotech.uc.aaa.UserAccess)
	importJava CreateUser(com.sorrisotech.uc.admin1ffc.createadmin.Get1FFCAdminUser)	
    importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil)
    importJava UcUserAssignmentAction(com.sorrisotech.app.registration.UcUserAssignmentAction)
	importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava User1FFC(com.sorrisotech.uc.admin.createadmin.User1FFC)
	    
    /**********************************************************************************************
     * DECLARE VARIABLES
     *********************************************************************************************/	
    import validation.passwordRegex
    import validation.usernameRegex
    import validation.emailRegex
    
    serviceStatus srStatus			
    serviceParam(PwRestrict.Validate) srValidateReq
    serviceResult(PwRestrict.Validate) srValidateResp
    
    structure(message) msgPasswordInvalid  [
        string(title) sTitle = "{Insecure password}"
        string(body) sBody = "{The password appears to be a variation of a common password.  Please choose a password harder to guess.}"
    ]
    
    string sPageName = "{Create New User}"
    string sRequiredFields = "{Fields marked with * are mandatory}"
    string sUserHeading = "{Username & Password}" 
    string sProfileHeading = "{Profile}"
    string sAccessHeading = "{Access}"
    
    native string sAppNameSpace = AuthUtil.getAppNameSpace()
    native string sUserId = Session.getUserId()                       // ID of the current user
    native string sAppType = Session.getAppType()
    native string sBizCompanyId = Session.getUserCompanyId()		
	native string sUserRole                                           // current Role of the user who logs in the system 	
	native string sDataId									           // UserId of a newly created user.
	native string sBizUsername
	native string sBizPassword
	native string sBizPin	
	native string sAuthCode
	native string sCurrentTime
    native string sClassCol3 = "col-md-4 col-sm-6"
    native string sClassCol2 = "col-md-6 col-sm-6"
    native string sClass
    native string sDocumentController = "ROLE_ADMIN_DOCUMENT_CONTROLLER"
    
	field fUserName [								// Input field for User Name 
		string(label) sLabel = "{* User name:}"      
		input (control) pInput(usernameRegex, fUserName.sValidation)  
		string(required) sRequired = "{This field is required.}"
		string(validation) sValidation = "{User name can be upto 50 characters in length, contain no spaces, and include solely the following special characters: underscore (_), comma (,), period (.), atsign(@), and hyphen (-).}"
		string(error) sError = "{Another user already has this user name. Please select another.}"
    ]
	        
 	field fFirstName [								// Input field for First Name
        string(label) sLabel = "{* First name:}"        
        input(control) pInput("^[\\p{L}-_ ]{0,29}$", fFirstName.sValidation)
        string(required) sRequired = "{This field is required.}"        
        string(validation) sValidation = "{User first name must be 1-30 characters in length and include solely the following special characters: underscore (_) and hyphen (-).}"
    ] 
      
	field fLastName [								// Input field for Last Name 
	    string(label) sLabel = "{* Last name:}"        
        input(control) pInput("^[\\p{L}-_ ]{0,29}$", fLastName.sValidation)
        string(required) sRequired = "{This field is required.}"         
        string(validation) sValidation = "{User last name must be 1-30 characters in length and include solely the following special characters: underscore (_) and hyphen (-).}"  
	]
	
	field fPassword [								// Input field for Password
	    string(label) sLabel = "{* Password:}"        
        password input(control) pInput(passwordRegex, fPassword.sValidation)
        string(required) sRequired = "{This field is required.}"        
        string(validation) sValidation = "{Password must be 7-20 characters in length and include at least one uppercase character, one lowercase character, and one numeric digit. No spaces are allowed.}"
        string(validation) sRestricted = "{The password appears to be a variation of a common password.  Please choose a password harder to guess.}"  
         
	]	
	
	field fConfirmPassword [						// Input field for Confirm password
	    string(label) sLabel = "{* Confirm password:}"               
        password input(control) pInput = ""
        string(required) sRequired = "{This field is required.}"                
        string(error) sError = "{Password and Confirm password fields should match.}" 
	]	
	
	field fPhoneNumber [
        string(label) sLabel = "{* Phone number:}"        
        input(control) pInput("^1?[\\s-]?\\(?(\\d{3})\\)?[\\s-]?\\d{3}[\\s-]?\\d{4}$", fPhoneNumber.sValidation)        
        string(validation) sValidation = "{Entry must be numeric and appear in the standard telephone number format: XXX-XXX-XXXX.}" 
        string(required) sRequired = "{This field is required.}"         
    ]
       
   	field fMobileNumber [
        string(label) sLabel = "{Mobile number:}"        
        input(control) pInput("^1?[\\s-]?\\(?(\\d{3})\\)?[\\s-]?\\d{3}[\\s-]?\\d{4}$", fMobileNumber.sValidation)        
        string(validation) sValidation = "{Entry must be numeric and appear in the standard telephone number format: XXX-XXX-XXXX.}" 
    ]
    
	field fEmailAddress [							// Input field for email
	    string(label) sLabel = "{* E-mail address:}"
        input(control) pInput(emailRegex, fEmailAddress.sValidation)
        string(validation) sValidation = "{Please provide a valid e-mail address. Your e-mail address may contain up to 50 characters and must appear in the standard e-mail address format: name@example.com. }"
        string(required) sRequired = "{This field is required.}" 
        string(error) sError = "{This e-mail address is already used by another user, please use different one.}" 
	]	
	
	field fRole [									// Input field for role
	    string(label) sLabel = "{* Role :}"		                   
	    auto dropDown(control) dRoleLists = Role_Admin_OrganizationAdmin   [	        	
        	CreateUser.populateRole(sUserRole)
	    ]        
	]	

/*
	field fBusinessUnit [							// Assigned business unit , only available to Sys Admin            
	    string(label) sLabel = "{* Business Unit :}"	               
	    auto dropDown(control) dBusinessUnitLists [
		    selectUnit1: "{Business Unit 1}"
		    selectUnit2: "{Business Unit 2}"
		    selectUnit3: "{Business Unit 3}"
	    ]        
	]	
*/
  
	field fDescription [							// Input field for Description
	    string(label) sLabel = "{Description:}"	        
		input (control) pInput("^[ \\p{L}\\d\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-\\=\\+\\;\\:\\'\\,\\.\\/\\?]{0,250}$", fDescription.sValidation) 	 
		string(validation) sValidation = "{Description can be up to 250 characters. They may contain letters, " +
										  "numbers and any of the following symbols ! @ # $ % ^ & * () _ - = + ; : ' , . / ?}" 
	]    
 
    // -- message strings for display when use case completes. 
    structure(message) msgDuplicate [
        string(title) sTitle = "{Duplicate user name}"
        string(body) sBody = "{The user name has already been used, please try a different user name.}"
    ]	
    
    structure(message) msgPasswordMatchFailure [
        string(title) sTitle = "{Input error}"
        string(body) sBody = "{The password and confirm password values do not match.}"
    ]    
    
    structure(message) msgFailure  [
        string(title) sTitle = "{Failure}"
        string(body) sBody = "{There was an internal error enrolling the user, please contact your System Administrator.}"
    ]
    
   structure(message) msgSuccess  [
        string(title) sTitle = "{Success}"
        string(body) sBody = "{You have been successfully enrolled.}"
    ]    

	structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
	]
	
    /**************************************************************************
     * Main Path.
     **************************************************************************/ 
         
    /*****************************************************************
     *  1. System loads the user's current role. Information is used
     * 		to populate fRole dropdown.
     */  
    action actionInit [
 //   	User1FFC.addRole(sDocumentController, "1")
    	
        loadProfile (
 			user_Role    : sUserRole
 		) 
 				
 		goto(verifyApp)	       
    ]      
    
    /*****************************************************************
     * 2. Verify if user or admin app. 
     */
    action verifyApp [
    	if "saas.user.namespace" == sAppNameSpace then 
    		assignCol2
    	else
    		assignCol3
    ]
    
    /*****************************************************************
     * 3. If user app assign the 2 column class. 
     */
    action assignCol2 [
    	sClass = sClassCol2
    	goto(adminCreateUser)	
    ]
    
    /*****************************************************************
     * 4. If admin app assign the 3 column class. 
     */
    action assignCol3 [
    	sClass = sClassCol3
    	goto(adminCreateUser)	
    ]
        
    /*************************************************************
     *  5. System Displays create user screen where user enters
     * 		user information.
     */  
    xsltFragment adminCreateUser("Create User") [
        
        form main [
            class: "modal-content st-create-user"

			div heading [ 
				class: "modal-header"
				
	            div headingRow [
	            	class: "row"
	            	
	            	h4 headingCol [
	            		class: "col-md-12"
	            		
						display sPageName 
					]
				]
	        ]
	                                 
			div formCreateUser [
                class: "modal-body"
								
		        div row0 [
	            	class: "row mandatory st-padding-top20"
	            	div notice[
						class: "col-md-12"					
						display sRequiredFields
					]	
	            ]
					
				div contentRow [
                	class: "row"
                    
	                div formContentCol1 [       
	            		class: sClass
	            		 logic: [
					    	if "saas.user.namespace" == sAppNameSpace then "remove"
					    ]
									    
	            		div userWell [
	            			class: "well well-sm"	
	            		
				            div userHeading [
								class: "row"
								
								div userHeadingCol [
									class: "col-md-12"
				                
					                display sUserHeading [
					                    type: "h4"
					                ]
								]
				            ]
		            		
			            	div userRow1 [
				            	class: "row"
				            	
				            	div userField1 [
				            		class: "col-md-12"
				            		
					        		display fUserName [
									    control_attr_tabindex: "20"
									    control_attr_autofocus: ""
									    control_attr_st-new-user-name: ""
									    sError_ng-show: "main['fUserName.pInput'].$invalid && !!main['fUserName.pInput'].$error.username && main['fUserName.pInput'].$dirty"
									    sError_attr_sorriso-error: "new-user-name"										    
									]
				        		]
			        		]
			        		
			            	div userRow2 [
				            	class: "row"
				            	
				            	div userField2 [
				            		class: "col-md-12"
				            		
					        		display fPassword [
			            				control_attr_tabindex: "21"				            				
			                    		control_attr_st-new-password: "main['fUserName.pInput'].$viewValue"
			                    		control_attr_st-restricted-password: "#fUserName"
			                    		sRestricted_ng-show: "main['fPassword.pInput'].$invalid && !!main['fPassword.pInput'].$error.stNewPassword && main['fPassword.pInput'].$dirty"
			                    		sRestricted_attr_sorriso-error: "restricted-password"
			            			]
				        		]
				        	]
				        	
				        	div userRow3 [
				            	class: "row"
				            	
				            	div userField3 [
				            		class: "col-md-12"
				            		
					        		display fConfirmPassword [
					                    pInput_attr_st-compare-to: "form.fPassword"
        								pInput_attr_st-same-as: 'fPassword'
					                    sError_ng-show: "!main.$error.stPattern && !!main.$error.stCompareTo"
        								sError_attr_sorriso-error: 'same-as'
					                    control_attr_tabindex: "22"						                    
					                ]
				        		]
				        	]				        		
		        		]
		        	]
		        	
		        	div formContentCol2 [       
	            		class: sClass
	            				            		
	            		div profileWell [
	            			class: "well well-sm"	
	            		
				            div profileHeading [
								class: "row"
								
								div profileHeadingCol [
									class: "col-md-12"
				                
					                display sProfileHeading [
					                    type: "h4"
					                ]
								]
				            ]
		            		
			            	div profileRow1 [
				            	class: "row"
				            	
				            	div profileField1 [
				            		class: "col-md-12"
				            		
					        		display fFirstName [
			                			control_attr_tabindex: "23"
			                		]
				        		]
			        		]
			        		
			            	div profileRow2 [
				            	class: "row"
				            	
				            	div profileField2 [
				            		class: "col-md-12"
				            		
					        		display fLastName [
			                			control_attr_tabindex: "24"
			                		]
				        		]
				        	]
				        	
				        	div profileRow3 [
			                    class: "row"
			                    
			                    div profileField3 [
			                        class: "col-md-12"
			                        display fPhoneNumber [
			                        	control_attr_tabindex: "25"
			                        ]
				                ]
			                ]	 
			                
							div profileRow4 [
								class: "row"
								
								div profileField4 [
									class: "col-md-12"
					                display fMobileNumber [
					                	control_attr_tabindex: "26"
					                ]
				                ]	 
							]		
			
				        	div profileRow5 [
				            	class: "row"
				            	
				            	div profileField5 [
				            		class: "col-md-12"
				            		
					        		display fEmailAddress[
										pInput_attr_new-email-address: ""
										pInput_attr_st-new-email: ""
										control_attr_tabindex: "27"
										sError_ng-show: "main['fEmailAddress.pInput'].$invalid && !!main['fEmailAddress.pInput'].$error.email && main['fEmailAddress.pInput'].$dirty"
										sError_attr_sorriso-error: "new-email"
									]
				        		]
				        	]				        		
		        		]
		        	]
		        	
		        	div formContentCol3 [       
	            		class: sClass
	            		
	            		div accessWell [
	            			class: "well well-sm"	
	            		
				            div accessHeading [
								class: "row"
								
								div accessHeadingCol [
									class: "col-md-12"
				                
					                display sAccessHeading [
					                    type: "h4"
					                ]
								]
				            ]
		            		
			            	div accessRow1 [
				            	class: "row"
				            	
				            	div accessField1 [
				            		class: "col-md-12"
				            		
					        		display fRole [
			                			control_attr_tabindex: "28"
			                		]
				        		]
			        		]
			        		
			            	div accessRow2 [
				            	class: "row"
				            	
				            	div accessField2 [
				            		class: "col-md-12"
				            		
					        		display fDescription [
			                			control_attr_tabindex: "29"
			                		]
				        		]
				        	]					        					        		
		        		]
		        	]			        								        		
	        	]
            ]                       
			
		    // Display buttons: Create and Cancel
            div buttons [
            	class: "modal-footer"
                
                navigation createUserSubmit(verifyPasswordIfCsr, "{Create}") [
                    class: "btn btn-primary"
					data: [fUserName, fFirstName, fLastName, fPassword, fConfirmPassword, fPhoneNumber, fMobileNumber, fEmailAddress, fRole, fDescription]
                    require: [fUserName, fFirstName, fLastName, fPassword, fConfirmPassword, fPhoneNumber, fEmailAddress, fRole]
                    attr_tabindex: "30"                            
               	]
            	
                navigation createUserCancel(createUserCancel, "{Cancel}") [
                    class: "btn btn-secondary"
                    attr_tabindex: "31"
                ]                            
            ]
        ]
    ]

    /*************************************************************
     * 6. User chooses to create user. This action determines the flow
     *  to be followed based on the namespace
     */  
    action verifyPasswordIfCsr [
    	if sAppNameSpace == "saas.csr.namespace" then
    		verifyPassword
    	else
    		generateBizUserCredentials  		
       
    ]
    
    action verifyPassword [
		srValidateReq.rFor      = sAppType
		srValidateReq.rUserName = fUserName.pInput
		srValidateReq.rPassword = fPassword.pInput
		
		switch apiCall PwRestrict.Validate(srValidateReq, srValidateResp, srStatus) [
		    case apiSuccess checkVerifyResult
		    default verifyPasswordFailed
		]
				    	
    ]
    
    action checkVerifyResult [
		if srValidateResp.valid == "YES" then 
			createUserAction 
		else 
			verifyPasswordFailed    	
    ]
    
    action verifyPasswordFailed [
    	displayMessage(type:"danger" msg: msgPasswordInvalid)
    	gotoUc(adminListUsers)
    ]
    

    /*************************************************************
     * 7. User chooses to create user.  System confirms that both
     * 		passwords are the same
     * 
     *   Note: this is also done client side but we need to validate
     * 		server side in case somebody bypasses java script. 
     */  
    action createUserAction [
    	if fPassword.pInput == fConfirmPassword.pInput then
    		verifyRole
    	else
    		passwordsDontMatch  		       
    ]  
    
    action verifyRole [ 
    	switch CreateUser.hasRole(fRole.dRoleLists) [
    		case "yes" performEnrollment
    		case "no" enrollmentFailure
    		default enrollmentFailure
    	]
    ]
    
     /************************************************************
     * 8. System enrolls the basic user.
     */
	action performEnrollment [
		CreateUser.hasRole(fRole.dRoleLists)
		
		sDataId = enroll(
					username 	 : fUserName.pInput
					password 	 : fPassword.pInput					
					namespace    : sAppNameSpace
					role		 : fRole.dRoleLists
			      )

		if success then 			updateUserProfileAction
        if duplicateUsername then 	errorDuplicate
        if failure then 			enrollmentFailure
		
	]   
	
	/**********************************************************
	 *  9. System updates the user's profile with information
	 * 		from the screen.
	 */ 
	action updateUserProfileAction [	
		updateProfile(
			userId 		 		: sDataId
 			firstName    		: fFirstName.pInput
 			lastName     		: fLastName.pInput
 			phoneNumber 		: fPhoneNumber.pInput
 			mobileNumber		: fMobileNumber.pInput
 			emailAddress 		: fEmailAddress.pInput 			
 //			businessUnit 		: fBusinessUnit.dBusinessUnitLists
 			description  		: fDescription.pInput
 			active		 		: "true"
 			accountStatus		: "open"
 			securityImage		: "24"
 			registrationStatus 	: "firstLogin" 
 			appType		 		: "csr"
 			secretQuestion1 	: "q1"
 			secretQuestion2 	: "q2"
 			secretQuestion3 	: "q3"
 			secretQuestion4 	: "q4"
 			secretQuestionAnswer1 : "111111"
 			secretQuestionAnswer2 : "111111"
 			secretQuestionAnswer3 : "111111"
 			secretQuestionAnswer4 : "111111"
 			csrResetPasswordFlag : "false"
		)
		                		
		if success then  sendConfirmationEmail
		if failure then  enrollmentFailure
 	]

	/**************************************************************************
     * 10. System sends the user a confirmation email stating that their 
     * enrollment is complete.     
     */     
    action sendConfirmationEmail [    	
    	switch CreateUser.sendConfirmationEmail(sDataId, fPassword.pInput) [        
            case "success" enrollmentSucceeded
            case "error"  enrollmentFailure  
            default enrollmentFailure         
        ]      	
    ]
 
    /**************************************************************************
     * 11. The enrollment succeeded, display a message.
     */
    action enrollmentSucceeded [    
    	auditLog(audit_admin.createUser_success) [
    		fUserName.pInput   		
    	]
    	
        displayMessage(type:"success" msg: msgSuccess)
        gotoUc(adminListUsers)                
    ]
    
    /**************************************************************************
     * Alternative Paths
     ************************************************************************** */

    /*********************************************************
     * 5A. User selects cancel button to cancel the registrations
     */ 
    action createUserCancel [    
    	auditLog(audit_admin.createUser_cancel)
    	
    	gotoUc(adminListUsers)
    ]  
    
    /*********************************************************
     * 7A. Password mismatch, display warning message
     */
    action passwordsDontMatch [    
        displayMessage(type: "danger" msg: msgPasswordMatchFailure)
        goto(adminCreateUser)        
    ]      
    
    /**************************************************************************
     * 8A. The enrollment failed due to duplicate user, 
     * 		display a message.
     */
    action errorDuplicate [
    	auditLog(audit_admin.createUser_duplicate) [
    		fUserName.pInput   		
    	]  
    	  	
        displayMessage(type: "warning" msg: msgDuplicate)
        fUserName = ""
        goto(adminCreateUser)                
    ] 
    
	/**************************************************************************
     * 8B. The enrollment failed, display a message.
     */
    action enrollmentFailure [   
    	auditLog(audit_admin.createUser_failure) [ fUserName.pInput ]
        displayMessage(type: "danger" msg: msgFailure)                
        goto(adminCreateUser)                
    ]
    
    
    /**************************************************************************
     * 6A. [B2B-UserApp] The enrollment failed, display a message.
     */
    action generateBizUserCredentials [
    	CreateUser.setupTemporaryCredentials(sBizUsername, sBizPassword, sBizPin)
     	goto(createBizUserAction)  		
       
    ]    
    
    /**************************************************************************
     * 6. [B2B-UserApp] Create new consumer user
     */
    action createBizUserAction [
    	sDataId = enroll(
					username 	 : sBizUsername
					password 	 : sBizPassword					
					namespace    : "saas.user.namespace"
					role		 : fRole.dRoleLists
			      )

		if success then 			updateBizUserProfileAction
        if duplicateUsername then 	regenerateUsername
        if failure then 			enrollmentFailure
     ]
     
     /**************************************************************************
     * 6. [B2B-UserApp] update the profile for new consumer user
     */
     action updateBizUserProfileAction [	
		updateProfile(
			userId 		 		: sDataId
 			firstName    		: fFirstName.pInput
 			lastName     		: fLastName.pInput
 			phoneNumber 		: fPhoneNumber.pInput
 			mobileNumber		: fMobileNumber.pInput
 			emailAddress 		: fEmailAddress.pInput
 			description         : fDescription.pInput
 			active		 		: "true"
 			accountStatus		: "open"
 			securityImage		: "24"
 			registrationStatus 	: "pending" 
 			appType		 		: "b2b"
 			secretQuestion1 	: "q1"
 			secretQuestion2 	: "q2"
 			secretQuestion3 	: "q3"
 			secretQuestion4 	: "q4"
 			secretQuestionAnswer1 : "111111"
 			secretQuestionAnswer2 : "111111"
 			secretQuestionAnswer3 : "111111"
 			secretQuestionAnswer4 : "111111"
		)
		                		
		if success then  enrollmentSucceededBiz
		if failure then  enrollmentFailure
 	]
 	
 	
 	/**************************************************************************
     * 6. [B2B-UserApp] Add the consumer user to company
     */
    action enrollmentSucceededBiz [    
    	auditLog(audit_admin.createUser_success) [
    		fUserName.pInput   		
    	]
    	
        switch UcUserAssignmentAction.assignOrgAdminWithNewCompany(sDataId, sBizCompanyId) [        
            case "success" checkBizUserForAccess
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]             
    ]
    
    /**************************************************************************
     * 7. [B2B-UserApp] Check if the user being created is OrgAdmin
     */
    action checkBizUserForAccess [
    	
    	if fRole.dRoleLists == "Role_Biz_OrganizationAdmin"
    	then 
    		assignAccessBizOrgAdmin
    	else	
    		generateAuthCode
    ]
    
    /**************************************************************************
     * 8. [B2B-UserApp] setup access tag-chain
     */
    action assignAccessBizOrgAdmin [
    	
    	UserAccess.setupAccess(sDataId, sBizCompanyId, sUserId)
    	
    	goto(generateAuthCode)
    ]
    
    
    
    /**************************************************************************
     * 9. [B2B-UserApp] get the auth code
     */
    action generateAuthCode [    	  	   	
        switch AuthUtil.generateAuthCode(sAuthCode) [        
            case "success" getCurrentTimeStamp
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ] 
    
    /**************************************************************************
     * 10. [B2B-UserApp] Get current time     
     */
    action getCurrentTimeStamp [    
        switch AuthUtil.getCurrentTime(sCurrentTime) [        
            case "success" saveAuthCodeDetails
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ]  
    
   /**************************************************************************
     * 11. [B2B-UserApp] Save auth code details     
     */
    action saveAuthCodeDetails [
    	 updateProfile(        	
        	userId: sDataId    
        	authCode: sAuthCode     
        	authCodeCreationTimestamp: sCurrentTime  	
            )
        if success then sendValidationEmail
        if failure then deleteUserProfile  
    ]    
    
    /**************************************************************************
     * 12. [B2B-UserApp] Send validation email     
     */
    action sendValidationEmail [    
        switch NotifUtil.sendOrgAdminRegistration(sDataId, sBizUsername, sBizPassword, sAuthCode, fFirstName.pInput, fLastName.pInput) [        
            case "success" gotoBizUserList
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ]   
    
    /**************************************************************************
     * 13. [B2B-UserApp] Return to user list page
     */
	action gotoBizUserList [
        displayMessage(type:"success" msg: msgSuccess)
        gotoUc(adminListUsers)
    ]
    
    /**************************************************************************
     * [B2B-UserApp] delete user profile 
     */
    action deleteUserProfile [    
    	auditLog(audit_admin.createUser_failure)  [
    		primary: sDataId
    		secondary: sDataId
    	]	
    	
        deleteUser(
            userId: sDataId           
            namespace: sAppNameSpace            
            )            
        if success then genericErrorMsg      
        if failure then genericErrorMsg    
    ]	
    
    /**************************************************************************
     * [B2B-UserApp] Display generic error message 
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)        
		gotoModule(LOGIN)
	]
	
	/**************************************************************************
     * [B2B-UserApp] Generate new username 
     */
	action regenerateUsername [
       
       CreateUser.regenerateUsername(sBizUsername)
        
        goto(createBizUserAction)                
    ]
    
 ]