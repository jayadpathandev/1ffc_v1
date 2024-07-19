useCase apiAddMigrationPaymentSource
[
	startAt init
	shortcut addMigrationPaymentSource(init)

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
    native string sSourceType		= JsonRequest.value("sourceType")
   	native string sSourceValue		= JsonRequest.value("sourceValue")
  	native string sAccountHolder	= JsonRequest.value("countHolder")
  	native string sMaskedNumber		= JsonRequest.value("maskedNumber")
  	native string sExpiration		= JsonRequest.value("expiration")
  	
  	native string sResponseCode = ""
	
	serviceStatus ssAddPaymentSource
    serviceParam (AgentPay.AddMigratedPaymentSource) spAddPaymentSource
    serviceResult(AgentPay.AddMigratedPaymentSource) srAddPaymentSource
	
   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Parse the JSON request.
	 */
    action init[

    	switch JsonRequest.load() [
    		case "success" verifyToken
    		default        actionInvalidRequest
    	]
    ]
    
	/*************************
	 * 2. Verify that the securityToken was provided.
	 */
    action verifyToken [    

    	if sSecurityToken != "" then 
    		verifyCustomerId
    	else
    		actionInvalidRequest 
    ]

	/*************************
	 * 3. Verify that the customerID was provided.
	 */
	action verifyCustomerId [

    	if sCustomerId != "" then 
    		verifyAccountId
    	else
    		actionInvalidRequest 
  		
	]
	
	/*************************
	 * 4. Verify that the accountId was provided.
	 */
	action verifyAccountId [

    	if sAccountId != "" then 
    		verifySourceType
    	else
    		actionInvalidRequest
	]
	
	/*************************
	 * 5. Verify that the sourceType was provided.
	 */
	action verifySourceType [
		
		if sSourceType != "" then
			verifySourceValue
		else
			actionInvalidRequest
	]
	
	/*************************
	 * 6. Verify that the sourceValue was provided.
	 */
	action verifySourceValue [
		
		if sSourceValue != "" then
			verifyAccountHolder
		else
			actionInvalidRequest
	]
    
    /*************************
	 * 7. Verify that the Account Holder name was provided.
	 */
    action verifyAccountHolder [
		
		if sAccountHolder != "" then
			verifyMaskedNumber
		else
			actionInvalidRequest
    ]
    
    /*************************
	 * 7. Verify that the masked card number was provided.
	 */
    action verifyMaskedNumber [
    	
		if sMaskedNumber != "" then
			verifyExpiration
		else
			actionInvalidRequest
    ]
    
    /*************************
	 * 8. Verify that the expiration was provided.
	 */
    action verifyExpiration [
    	
		if sExpiration != "" then
			authenticateRequest
		else
			actionInvalidRequest
    ]
    
 	/*************************
	 * 9. Authenticate the request.
	 */
    action authenticateRequest [

    	login(
            username: sServiceUserName
            password: sSecurityToken
            namespace: sServiceNameSpace
        )
        if success then addPaymentSource
        if authenticationFailure then actionInvalidSecurityToken
        if failure then actionInternalError
    ]
    
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
    
    action checkSuccessResponseStatus [
    	
    	sResponseCode = srAddPaymentSource.addResponseStatus
    	
    	switch sResponseCode [
    		case "201" actionSuccessResponse
    		case "400" actionInvalidRequest
    		default actionInternalError
    	]
    ]
	
	action actionSuccessResponse [
		JsonResponse.reset()

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
		JsonResponse.setNumber("statuscode", "400")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Invalid request parameter.")
		JsonResponse.setString("error", "InvalidAcctInfo")

	    auditLog(audit_agent_pay.add_migrated_payment_source_failure) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.error("addMigrationPaymentSource", sCustomerId, sAccountId, "Invalid request parameter.")
		
		foreignHandler JsonResponse.errorWithData("400")
    ]
    
     action actionInternalError [
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode", "402")
		JsonResponse.setBoolean("success", "false")
		JsonResponse.setString("payload", "Internal software error.")
		JsonResponse.setString("error", "InternalError")

	    auditLog(audit_agent_pay.add_migrated_payment_source_failure) [
	   		sCustomerId sAccountId sSourceType sSourceValue sAccountHolder sMaskedNumber
	    ]
		Log.error("addMigrationPaymentSource", sCustomerId, sAccountId, "Internal software error.")
		
		foreignHandler JsonResponse.errorWithData("402")
    ]
    
]
