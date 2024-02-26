useCase apiCreateAutomaticPaymentRuleForAgent
[
	startAt init
	shortcut createAutomaticPaymentRuleForAgent(init)

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
	importJava Automatic(com.sorrisotech.fffc.agent.pay.Automatic)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Log(api.Log)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
	
	native string sSecurityToken = JsonRequest.value("securityToken")
	native string sTransactionId = JsonRequest.value("transactionId")
	native string sDateRule      = JsonRequest.value("paymentDateRule")
	native string sAmountRule    = JsonRequest.value("paymentAmountRule")
	native string sCountRule     = JsonRequest.value("paymentCountRule")
	
	native volatile string sCustomerId = ApiPay.customerId()
	native volatile string sAccountId  = ApiPay.accountId()
	native volatile string sIsOneTime  = ApiPay.isOneTime()
	native volatile string sDecodeCode = Automatic.errorCode()
	native volatile string sDecodeText = Automatic.errorText()
	
	native string sErrorStatus
	native string sErrorDesc
	native string sErrorCode
	
	serviceStatus status	
	serviceParam(Payment.SetAutomaticPayment)  createRequest
	serviceResult(Payment.SetAutomaticPayment) createResponse

	serviceParam(Payment.SetAutomaticPaymentHistory)  historyRequest
	
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
    		verifyDateRule
    	else
    		actionFailure 
    ]

 	/*************************
	 * 4. Action verify the paymentDateRule was provided.
	 */
    action verifyDateRule [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentDateRule]."
    	sErrorCode   = "no_payment_date_rule"
    	
    	if sDateRule != "" then 
    		verifyAmountRule
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 5. Action verify the paymentAmountRule was provided.
	 */
    action verifyAmountRule [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentAmountRule]."
    	sErrorCode   = "no_payment_amount_rule"

    	if sAmountRule != "" then 
    		verifyCountRule
    	else
    		actionFailure 
    ]

 	/*************************
	 * 6. Action verify the paymentCountRule was provided.
	 */
    action verifyCountRule [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [paymentCountRule]."
    	sErrorCode   = "no_payment_count_rule"

    	if sCountRule != "" then 
    		authenticateRequest
    	else
    		actionFailure 
    ]
    
 	/*************************
	 * 7. Authenticate the request.
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
	 * 8. Load the transaction.
	 */
	action actionLoadTransaction [ 
		sErrorStatus = "400"
    	sErrorDesc   = "Invalid [transactionId] no matching transaction."
    	sErrorCode   = "invalid_transaction_id"

		switch ApiPay.load(sTransactionId) [
			case "true" decodeAmountOption
			default	actionFailure
		]
	]	

 	/*************************
	 * 9. Decode the paymentAmountRule value.
	 */
	action decodeAmountOption [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentAmountRule] option."
    	sErrorCode   = "internal_error_amount_option"
				
		switch Automatic.translateAmountOption(sAmountRule, createRequest.PAY_AMOUNT_OPTION) [
		   case "good" decodeAmount
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 10. Decode the paymentAmountRule amount.
	 */
	action decodeAmount [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentAmountRule] amount."
    	sErrorCode   = "internal_error_amount"
				
		switch Automatic.translateAmount(sAmountRule, createRequest.PAY_UPTO) [
		   case "good" decodeDateOption
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 11. Decode the paymentDateRule value.
	 */
	action decodeDateOption [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentDateRule] option."
    	sErrorCode   = "internal_error_date_option"
				
		switch Automatic.translateDateOption(sDateRule, createRequest.PAY_INVOICES_OPTION) [
		   case "good" decodeDate
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 12. Decode the paymentAmountRule amount.
	 */
	action decodeDate [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentDateRule] days amount."
    	sErrorCode   = "internal_error_date_days"
				
		switch Automatic.translateDate(sDateRule, createRequest.PAY_DATE, createRequest.PAY_PRIOR_DAYS) [
		   case "good" decodeCountOption
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 13. Decode the paymentCountRule value.
	 */
	action decodeCountOption [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentCountRule] option."
    	sErrorCode   = "internal_error_count_option"
				
		switch Automatic.translateCountOption(sCountRule, createRequest.EFFECTIVE_UNTIL_OPTION) [
		   case "good" decodeCount
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 14. Decode the paymentCountRule amount.
	 */
	action decodeCount [
		sErrorStatus = "402"
    	sErrorDesc   = "Internal error decoding [paymentCountRule] count."
    	sErrorCode   = "internal_error_count"
				
		createRequest.EXPIRY_DATE = ""
		switch Automatic.translateCout(sCountRule, createRequest.PAY_COUNT) [
		   case "good" actionCheckExitingAutoPay
		   case "bad"  actionDecodeFailure
           default     actionFailure    
		]
	]

 	/*************************
	 * 15. Check for an existing automatic payment.
	 */
	action actionCheckExitingAutoPay [ 
		switch ApiPay.automaticPayment() [
			case "true" actionExistingAutoPay
			default	actionCheckSavedSource
		]
	]
	
 	/*************************
	 * 15a. Cannot create the automatic payment, one already exists. 
	 */
	 action actionExistingAutoPay [
	    auditLog(audit_agent_pay.create_auto_payment_for_agent_failure) [
	   		sCustomerId sAccountId
	    ]
		Log.warn("createAutomaticPaymentRuleForAgent", sCustomerId, sAccountId, sDateRule, sAmountRule, sCountRule, "Automatic payment exists.")

		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "210")
		JsonResponse.setBoolean("success",   "false")
		JsonResponse.setString("payload",    "Automatic payment exists.")
		JsonResponse.setString("error",      "automatic_payment_exists")

		foreignHandler JsonResponse.errorWithData("210")
	 ]

 	/*************************
	 * 16. Check for an existing automatic payment.
	 */
	action actionCheckSavedSource [ 
		switch sIsOneTime [
			case "true" actionMethodNotSaved
			default	actionCreateAutomatic
		]
	]

 	/*************************
	 * 16a. Payment method was not saved. 
	 */
	 action actionMethodNotSaved [
	    auditLog(audit_agent_pay.create_auto_payment_for_agent_failure) [
	   		sCustomerId sAccountId
	    ]
		Log.warn("createAutomaticPaymentRuleForAgent", sCustomerId, sAccountId, sDateRule, sAmountRule, sCountRule, "Payment method is one time.")

		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "210")
		JsonResponse.setBoolean("success",   "false")
		JsonResponse.setString("payload",    "Payment source was not saved.")
		JsonResponse.setString("error",      "source_not_saved")

		foreignHandler JsonResponse.errorWithData("210")
	 ]

 	/*************************
	 * 15. Issue the payment request.
	 */
	action actionCreateAutomatic [
		createRequest.OLD_SOURCE_ID = ""
		createRequest.AUTOMATIC_ID  = ""

		ApiPay.createAutomaticJson(createRequest.GROUPING_JSON)
		ApiPay.walletToken        (createRequest.SOURCE_ID)
		ApiPay.userid             (createRequest.USER_ID)

		ApiPay.setTransactionOneTimeInProgress(sTransactionId)	
		switch apiCall Payment.SetAutomaticPayment(createRequest, createResponse, status) [
            case apiSuccess actionCreateAutomaticHistory
            default actionFailureResponse
        ]	
	]

 	/*************************
	 * 16. Record the successful transaction.
	 */
	action actionCreateAutomaticHistory [
		ApiPay.userid(historyRequest.USER_ID)
		
    	historyRequest.AUTOMATIC_ID  = createResponse.AUTOMATIC_ID
    	historyRequest.CONFIG_CHANGE = "Automatic payment created."
		
		switch apiCall Payment.SetAutomaticPaymentHistory(historyRequest, status) [
            default actionSuccessResponse
        ]	
	]
	
 	/*************************
	 * 17. Response with success back to the client.
	 */
	 action actionSuccessResponse [
		JsonResponse.reset()
		JsonResponse.setString("status", "created")
		ApiPay.setAutomaticPayment("true")

	    auditLog(audit_agent_pay.create_auto_payment_for_agent_success) [
	   		sCustomerId sAccountId
	    ]
		Log.^success("createAutomaticPaymentRuleForAgent", sCustomerId, sAccountId, sDateRule, sAmountRule, sCountRule, "success")

		ApiPay.setTransactionComplete(sTransactionId)
		foreignHandler JsonResponse.send()	 	
	 ]

 	/*************************
	 * 16a. The operation failed.
	 */
	 action actionFailureResponse [
		ApiPay.setTransactionError(sTransactionId)
	    auditLog(audit_agent_pay.create_auto_payment_for_agent_failure) [
	   		sCustomerId sAccountId
	    ]
		Log.warn("createAutomaticPaymentRuleForAgent", sCustomerId, sAccountId, sDateRule, sAmountRule, sCountRule, "failure")

		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "210")
		JsonResponse.setBoolean("success",   "false")
		JsonResponse.setString("payload",    "Could not create the automatic payment.")
		JsonResponse.setString("error",      "automatic_payment_not_created")

		foreignHandler JsonResponse.errorWithData("210")
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

		Log.error("makeOneTimePaymentForAgent", sTransactionId, sDateRule, sAmountRule, sCountRule, sErrorDesc)

		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]

    /********************************
     * Invalid Security Token
     */
    action actionInvalidSecurityToken [
		ApiPay.setTransactionError(sTransactionId)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success",   "false")
		JsonResponse.setString("payload",    "Invalid security token.")
		JsonResponse.setString("error",      "invalid_security_token")

		Log.error("makeOneTimePaymentForAgent", sTransactionId, sDateRule, sAmountRule, sCountRule, "Invalid security token.")
		
		foreignHandler JsonResponse.errorWithData("401")
    ]

    /********************************
     * Send a response back that we could not decode some of the data.
     */
    action actionDecodeFailure [
		ApiPay.setTransactionError(sTransactionId)
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sDecodeText)
		JsonResponse.setString("error", sDecodeCode)

		Log.error("makeOneTimePaymentForAgent", sTransactionId, sDateRule, sAmountRule, sCountRule, sErrorDesc)

		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]    
]
