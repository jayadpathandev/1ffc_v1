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

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
    native string securityToken
    native string customerId
    native string accountId
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
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
        if success then actionProcess
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]

 	/*************************
	 * 5. Query the database for payment information about the account.
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
	 * 6. Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
		JsonResponse.reset()
		
	   	auditLog(audit_agent_pay.start_payment_for_agent_success) [
			customerId accountId
	   	]
		Log.^success("getScheduledPaymentsForAgent", customerId, accountId, "Success")
		
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
		Log.error("getScheduledPaymentsForAgent", customerId, accountId, "Invalid security token.")

		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
