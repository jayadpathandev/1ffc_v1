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
    *        1.1 2024-June-20
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
    startAt loadGetConvenienceFeeRequest

	shortcut getConvenienceFee(loadGetConvenienceFeeRequest)
	
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
    importJava JsonResponse(com.sorrisotech.app.common.JsonResponse)
    
    /* Status service goes here */
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetDebitConvenienceFee) srGetDebitConvenienceFeeParams
    serviceResult (AccountStatus.GetDebitConvenienceFee) srGetDebitConvenienceFeeResult	
	
	native string szUserId = Session.getUserId()
	native string szPaymentGroup = JsonRequest.value("paymentGroup")
	native string szAccountNumber = JsonRequest.value("accountNumber")
	
	native string convenienceFeeAmount = ""

	action loadGetConvenienceFeeRequest [
		JsonRequest.load()
		goto(validatePaymentGroup)
	]
	
	action validatePaymentGroup [
		if szPaymentGroup != "" then
			validateAccountNumber
		else
			invalidPaymentGroup
	]
	
	action invalidPaymentGroup [
		
		JsonResponse.reset()
		
		JsonResponse.setString("convenienceFee", "0")
		JsonResponse.setString("message", "Invalid payment group")
		JsonResponse.setString("status", "400")
		
		foreignHandler JsonResponse.errorWithData("400")
	]
	
	action validateAccountNumber [
		if szAccountNumber != "" then 
			validateUser
		else
			invalidAccountNumber
	]
	
	action invalidAccountNumber [
		JsonResponse.reset()
		
		JsonResponse.setString("convenienceFee", "0")
		JsonResponse.setString("message", "Invalid account number")
		JsonResponse.setString("status", "400")
		
		foreignHandler JsonResponse.errorWithData("400")
	]
	
	action validateUser [
		if szUserId != "" then
			fetchConvenienceFee
		else
			invalidUser

	]
	
	action invalidUser [
		JsonResponse.reset()
		
		JsonResponse.setString("convenienceFee", "0")
		JsonResponse.setString("message", "Unauthorized user")
		JsonResponse.setString("status", "403")
		
		foreignHandler JsonResponse.errorWithData("403")
	]
	
	action fetchConvenienceFee [
		srGetDebitConvenienceFeeParams.user 		= szUserId
		srGetDebitConvenienceFeeParams.paymentGroup = szPaymentGroup
		srGetDebitConvenienceFeeParams.account		= szAccountNumber
		
		// getting the convenienceFee value
   		switch apiCall AccountStatus.GetDebitConvenienceFee(srGetDebitConvenienceFeeParams, srGetDebitConvenienceFeeResult, srAccountStatusCode) [
    		case apiSuccess retriveConvenienceFee
    		default internalServerError
    	]
	]
	
	action retriveConvenienceFee [
		convenienceFeeAmount = srGetDebitConvenienceFeeResult.convenienceFeeAmt
		goto(sendSuccessResponse)
	]
	
	action sendSuccessResponse [
		JsonResponse.reset()
		
		JsonResponse.setString("convenienceFee", convenienceFeeAmount)
		JsonResponse.setString("message", "Convenience fee found successfully")
		JsonResponse.setString("status", "200")
		
		foreignHandler JsonResponse.send()
	]
	
	action internalServerError [
		JsonResponse.reset()
		
		JsonResponse.setString("convenienceFee" , "0")
		JsonResponse.setString("message", "Internal server error")
		JsonResponse.setString("status", "500")
		
		foreignHandler JsonResponse.errorWithData("500")
	]
]