useCase fffcReg01EmailTnC [

    documentation [
        preConditions: [[
            1. The user want's to enroll in the application.
        ]]
        triggers: [[
            1. The user clicks the signup button within the application.
        ]]
        postConditions: [[
            1. The user's email address is collected, but not verified.
        ]]
    ]
	startAt initialize

  	/**************************
	 * DATA ITEMS SECTION
	 **************************/  
	importJava TermsAndConditions(com.sorrisotech.fffc.auth.TermsAndConditions)
	
	import regCompleteEnrollment.sUserId
	import regCompleteEnrollment.sUserName	
	import regCompleteEnrollment.sAppType
	import regCompleteEnrollment.sLoginAction
	
	import regChecklist.sAccountInfoFailed
	import regChecklist.sUserNameFailed
	import regChecklist.sEmailFailed
	
	import fffcReg05BillingInfo.fAccountNumber
	import fffcReg05BillingInfo.fServiceNumber
	import fffcReg05BillingInfo.fBillingDate
	import fffcReg05BillingInfo.fAmount
	import fffcReg05BillingInfo.fSelfReg0
	import fffcReg05BillingInfo.fSelfReg1
	import fffcReg05BillingInfo.fSelfReg2
	import fffcReg05BillingInfo.fSelfReg3
	import fffcReg05BillingInfo.fSelfReg4
	
    import fffcReg06LoginInfo.fUserName
	import fffcReg06LoginInfo.fFirstName
	import fffcReg06LoginInfo.fLastName

	import fffcReg07SecretQuestion.dSecretQuestion1
	import fffcReg07SecretQuestion.dSecretQuestion2
	import fffcReg07SecretQuestion.dSecretQuestion3
	import fffcReg07SecretQuestion.dSecretQuestion4
	import fffcReg07SecretQuestion.fSecretAnswer1
	import fffcReg07SecretQuestion.fSecretAnswer2
	import fffcReg07SecretQuestion.fSecretAnswer3
	import fffcReg07SecretQuestion.fSecretAnswer4
	
	import fffcReg08PersonalImage.groupId
	import fffcReg08PersonalImage.memberId
	import fffcReg08PersonalImage.imageId

	import regContactInfo.fUserEmail
	import regContactInfo.fUserEmailRetype
	import regContactInfo.fMobileNumber
	import regContactInfo.fPhoneNumber
						
    string sPageName = "{Registration - E-mail consent (step 1 of 8)}"   
	
    tag hTermsText = TermsAndConditions.loadFile("terms_email_en_us.html")
    
    persistent input  sGeolocation			       
        
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree to the terms of use.}"            
        ]        
    ]

	/**************************************************************************
     * Main Path.
     **************************************************************************/ 
     
    /**************************************************************************
	 * 1. Initialize and loads all terms and conditions
	 */
    action initialize [
    	sAppType = "b2c"
    	sLoginAction = "signup"
    	
    	fAccountNumber.pInput = ""
    	fServiceNumber.pInput = ""
		fBillingDate.aDate = ""
		fAmount.pInput = ""
		fSelfReg0.pInput = ""
		fSelfReg1.pInput = ""
		fSelfReg2.pInput = ""
		fSelfReg3.pInput = ""
		fSelfReg4.pInput = ""
			
		fUserName.pInput = ""
		fFirstName.pInput = ""
		fLastName.pInput = ""
			
		dSecretQuestion1=""
		dSecretQuestion2=""
		dSecretQuestion3=""
		dSecretQuestion4=""
		fSecretAnswer1.pInput = ""
		fSecretAnswer2.pInput = ""
		fSecretAnswer3.pInput = ""
		fSecretAnswer4.pInput = ""
		
		groupId="1"
		memberId="1"	
		imageId=""
		
		fUserEmail.pInput = ""
		fUserEmailRetype.pInput = ""
		fMobileNumber.pInput = ""
		fPhoneNumber.pInput = ""
		
		sAccountInfoFailed = "false"
		sUserNameFailed = "false"
		sEmailFailed = "false"

		goto(regTermsAndConditionsScreen)		
	] 	   
	       
   /**************************************************************************
    * 2. System displays View Terms and Conditions screen.
    */    
    noMenu xsltScreen regTermsAndConditionsScreen("{Registration - E-Mail Consent (step 1 of 8)}") [
    	    	
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
					    display fUserEmail [
	            			item_control_attr_tabindex: "1"
	            			control_attr_autofocus: ""
							pInput_attr_st-new-email: ""
							sError_attr_sorriso-error: "new-email"
					    ]
						
	            		display fCheckBoxes [
	            			control_attr_tabindex: "2"
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
						
						display sGeolocation [
							control_attr_sorriso-geo: ""
							logic: [
								if "true" == "true" then "hide"
							]
						]
						
						navigation termsConditionsSubmit(gotoRegBillingInfo, "{Next}") [			                
		                	class: "btn btn-primary"  
		                	 
		                	require: [
		                		fUserEmail,
		                		fCheckBoxes
		                	]
		                	data: [
		                		sGeolocation
		                	]
		                	attr_tabindex: "3"                			                    
		                ]        			
						
		                navigation termsConditionsCancel(gotoLogin, "{Cancel}") [			                
							class: "btn btn-secondary"
							attr_tabindex: "4"
						] 							
						
						navigation termsConditionsBack(gotoLogin, "{Back}") [
							attr_tabindex: "5"
						]
					]
				]					
			]			
        ]
    ]
  
    action gotoRegBillingInfo [        
        gotoUc(fffcReg02SmsTnC)
    ]     
    
    action gotoLogin [
    	gotoModule(LOGIN)
    ]
]