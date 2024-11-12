useCase apiGetAutomaticPaymentRuleForAgent
[
	startAt init

	shortcut getAutomaticPaymentRuleForAgent(init) [
		securityToken
		customerId
		accountId
	]

	importJava Log(api.Log)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava Enroll(com.sorrisotech.fffc.agent.pay.Enroll)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
    native string securityToken
    native string customerId
    native string accountId
    native string sDeleteAutoHistoryConfigChange = "Recurring payment deleted."
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus                 	   ssStatus
    serviceParam (AgentPay.GetAutoPay) spGetAutoPay
    serviceResult(AgentPay.GetAutoPay) srGetAutoPay

    // -- using status call to retrieve accounts for registration from
	//		the status feed and make certain they are ALL in tm_accounts --
    serviceStatus ssAcctsReg
    serviceParam (AccountStatus.GetAccountsForRegistration) spAcctsReg
    serviceResult (AccountStatus.GetAccountsForRegistration) srAcctsReg
    native string sStatusPayGroup = Config.get("1ffc.ignore.group")
    native string sBillPayGroup = Config.get("1ffc.bill.group")
    
    serviceParam (Payment.GetWalletByToken) srGetWalletInfoParam
	serviceResult (Payment.GetWalletByToken) srGetWalletInfoResult

   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Verify that the securityToken was provided.
	 */
    action init[
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [securityToken]."
    	sErrorCode   = "no_security_token"
    	
    	if securityToken != "" then 
    		verifyCustomerId
    	else
    		actionFailure 
    ]

	/*************************
	 * 2. Verify the customerId was provided.
	 */
    action verifyCustomerId[
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [customerId]."
    	sErrorCode   = "no_customer_id"
    	
    	if customerId != "" then 
    		verifyAccountId
    	else
    		actionFailure 
    ]

 	/*************************
	 * 3. Action verify the accountId was provided.
	 */
    action verifyAccountId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [accountId]."
    	sErrorCode   = "no_account_id"
    	
    	if accountId != "" then 
    		authenticateRequest
    	else
    		actionFailure 
    ]
    
     
 	/*************************
	 * 4. Authenticate the request.
	 */
    action authenticateRequest [
    	sErrorStatus = "402"
    	sErrorDesc = "There was an internal error while authenticating the request."
    	sErrorCode = "internal_error"
    	 login(
            username: sServiceUserName
            password: securityToken
            namespace: sServiceNameSpace
            )
        if success then actionCheckForUser
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]
    
    /*************************
	 * 5. Check if the user exists.
	 */
	action actionCheckForUser [
		switch Enroll.isUserAlreadyRegistered(customerId)[
			case "registered"       actionProcess
			case "not_registered"   actionValidateAccounts
			default 				actionFailure
		]
	]
	
	/**************************
	 * 6. Make certain all accounts are
	 * 		properly created for registration
	 */
	 action actionValidateAccounts [
		spAcctsReg.statusPaymentGroup = sStatusPayGroup
		spAcctsReg.billPaymentGroup = sBillPayGroup
		spAcctsReg.account = accountId
		switch apiCall AccountStatus.GetAccountsForRegistration(spAcctsReg, srAcctsReg, ssAcctsReg ) [
			case apiSuccess actionCreateUser
			default actionFailure
		]
	 ]

 	/*************************
	 * 6s. Check if the user exists.
	 */
	action actionCreateUser [
    	sErrorStatus = "402"
    	sErrorDesc = "There was an internal error while creating the user."
    	sErrorCode = "internal_error"
		
		switch Enroll.create_account(customerId, accountId) [
			case "invalid" actionInvalidCustomerAccountPair
			case "success" actionProcess
			default actionFailure
		]
	]
    

 	/*************************
	 * 7. Query the database for payment information about the account.
	 */
	action actionProcess [
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid [customerId] or [accountId] cannot find details."
    	sErrorCode   = "invalid_customer_id_account_id_pair"
		
		spGetAutoPay.customerId		 = customerId
		spGetAutoPay.accountId  	 = accountId
		spGetAutoPay.configChange  	 = sDeleteAutoHistoryConfigChange
		
		switch apiCall AgentPay.GetAutoPay(spGetAutoPay, srGetAutoPay, ssStatus) [
		   case apiSuccess checkSourceId
           default         actionFailure    
		]
	]
	
	/*************************
	 * 7a. Check source id available or not.
	 */
	action checkSourceId [
		JsonResponse.reset()
	 	if srGetAutoPay.automaticSourceId == "" then
	 		actionSendResponse
	 	else
	 		fetchWalletData 
	]
	
	/*************************
	 * 7b. Fetch wallet data to send wallet info in success response.
	 */
     action fetchWalletData [
    	sErrorStatus = "400"
    	sErrorDesc   = "Unable to fetch wallet details."
    	sErrorCode   = "no_payment_source"
    	
    	srGetWalletInfoParam.SOURCE_ID = srGetAutoPay.automaticSourceId
    	
    	switch apiCall Payment.GetWalletByToken(srGetWalletInfoParam, srGetWalletInfoResult, ssStatus) [
		    case apiSuccess setWalletData
		    default actionFailure
		]
    	
    ]
    
    action setWalletData [
    	JsonResponse.setString("nickName", srGetWalletInfoResult.SOURCE_NAME)
		JsonResponse.setString("paymentAccount", srGetWalletInfoResult.SOURCE_NUM)
		JsonResponse.setString("paymentAcctType", srGetWalletInfoResult.SOURCE_TYPE)
    	goto(actionSendResponse)
    ]

  	/*************************
	 * 8. Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
//		JsonResponse.reset()
		JsonResponse.setBoolean("hasRule", srGetAutoPay.automaticEnabled)
		JsonResponse.setString("paymentDateRule", srGetAutoPay.automaticDate)
		JsonResponse.setString("paymentAmountRule", srGetAutoPay.automaticAmount)
		JsonResponse.setString("paymentCountRule", srGetAutoPay.automaticCount)
		
	   	auditLog(audit_agent_pay.start_payment_for_agent_success) [
			customerId accountId
	   	]
		Log.^success("getAutomaticPaymentRuleForAgent", customerId, accountId, "Success")
		
		logout()
	    foreignHandler JsonResponse.send()
	]

    /********************************
     * E1. Send a response back that we could not process the request.
     */
    action actionFailure [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorDesc)
		JsonResponse.setString("error", sErrorCode)

	    auditLog(audit_agent_pay.start_payment_for_agent_failure) [
	   		customerId accountId
	    ]
		Log.error("getAutomaticPaymentRuleForAgent", customerId, accountId, sErrorDesc)

		logout()
		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]

    /********************************
     * E2. Invalid Security Token
     */
    action actionInvalidSecurityToken [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid security token.")
		JsonResponse.setString("error", "invalid_security_token")

		auditLog(audit_agent_pay.start_payment_for_agent_failure) [
			customerId accountId
	   	]
		Log.error("getAutomaticPaymentRuleForAgent", customerId, accountId, "Invalid security token.")

		logout()
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
    /********************************
     * Invalid customer id/account id
     */
    action actionInvalidCustomerAccountPair [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "400")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Account ID does not belong to Customer ID.")
		JsonResponse.setString("error", "invalid_customer_account_pair")

		auditLog(audit_agent_pay.start_payment_for_agent_failure) [
			customerId accountId
	   	]
		Log.error("getAutomaticPaymentRuleForAgent", customerId, accountId, "Account ID does not belong to Customer ID.")

		logout()
		foreignHandler JsonResponse.errorWithData("400")
    ]
    
]
