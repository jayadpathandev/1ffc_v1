useCase fffcReg04WebTnC [

   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 23-Oct-2015
    *
    *  Primary Goal:
    *       1. System displays Registration - View Terms and Conditions screen (Step 1 of 6). 
    *       
    *  Alternative Outcomes:
    *       1. User cancels the information and returns to the login page 
    *                     
    *   Major Versions:
    *        1.0 1-Oct-2015 First Version Coded [Maybelle Johnsy Kanjirapallil]
    */
    

    documentation [
        preConditions: [[
            1. A user accesses the application
        ]]
        triggers: [[
            1. Users click on start button on the registration checklist page
        ]]
        postConditions: [[
            1. Primary -- System displays Registration - bill information
            2. Alternative 1 -- the transaction was canceled, Sytem takes users to the login page
            3. Alternative 2 -- User selects back link, System takes users to the registration checklist page
        ]]
    ]
	startAt initialize

  	/**************************
	 * DATA ITEMS SECTION
	 **************************/  
	 	
	importJava UcTermsConditions(com.sorrisotech.uc.termsconditions.UcTermsConditions)
	
    string sPageName = "{Registration - Terms and Conditions (step 4 of 7)}"   
	
    tag hTermsText = UcTermsConditions.getTermsConditions(sorrisoLanguage, sorrisoCountry)			       
    
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]

   // -- message strings for display when use case completes.       
    structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
	]

	/**************************************************************************
     * Main Path.
     **************************************************************************/ 
     
    /**************************************************************************
	 * 1. Initialize and loads all terms and conditions
	 */
    action initialize [		
		switch UcTermsConditions.init() [
			case "success" regTermsAndConditionsScreen
		 	case "error" genericErrorMsg						
			default genericErrorMsg
		]
	] 	   
	       
   /**************************************************************************
    * 2. System displays View Terms and Conditions screen.
    */    
    noMenu xsltScreen regTermsAndConditionsScreen("{Registration - Terms and Conditions (step 4 of 8)}") [
    	    	
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
									
				div row2 [
					class: "row"
					
					div col2 [
						class: "col-md-12"
		
						display hTermsText    
					]
				]
				
				div row3 [
					class: "row"
                
					div col3 [
						class: "col-md-12"
                
                		display fCheckBoxes [
                			class: "st-spacing-top"
                			control_attr_tabindex: "1"
							control_attr_autofocus: ""
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
		                		fCheckBoxes
		                	]
		                	attr_tabindex: "2"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(gotoLogin, "{Cancel}") [			                
							class: "btn btn-secondary"
							attr_tabindex: "3"
						] 							
						
						navigation termsConditionsBack(gotoLogin, "{Back}") [
							attr_tabindex: "4"
						]
					]
				]					
			]			
        ]
    ]
      	 
    /**************************************************************************
	 * 6. Go to the registration billing info usecase
	 */ 
    action gotoRegBillingInfo [        
        gotoUc(fffcReg05BillingInfo)
    ]     
    
    /**************************************************************************
	 * 7. Go to the login usecase
	 */     
    action gotoLogin [
    	gotoModule(LOGIN)
    ]
    
    /**************************************************************************
     * Alternative Paths
     ***************************************************************************/    
	
	/**************************************************************************
     * Display generic error message 
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)
        goto(regTermsAndConditionsScreen)
	]		
 ]