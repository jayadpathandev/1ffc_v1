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
	importJava Enroll(com.sorrisotech.fffc.agent.pay.Enroll)

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
    
    // -- using status call to retrieve accounts for registration from
	//		the status feed and make certain they are ALL in tm_accounts --
    serviceStatus ssAcctsReg
    serviceParam (AccountStatus.GetAccountsForRegistration) spAcctsReg
    serviceResult (AccountStatus.GetAccountsForRegistration) srAcctsReg
    native string sStatusPayGroup = Config.get("1ffc.ignore.group")
    native string sBillPayGroup = Config.get("1ffc.bill.group")
    native string sDeleteAutoHistoryConfigChange = "Recurring payment deleted."

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
        if success then actionCreateSession
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]

 	/*************************
	 * 10. Create the session.
	 */
	action actionCreateSession [
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid value for [paymentTransactionType]."
    	sErrorCode   = "invalid_payment_transaction_type"

		switch ApiPay.create(paymentTransactionType) [
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
		
		spStart.customerId    = customerId
		spStart.accountId  	  = accountId
		spStart.configChange  = sDeleteAutoHistoryConfigChange
		
		switch apiCall AgentPay.Start(spStart, srStart, ssStart) [
		   case apiSuccess actionCheckForUser
           default         actionFailure    
		]
	]

 	/*************************
	 * 9. Check if the user exists.
	 */
	action actionCheckForUser [
		
		if srStart.userid != "" then
			actionSendResponse
		else
			actionValidateAccounts
	]

	/**************************
	 * 9a. Make certain all accounts are
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
	 * 9b. Check if the user exists.
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
	 * 11. Everything is good, reply with the data the client needs.
	 */
	action actionSendResponse [
		ApiPay.setTransactionStarted(id)
		ApiPay.setCustomerId(customerId)
		ApiPay.setAccountId(accountId)
		ApiPay.setAccountNumber(srStart.accountNumber)
		ApiPay.setInvoice(srStart.invoice)
		ApiPay.setPayGroup(srStart.payGroup)
		ApiPay.setUserid(srStart.userid)
		ApiPay.setUserName(srStart.userName)
		ApiPay.setCompanyId(srStart.companyId)
		ApiPay.setAutomaticPayment(srStart.automaticEnabled)
		
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

		logout()
	    foreignHandler JsonResponse.send()
	]

    /********************************
     * Send a response back that we could not process the request.
     */
    action actionFailure [
		ApiPay.setTransactionError(id)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorDesc)
		JsonResponse.setString("error", sErrorCode)

	    auditLog(audit_agent_pay.start_payment_for_agent_failure) [
	   		customerId accountId
	    ]
		Log.error("startPaymentForAgent", customerId, accountId, sErrorDesc)

		logout()
		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]

    /********************************
     * Invalid Security Token
     */
    action actionInvalidSecurityToken [
		ApiPay.setTransactionError(id)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid security token.")
		JsonResponse.setString("error", "invalid_security_token")

		auditLog(audit_agent_pay.start_payment_for_agent_failure) [
			customerId accountId
	   	]
		Log.error("startPaymentForAgent", customerId, accountId, "Invalid security token.")

		logout()
		foreignHandler JsonResponse.errorWithData("401")
    ]

    /********************************
     * Invalid Security Token
     */
    action actionInvalidCustomerAccountPair [
		ApiPay.setTransactionError(id)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "400")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Account ID does not belong to Customer ID.")
		JsonResponse.setString("error", "invalid_customer_account_pair")

		auditLog(audit_agent_pay.start_payment_for_agent_failure) [
			customerId accountId
	   	]
		Log.error("startPaymentForAgent", customerId, accountId, "Account ID does not belong to Customer ID.")

		logout()
		foreignHandler JsonResponse.errorWithData("400")
    ]
    
]
