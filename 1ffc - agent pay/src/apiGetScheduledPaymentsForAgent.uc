useCase apiGetScheduledPaymentsForAgent
[
	startAt init

	shortcut getScheduledPaymentsForAgent(init) [
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
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode

    // -- using status call to retrieve accounts for registration from
	//		the status feed and make certain they are ALL in tm_accounts --
    serviceStatus ssAcctsReg
    serviceParam (AccountStatus.GetAccountsForRegistration) spAcctsReg
    serviceResult (AccountStatus.GetAccountsForRegistration) srAcctsReg
    native string sStatusPayGroup = Config.get("1ffc.ignore.group")
    native string sBillPayGroup = Config.get("1ffc.bill.group")

	
	serviceStatus                 	   ssGetScheduledPay
    serviceParam (AgentPay.GetScheduledPaymentsAsJson) spGetScheduledPay
    serviceResult(AgentPay.GetScheduledPaymentsAsJson) srGetScheduledPay

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
	 * 6a. Check if the user exists.
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
		
		spGetScheduledPay.customerId = customerId
		spGetScheduledPay.accountId  = accountId
		
		switch apiCall AgentPay.GetScheduledPaymentsAsJson(spGetScheduledPay, srGetScheduledPay, ssGetScheduledPay) [
		   case apiSuccess actionSendResponse
           default         actionFailure    
		]
	]

  	/*************************
	 * 8. Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
		JsonResponse.reset()
		
	   	auditLog(audit_agent_pay.start_payment_for_agent_success) [
			customerId accountId
	   	]
		Log.^success("getScheduledPaymentsForAgent", customerId, accountId, "Success")
		
		logout()
	    foreignHandler JsonResponse.send(srGetScheduledPay.scheduledPayments)
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
		Log.error("getScheduledPaymentsForAgent", customerId, accountId, sErrorDesc)

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
		Log.warn("getScheduledPaymentsForAgent", customerId, accountId, "Invalid security token.")

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
		Log.warn("getScheduledPaymentsForAgent", customerId, accountId, "Account ID does not belong to Customer ID.")

		logout()
		foreignHandler JsonResponse.errorWithData("400")
    ]
]
