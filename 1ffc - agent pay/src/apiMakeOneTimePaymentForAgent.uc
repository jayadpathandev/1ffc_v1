useCase apiMakeOneTimePaymentForAgent
[
	startAt init
	shortcut makeOneTimePaymentForAgent(init)

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
    importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Log(api.Log)
    importJava TransactionIdGen(com.sorrisotech.svcs.payment.util.RequestTransactionIdUtil)    

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
    native string sPaymentUrl		= AppConfig.get("epayment.url")
	native string sTransactionIdUrl
	native string sPayId            = TransactionIdGen.getTransactionId(sTransactionIdUrl)
	
	native string sSecurityToken = JsonRequest.value("securityToken")
	native string sTransactionId = JsonRequest.value("transactionId")
	native string sPaymentDate   = JsonRequest.value("paymentDate")
	native string sPayAmount     = JsonRequest.value("paymentAmount")
	
	native volatile string sCustomerId   = ApiPay.customerId()
	native volatile string sAccountId    = ApiPay.accountId()
	native volatile string sHasPaySource = ApiPay.hasWallet()
	
	native string sStatus
	native string sTransId
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus status
	
	serviceParam (Payment.MakePayment)  makeRequest
	serviceResult (Payment.MakePayment) makeResult

	serviceParam (Payment.SetScheduledPayment)  schedRequest
	serviceResult (Payment.SetScheduledPayment) schedResult
	
	serviceParam(Payment.StartPaymentTransaction) logRequest
	
   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Parse the JSON request.
	 */
    action init[
    	sTransactionIdUrl = sPaymentUrl + "generateTransactionId"
    	
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
    		verifyTransactionId
    	else
    		actionFailure 
    ]

	/*************************
	 * 3. Verify the transactionId was provided.
	 */
    action verifyTransactionId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [transactionId]."
    	sErrorCode   = "no_transaction_id"
    	
    	if sTransactionId != "" then 
    		verifyPaymentDate
    	else
    		actionFailure 
    ]

 	/*************************
	 * 4. Action verify the paymentDate was provided.
	 */
    action verifyPaymentDate [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentDate]."
    	sErrorCode   = "no_payment_date"
    	
    	if sPaymentDate != "" then 
    		verifyPaymentAmount
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 5. Action verify the paymentAmount was provided.
	 */
    action verifyPaymentAmount [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentAmount]."
    	sErrorCode   = "no_payment_amount"

    	if sPayAmount != "" then 
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
        if success then actionLoadTransaction
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionFailure
    ]

 	/*************************
	 * 7. Load the transaction.
	 */
	action actionLoadTransaction [ 
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid [transactionId] no matching transaction."
    	sErrorCode   = "invalid_transaction_id"

		switch ApiPay.load(sTransactionId) [
			case "true" verifyWalletSelected
			default	actionFailure
		]
	]
	
 	/*************************
	 * 7a. Verify a wallet item was selected.
	 */
    action verifyWalletSelected [
    	sErrorStatus = "400"
    	sErrorDesc   = "No payment source selected/created."
    	sErrorCode   = "no_payment_source"

    	if sHasPaySource == "true" then 
    		actionVerifyData
    	else
    		actionFailure 
    ]

 	/*************************
	 * 8. Query the database for payment information about the account.
	 */
	action actionVerifyData [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error verifying date and amount."
    	sErrorCode   = "internal_verify_error"
				
		switch ApiPay.saveDateAndAmount(sPaymentDate, sPayAmount) [
		   case "invalid_date"   actionInvalidDate
		   case "invalid_amount" actionInvalidAmount
		   case "success"        isPaymentImmediate
           default               actionFailure    
		]
	]

 	/*************************
	 * 9. Determine what type of payment is being done.
	 */
	action isPaymentImmediate [
		switch ApiPay.is_immediate_payment() [
			case "true" actionMakePayment
			case "false" actionSchedulePayment
		]	
	]
	
 	/*************************
	 * 9a. Issue the payment request.
	 */
	action actionMakePayment [
		ApiPay.setTransactionOneTimeInProgress(sTransactionId)	

		ApiPay.makePaymentJson(makeRequest.GROUPING_JSON)
		makeRequest.ONLINE_TRANS_ID = sPayId
		ApiPay.amount(makeRequest.AMOUNT)
		makeRequest.CURRENCY = "USD"
		ApiPay.companyId(makeRequest.COMPANY_ID) 
		ApiPay.payDate(makeRequest.PMT_DATE)
		ApiPay.userid(makeRequest.USER_ID)
		ApiPay.walletToken(makeRequest.TOKEN)
		
		switch apiCall Payment.MakePayment(makeRequest, makeResult, status) [
			case apiSuccess actionMakeSuccess
			default         actionPayFailure
		]		
	]

 	/*************************
	 * 9b. Record the successful transaction.
	 */
	action actionMakeSuccess [
		sStatus  = "posted"
		sTransId = makeResult.ONLINE_TRANS_ID
		
    	logRequest.TRANSACTION_ID  = sPayId
		logRequest.ONLINE_TRANS_ID = sPayId
		logRequest.PAY_CHANNEL     = "online"
		logRequest.PAY_STATUS      = "posted"

		ApiPay.payGroup       (logRequest.PMT_PROVIDER_ID)
		ApiPay.makePaymentJson(logRequest.GROUPING_JSON)
		ApiPay.walletFrom     (logRequest.PAY_FROM_ACCOUNT)
		ApiPay.payDate        (logRequest.PAY_DATE)
		ApiPay.amount         (logRequest.PAY_AMT)		
		ApiPay.userid         (logRequest.USER_ID)
		
		switch apiCall Payment.StartPaymentTransaction(logRequest, status) [
            case apiSuccess actionSuccessResponse
            default         actionSuccessResponse
        ]	
	]

 	/*************************
	 * 9c. Schedule the payment.
	 */
	action actionSchedulePayment [
		ApiPay.setTransactionOneTimeInProgress(sTransactionId)	

		schedRequest.ONLINE_TRANS_ID = sPayId
		schedRequest.PAY_TYPE        = "onetime"
		schedRequest.PAY_STATUS      = "scheduled"		
			
		ApiPay.makePaymentJson(schedRequest.GROUPING_JSON)
		ApiPay.walletToken    (schedRequest.SOURCE_ID)
		ApiPay.amount         (schedRequest.PAY_AMT)
		ApiPay.payDate        (schedRequest.PAY_DATE)
		ApiPay.userid         (schedRequest.USER_ID)
		ApiPay.walletFrom     (schedRequest.SOURCE_DETAILS)
		
		switch apiCall Payment.SetScheduledPayment(schedRequest, schedResult, status) [
            case apiSuccess schedulePaymentSuccess
            default         actionFailureResponse
        ]	
			
	]

 	/*************************
	 * 9b. Record the successful transaction.
	 */
	action schedulePaymentSuccess [
		sStatus = "scheduled"
		sTransId = schedResult.ONLINE_TRANS_ID
		goto(actionSuccessResponse)
	]
	
 	/*************************
	 * 10. Response with success back to the client.
	 */
	 action actionSuccessResponse [
		JsonResponse.reset()
		JsonResponse.setString("status", sStatus)
		JsonResponse.setString("paymentId", sTransId)

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_success) [
	   		sCustomerId sAccountId sPaymentDate sPayAmount
	    ]
		Log.^success("startPaymentForAgent", sCustomerId, sAccountId, sPaymentDate, sPayAmount)

		ApiPay.setTransactionComplete(sTransactionId)
		foreignHandler JsonResponse.send()	 	
	 ]

 	/*************************
	 * 10a. Record the failed transaction.
	 */
	action actionPayFailure [
    	logRequest.TRANSACTION_ID   = sPayId
		logRequest.ONLINE_TRANS_ID  = sPayId
		ApiPay.payGroup               (logRequest.PMT_PROVIDER_ID)
		ApiPay.makePaymentJson        (logRequest.GROUPING_JSON)
		ApiPay.walletFrom             (logRequest.PAY_FROM_ACCOUNT)
		logRequest.PAY_CHANNEL      = "online"
		ApiPay.payDate                (logRequest.PAY_DATE)
		ApiPay.amount                 (logRequest.PAY_AMT)		
		logRequest.PAY_STATUS       = "failed"
		ApiPay.userid                 (logRequest.USER_ID)
		
		switch apiCall Payment.StartPaymentTransaction(logRequest, status) [
            case apiSuccess actionFailureResponse
            default         actionFailureResponse
        ]	
	]

 	/*************************
	 * 10a 1. Response with a failure back to the client.
	 */
	 action actionFailureResponse [
		JsonResponse.reset()

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_failure) [
	   		sCustomerId sAccountId sPaymentDate sPayAmount
	    ]
		Log.error("makeOneTimePaymentForAgent", sCustomerId, sAccountId, sPaymentDate, sPayAmount)

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
		ApiPay.setTransactionError(sTransactionId)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorDesc)
		JsonResponse.setString("error", sErrorCode)

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_bad) [
	   		sTransactionId sPaymentDate sPayAmount
	    ]
		Log.error("makeOneTimePaymentForAgent", sTransactionId, sPaymentDate, sPayAmount, sErrorDesc)

		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]

    /********************************
     * Invalid Security Token
     */
    action actionInvalidSecurityToken [
		ApiPay.setTransactionError(sTransactionId)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid security token.")
		JsonResponse.setString("error", "invalid_security_token")

	    auditLog(audit_agent_pay.make_one_time_payment_for_agent_bad) [
	   		sTransactionId sPaymentDate sPayAmount
	    ]
		Log.error("makeOneTimePaymentForAgent", sTransactionId, sPaymentDate, sPayAmount, "Invalid security token.")
		
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
]
