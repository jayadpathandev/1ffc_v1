application [
			 
	defaultLanguage en_us
	languages [ en_us ]
	
	initialUseCase saml

	menu main [
		item appOverview ("{Account Summary}", overview) [
			class: "menu"			
		]
		
		item menuBilling ("{Billing & Usage}", billSummary) [
			class: "menu"
		]
		
	    item appDashboard ("{Statements & Letters}", document) [
			class: "menu"			
		]
			
		item menuPayment("{Make a Payment}", payment) [
			class: "menu"
	    ]	    

		// 1FFC 
	    // item menuRenew("{Renew Now}", fffcRenewNow) [
	    //     class: "renew"
	    // ]
	]
	
	modules [
		OVERVIEW => overview
		DASHBOARD => dashboard
		BILLING => billSummary
		CORRESPONDENCE => document
		NOTIFICATIONS => notifications
		PAYMENT => payment	
		PROFILE => profile
		LOGIN => saml		
	]
	
	include [
	    samlLib	    
		dashboard
        billLib
        paymentLib		
		profileLib
		utilsLib
		accountBalanceLib
		accountOverviewLib
		auditLib
		applicationIntegrationLib
		userServicesLib
		videoLib
		reportLib
		
		// 1FFC 
		fffcRenewNow
		fffcViewDocument
 	]
 	
 	roles [
 	    role Role_Consumer_EndUser [
 	        bill
 	        document
            payment
 	        profile
 	        reports
 	    ]
 	    
 	    role Role_Biz_OrganizationAdmin [
 	    	hierarchy
 	    	businessUsers
 	    	bill
 	    	profile
 	    	user_management
 	    	admin_management
 	    	role_management
 	    	reports
 	    ]
 	    
 	    role Role_Biz_Admin [
 	    	hierarchy
 	    	businessUsers
 	    	bill
 	    	profile
 	    	user_management
 	    	admin_management
 	    	role_management
 	    	reports
 	    ]
 	    
 	    role Role_Biz_EndUser [
 	    	bill
 	    	document
            payment
 	    	profile
 	    ]
 	]		
]