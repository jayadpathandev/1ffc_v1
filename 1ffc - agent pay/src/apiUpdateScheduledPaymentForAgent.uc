useCase apiUpdateScheduledPaymentForAgent
[
	startAt init
	
	shortcut updateScheduledPaymentForAgent(init)
	
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Log(api.Log)
	importJava UcPaymentUtils(com.sorrisotech.fffc.agent.pay.UcPaymentUtils)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
	
	native string securityToken = JsonRequest.value("securityToken")
	native string customerId = JsonRequest.value("customerId")
	native string accountId   = JsonRequest.value("accountId")
	native string paymentId     = JsonRequest.value("paymentId")
	native string payAmount     = JsonRequest.value("amount")
	native string payDate     = JsonRequest.value("date") 
	
	native string sStatus
	native volatile string sUserId = UcPaymentUtils.getUserIdFromCustomerId(customerId)
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus ssUpdateSchedPayment
    serviceParam(Payment.UpdateScheduledPaymentB2C) spUpdateSchedPayment
    serviceResult(Payment.UpdateScheduledPaymentB2C) srUpdateSchedPayment
	
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
    	
    	if securityToken != "" then 
    		verifycustomerId
    	else
    		actionFailure 
    ]

	/*************************
	 * 3. Verify the customerId was provided.
	 */
    action verifycustomerId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [customerId]."
    	sErrorCode   = "no_customer_id"
    	
    	if customerId != "" then 
    		verifyAccountId
    	else
    		actionFailure 
    ]
    
    /*************************
	 * 4. Action verify the accountId was provided.
	 */
    action verifyAccountId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [accountId]."
    	sErrorCode   = "no_account_id"
    	
    	if accountId != "" then 
    		verifyPaymentId
    	else
    		actionFailure 
    ]

 	/*************************
	 * 5. Action verify the paymentId was provided.
	 */
    action verifyPaymentId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentId]."
    	sErrorCode   = "no_payment_id"
    	
    	if paymentId != "" then 
    		verifyPaymentDate
    	else
    		actionFailure 
    ]
    
    /*************************
	 * 6. Action verify the paymentAmount was provided.
	 */
    action verifyPaymentAmount [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [amount]."
    	sErrorCode   = "no_payment_amount"

    	if payAmount != "" then 
    		authenticateRequest
    	else
    		actionFailure 
    ]
 	
 	/*************************
	 * 7. Action verify the paymentDate was provided.
	 */
    action verifyPaymentDate [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [date]."
    	sErrorCode   = "no_payment_date"
    	
    	if payDate != "" then 
    		verifyPaymentAmount
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 8. Authenticate the request.
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
        if success then verifyUserId
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]
	
	/*************************
	 * 9. Verify user id.
	 */
	action verifyUserId [
    	sErrorStatus = "402"
    	sErrorDesc = "There was an error while getting user details."
    	sErrorCode = "internal_verify_error"
    	
    	if sUserId != "" then 
    		actionVerifyData
    	else
    		actionFailure 
    ]
    
    /*************************
	 * 10. Query the database for payment information about the account.
	 */
	action actionVerifyData [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error verifying date and amount."
    	sErrorCode   = "internal_verify_error"
				
		switch UcPaymentUtils.validateDateAndAmount(payDate, payAmount) [
		   case "invalid_date"   actionInvalidDate
		   case "invalid_amount" actionInvalidAmount
		   case "success"        updateScheduledPayment
           default               actionFailure    
		]
	]
	
	/*************************
	 * 11. Update scheduled payment.
	 */
	action updateScheduledPayment [
		spUpdateSchedPayment.USER_ID = sUserId
		spUpdateSchedPayment.ONLINE_TRANS_ID = paymentId
		
		UcPaymentUtils.amount         (spUpdateSchedPayment.PAY_AMOUNT)
		UcPaymentUtils.payDate        (spUpdateSchedPayment.PAY_DATE)
		
		switch apiCall Payment.UpdateScheduledPaymentB2C (spUpdateSchedPayment, srUpdateSchedPayment, ssUpdateSchedPayment ) [
		    case apiSuccess schedulePaymentSuccess
		    default actionFailureResponse
		]
	]	

 	/*************************
	 * 12. Record the successful transaction.
	 */
	action schedulePaymentSuccess [
		sStatus = "scheduled"
		goto(actionSuccessResponse)
	]
	
 	/*************************
	 * 13. Response with success back to the client.
	 */
	 action actionSuccessResponse [
		JsonResponse.reset()
		JsonResponse.setString("status", sStatus)

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_success) [
	   		customerId accountId payDate payAmount
	    ]
		Log.^success("updateScheduledPaymentForAgent", customerId, accountId, payDate, payAmount)

		foreignHandler JsonResponse.send()	 	
	 ]

 	/*************************
	 * 14 Response with a failure back to the client.
	 */
	 action actionFailureResponse [
		JsonResponse.reset()

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_failure) [
	   		customerId accountId payDate payAmount
	    ]
		Log.error("makeOneTimePaymentForAgent", customerId, accountId, payDate, payAmount)

		sErrorStatus = "210"
    	sErrorDesc   = "Payment failure."
    	sErrorCode   = "payment_failure"

		goto(actionFailure)    	
	 ]

    /********************************
     * Send an invalid amount failure.
     */
    action actionInvalidAmount [
		sErrorStatus = "401"
    	sErrorDesc   = "Invalid amount specified."
    	sErrorCode   = "invalid_amount"
		goto(actionFailure)    	
    ]

    /********************************
     * Send an invalid date failure.
     */
    action actionInvalidDate [
		sErrorStatus = "401"
    	sErrorDesc   = "Invalid date specified."
    	sErrorCode   = "invalid_date"
		goto(actionFailure)    	
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

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_bad) [
	   		paymentId payDate payAmount
	    ]
		Log.error("updateScheduledPaymentForAgent", paymentId, payDate, payAmount, sErrorDesc)

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

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_bad) [
	   		paymentId payDate payAmount
	    ]
		Log.error("updateScheduledPaymentForAgent", paymentId, payDate, payAmount, "Invalid security token.")
		
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
