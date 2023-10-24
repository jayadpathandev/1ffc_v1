useCase fffcReg02SmsTnC [

    documentation [
        preConditions: [[
            1. The user want's to enroll in the application.
        ]]
        triggers: [[
            1. The user accepts the email terms and conditions.
        ]]
        postConditions: [[
            1. The user's Mobile number may be collected if they consented.
        ]]
    ]

	startAt regTermsAndConditionsScreen

  	/**************************
	 * DATA ITEMS SECTION
	 **************************/  	 	
	importJava TermsAndConditions(com.sorrisotech.fffc.auth.TermsAndConditions)

	import regContactInfo.fMobileNumber
			
    string sPageName = "{Registration - Text Message Consent (step 2 of 8)}"   
	
    tag hTermsText = TermsAndConditions.loadFile("terms_sms_en_us.html")			       
        
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]

	/**************************************************************************
     * Main Path.
     **************************************************************************/ 
    noMenu xsltScreen regTermsAndConditionsScreen("{Registration - Text Message Consent (step 2 of 8)}") [
    	    	
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
        			class: "row border-top mt-3 pt-3"
					
					div fields [
				    	class: "col-12 col-sm-8 col-md-6 col-lg-5 col-xl-4"
					    display fMobileNumber [
	            			control_attr_tabindex: "1"
					    ]
						
	            		display fCheckBoxes [
	            			item_control_attr_tabindex: "2"
	            		]
            		]
				]
			]
			
			div buttons [
				class: "st-buttons"
				
				div row [
					class: "row"
					
					div col1 [
						class: "col-md-12"
						
						navigation termsConditionsSubmit(gotoRegBillingInfo, "{Next}") [			                
		                	class: "btn btn-primary"  
		                	 
		                	require: [
		                		fMobileNumber,
		                		fCheckBoxes
		                	]
		                	attr_tabindex: "3"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(gotoRegBillingInfo, "{Skip}") [			                
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
  
    action gotoRegBillingInfo [        
        gotoUc(fffcReg03ElectronicTnC)
    ]     

    action gotoEmailConsent [        
        gotoUc(fffcReg01EmailTnC)
    ]     
]