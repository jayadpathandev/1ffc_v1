useCase fffcReg99B2C [

    /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 01-Oct-2015
    *
    *  Primary Goal:
    *       1. System performs the enrollment and update user profile. 
    *       
    *  Alternative Outcomes:
    *       1. 
    *                     
    *   Major Versions:
    *        1.0 1-Oct-2015 First Version Coded [Maybelle Johnsy Kanjirapallil]
    */
    

    documentation [
        preConditions: [[
            1. A user accesses the application
        ]]
        triggers: [[
            1. Users enters correct information on the Registration - Setup Email address
            ; and selects next button
        ]]
        postConditions: [[
            1. Primary -- System displays Registration - Validate Email Address screen
            ;
        ]]
    ]
	startAt resetFlags

	/**************************
	 * DATA ITEMS SECTION
	 **************************/
	
	importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil) 
	importJava CreateSaml(com.sorrisotech.app.^library.saml.CreateSaml)
	importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava UcBillRegistration(com.sorrisotech.uc.billstream.UcBillRegistration) 
	importJava UserProfile(com.sorrisotech.app.utils.UserProfile) 
	importJava FffcRegistration(com.sorrisotech.fffc.auth.FffcRegistration)
	importJava Config(com.sorrisotech.utils.AppConfig)
	   
	import regChecklist.sAccountInfoFailed
	import regChecklist.sUserNameFailed
	import regChecklist.sEmailFailed
	
	import fffcReg01EmailTnC.sGeolocation
	import fffcReg03ElectronicTnC.sPaperLessOption
	    	
	import fffcReg05BillingInfo.sSelectedBillStream
	import fffcReg05BillingInfo.fAccountNumber
	import fffcReg05BillingInfo.fBillingDate
	import fffcReg05BillingInfo.fAmount
	import fffcReg05BillingInfo.fSelfReg0
	import fffcReg05BillingInfo.fSelfReg1
	import fffcReg05BillingInfo.fSelfReg2
	import fffcReg05BillingInfo.fSelfReg3
	import fffcReg05BillingInfo.fSelfReg4
		
	import fffcReg06LoginInfo.fUserName
	import fffcReg06LoginInfo.fFirstName
	import fffcReg06LoginInfo.fLastName
		
	import fffcReg07SecretQuestion.dSecretQuestion1
	import fffcReg07SecretQuestion.dSecretQuestion2
	import fffcReg07SecretQuestion.dSecretQuestion3
	import fffcReg07SecretQuestion.dSecretQuestion4
	import fffcReg07SecretQuestion.fSecretAnswer1
	import fffcReg07SecretQuestion.fSecretAnswer2
	import fffcReg07SecretQuestion.fSecretAnswer3
	import fffcReg07SecretQuestion.fSecretAnswer4
	
	import fffcReg08PersonalImage.imageId
	import regContactInfo.fUserEmail
	import regContactInfo.fUserEmailRetype
	import regContactInfo.fMobileNumber
	import regContactInfo.fPhoneNumber
				
	import utilIsUserNameAvailable.sReqWorkflow
					
	native string sUserId	     
    native string sAuthCode
	native string sCurrentTime
	native string sUserAccountId =  UcBillRegistration.getUserAccountId()		
	native string sNameSpace = CreateSaml.getNameSpace()	
	native string sEmailChannel = "email"
	native string sSmsChannel = "sms"
    native string sPortalChannel = "portal"
	native string sOrgId
	native string sIpAddress = Session.getExternalIpAddress()
    native string sCategory = "terms_and_conditions "
    native string sType = "web_site "
    native string sOperation = "User signed consent related to terms and conditions during registration process."
    native string bIsConsentActive = "true"
    native string sAppType = Session.getAppType()
    native volatile string sCurrentEpochTime = FffcRegistration.getCurrentEpochTime()
	
    serviceParam (Profile.AddLocationTrackedEvent) setLocationData
	serviceResult (Profile.AddLocationTrackedEvent) setLocationResp
    
    serviceStatus status
	serviceParam(Notifications.SetUserAddress) setData
	serviceParam(Notifications.RegisterUser)   setRegData
	serviceParam(Notifications.GetUserAddresses) getData
	serviceResult(Notifications.GetUserAddresses) getResponse
	serviceParam(Notifications.GetContactSettings) getContactData
	serviceResult(Notifications.GetContactSettings) getContactResponse
	serviceParam(Notifications.ChangeTopicState) setTopicStateData
	
	
	serviceParam(FffcNotify.SetUserAddressNls) setDataFffc
	serviceParam(FffcNotify.RegisterUserNls) setDefaultDataFffc
	
	// -- using status call to retrieve accounts for registration from
	//		the status feed and make certain they are ALL in tm_accounts --
   serviceStatus srGetRegistrationAcctsCode
   serviceParam (AccountStatus.GetAccountsForRegistration) srGetRegistrationAcctsParams
   serviceResult (AccountStatus.GetAccountsForRegistration) srGetRegistrationAcctsResult
   native string sStatusPaymentGroup = Config.get("1ffc.ignore.group")
   native string sBillPaymentGroup = Config.get("1ffc.bill.group")
   native string sAcctForReg = UcBillRegistration.getAccountNum()
   native string sNtfParams = ""
 	
    // -- message strings for display when use case completes. 			
    structure(message) msgDuplicateAccount [    
        string(title) sTitle = "{Failure}"
        string(body) sBody = "{Sorry, your registration cannot be completed at this time. Another user has already registered with this account. Please contact Customer Service if you require further assistance.}"
    ]
  
    structure(message) msgDuplicateUser [    
        string(title) sTitle = "{Failure}"
        string(body) sBody = "{The user name has already been enrolled, try another one.}"
    ]
    
    structure(message) msgDuplicateEmail [    
        string(title) sTitle = "{Duplicate E-mail Address}"
        string(body) sBody = "{This e-mail address has been used. Please enter a different one.}"
    ]
    
    structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
	]

    structure(message) msgNlsUnvailableError [
		string(title) sTitle = "{Your registration cannot be completed at this time.}"
		string(body) sBody = "{An error occurred while trying to complete your online registration. Please wait a few hours and try again. If the issue persists, please contact 1st Franklin Support.}"
	]
	
	    /**************************************************************************
     * Main Path.
     **************************************************************************/   
       
    /**************************************************************************
	 * 1. System reset all flags to false.
	 */    
     action resetFlags [
    	sAccountInfoFailed = "false"
		sUserNameFailed = "false"
		sEmailFailed = "false"
		goto(verifyAccountDetails)
    ]		
        
	/**************************************************************************
	 * 2. Check if another user has already registered the account number between 
	 *    the time the user entered the account information and now.
	 */
	 action verifyAccountDetails [   				 
		UcBillRegistration.init(sSelectedBillStream, fAccountNumber.pInput, fBillingDate.aDate, 
		 	                     fAmount.pInput, fSelfReg0.pInput, fSelfReg1.pInput, 
		 	                     fSelfReg2.pInput, fSelfReg3.pInput, fSelfReg4.pInput)
		 	                     	 	
		switch UcBillRegistration.isAccountAvailable() [		 
		 	case "success" verifyUserName
		 	case "duplicate_account" resetBillInfoFields 						
			default genericErrorMsg
		]
	]     

	/**************************************************************************
	 * 3. Checks if another user has already registered the user name between the 
	 *    time the user chose a user name and now.
	 */     
    action verifyUserName [    
        switch UserProfile.isUserNameAvailable(fUserName.pInput, sNameSpace) [        
            case "yes" verifyEmail
            case "no"  resetLoginInfoFields
            default genericErrorMsg
        ]
    ]      

    /**************************************************************************
	 * 4. Checks if another user has registered that email address since we 
	 * last checked.
	 */     
    action verifyEmail [    
        switch UserProfile.isAddressAvailable(sNameSpace, "email", fUserEmail.pInput) [         
            case "yes" areWeHijacking
            case "no"  resetContactInfoFields
            default genericErrorMsg
        ]
    ]

    /**************************************************************************
     * 5a. See if we are hijacking an existing account.
     */
    action areWeHijacking [
    	FffcRegistration.hijack_user(sUserAccountId, sUserId)
    	
    	if sUserId == "" then
    		getAccountsForUserRegistration 
    	else
    		performHijack
    ]
    
    /**************************************************************************
     * 5b. Change the user name.
     */
    action performHijack [
 		updateAuthentication (
 			userId:   sUserId
            username: fUserName.pInput
 		)		
 
        if success then addProfileDetails
        if duplicateUsername then resetLoginInfoFields
        if failure then deleteUserProfile
    ]
	
   /**************************************************************************
     * 5b. pull in list of eligible accounts for this organization
     */
	action getAccountsForUserRegistration[
		srGetRegistrationAcctsParams.statusPaymentGroup = sStatusPaymentGroup
		srGetRegistrationAcctsParams.billPaymentGroup = sBillPaymentGroup
		srGetRegistrationAcctsParams.account = sAcctForReg
		switch apiCall AccountStatus.GetAccountsForRegistration(srGetRegistrationAcctsParams, 
														 srGetRegistrationAcctsResult,
														 srGetRegistrationAcctsCode ) [
			case apiSuccess performEnrollment
			default deleteUserProfile
		]
	] 	
	    
    /**************************************************************************
     * 5. Everything looks good, perform the enrollment.
     */
    action performEnrollment [
        sUserId = enroll(
            username: fUserName.pInput
            password: "7c289e59-7f6d-4a6c-8254-8185c4ad69ad"
            namespace: sNameSpace
            role: "Role_Consumer_EndUser"
            )            
        if success then addProfileDetails
        if duplicateUsername then resetLoginInfoFields
        if failure then deleteUserProfile
    ]   

    /**************************************************************************
     * 6. Save profile details.     
     */
    action addProfileDetails [       	
    	Session.setUserId(sUserId) 
    	auditLog(audit_registration.registration_success)[
    		primary: sUserId
    		secondary: sUserId
    		fUserName.pInput
    	]
    	
        updateProfile(
        	userId: sUserId        	
        	firstName: fFirstName.pInput
        	lastName: fLastName.pInput        	
        	appType: sAppType
            phoneNumber: fPhoneNumber.pInput
            secretQuestion1: dSecretQuestion1
        	secretQuestion2: dSecretQuestion2
        	secretQuestion3: dSecretQuestion3
            secretQuestion4: dSecretQuestion4
            secretQuestionAnswer1: fSecretAnswer1.pInput
        	secretQuestionAnswer2: fSecretAnswer2.pInput
        	secretQuestionAnswer3: fSecretAnswer3.pInput
            secretQuestionAnswer4: fSecretAnswer4.pInput
            securityImage: imageId   
            eSignConsentEnabled: "true"
            eSignConsentLastUpdatedTimeStamp: sCurrentEpochTime   
            )
        if success then saveEmailAddress
        if failure then deleteUserProfile    
    ]  
    
    /**************************************************************************
     * 7. Initialize user's contact preferences - email.     
     */
	action saveEmailAddress [
		setData.userid = sUserId
		setData.channel = sEmailChannel
		setData.address = fUserEmail.pInput
	    switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess isSmsAddressEmpty
		    default deleteUserProfile
		]
	]

    /**************************************************************************
     * Check if sms address is empty, if not then update sms address    
     */	
	action isSmsAddressEmpty [
		if fMobileNumber.pInput == "" then
		   enableNotifications
		else
		   saveSmsAddress
	]	
	
	/**************************************************************************
     * 8. Initialize user's contact preferences - sms.     
     */
	action saveSmsAddress [
    	setData.userid = sUserId
    	setData.channel = sSmsChannel
    	setData.address = fMobileNumber.pInput
        switch apiCall Notifications.SetUserAddress(setData, status) [
		    case apiSuccess enableNotifications
		    default deleteUserProfile
		]		
	]
	
    /**************************************************************************
     * 9a. System links the user with their account.     
     */
	action enableNotifications [
		setRegData.userid = sUserId
	    switch apiCall Notifications.RegisterUser(setRegData, status) [
		    case apiSuccess setPaperBillNotification
		    default deleteUserProfile
		]
	]

    /**************************************************************************
     * 9b. Set paperbill on or off     
     */
     action setPaperBillNotification [
		setTopicStateData.userid = sUserId
		setTopicStateData.topic = "paperless"
		setTopicStateData.channel = sEmailChannel
		setTopicStateData.option = sPaperLessOption
        switch apiCall Notifications.ChangeTopicState(setTopicStateData, status) [        
            case apiSuccess assignUserToAccountWithNewCompany
            default deleteUserProfile    
		]
     ]
		       
    /**************************************************************************
     * 9c. System links the user with their account.     
     */
    action assignUserToAccountWithNewCompany [    
        switch FffcRegistration.assignUserToAccountWithNewCompany(sUserAccountId, sUserId, sOrgId) [        
            case "success" setAccountStatus
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ]
    
    /**************************************************************************
     * 10. Set status of user account.     
     */
    action setAccountStatus [    
        updateProfile(        	
        	userId: sUserId
        	fffcCustomerId: sOrgId   
        	accountStatus: "open"        	
        	registrationStatus: "pending" 
            )
        if success then addLocationTrackedEvent
        if failure then deleteUserProfile    
    ]
    
    /**************************************************************************
	 *  11. Adding user geolocation tracked events.
	 */
    action addLocationTrackedEvent [
    	setLocationData.sUser = sUserId
    	setLocationData.sCategory = sCategory
    	setLocationData.sType = sType
    	setLocationData.sIpAddress = sIpAddress
    	setLocationData.sBrowserGeo = sGeolocation
    	setLocationData.sOperation = sOperation
    	
    	switch apiCall Profile.AddLocationTrackedEvent(setLocationData, setLocationResp, status) [
    		case apiSuccess getUserAddresses
    		default deleteUserProfile
    	]
    ]
    
    /**************************************************************************
	 *  12. Getting user addresses from database.
	 */
    action getUserAddresses [   
        getData.userid = sUserId
        switch apiCall Notifications.GetUserAddresses(getData, getResponse, status) [
		    case apiSuccess getDefaultContactPrefSetting
		    default deleteUserProfile
		]
    ]
    
    /**************************************************************************
	 *  13. Getting default contact preferences setting from database.
	 */
    action getDefaultContactPrefSetting [ 
    	getContactData.userid = sUserId
    	switch apiCall Notifications.GetContactSettings(getContactData, getContactResponse, status) [
		    case apiSuccess saveDefaultSettingNls
		    default deleteUserProfile
		]   
    ]
    
    /****************************************************************************************
	 *  14. Sending the user addresses and default contact preferences setting to NLS.
	 */
    action saveDefaultSettingNls [
//		Initialize.init()
		loadProfile(            
            fffcCustomerId: sOrgId   
            )
		setDefaultDataFffc.customerId = sOrgId
		setDefaultDataFffc.channelAddrJsonConfig = getResponse.jsonAddresses
		setDefaultDataFffc.contactPrefsJsonConfig = getContactResponse.jsonConfig
		setDefaultDataFffc.browserGeo = sGeolocation
    	setDefaultDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDefaultDataFffc.ipAddress = sIpAddress
    	setDefaultDataFffc.sConsentActive = bIsConsentActive
    	
	    switch apiCall FffcNotify.RegisterUserNls(setDefaultDataFffc, status) [
		    case apiSuccess saveRegConsentNls
		    default deleteNlsUserProfile
		]
	]
    
	/**************************************************************************
     * 15. Save user's registration consent. (NLS side).
     * As per discussion saving channel name as "portal" and channel 
     * address as email address.
     */
	action saveRegConsentNls [
		loadProfile(            
            fffcCustomerId: sOrgId   
            )
		setDataFffc.customerId = sOrgId
		setDataFffc.channel = sPortalChannel
		setDataFffc.address = fUserEmail.pInput
		setDataFffc.browserGeo = sGeolocation
    	setDataFffc.ipGeo = setLocationResp.IP_GEO
    	setDataFffc.ipAddress = sIpAddress
    	setDataFffc.sConsentActive = bIsConsentActive
    	
	    switch apiCall FffcNotify.SetUserAddressNls(setDataFffc, status) [
		    case apiSuccess generateAuthCode
		    default deleteNlsUserProfile
		]
	]  
    
    /**************************************************************************
     * 16. Generate authorization code.     
     */
    action generateAuthCode [    	  	   	
        switch AuthUtil.generateAuthCode(sAuthCode) [        
            case "success" getCurrentTimeStamp
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ] 
    
    /**************************************************************************
     * 17. Get current timestamp.     
     */
    action getCurrentTimeStamp [    
        switch AuthUtil.getCurrentTime(sCurrentTime) [        
            case "success" saveAuthCodeDetails
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ]  
    
    /**************************************************************************
     * 18. Save auth code details.     
     */
    action saveAuthCodeDetails [
    	 updateProfile(        	
        	userId: sUserId    
        	authCode: sAuthCode     
        	authCodeCreationTimestamp: sCurrentTime  	
            )
        if success then sendValidationEmail
        if failure then deleteUserProfile  
    ]    
    
    /**************************************************************************
     * 19. Send validation email.     
     */
    action sendValidationEmail [    
        switch NotifUtil.sendAuthCode(sUserId, sNameSpace, "b2c", fUserName.pInput, sAuthCode, fFirstName.pInput, fLastName.pInput) [         
            case "success" checkPaperlessEnabled
            case "error"  deleteUserProfile
            default deleteUserProfile
        ]   
    ]
    
    action checkPaperlessEnabled [    
           if sPaperLessOption == "true" then
           	  sendPaperlessDeliveryEmail
           else
           	   gotoRegValidateEmailAddress
    ]
    
    action sendPaperlessDeliveryEmail [
    	
    	sNtfParams = "firstName=" + fFirstName.pInput + "|" + 
					 "lastName=" + fLastName.pInput + "|" +
		             "paperBillingEnabled=" + sPaperLessOption 

		switch NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "profile_paper_billing") [
			case "success" gotoRegValidateEmailAddress
			default deleteUserProfile
		]
    ]   
    
    /**************************************************************************
	 * 20. Go to the registration validation email address usecase.
	 */
    action gotoRegValidateEmailAddress [
    	sReqWorkflow = ""
    	
        gotoUc(regValidateEmailAddress)
    ]          

    /**************************************************************************
     * Alternative Paths
     ***************************************************************************/
     
    /**************************************************************************
     * 2.1 Clears the registration billing fields for new inputs and sets 
     *     the flag.
     */
	action resetBillInfoFields [
		fAccountNumber.pInput = ""
        fBillingDate.aDate = ""  
        fAmount.pInput = ""  
        fSelfReg0.pInput = ""  
        fSelfReg1.pInput = ""  
        fSelfReg2.pInput = ""  
        fSelfReg3.pInput = ""  
        fSelfReg4.pInput = ""   
        sAccountInfoFailed = "true" 
        goto(duplicateAccountMsg) 
	]     
	
    /**************************************************************************
     * 2.2 Display duplicate account error message.
     */ 
    action duplicateAccountMsg [    
        displayMessage(type: "danger" msg: msgDuplicateAccount)                       
		   gotoUc(fffcReg05BillingInfo)           
    ]
        	  
	/**************************************************************************
     * 3.1 Clears the login info fields for new inputs and set the flag.
     * 5.1 Clears the login info fields for new inputs and set the flag.    
     */
	action resetLoginInfoFields [
		fUserName.pInput = ""  
		sUserNameFailed = "true"      
        goto(duplicateUserMsg) 
	] 
	
    /**************************************************************************
     * 4.1 Clears the contact info fields for new inputs and set the flag.
     */
	action resetContactInfoFields [
		fUserEmail.pInput = ""
        fUserEmailRetype.pInput = ""
		sEmailFailed = "true"      
        goto(duplicateEmailMsg) 
	]  

    /**************************************************************************
     * 4.2 Display the email not unique error message. 
     */    
    action duplicateEmailMsg [
        displayMessage(type: "danger" msg: msgDuplicateEmail)  
        gotoUc(fffcReg01EmailTnC)      
	]	
    
    /**************************************************************************
     * 8.2 Display duplicate user error message.
     */ 
    action duplicateUserMsg [    
        displayMessage(type: "danger" msg: msgDuplicateUser)    
        gotoUc(fffcReg06LoginInfo)                             
    ]
    	
    /**************************************************************************
     * Delete User Profile when something goes wrong.
     */    
    action deleteUserProfile [    
    	auditLog(audit_registration.registratrion_failure)  [
    		primary: sUserId
    		secondary: sUserId
    	]	
    	
  		FffcRegistration.delete_users_company(sUserId)
 		goto(genericErrorMsg)   	
    ]	
    
    /**************************************************************************
     * Delete User Profile due to the unavailability of the NLS system.
     */
    action deleteNlsUserProfile [    
    	// goto(generateAuthCode)
    	auditLog(audit_registration.registratrion_failure)  [
    		primary: sUserId
    		secondary: sUserId
    	]	

  		FffcRegistration.delete_users_company(sUserId)
 		goto(nlsUnavailableErrorMsg)   	
    ]		

    /**************************************************************************
     * Display generic error message. 
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)        
		gotoModule(LOGIN)
	]
	
    /**************************************************************************
     * Display generic error message due to the unavailability of Nls 
     */
	action nlsUnavailableErrorMsg [
		displayMessage(type: "danger" msg: msgNlsUnvailableError)        
		gotoModule(LOGIN)
	]
	
 ]