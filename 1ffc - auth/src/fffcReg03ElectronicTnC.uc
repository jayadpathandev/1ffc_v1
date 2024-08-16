useCase fffcReg03ElectronicTnC [

    documentation [
        preConditions: [[
            1. The user wants to enroll in the application. Step 2 of 5
        ]]
        triggers: [[
            1. The user entered the billing info on the billing info screen and click on next
        ]]
        postConditions: [[
        	None.
        ]]
    ]

	startAt regTermsAndConditionsScreen

  	/**************************
	 * DATA ITEMS SECTION
	 **************************/  	 
	importJava PersonaData(com.sorrisotech.app.utils.PersonaData)	
	importJava TermsAndConditions(com.sorrisotech.fffc.auth.TermsAndConditions)

    string sPageName = "{Registration - E-SIGN Consent (step 2 of 5)}"   
	
    tag hTermsText = TermsAndConditions.loadFile("terms_electronic_en_us.html")			       
        
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]
    
	checkBoxes cPaperLessOption = false [
		true : "{Yes, I would like to opt-in for paperless delivery and stop receiving paper statements by mail.}"	
	]    
	
	persistent native string sPaperLessOption = "false"

	/**************************************************************************
     * Main Path.
     **************************************************************************/ 
    noMenu xsltScreen regTermsAndConditionsScreen("{Registration - ESIGN Consent (step 2 of 5)}") [
    	    	
        form regTermsAndConditionsForm [
	    	class: "st-login"
	            
	        div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
	            
    	            display sPageName
	            ]
			]
	    
	    	div content [
									
				div terms [
					display hTermsText    
				]
								
				div divider [
        			class: "border-top mt-3 pt-3"
					
				
            		display fCheckBoxes [
            			control_attr_tabindex: "1"
						control_attr_autofocus: ""
            		]
            		
            		display cPaperLessOption [
            			control_attr_tabindex: "2"
						control_attr_autofocus: ""
            		]            		
				]
			]
			
			div buttons [
				class: "st-buttons"
				
				div row [
					class: "row"
					
					div col1 [
						class: "col-md-12"
						
						navigation termsConditionsSubmit(isOptinPaperless, "{Next}") [			             
		                	class: "btn btn-primary"  

		                    data :[
		                    	cPaperLessOption
		                    ]	
		                    	                	 
		                	require: [
		                		fCheckBoxes
		                	]
		                	attr_tabindex: "3"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(gotoLogin, "{Cancel}") [			                
							class: "btn btn-secondary"
							attr_tabindex: "4"
						] 							
						
						navigation termsConditionsBack(gotoBillingInfo, "{Back}") [
							attr_tabindex: "5"
						]
					]
				]					
			]			
        ]
    ]

    /**************************************************************************
     * 1. Retrieve the value of paperless option
     */ 
	action isOptinPaperless [		
		PersonaData.isSelected(cPaperLessOption, "true", sPaperLessOption)
		goto(gotoLoginInfo)		
	]  

    /**************************************************************************
     * 2. Go to next step, login info
     */ 	
	action gotoLoginInfo [
		gotoUc(fffcReg06LoginInfo)	
	]	

   /**************************************************************************
    * User clicks on cancel
    */ 		
    action gotoLogin [
    	gotoModule(LOGIN)
    ]    

   /**************************************************************************
    * User clicks on back link
    */ 		
    action gotoBillingInfo [        
        gotoUc(fffcReg05BillingInfo)
    ]     
]