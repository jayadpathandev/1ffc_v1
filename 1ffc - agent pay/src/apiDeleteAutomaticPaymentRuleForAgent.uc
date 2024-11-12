useCase apiDeleteAutomaticPaymentRuleForAgent
[
	startAt init
	shortcut deleteAutomaticPaymentRuleForAgent(init)

	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Log(api.Log)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
	
	// -- data from request --
	native string sSecurityToken 	= JsonRequest.value("securityToken")
	native string sCustomerId 		= JsonRequest.value("customerId")
	native string sAccountId	   	= JsonRequest.value("accountId")
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	native string sAutoPayId
	native string sDeleteAutoHistoryConfigChange = "Recurring payment deleted."
	
	serviceStatus                 	   ssGetAutoPay
    serviceParam (AgentPay.GetAutoPay) spGetAutoPay
    serviceResult(AgentPay.GetAutoPay) srGetAutoPay
	
	serviceStatus								   ssDeleteRequest
	serviceParam (Payment.DeleteAutomaticPayment)  spDeleteRequest
	serviceResult (Payment.DeleteAutomaticPayment) srDeleteResult
	
	serviceParam(Payment.DeleteAutomaticPaymentHistory)  srDeleteAutomaticHistoryParam
    serviceResult(Payment.DeleteAutomaticPaymentHistory) srDeleteAutomaticHistoryResult
	
   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Parse the JSON request.
	 */
    action init[
    	
    	sErrorStatus = "400"
    	sErrorDesc   = "Cannot parse JSON request."
    	sErrorCode   = "invalid_json"
    	
    	switch JsonRequest.load() [
    		case "success" verifyToken
    		default        actionFailure
    	]
    ]
    
	/*************************
	 * 2. Verify that the securityToken was provided.
	 */
    action verifyToken [    
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [securityToken]."
    	sErrorCode   = "no_security_token"
    	
    	if sSecurityToken != "" then 
    		verifyCustomerId
    	else
    		actionFailure 
    ]

	/*************************
	 * 3. Verify that the customerID was provided.
	 */
	action verifyCustomerId [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [customerId]."
    	sErrorCode   = "no_customerid"
    	
    	if sCustomerId != "" then 
    		verifyAccountId
    	else
    		actionFailure 
  		
	]
	
	/*************************
	 * 4. Verify that the accountId was provided.
	 */
	action verifyAccountId [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [accountId]."
    	sErrorCode   = "no_accountid"
    	
    	if sAccountId != "" then 
    		authenticateRequest
    	else
    		actionFailure 
  		
		
	]
    
 	/*************************
	 * 5. Authenticate the request.
	 */
    action authenticateRequest [
    	sErrorStatus = "402"
    	sErrorDesc = "There was an internal error while authenticating the request."
    	sErrorCode = "internal_error"
    	 login(
            username: sServiceUserName
            password: sSecurityToken
            namespace: sServiceNameSpace
            )
        if success then getAutomaticPaymentId
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]

 	/*************************
	 * 6. Query the database for auto payment information about the account.
	 * 		all the system needs is the id.
	 */
	action getAutomaticPaymentId [
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid [customerId] or [accountId] cannot find details."
    	sErrorCode   = "invalid_customer_id_account_id_pair"
		
		spGetAutoPay.customerId 	= sCustomerId
		spGetAutoPay.accountId  	= sAccountId
		spGetAutoPay.configChange  	= sDeleteAutoHistoryConfigChange
		
		switch apiCall AgentPay.GetAutoPay(spGetAutoPay, srGetAutoPay, ssGetAutoPay) [
		   case apiSuccess checkAutomaticPaymentId
           default         actionFailure    
		]
	]
	
 	/*************************
	 * 6. Check the id to see that there is an autopay rule. If not,
	 * 		that's a failure.
	 */
	action checkAutomaticPaymentId [
		sErrorStatus = "400"
		sErrorDesc = "No autoPayment rule for [customerId] and [accountId]."
		sErrorCode = "no_autopay_rule"
		sAutoPayId = srGetAutoPay.automaticPaymentId
		if sAutoPayId == "0" then
			actionFailure
		else
			deleteAutoPayRule
	]


 	/*************************
	 * 6a. Delete the autopay rule.
	 */
	action deleteAutoPayRule [
		sErrorStatus = "400"
		sErrorDesc = "AutoPayment rule delete failed"
		sErrorCode = "autopay_delete_failed"

		spDeleteRequest.USER_ID      = srGetAutoPay.^userId
		spDeleteRequest.AUTOMATIC_ID = sAutoPayId
		
		switch apiCall Payment.DeleteAutomaticPayment(spDeleteRequest, srDeleteResult, ssDeleteRequest) [
			case apiSuccess deleteAutomaticPaymentHistory
			default actionFailure
		]
	]
	
	/*************************
	* 6b. Delete automatic payment history. 
	*/	
	action deleteAutomaticPaymentHistory [
		srDeleteAutomaticHistoryParam.AUTOMATIC_ID = sAutoPayId
		srDeleteAutomaticHistoryParam.CONFIG_CHANGE = sDeleteAutoHistoryConfigChange
		switch apiCall Payment.DeleteAutomaticPaymentHistory(srDeleteAutomaticHistoryParam, srDeleteAutomaticHistoryResult, ssDeleteRequest) [
            case apiSuccess actionSuccessResponse
            default actionFailure
        ]	
    ]
	
 	/*************************
	 * 7. Response with success back to the client.
	 */
	 action actionSuccessResponse [
		JsonResponse.reset()

	    auditLog(audit_agent_pay.delete_autopay_for_agent_success) [
	   		sCustomerId sAccountId
	    ]
		Log.^success("deleteAutomaticPaymentForAgent", sCustomerId, sAccountId)

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

	    auditLog(audit_agent_pay.delete_autopay_for_agent_failure) [
	   		sCustomerId sAccountId
	    ]
		Log.error("deleteAutomaticPaymentForAgent", sCustomerId, sAccountId, sErrorDesc)

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

	    auditLog(audit_agent_pay.delete_autopay_for_agent_failure) [
	   		sCustomerId sAccountId
	    ]
		Log.error("makeOneTimePaymentForAgent", sCustomerId, sAccountId, "Invalid security token.")
		
		logout()
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
