useCase apiStartChooseSource [
	shortcut startChooseSource(init) [
		code
	]

	shortcut startAgentPayIframe(actionDisplay) [
		itemType
	]
	
	shortcut startUseSource(actionUseSource) [
		walletToken
	]
	
	shortcut startNewSource(actionNewSource) [
		walletType
		walletAccount
		walletExpiry
		walletToken
	]
	
	shortcut startChooseSourceFailure(actionWalletError) [
		error
	]
	
	shortcut startDeleteWallet(actionCheckDeleteSource)
	
	startAt init [
		code
	]

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
    importJava Session(com.sorrisotech.app.utils.Session)    
	
	native string code
	native string itemType
	
	native string walletType
	native string walletAccount
	native string walletExpiry
	native string walletToken
	
	native volatile string sUserId   = ApiPay.userid()
	native volatile string sUserName = ApiPay.userName()
	native volatile string sPaymentType = ApiPay.sourceType()
	native volatile string sPaymentSourceId = ApiPay.sourceToken()
	
	static paymentMethodTemporaryError 		  = "{Payment method is temporary, cannot delete.}"
	static paymentMethodScheduleError  		  = "{Payment method is used in a scheduled payment, cannot delete.}"
	static paymentMethodScheduleProgressError = "{Scheduled payment is currently be made with the payment method, cannot delete.}"
	static paymentMethodAutoError 			  = "{Payment method is used in an auto payment schedule, cannot delete.}"
	static paymentMethodUnknownError		  = "{Payment could not be delete for unknown reasons.}"
	
	native string error
	native string sDeleteWalletError
	
    serviceStatus reqStatus
    serviceParam (AccountStatus.GetStatus) reqParams
    serviceResult (AccountStatus.GetStatus) reqResult
    
    serviceParam(Payment.DeleteWallet) srDeleteSourceParam	
	serviceResult(Payment.DeleteWallet) srDeleteSourceResult
	
	action init [
		switch ApiPay.load(code) [
			case "true" checkStatus
			default     actionError 
		]
	]

	action checkStatus [
		Session.setUserId(sUserId)
		Session.setUsername(sUserName)

		ApiPay.userid(reqParams.user)
		ApiPay.payGroup(reqParams.paymentGroup)
		ApiPay.accountId(reqParams.account)
		
   		switch apiCall AccountStatus.GetStatus(reqParams, reqResult, reqStatus) [
    		case apiSuccess checkAch
    		default         actionError
    	]		
	]
	
	action checkAch [
		if reqResult.achEnabled == "false" then
			disableAch
		else
			actionDisplay			
	]
	
	action disableAch [
		ApiPay.disableAch()
		goto(actionDisplay)
	]
		
	action actionDisplay [
		error = ""
		sDeleteWalletError = ""
		ApiPay.setError(error)
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
 	action actionError [
		foreignHandler ApiPay.showHtmlError("api_start_add_source_error.html")		
	]
	
 	action actionWalletError [
		ApiPay.setError(error)
		sDeleteWalletError = ""
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]	

	action actionUseSource [
		error = ""
		sDeleteWalletError = ""
		ApiPay.setError(error)
		ApiPay.setWallet(walletToken)
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
	action actionNewSource [
		error = ""
		sDeleteWalletError = ""
		ApiPay.setError(error)
		ApiPay.setWallet(walletType, walletAccount, walletExpiry, walletToken)
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
	action actionCheckDeleteSource [
		switch ApiPay.isWalletDeletable() [
			case "true"	deleteWallet
			case "false" deleteWalletError
		]
	]
	
	action deleteWallet [
		
		srDeleteSourceParam.SOURCE_TYPE = sPaymentType
		srDeleteSourceParam.SOURCE_ID   = sPaymentSourceId
	    srDeleteSourceParam.USER_ID     = sUserId
	    
		switch apiCall Payment.DeleteWallet(srDeleteSourceParam, srDeleteSourceResult, reqStatus) [
            case apiSuccess checkDeleteResult
            default deleteWalletError
        ]	
    ]
    
    action checkDeleteResult [		
		if srDeleteSourceResult.RESULT == "success" then
    		deleteWalletSuccess
    	else 
    		deleteWalletError	
	]
	
	action deleteWalletSuccess [
		ApiPay.clearWallet()
		error = ""
		sDeleteWalletError = ""
		ApiPay.setError(error)
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()
	]
	
	action deleteWalletError [
		error = ""
		sDeleteWalletError = ""
		ApiPay.setError(error)
		ApiPay.setDeleteError(sDeleteWalletError)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()
	]
]
