useCase apiShowIframe [
	shortcut startAddSourceBank(actionNewBank)
	shortcut startAddSourceDebit(actionNewDebit)
	shortcut startEditSource(actionEditSource)
	
	startAt actionInvalid

	importJava ApiPay(com.sorrisotech.fffc.agent.pay.ApiPay)
	importJava AppConfig(com.sorrisotech.utils.AppConfig)		
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
    importJava Session(com.sorrisotech.app.utils.Session)    

   	native string sUserId   		= Session.getUserId()
   	native string sUserName 		= Session.getUsername()      
	native string sAppType 			= AppConfig.get("application.type")
	native string sOneTime  		= ApiPay.isOneTime()
	native string sSourceToken 		= ApiPay.sourceToken()
	native string sSourceName 		= ApiPay.sourceName()
	native string sSourceType 		= ApiPay.sourceType()
	native string sSourceDefault 	= ApiPay.sourceDefault()
	 
	action actionInvalid [
		gotoUc(appLogout)		
	]
	
	action actionNewBank [
		foreignHandler UcPaymentAction.writeIframeAddSourceResponse(
			"iframeAddSourceSubmit.ftl", 
			sorrisoLanguage, 
			sorrisoCountry, 
			sUserId, 
			sUserName, 
			"bank", 
			sOneTime, 
			sAppType
			)		
	] 
	
	action actionNewDebit [
		foreignHandler UcPaymentAction.writeIframeAddSourceResponse(
			"iframeAddSourceSubmit.ftl", 
			sorrisoLanguage, 
			sorrisoCountry, 
			sUserId, 
			sUserName, 
			"debit", 
			sOneTime, 
			sAppType
			)
		
	] 
	
	action actionEditSource [
		foreignHandler UcPaymentAction.writeIframeEditSourceResponse(
			"iframeEditSourceSubmit.ftl", 
			sorrisoLanguage, 
			sorrisoCountry, 
			sUserId, 
			sUserName, 
			sSourceType, 
			sSourceToken,
			sSourceName,
			sSourceDefault, 
			sAppType
			)
	] 
	
]