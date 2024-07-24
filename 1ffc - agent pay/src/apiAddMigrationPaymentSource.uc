useCase apiAddMigrationPaymentSource
[
	startAt init
	shortcut addMigrationPaymentSource(init)

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
	importJava Config(com.sorrisotech.utils.AppConfig)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
	importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	importJava Log(api.Log)

	native string sServiceUserName  = Config.get("service.api.username")
    native string sServiceNameSpace = Config.get("service.api.namespace")
	
	// -- data from request --
	native string sSecurityToken 	= JsonRequest.value("securityToken")
	native string sTransactionId 	= JsonRequest.value("transactionId")
	native string sCustomerId 		= JsonRequest.value("customerId")
	native string sAccountId	   	= JsonRequest.value("accountId")
    native string sSourceType		= JsonRequest.value("sourceType")
   	native string sSourceValue		= JsonRequest.value("sourceValue")
  	native string sAccountHolder	= JsonRequest.value("accountHolder")
  	native string sMaskedNumber		= JsonRequest.value("maskedNumber")
  	native string sExpiration		= JsonRequest.value("expiration")
  	
  	native string sResponseCode = ""
	
	serviceStatus ssAddPaymentSource
    serviceParam (AgentPay.AddMigratedPaymentSource) spAddPaymentSource
    serviceResult(AgentPay.AddMigratedPaymentSource) srAddPaymentSource

	native string sErrorStatus = ""
	native string sErrorDesc = ""
	native string sErrorCode = "" 
	
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
    		default        actionInvalidRequest
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
    		actionInvalidRequest 
    ]

	/*************************
	 * 3. Verify the transactionId was provided.
	 */
    action verifyTransactionId [
    	sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [transactionId]."
    	sErrorCode   = "no_transaction_id"
    	
    	if sTransactionId != "" then 
    		verifyCustomerId
    	else
    		actionInvalidRequest 
    ]

	/*************************
	 * 4. Verify that the customerID was provided.
	 */
	action verifyCustomerId [
   		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [customerId]."
    	sErrorCode   = "no_customerId"
 
    	if sCustomerId != "" then 
    		verifyAccountId
    	else
    		actionInvalidRequest 
  		
	]
	
	/*************************
	 * 5. Verify that the accountId was provided.
	 */
	action verifyAccountId [
   		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [accountId]."
    	sErrorCode   = "no_accountId"

    	if sAccountId != "" then 
    		verifySourceType
    	else
    		actionInvalidRequest
	]
	
	/*************************
	 * 6. Verify that the sourceType was provided and 
	 * 		is a valid value.
	 */
	action verifySourceType [
   		sErrorStatus = "400"
    	sErrorDesc   = "Invalid or missing [sourceType]."
    	sErrorCode   = "invalid_sourceType"
		
		switch sSourceType [
			case "debit"	verifySourceValue
			case "credit"	verifySourceValue
			case "bank"		verifySourceValue
			case "sepa"		verifySourceValue
			default actionInvalidRequest
		]
	]
	
	/*************************
	 * 7. Verify that the sourceValue was provided.
	 */
	action verifySourceValue [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [sourceValue]."
    	sErrorCode   = "no_sourceValue"
		
		if sSourceValue != "" then
			verifyAccountHolder
		else
			actionInvalidRequest
	]
    
    /*************************
	 * 8. Verify that the Account Holder name was provided.
	 */
    action verifyAccountHolder [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [accountHolder]."
    	sErrorCode   = "no_accountHolder"
		
		if sAccountHolder != "" then
			verifyMaskedNumber
		else
			actionInvalidRequest
    ]
    
    /*************************
	 * 9. Verify that the masked card number was provided.
	 */
    action verifyMaskedNumber [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [maskedNumber]."
    	sErrorCode   = "no_maskedNumber"
    	
		if sMaskedNumber != "" then
			verifyExpiration
		else
			actionInvalidRequest
    ]
    
    /*************************
	 * 10. Verify that the expiration was provided.
	 */
    action verifyExpiration [
  		sErrorStatus = "400"
    	sErrorDesc   = "Missing required parameter [expiration]."
    	sErrorCode   = "no_expiration"
    	
		if sExpiration != "" then
			authenticateRequest
		else
			actionInvalidRequest
    ]
    
 	/*************************
	 * 11. Authenticate the request.
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
        if failure then actionInternalError
    ]
    
   	/*************************
	 * 12. Load the transaction.
	 */
	action actionLoadTransaction [ 
		sErrorStatus = "402"
    	sErrorDesc   = "Invalid [transactionId] no matching transaction."
    	sErrorCode   = "invalid_transaction_id"

		switch ApiPay.load(sTransactionId) [
			case "true" addPaymentSource
			default	actionInternalError
		]
	]	
    
   	/*************************
	 * 13. Add the payment source to the payment server.
	 */
    action addPaymentSource [
    	spAddPaymentSource.customerId = sCustomerId
    	spAddPaymentSource.accountId = sAccountId
    	spAddPaymentSource.sourceType = sSourceType
    	spAddPaymentSource.sourceValue = sSourceValue
    	spAddPaymentSource.accountHolder = sAccountHolder
    	spAddPaymentSource.maskedNumber = sMaskedNumber
    	spAddPaymentSource.expiration = sExpiration
    	
    	switch apiCall AgentPay.AddMigratedPaymentSource(spAddPaymentSource, srAddPaymentSource, ssAddPaymentSource) [
            case apiSuccess checkSuccessResponseStatus
            default actionInternalError
        ]
    ]
    
  	/*************************
	 * 14. Check Results, save wallet if success, otherwise.. invvalid
	 */
    action checkSuccessResponseStatus [
		sErrorStatus = "402"
    	sErrorDesc   = "Failed to add payment source."
    	sErrorCode   = "Failed_AddSourceCall"
    	
    	sResponseCode = srAddPaymentSource.addResponseStatus
    	
    	switch sResponseCode [
    		case "201" saveWallet
    		case "400" actionInvalidRequest
    		default actionInternalError
    	]
    ]

  	/*************************
	 * 12. Success saves wallet item to transaction and 
	 * 		moves transaction state forward
	 */
	action saveWallet [
		ApiPay.setWallet(srAddPaymentSource.token)
		goto(actionSuccessResponse)
	]
	
  	/*************************
	 * 13. Send success response
	 */
	action actionSuccessResponse [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "201")
		JsonResponse.setBoolean("success", "true")
		JsonResponse.setString("payload", "Migrated token added")
		JsonResponse.setString("error", "success")

	    auditLog(audit_agent_pay.add_migrated_payment_source_success) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.^success("addMigrationPaymentSource", sCustomerId, sAccountId, "the specified payment source is added.")

		foreignHandler JsonResponse.sendStatus("201")	 	
	 ]

    action actionInvalidSecurityToken [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "401")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid security token.")
		JsonResponse.setString("error", "InvalidToken")

	    auditLog(audit_agent_pay.add_migrated_payment_source_failure) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.error("addMigrationPaymentSource", sCustomerId, sAccountId, "Invalid security token.")
		
		foreignHandler JsonResponse.errorWithData("401")
    ]
    
    action actionInvalidRequest [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorCode)
		JsonResponse.setString("error", sErrorCode)

	    auditLog(audit_agent_pay.add_migrated_payment_source_failure) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.error("addMigrationPaymentSource", sCustomerId, sAccountId, "Invalid request parameter.")
		
		foreignHandler JsonResponse.errorWithData(sErrorStatus)
    ]
    
     action actionInternalError [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", sErrorStatus)
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", sErrorDesc)
		JsonResponse.setString("error", sErrorCode)

	    auditLog(audit_agent_pay.add_migrated_payment_source_failure) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.error("addMigrationPaymentSource", sCustomerId, sAccountId, "Internal software error.")
		
		foreignHandler JsonResponse.errorWithData("402")
    ]
    
]
