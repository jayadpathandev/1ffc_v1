useCase paymentGetAcctInfoByExtAcct [
	 
	 
	 
	 documentation [
	 	
        preConditions: [[
            1.  None
        ]]
        triggers: [[
            1.  Shortcut URL accessed with token and external acct Number (displayName)
        ]]
        postConditions: [[
            1. Primary -- Sends back the json success with internal acct and customer id or error response.
        ]]
    ]
    
    /* entry point of this UC file */
    startAt validateToken
    
    /*
     *  External call used by migration program
     */
	shortcut getAcctInfoByExtAcct(validateToken) [
		securityToken, 
		externalAcctId  // -- sometimes externalAccount, DisplayName, etc.
	]

   /*************************
	* DATA ITEMS SECTION
	*************************/  

	importJava Config(com.sorrisotech.utils.AppConfig)
    
	native string securityToken = ""
	native string externalAcctId = ""
	native string cszSecurityTokenValue = "c56169b8-7bd5-4843-a005-b5e2a184471f" // -- our secret key --
	native string cszBillPaymentGroup = Config.get("1ffc.bill.group")
	
	/**
	 * Return values:
	 */
	native string cszCustomerId = "customerId"
	native string cszInternalAcctId = "internalAcctId"
	native string retCustomerId = ""
	native string retInternalAcctId = ""
	
	serviceStatus srAcctInfoStatus
	serviceParam (AccountStatus.GetAccountInfoFromExtAcct) spGetAcctInfoParams
	serviceResult (AccountStatus.GetAccountInfoFromExtAcct) srGetAcctInfoResult
	
	/*
	 * Response codes:
	 * 
	 * Success:    200 ConvenienceFee returned successfully.
	 * 
	 * Failure:    400 Invalid input. See required parameters
	 *             500 Error occurred while processing
	 * 
	 */
    importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
                     
	native string successCode       = "200"	
	native string invalidInput   	= "400"	
	native string noInfo 			= "402"
	
	native string statusSuccess		= "Success"
	native string statusInvalid		= "Invalid input provided"
	native string statusNoInfo		= "No information for account"
	
	native string resultCode 		= ""        
	native string httpStatus		= ""
	
   /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

	/**
	 * 1. System checks security token to see if this is a valid caller.
	 */
	action validateToken [
		if securityToken == cszSecurityTokenValue then
			getAccountInformation
		else
			validationError
	]
	
	/**
	 * 2. System retrieves internal account information based on external account number
	 *		if that information is available.
	 */
	action getAccountInformation [
		spGetAcctInfoParams.billPaymentGroup = cszBillPaymentGroup
		spGetAcctInfoParams.externalAccount = externalAcctId
		
		switch apiCall AccountStatus.GetAccountInfoFromExtAcct(spGetAcctInfoParams, srGetAcctInfoResult, srAcctInfoStatus) [
			case apiSuccess setResponseValues
			default noAccountError
		]
	]
	
	/**
	 * 3. System found the internal account information for the specified external
	 *		account. Sets value for return of web service call.
	 */
	action setResponseValues [
		resultCode = successCode
		httpStatus = statusSuccess
    	retCustomerId = srGetAcctInfoResult.customerId
    	retInternalAcctId = srGetAcctInfoResult.internalAccount

		goto (returnResponse)
	]
	
	/**
	 * 1A. Return validation error.  Bad security token.
	 */
	action validationError [
		resultCode = invalidInput
		httpStatus = statusInvalid
    	
		goto (returnResponse)
	]
	
	/**
	 * 2A. Return information no info for this account
	 */
	action noAccountError[
		resultCode = noInfo
		httpStatus = statusNoInfo
		
		goto (returnResponse)
	]
	
	/**
	 * 4. package up valid response and return it.
	 */
	action returnResponse [
		
		JsonResponse.reset()
		JsonResponse.setString("resultCode", resultCode)
		JsonResponse.setString("httpStatus", httpStatus)
		JsonResponse.setString(cszCustomerId, retCustomerId)
		JsonResponse.setString(cszInternalAcctId, retInternalAcctId)
		
		foreignHandler JsonResponse.send() 
	]
]