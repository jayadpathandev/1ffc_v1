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
	
	native string error
	
    serviceStatus reqStatus
    serviceParam (AccountStatus.GetStatus) reqParams
    serviceResult (AccountStatus.GetStatus) reqResult
	
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
		ApiPay.setError(error)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
 	action actionError [
		foreignHandler ApiPay.showHtmlError("api_start_add_source_error.html")		
	]
	
 	action actionWalletError [
		ApiPay.setError(error)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]	

	action actionUseSource [
		error = ""
		ApiPay.setError(error)
		ApiPay.setWallet(walletToken)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
	action actionNewSource [
		error = ""
		ApiPay.setError(error)
		ApiPay.setWallet(walletType, walletAccount, walletExpiry, walletToken)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
]