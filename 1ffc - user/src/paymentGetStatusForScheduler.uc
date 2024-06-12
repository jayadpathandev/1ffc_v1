useCase paymentGetStatusForScheduler [
   /**
    *  author: Rohit Singh
    *  created: 31-January-2024
    *
    *  Primary Goal:
    *       1. Get the status information for an account
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    * 		1.1 2024-June-09	jak		Expanded result for update to payment scheduler rules
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
    startAt validateToken
    
    /* */
	shortcut getStatusForScheduler(validateUserId) [
    	securityToken,
    	userIdentifier,
        payGroup,
	    accountId
    ]

    /*************************
	* DATA ITEMS SECTION
	*************************/  
    importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
	
	native string securityToken 	= ""
	native string userIdentifier	= ""
	native string payGroup			= ""
	native string accountId			= ""
	
	// -- items returned in response -- 
	native string cszMonthlyAmt = "monthlyContractedAmount"
	native string cszMaxPaymentAmt = "maxPaymentAmount"
	native string cszDebitFeeAmt = "debitFeeAmount"
	native string cszAccountCanAutoPay = "accountCanAutoPay"
	native string cszAccountCanUseBankPay = "accountCanUseBankPay"
	native string cszPaymentDueDate = "paymentDueDate"
	native string cszSecurityTokenValue = "f4f1a3fb-5b06-45e2-b668-85991cb53a49"

	native string retMnthlyContractedAmount 	= ""
	native string retMaxPaymentAmt				= ""
	native string retDebitFeeAmt				= ""
	native string retAccountCanAutoPay			= ""
	native string retAccountCanUseBankPay		= ""
	native string retPaymentDueDate				= ""

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
	
	serviceStatus srGetAccountStatusStatus
	serviceParam (AccountStatus.GetStatus) spGetAccountStatusParams
	serviceResult (AccountStatus.GetStatus) srGetAccountStatusResult
        
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

	/**
	 * 1. System checks security token to see if this is our payment scheduler 
	 *		calling.
	 */
	action validateToken [
		if securityToken == cszSecurityTokenValue then
			validateUserId
		else
			validationError
	]	

	/**
	 * 2. System checks to be sure we have a user id.
	 */
	action validateUserId [
		if userIdentifier == "" then  
			validationError
	    else
	    	validatePayGroup
	]

	/**
	 *  3. System checks to be sure we have a paymnent group.
	 */
	action validatePayGroup [
		if payGroup == "" then 
			validationError
	 	else
	 		validateAccountIdentifier
	]
	
	/**
	 *  4. System checks to be sure its been given an account id.
	 */
	action validateAccountIdentifier [
		if accountId == "" then 
			validationError
	 	else
	 		fetchMainStatus
	]
	
	/**
	 * 5. System retrieves account status
	 */
	action fetchMainStatus [
		spGetAccountStatusParams.account = accountId
		spGetAccountStatusParams.paymentGroup = payGroup
		spGetAccountStatusParams.user = userIdentifier
		
		switch apiCall AccountStatus.GetStatus(spGetAccountStatusParams, srGetAccountStatusResult, srGetAccountStatusStatus) [
			case apiSuccess getStatusSuccess
			default contractedAmountErrorResponse
		]
	]
	
	/**
	 * 6. System assigns results that don't need further processing
	 */
	action getStatusSuccess [
		retMaxPaymentAmt = srGetAccountStatusResult.maximumPaymentAmount
		retDebitFeeAmt = srGetAccountStatusResult.debitConvenienceFeeAmt
		retAccountCanAutoPay = srGetAccountStatusResult.automaticPaymentStatus
		retAccountCanUseBankPay	= srGetAccountStatusResult.achEnabled
		retPaymentDueDate	= srGetAccountStatusResult.paymentDueDate
		
		goto (getCanAutoPay)
	]
	
	/**
	 * 7. System sees if account can autopay.
	 */
	action getCanAutoPay [
		retAccountCanAutoPay = "false"
		switch srGetAccountStatusResult.automaticPaymentStatus [
			case "eligible" setAutoPayTrue	
			case "enrolled" setAutoPayTrue
			default 		getCanUseBankPay
		]
	]
	
	/**
	 * 8. System sets autopay result true,
	 */
	action setAutoPayTrue [
		retAccountCanAutoPay = "true"
		goto (getCanUseBankPay)
	]

	/**
	 * 9. System sees if account can bank pay.
	 */
	action getCanUseBankPay [
		retAccountCanUseBankPay = "false"
		switch srGetAccountStatusResult.achEnabled [
			case "enabled" 	setBankPayTrue
			default 		fetchMonthlyContractedAmount
		]
	]
	
	/**
	 * 10. system sets bank pay result true.
	 */
	action setBankPayTrue [
		retAccountCanUseBankPay = "true"
		goto (fetchMonthlyContractedAmount)
	]
	
	/**
	 * 11. system retrieves monthly contracted amount.
	 */
	action fetchMonthlyContractedAmount[
		spGetContractedPaymentParams.account = accountId
		spGetContractedPaymentParams.paymentGroup = payGroup
		spGetContractedPaymentParams.user = userIdentifier
		
		switch apiCall AccountStatus.GetContractualMonthlyPaymentAmount(spGetContractedPaymentParams, srGetContractedPaymentResult, srGetContractedPaymentStatus) [
			case apiSuccess monthlyContractedAmountResponse
			default contractedAmountErrorResponse
		]
	]
	
	/**
	 * 12. system assigns monthly contracted amount to value for return
	 */
	action monthlyContractedAmountResponse [
		retMnthlyContractedAmount = srGetContractedPaymentResult.monthlyPaymentAmount
		resultCode = successCode
		httpStatus = statusSuccess
		
		goto(returnResponse)
	]
	
	/**
	 * 13. Return a processing error, usually Status API Call.
	 */
	action contractedAmountErrorResponse [
		retMnthlyContractedAmount = "0"
		resultCode = generalError
		httpStatus = statusFailure
		
		goto(returnResponse)
    ]
	
	/**
	 * 14. return validation error.  Something is wrong in the request.
	 */
	action validationError [
		resultCode = invalidInput
		httpStatus = statusInvalid
		retMnthlyContractedAmount = "0"
    	
		goto (returnResponse)
	]
	
	/**
	 * 15. package up valid response and return it.
	 */
	action returnResponse [
		
		JsonResponse.reset()
		JsonResponse.setString("resultCode", resultCode)
		JsonResponse.setString("httpStatus", httpStatus)
		JsonResponse.setString(cszMonthlyAmt, retMnthlyContractedAmount)
		JsonResponse.setString(cszDebitFeeAmt, retDebitFeeAmt)
		JsonResponse.setString(cszMaxPaymentAmt, retMaxPaymentAmt)
		JsonResponse.setString(cszAccountCanAutoPay, retAccountCanAutoPay)
		JsonResponse.setString(cszAccountCanUseBankPay, retAccountCanUseBankPay)
		JsonResponse.setString(cszPaymentDueDate, retPaymentDueDate)
		
		foreignHandler JsonResponse.send()
	]

]
	
