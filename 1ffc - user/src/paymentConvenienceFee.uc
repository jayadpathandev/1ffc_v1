useCase paymentConvenienceFee [
   /**
    *  author: Rohit Singh
    *  created: 31-January-2024
    *
    *  Primary Goal:
    *       1. Get the convenienceFee for an account
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        
    */

    documentation [
        preConditions: [[
            1.
        ]]
        triggers: [[
            1.
        ]]
        postConditions: [[
            1. Primary -- Sends back the json succes or error response.
        ]]
    ]
    
    /* entry point of this UC file */
    startAt init
    
    /* Name of the endpoint which we will call to get the convenienceFee value  */
    shortcut getConvenienceFee(init) [
        pay_group,
	    pay_account_number
    ]
    
    shortcut getConvenienceFeePay(convenienceFeeInit)

    /*************************
	* DATA ITEMS SECTION
	*************************/  
    importJava Session(com.sorrisotech.app.utils.Session)
    importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
    importJava JsonResponse(com.sorrisotech.fffc.user.JsonResponse)
    importJava Config(com.sorrisotech.utils.AppConfig)

    /* Status service goes here */
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetDebitConvenienceFee) srGetDebitConvenienceFeeParams
    serviceResult (AccountStatus.GetDebitConvenienceFee) srGetDebitConvenienceFeeResult	
	
	native string user_id				= Session.getUserId()
	native string pay_group				= ""
	native string pay_account_number	= ""
	
	native string auth_code				= JsonRequest.value("authCode")
	native string user_id_pay 			= JsonRequest.value("userId")
    native string pay_group_pay			= JsonRequest.value("payGroup")
	native string account_number_pay	= JsonRequest.value("accountNumber")
	
	native string convenienceFeeAmount	= ""
	native string convenienceFeeAmountPay = ""

	native string sServiceUserName    	= Config.get("service.api.username")
    native string sServiceNameSpace   	= Config.get("service.api.namespace")
	
	/*
	 * Response codes:
	 * 
	 * Success:    200 ConvenienceFee returned successfully.
	 * 
	 * Failure:    400 Invalid input. See required parameters
	 *             500 Error occurred while processing
	 * 
	 */
                     
	native string successCode       = "200"	
	native string invalidInput   	= "400"
	native string authenticationError = "401"	
	native string generalError	    = "500"
	
	native string statusSuccess		= "Success"
	native string statusFailure		= "Internal server error"
	native string statusInvalid		= "Invalid input provided"
	
	native string resultCode 		= ""        
	native string httpStatus		= ""
	
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
		
	/* 1. Initialize */
	/* 1.1. Validate userId */
	action init [
		if user_id == "" then  
			validationError
	    else
	    	validatePayGroup
	]

	/* 2. Validate PayGroup */
	action validatePayGroup [
		if pay_group == "" then 
			validationError
	 	else
	 		validateAccountNumber
	]
	
	/* 3. Validate PayGroup */
	action validateAccountNumber [
		if pay_account_number == "" then 
			validationError
	 	else
	 		getConvenienceFee
	]
	
	/* 4.1. get convenience fee value */
    action getConvenienceFee [
		srGetDebitConvenienceFeeParams.user 		= user_id
		srGetDebitConvenienceFeeParams.paymentGroup = pay_group
		srGetDebitConvenienceFeeParams.account		= pay_account_number
		
		// getting the convenienceFee value
   		switch apiCall AccountStatus.GetDebitConvenienceFee(srGetDebitConvenienceFeeParams, srGetDebitConvenienceFeeResult, srAccountStatusCode) [
    		case apiSuccess getConvenienceFeeSuccessAudit
    		default getConvenienceFeeErrorAudit
    	]
	]	
	
	/* 5.1. audit log for success */
	action getConvenienceFeeSuccessAudit [
		resultCode = successCode
		httpStatus = statusSuccess
		convenienceFeeAmount = srGetDebitConvenienceFeeResult.convenienceFeeAmt
		
    	auditLog(audit_payment.convenience_fee_success) [
			user_id
    		pay_group
    		pay_account_number 
			resultCode
			convenienceFeeAmount
    	]
    	goto (returnSuccess)
    ]
	
	/* 6.1. Json Success.*/
	json returnSuccess [	
		display resultCode
		display httpStatus
		display convenienceFeeAmount	    
	]
	
	/* 5.2. audit log for success */
	action getConvenienceFeeErrorAudit [
		resultCode = generalError
		httpStatus = statusFailure
		convenienceFeeAmount = "0"
		auditLog(audit_payment.convenience_fee_failure) [
			user_id
    		pay_group
    		pay_account_number 
			resultCode
			convenienceFeeAmount
    	]	
    	goto (returnError)
    ]		

	/* 4.2. Invalid input data  */
	action validationError [
		resultCode = invalidInput
		httpStatus = statusInvalid
		convenienceFeeAmount = "0"
    	auditLog(audit_payment.convenience_fee_invalid_input) [
    		user_id
    		pay_group
    		pay_account_number
    	]			
		goto (returnError)
	]
	
		/* 6.2 Json error.*/
	json returnError [
		display resultCode
		display httpStatus
		display convenienceFeeAmount
	]
	
	action convenienceFeeInit [
		JsonRequest.load()
		
		goto(validateAuthCode)
	]
	
	action validateAuthCode [
		if auth_code == "" then
			validationErrorPay
		else
			validateUserIdPay
	]
	
	action validateUserIdPay [
		if user_id_pay == "" then  
			validationErrorPay
	    else
	    	validatePayGroupPay
	]

	/* 2. Validate PayGroup */
	action validatePayGroupPay [
		if pay_group_pay == "" then 
			validationErrorPay
	 	else
	 		validateAccountNumberPay
	]
	
	/* 3. Validate PayGroup */
	action validateAccountNumberPay [
		if account_number_pay == "" then 
			validationErrorPay
	 	else
	 		authenticateRequest
	]
	
	action authenticateRequest  [                                     
        authenticate(          
            username: sServiceUserName
            password: auth_code
            namespace: sServiceNameSpace            
            )
        if success then getConvenienceFeePay
        if authenticationFailure then actionAuthFailure
        if failure then actionAuthFailure
    ]  
	
	action actionAuthFailure [
		convenienceFeeAmountPay = "0" 
		resultCode = authenticationError
		httpStatus = "Unauthorized Request"
		goto(returnErrorPay)
    ]   
	
	action getConvenienceFeePay [
		srGetDebitConvenienceFeeParams.user 		= user_id_pay
		srGetDebitConvenienceFeeParams.paymentGroup = pay_group_pay
		srGetDebitConvenienceFeeParams.account		= account_number_pay
		
		// getting the convenienceFee value
   		switch apiCall AccountStatus.GetDebitConvenienceFee(srGetDebitConvenienceFeeParams, srGetDebitConvenienceFeeResult, srAccountStatusCode) [
    		case apiSuccess getConvenienceFeeSuccessResponse
    		default getConvenienceFeeErrorResponse
    	]
	]
	
	action getConvenienceFeeSuccessResponse [
		
		convenienceFeeAmountPay = srGetDebitConvenienceFeeResult.convenienceFeeAmt
		
		JsonResponse.reset()
		JsonResponse.setString("convenienceFeePay", convenienceFeeAmountPay)
		
		logout()
		
	    foreignHandler JsonResponse.send()
    ]
		
		/* 5.2. audit log for success */
	action getConvenienceFeeErrorResponse [
		convenienceFeeAmountPay = "0"
		
		JsonResponse.reset()
		JsonResponse.setString("convenienceFeePay", convenienceFeeAmountPay)
		JsonResponse.setString("Error","Couldn't retrieve convenienceFeeAmount")
		
	    foreignHandler JsonResponse.send()
	    
	 ]		
	
		
	action validationErrorPay [
		resultCode = invalidInput
		httpStatus = statusInvalid
		convenienceFeeAmountPay = "0"
    	auditLog(audit_payment.convenience_fee_invalid_input) [
    		user_id_pay
    		pay_group_pay
    		account_number_pay
    	]			
		goto (returnErrorPay)
	]
	
	/* 6.2 Json error.*/
	action returnErrorPay [
		
		JsonResponse.reset()
		JsonResponse.setString("resultCode", resultCode)
		JsonResponse.setString("httpStatus", httpStatus)
		JsonResponse.setString("convenienceFeeAmount", convenienceFeeAmountPay)
		
		logout()
		
		foreignHandler JsonResponse.errorWithResponse(resultCode)
	]

]
	
