useCase apiStartChooseSource [
	shortcut startChooseSource(init) [
		code
	]

	shortcut startChooseNew(actionDisplay) [
		itemType
	]
	
	shortcut startUseSource(actionUseSource) [
		walletItem
	]
	shortcut startChooseSourceFailure(actionWalletError) 
	
	startAt init [
		code
	]

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
    importJava Session(com.sorrisotech.app.utils.Session)    
	
	native string code
	native string itemType
	native string walletItem
	native volatile string sUserId   = ApiPay.userid()
	native volatile string sUserName = ApiPay.userName()
	
	action init [
		switch ApiPay.load(code) [
			case "true" actionDisplay
			default     actionError 
		]
	]
		
	action actionDisplay [
		Session.setUserId(sUserId)
		Session.setUsername(sUserName)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
	
 	action actionError [
		foreignHandler ApiPay.showHtmlError("api_start_add_source_error.html")		
	]
	
 	action actionWalletError [
		foreignHandler ApiPay.showHtmlError("api_start_wallet_error.html")		
		
	]
	
	
	action actionUseSource [
		ApiPay.setWallet(walletItem)
		ApiPay.prepareIframe(itemType)
		foreignHandler ApiPay.showIframe()		
	]
]