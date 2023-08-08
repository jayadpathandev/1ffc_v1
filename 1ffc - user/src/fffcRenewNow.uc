useCase fffcRenewNow [
	
     documentation [
		triggers: [[
			1. The user selects the "Renew Now" link from the menu.
		]]
		preConditions: [[
			1. The user must be logged into the system for this use case to work.
		]]
		postConditions: [[
			1. The user is sent to an external site.
		]]
	]
	
	startAt actionRedirect
	
	importJava AppConfig(com.sorrisotech.utils.AppConfig)	
	importJava ForeignProcessor(com.sorrisotech.app.common.ForeignProcessor)
	
    /*=============================================================================================
     * Data objects used by the use case.
     *===========================================================================================*/
	native string sRenewNow  = AppConfig.get("1ffc.renew.url")
	
    /*=============================================================================================
     * 1. System redirects user to an external site.
     *===========================================================================================*/
    action actionRedirect [
    	foreignHandler ForeignProcessor.writeResponse(sRenewNow)
    ]
]
