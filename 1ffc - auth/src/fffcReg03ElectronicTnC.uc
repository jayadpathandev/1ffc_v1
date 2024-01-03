useCase fffcReg03ElectronicTnC [

    documentation [
        preConditions: [[
            1. The user want's to enroll in the application.
        ]]
        triggers: [[
            1. The user accepts the has completed the SMS Terms and Conditions.
        ]]
        postConditions: [[
        	None.
        ]]
    ]

	startAt regTermsAndConditionsScreen

  	/**************************
	 * DATA ITEMS SECTION
	 **************************/  	 	
	importJava TermsAndConditions(com.sorrisotech.fffc.auth.TermsAndConditions)

    string sPageName = "{Registration - E-SIGN Consent (step 3 of 8)}"   
	
    tag hTermsText = TermsAndConditions.loadFile("terms_electronic_en_us.html")			       
        
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]

	/**************************************************************************
     * Main Path.
     **************************************************************************/ 
    noMenu xsltScreen regTermsAndConditionsScreen("{Registration - ESIGN Consent (step 3 of 8)}") [
    	    	
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
						
						navigation termsConditionsSubmit(gotoWebTnC, "{Next}") [			                
		                	class: "btn btn-primary"  
		                	 
		                	require: [
		                		fCheckBoxes
		                	]
		                	attr_tabindex: "3"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(gotoLogin, "{Cancel}") [			                
							class: "btn btn-secondary"
							attr_tabindex: "4"
						] 							
						
						navigation termsConditionsBack(gotoEmailConsent, "{Back}") [
							attr_tabindex: "5"
						]
					]
				]					
			]			
        ]
    ]
  
    action gotoWebTnC [        
        gotoUc(fffcReg04WebTnC)
    ]     

    action gotoLogin [
    	gotoModule(LOGIN)
    ]    

    action gotoEmailConsent [        
        gotoUc(fffcReg02SmsTnC)
    ]     
]