useCase apiStartPaymentForAgent
[
	startAt init

	shortcut startPaymentForAgent(init) [
		securityToken
		customerId
		accountId
		paymentTransactionType
	]

	importJava Log(api.Log)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
    native string sBaseUrl          = Config.get("user.app.url")
    native string securityToken
    native string customerId
    native string accountId
    native string paymentTransactionType
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus                 ssStart
    serviceParam (AgentPay.Start) spStart
    serviceResult(AgentPay.Start) srStart

	native volatile string id        = ApiPay.id()
	native volatile string sourceUrl = ApiPay.sourceUrl(sBaseUrl, "startChooseSource")
	
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
    		verifyTransactionType
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 5. Action verify the paymentTransactionType was provided.
	 */
    action verifyTransactionType [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentTransactionType]."
    	sErrorCode   = "no_payment_transaction_type"

    	if paymentTransactionType != "" then 
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
        if success then actionProcess
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]

 	/*************************
	 * 7. Query the database for payment information about the account.
	 */
	action actionProcess [
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid [customerId] or [accountId] cannot find details."
    	sErrorCode   = "invalid_customer_id_account_id_pair"
		
		spStart.customerId = customerId
		spStart.accountId  = accountId
		
		switch apiCall AgentPay.Start(spStart, srStart, ssStart) [
		   case apiSuccess actionCreateSession
           default         actionFailure    
		]
	]

 	/*************************
	 * 8. Create the session.
	 */
	action actionCreateSession [
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid value for [paymentTransactionType]."
    	sErrorCode   = "invalid_payment_transaction_type"

		switch ApiPay.create(paymentTransactionType) [
			case "success" actionSendResponse
			default actionFailure
		]		
	]

 	/*************************
	 * Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
		ApiPay.setCustomerId(customerId)
		ApiPay.setAccountId(accountId)
		ApiPay.setAccountNumber(srStart.accountNumber)
		ApiPay.setInvoice(srStart.invoice)
		ApiPay.setPayGroup(srStart.payGroup)
		ApiPay.setUserid(srStart.userid)
		ApiPay.setUserName(srStart.userName)
		ApiPay.setCompanyId(srStart.companyId)
		
		JsonResponse.reset()
		JsonResponse.setString("transactionId", id)
		JsonResponse.setString("payAccountUrl", sourceUrl)
		JsonResponse.setString("payCurrency", "USD")
		JsonResponse.setNumber("scheduledPaymentCount", srStart.scheduledCount)
		JsonResponse.setString("scheduledPaymentDate", srStart.scheduledDate)
		JsonResponse.setString("scheduledPaymentTotal", srStart.scheduledTotal)
		JsonResponse.setBoolean("automaticPaymentRuleEnabled", srStart.automaticEnabled)
		JsonResponse.setString("automaticPaymentDateRule", srStart.automaticDate)
		JsonResponse.setString("automaticPaymentAmountRule", srStart.automaticAmount)
		JsonResponse.setString("automaticPaymentCountRule", srStart.automaticCount)
		
	   	auditLog(audit_agent_pay.start_payment_for_agent_success) [
			customerId accountId
	   	]
		Log.^success("startPaymentForAgent", customerId, accountId, "Success")
		
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

	    auditLog(audit_agent_pay.start_payment_for_agent_failure) [
	   		customerId accountId
	    ]
		Log.error("startPaymentForAgent", customerId, accountId, sErrorDesc)

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

		auditLog(audit_agent_pay.start_payment_for_agent_failure) [
			customerId accountId
	   	]
		Log.error("startPaymentForAgent", customerId, accountId, "Invalid security token.")

		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
