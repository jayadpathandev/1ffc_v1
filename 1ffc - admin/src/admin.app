application [
	
	/* ****************************************************************************
	 * 	File:					admin.app
	 *  Creation date: 			18-August-2015
	 *  
	 *  Description:
	 * 		Application definition for Persona Archive
	 * 	  
	 * ***************************************************************************** */
	 
	defaultLanguage en_us
	languages [ en_us ]
	
	initialUseCase saml

	modules [
		DASHBOARD => dashboard
		NOTIFICATIONS => contactPreferences
		PROFILE => profile
		ASSIST_BUSINESS_CUSTOMERS => assistBusinessCustomers
	]
	
	menu main [
	   
		item itemUserSearch ("{Assist Customer}", customerSearch) [ 
			class: "menu"   
		]

        item itemFindDocuments ("{Find Documents}", documentSearch) [ 
			class: "menu"   
		]   	
					
        item itemAudit ("{Audit Log}", auditView) [
            class: "menu"           
        ]       
        
	    item itemAdmin ("{Manage Users}", adminListUsers) [
            class: "menu"

		] 
        
       	item itemConfig ("{Configuration}", configProcess) [
            class: "menu"           
        ]  
        
 	    menu menuVideo("{Video}") [  
	    	class: "menu"

			item itemManageVideoVideApps ("{Manage Video Apps}", manageVideoViewApps) [  
				class: "menu"			
			]		
			
			item itemManageVideoUbfMapping ("{Configure UBF Mappings}", manageVideoUbfMapping) [ 
				class: "menu"			
			]
			
			item itemManageVideoTriggerReport ("{View Reports}", manageVideoTriggerReport) [ 
				class: "menu"			
			]				
		]       		
	]
	
	include [
	    dashboard
	    adminLib
		assitCustomersLib
		billLib
		createAdminLib	
//		registrationLib	
		auditLib
		profileLib		
		utilsLib
		configLib
		serviceAPILib
		accountBalanceLib
		userServicesLib
		samlLib
		manageVideoLib 
	]
		
    features [
        feature bill [
            assist_bill
        ]
        feature document [ 
            assist_document
        ]
        feature documentController [ 
            assist_document_controller
        ]
        feature payment [
            assist_payment
        ]
    ]
		
	roles [
		//James temp - ask Josh if we need all these features considering the new roles
        role Role_Admin_SystemAdmin [
            admin_management
            profile
            admin_config
            payment
            doc_search_manage
            b2b
            b2c
            config_pw_restrictions
            admin_config_full
        ]
        
        //James temp - ask Josh if we need all these features considering the new roles
        role Role_Admin_Company [
            admin_management
            profile
            admin_config
            payment
            doc_search_manage
            b2b
            b2c
            config_pw_restrictions
            admin_config_company
        ]

	    role Role_Admin_OrganizationAdmin [
	        role_management
	        user_management
	        admin_management
	        b2c_customer_management
	        b2b_customer_management
	        profile
            bill
            document
            payment
            doc_search_assist 
            view_audit_log
            video
	    ]

        role Role_Admin_Agent [
            b2c_customer_management
            b2b_customer_management
            profile
            bill
            document
            payment
            doc_search_assist 
            video
        ]
        
        role Role_Admin_DocumentController [
            profile
            document
            documentController
         ]
	]
]