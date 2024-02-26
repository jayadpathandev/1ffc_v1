useCase apiDeleteScheduledPaymentForAgent
[
	startAt init
	shortcut deleteScheduledPaymentForAgent(init)

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
	native string sPaymentId		= JsonRequest.value("paymentId")
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus								   ssDeleteRequest
	serviceParam (Payment.DeleteScheduledPayment)  spDeleteRequest
	serviceResult (Payment.DeleteScheduledPayment) srDeleteResult
	
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
    		verifyPaymentId
    	else
    		actionFailure 
  		
		
	]
    
	/*************************
	 * 5. Verify that the paymentId was provided.
	 */
	action verifyPaymentId [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentId]."
    	sErrorCode   = "no_paymentid"
    	
    	if sPaymentId != "" then 
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
            password: sSecurityToken
            namespace: sServiceNameSpace
            )
        if success then delete
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]



 	/*************************
	 * 7. Delete the scheduled payment.
	 */
	action delete [
		sErrorStatus = "400"
		sErrorDesc = "Scheduled payment delete failed"
		sErrorCode = "scheduledpay_delete_failed"

		spDeleteRequest.ONLINE_TRANS_ID = sPaymentId
		switch apiCall Payment.DeleteScheduledPayment(spDeleteRequest, srDeleteResult, ssDeleteRequest) [
			case apiSuccess actionSuccessResponse
			default actionFailure
		]
	]
	
 	/*************************
	 * 8. Response with success back to the client.
	 */
	 action actionSuccessResponse [
		JsonResponse.reset()

	    auditLog(audit_agent_pay.delete_scheduledpay_for_agent_success) [
	   		sCustomerId sAccountId sPaymentId
	    ]
		Log.^success("deleteScheduledPaymentForAgent", sCustomerId, sAccountId, sPaymentId)

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

	    auditLog(audit_agent_pay.delete_scheduledpay_for_agent_failed) [
	   		sCustomerId sAccountId sPaymentId
	    ]
		Log.error("deleteScheduledPaymentForAgent", sCustomerId, sAccountId, sPaymentId, sErrorDesc)

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

	    auditLog(audit_agent_pay.delete_scheduledpay_for_agent_failed) [
	   		sCustomerId sAccountId sPaymentId
	    ]
		Log.error("makeOneTimePaymentForAgent", sCustomerId, sAccountId, sPaymentId, "Invalid security token.")
		
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
