useCase testFtlTemplating [
	

	startAt init
	
	importJava FtlTemplate(com.sorrisotech.ftlrender.UcFtlDisplay)
	
	// -- identifier for template used after initialization --
	native string TemplateIdNewAccount = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummaryNoBills.ftl",
											"none",
											"none" 	)
	// -- identifier for template used after initialization --
	native string TemplateIdClosedAccount = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummaryNoBills.ftl",
											"none",
											"none" 	)

	// -- identifier for template used after initialization --
	native string TemplateIdAccessDenied = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummaryNoBills.ftl",
											"none",
											"none" 	)
											
	// -- identifier for template used after initialization --
	native string TemplateIdNoBillsFound = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummaryNoBills.ftl",
											"none",
											"none" 	)
											
		
	tag showTemplateNewAccount = FtlTemplate.renderTemplate(TemplateIdNewAccount)
	tag showTemplateClosedAccount = FtlTemplate.renderTemplate(TemplateIdClosedAccount)
	tag showTemplateAccessDenied = FtlTemplate.renderTemplate(TemplateIdAccessDenied)
	tag showTemplateNoBillsFound = FtlTemplate.renderTemplate(TemplateIdNoBillsFound)
	
	action init [
		
		goto (addData)
	]	
	
	action addData [
		// -- values for new account --
		FtlTemplate.setItemValue(TemplateIdNewAccount, "status",
					"viewAccount", "string", "enabled" )
		FtlTemplate.setItemValue(TemplateIdNewAccount, "status",
					"accountStatus", "string", "newAccount")
		FtlTemplate.setItemValue(TemplateIdNewAccount, "root",
					"displayAccount", "number", "12345678")
		// -- values for closed account --
		FtlTemplate.setItemValue(TemplateIdClosedAccount, "status",
					"viewAccount", "string", "enabled" )
		FtlTemplate.setItemValue(TemplateIdClosedAccount, "status",
					"accountStatus", "string", "closedAccount")
		FtlTemplate.setItemValue(TemplateIdClosedAccount, "root",
					"displayAccount", "currency", "34567890")
		FtlTemplate.setItemValue(TemplateIdClosedAccount, "root",
					"jumpToOffset", "string", "foo")
		// -- values for account access denied
		FtlTemplate.setItemValue(TemplateIdAccessDenied, "status",
					"viewAccount", "string", "disabledAccount" )
		FtlTemplate.setItemValue(TemplateIdAccessDenied, "status",
					"accountStatus", "string", "activeAccount")
		FtlTemplate.setItemValue(TemplateIdAccessDenied, "root",
					"displayAccount", "dateDb", "20231211")
		// -- values for no bills found problem
		FtlTemplate.setItemValue(TemplateIdNoBillsFound, "status",
					"viewAccount", "string", "enabled" )
		FtlTemplate.setItemValue(TemplateIdNoBillsFound, "status",
					"accountStatus", "string", "activeAccount")
		FtlTemplate.setItemValue(TemplateIdNoBillsFound, "root",
					"displayAccount", "string", "20231211111")
		FtlTemplate.setItemValue(TemplateIdNoBillsFound, "root",
					"jumpToOffset", "string", "foo")
					
		goto (showTemplate)
	]
	
	xsltScreen showTemplate [
		display showTemplateNewAccount
		display showTemplateClosedAccount
		display showTemplateAccessDenied
		display showTemplateNoBillsFound
	]

	
]