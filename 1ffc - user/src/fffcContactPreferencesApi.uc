useCase fffcContactPreferencesApi
[
	startAt init

	shortcut contactpreferences(init)

	importJava Log(api.Log)
	importJava JsonRequest(com.sorrisotech.app.common.JsonRequest)
	importJava JsonResponse(com.sorrisotech.fffc.user.JsonResponse)
	importJava Config(com.sorrisotech.utils.AppConfig)
    
    serviceStatus srFetchUserIdStatus
    serviceParam(FffcNotify.FetchUserId) srFetchUserIdParam
    serviceResult(FffcNotify.FetchUserId) srFetchUserIdResponse
    
    serviceStatus srSetContactPrefsStatus
    serviceParam  (FffcNotify.SetContactPreferences) srSetContactPrefsParam
    serviceResult (FffcNotify.SetContactPreferences) srSetContactPrefsResult

    native string sServiceUserName    = Config.get("service.api.username")
    native string sServiceNameSpace   = Config.get("service.api.namespace")
    native string sToken			  = JsonRequest.value("security_token")
    native string sSession            = JsonRequest.value("session")
	native string sCustomerId		  = JsonRequest.value("customer_id")
    native string sdateTime			  = JsonRequest.value("date_time")
    native string sChannelAddresses   = JsonRequest.value("channel_addresses")
    native string sTopicPreferences   = JsonRequest.value("topic_preferences")

   /*************************
     * MAIN SUCCESS SCENARIO
     *************************/

	/*************************
	 * 1. Action Init
	 */
    action init[
    	JsonRequest.load()
    	goto(verifyToken)
    ]

	/**********************************
	 * 2. Verify Token
	 */
    action verifyToken [
    	switch JsonRequest.exist("security_token") [
    		case "true"   verifyCustomerId
    		default       actionBadRequest
    	]
    ]

	/**********************************
	 * 3. Verify Customer id
	 */
    action verifyCustomerId[
    	switch JsonRequest.exist("customer_id") [
    		case "true"   verifyDateTime
    		default       actionBadRequest
    	]
    ]

 	/************************************
	 * 4. Action verify date and time 
	 */
    action verifyDateTime [
    	switch JsonRequest.exist("date_time") [
    		case "true"   verifyHasChannelAddresses
    		default       actionBadRequest
    	]
    ]

    action verifyHasChannelAddresses [
        switch JsonRequest.exist("channel_addresses") [
            case "true"   verifyTopicPreferences
            default       actionBadRequest
        ]        
    ]

    action verifyTopicPreferences [
       switch JsonRequest.exist("topic_preferences") [
            case "true"   verify
            default       actionBadRequest
        ]
    ]
	
	/************************
	 * 5. Action verify
	 */
    action verify [
    	 login(
            username: sServiceUserName
            password: sToken
            namespace: sServiceNameSpace
            )
        if success then actionProcess
        if authenticationFailure then actionAuthFailure
        if failure then actionAuthFailure
    ]
    
	action actionProcess [
		JsonResponse.reset()
		goto (verifyUserForCustomerId)
	]
	
	/****************************************************************************************************
	 * Verifying the customerId and fetching userId for given customerId. 
	 */
	action verifyUserForCustomerId [
      
      srFetchUserIdParam.customerId = sCustomerId
      
      switch apiCall FffcNotify.FetchUserId(srFetchUserIdParam, srFetchUserIdResponse, srFetchUserIdStatus) [
    	  case apiSuccess saveContactPreferences
      	  default actionUserNotFound
      ]
    ]
	
	/****************************************************************************************************
	 * Saving contact preferences to database. 
	 */
	action saveContactPreferences [
		
		srSetContactPrefsParam.userid   		= srFetchUserIdResponse.userid
		srSetContactPrefsParam.dateTime 		= sdateTime
		srSetContactPrefsParam.channelAddresses = sChannelAddresses
		srSetContactPrefsParam.topicPrefrences  = sTopicPreferences
		
		switch apiCall FffcNotify.SetContactPreferences(srSetContactPrefsParam, srSetContactPrefsStatus, srSetContactPrefsResult)[
			case apiSuccess actionCheckStatus
			default actionBadRequest
		]
	]
	
	/****************************************************************************************************
	 * Checking status. 
	 */
	action actionCheckStatus [
		switch srSetContactPrefsResult.status [
			case good      		 actionSuccess
    		case validationError actionBadRequestValidationError
    		case error           actionBadRequest
		]
	]
	
	/********************************
	 * Success.
	 */
	action actionSuccess [
	   if sSession == "true" then
           actionSuccessFinish
	   else
           actionSuccessClear    
	]
	
	action actionSuccessClear [
	    logout()
	    goto(actionSuccessFinish)
	]
	 
	action actionSuccessFinish [
		Log.warn("setContactPreferences", "*****", sCustomerId, "Success")
		JsonResponse.setNumber("statuscode","200")
		JsonResponse.setBoolean("success","true")
		JsonResponse.setString("payload", "Successfully updated the contact preferences for given customerId.")
		JsonResponse.setNull("errors[+]")
		foreignHandler JsonResponse.send()
	]
	
	/********************************
	 * Bad Request.
	 */
	action actionBadRequest [
       if sSession == "true" then
           actionBadRequestFinish
       else
           actionBadRequestClear
    ]
    
    action actionBadRequestClear [
        logout()
        goto(actionBadRequestFinish)
    ]
	
	action actionBadRequestFinish [
		Log.warn("setContactPreferences", "*****", sCustomerId, "failed")
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode","400")
		JsonResponse.setBoolean("success","false")
		JsonResponse.setNull("payload")
		JsonResponse.setString("errors[+]", "Bad request received.")
		foreignHandler JsonResponse.errorWithResponse("400")
	]
	
	/********************************
	 * Request Validation Failed
	 */
	action actionBadRequestValidationError [
       if sSession == "true" then
           actionBadRequestValidationErrorFinish
       else
           actionBadRequestValidationErrorClear
    ]
    
    action actionBadRequestValidationErrorClear [
        logout()
        goto(actionBadRequestValidationErrorFinish)
    ]
	
	action actionBadRequestValidationErrorFinish [
		Log.warn("setContactPreferences ", "*****"," request validation failed", sCustomerId, "failed")
		JsonResponse.reset()
		JsonResponse.setNumber("statuscode","400")
		JsonResponse.setBoolean("success","false")
		JsonResponse.setNull("payload")
		JsonResponse.setString("errors[+]", "Bad request received for topic preferences.")
		foreignHandler JsonResponse.errorWithResponse("400")
	]
	
	
	/********************************
	 * User not found.
	 */
	action actionUserNotFound [
       if sSession == "true" then
           actionUserNotFoundFinish
       else
           actionUserNotFoundClear
    ]
    
    action actionUserNotFoundClear [
        logout()
        goto(actionUserNotFoundFinish)
    ]
	
	
	action actionUserNotFoundFinish [
		JsonResponse.setNumber("statuscode","404")
		JsonResponse.setBoolean("success","false")
		JsonResponse.setNull("payload")
		JsonResponse.setString("errors[+]","No user found for given customerId!")
		Log.warn("setContactPreferences", "*****", "error, user not registered yet")
		foreignHandler JsonResponse.errorWithResponse("404")
	]
	
	/********************************
	 * Authentication Failure
	 */
	action actionAuthFailure [
       if sSession == "true" then
           actionAuthFailureFinish
       else
           actionAuthFailureClear    
    ]
    
    action actionAuthFailureClear [
        logout()
        goto(actionAuthFailureFinish)
    ]
	 
	action actionAuthFailureFinish [
		Log.warn("setContactPreferences", "*****", sCustomerId, "Authentication Failure")
		foreignHandler JsonResponse.error("401")
	]
]
