useCase apiStartChooseSource [
	shortcut startChooseSource(init) [
		code
	]

	shortcut startChooseNew(actionDisplay) [
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
	
	action init [
		switch ApiPay.load(code) [
			case "true" actionDisplay
			default     actionError 
		]
	]
		
	action actionDisplay [
		error = ""
		Session.setUserId(sUserId)
		Session.setUsername(sUserName)
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