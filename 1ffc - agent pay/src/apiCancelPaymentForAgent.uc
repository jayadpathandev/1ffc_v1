useCase apiCancelPaymentForAgent
[
	startAt init

	shortcut cancelPaymentForAgent(init) [
		securityToken
		transactionId
	]

	importJava Log(api.Log)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
    native string securityToken
    native string transactionId
    native volatile string customerId = ApiPay.customerId()
    native volatile string accountId  = ApiPay.accountId()
	
	string saveCustomerId
	string saveAccountId
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Verify that the securityToken was provided.
	 */
    action init[
    	sErrorStatus = "401"
    	sErrorDesc   = "Missing required parameter [securityToken]."
    	sErrorCode   = "no_security_token"
    	
    	if securityToken != "" then 
    		verifyTransactionId
    	else
    		actionFailure 
    ]

	/*************************
	 * 2. Verify the transactionId was provided.
	 */
    action verifyTransactionId[
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [transactionId]."
    	sErrorCode   = "no_customer_id"
    	
    	if transactionId != "" then 
    		authenticateRequest
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 6. Authenticate the request.
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
        if success               then loadTransaction
        if authenticationFailure then actionInvalidSecurityToken
        if failure               then actionFailure
    ]

 	/*************************
	 * 6a. Load session.
	 */
	action loadTransaction [
		sErrorStatus = "401"
    	sErrorDesc   = "Invalid [transactionId] no matching transaction."
    	sErrorCode   = "invalid_transaction_id"
		
		switch ApiPay.load(transactionId) [
			case "true" actionSaveInfo
			default     actionFailure 
		]		
	]

 	/*************************
	 * 6b. Load session.
	 */
	action actionSaveInfo [
		saveCustomerId = customerId
		saveAccountId  = accountId
		goto(actionProcess)
	]

 	/*************************
	 * 7. Query the database for payment information about the account.
	 */
	action actionProcess [
		sErrorStatus = "401"
    	sErrorDesc   = "Invalid [transactionId] no matching transaction."
    	sErrorCode   = "invalid_transaction_id"
		
		switch ApiPay.clear(transactionId) [
		   case "true" actionSendResponse
           default     actionFailure    
		]
	]

 	/*************************
	 * Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
		JsonResponse.reset()
		JsonResponse.setString("status", "cancelled")
		
	    auditLog(audit_agent_pay.cancel_payment_for_agent_success) [
	   		saveCustomerId saveAccountId
	    ]
		Log.^success("cancelPaymentForAgent", transactionId, "Success")
		
		logout()
	    foreignHandler JsonResponse.send()
	]

    /********************************
     * Send a response back that we could not process the request.
     */
    action actionFailure [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorDesc)
		JsonResponse.setString("error", sErrorCode)

		auditLog(audit_agent_pay.cancel_payment_for_agent_failure) [
	   		transactionId
   		]
		Log.error("cancelPaymentForAgent", transactionId, sErrorDesc)

		logout()
		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]

    /********************************
     * Invalid Security Token
     */
    action actionInvalidSecurityToken [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid security token.")
		JsonResponse.setString("error", "invalid_security_token")

		auditLog(audit_agent_pay.cancel_payment_for_agent_failure) [
	   		transactionId
   		]
		Log.error("cancelPaymentForAgent", transactionId, "Invalid security token.")

		logout()
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
