useCase paymentContractedAmount [
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
    startAt validateUserId
    
	shortcut getMonthlyContractedAmount(validateUserId) [
    	user_id
        pay_group,
	    pay_account_number
    ]

    /*************************
	* DATA ITEMS SECTION
	*************************/  
    importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	
	native string user_id				= ""
	native string pay_group				= ""
	native string pay_account_number	= ""
	
	native string monthlyContractedAmount = ""
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
	native string generalError	    = "500"
	
	native string statusSuccess		= "Success"
	native string statusFailure		= "Internal server error"
	native string statusInvalid		= "Invalid input provided"
	
	native string resultCode 		= ""        
	native string httpStatus		= ""
	
	serviceStatus srGetContractedPaymentStatus
	serviceParam (AccountStatus.GetContractualMonthlyPaymentAmount) spGetContractedPaymentParams
	serviceResult (AccountStatus.GetContractualMonthlyPaymentAmount) srGetContractedPaymentResult
        
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
		
	action fetchMonthlyContractedAmount[
		spGetContractedPaymentParams.account = pay_account_number
		spGetContractedPaymentParams.paymentGroup = pay_group
		spGetContractedPaymentParams.user = user_id
		
		switch apiCall AccountStatus.GetContractualMonthlyPaymentAmount(spGetContractedPaymentParams, srGetContractedPaymentResult, srGetContractedPaymentStatus) [
			case apiSuccess monthlyContractedAmountResponse
			default contractedAmountErrorResponse
		]
	]
	
	action monthlyContractedAmountResponse [
		monthlyContractedAmount = srGetContractedPaymentResult.monthlyPaymentAmount
		resultCode = successCode
		httpStatus = statusSuccess
		
		goto(returnResponse)
	]
	
	action contractedAmountErrorResponse [
		monthlyContractedAmount = "0"
		resultCode = generalError
		httpStatus = statusFailure
		
		goto(returnResponse)
    ]

	action validateUserId [
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
	 		fetchMonthlyContractedAmount
	]
	
	action validationError [
		resultCode = invalidInput
		httpStatus = statusInvalid
		monthlyContractedAmount = "0"
    	
		goto (returnResponse)
	]
	
	action returnResponse [
		
		JsonResponse.reset()
		JsonResponse.setString("resultCode", resultCode)
		JsonResponse.setString("httpStatus", httpStatus)
		JsonResponse.setString("monthlyContractedAmount", monthlyContractedAmount)
		
		foreignHandler JsonResponse.send()
	]

]
	
