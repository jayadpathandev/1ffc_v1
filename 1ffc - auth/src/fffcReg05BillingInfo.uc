useCase fffcReg05BillingInfo [

   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 23-Oct-2015
    *
    *  Primary Goal:
    *       1. System displays Registration - Billing Information page (Step 1 of 5)
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
            1. Users click on the Signup button at the login screen
        ]]
        postConditions: [[
            1. Primary -- System displays Registration - Setup Login Profile
            2. Alternative 1 -- Users selects back link, System take users to the Login page
        ]]
    ]
    
	startAt startInit [
		sLoginAction,
		sAppType
	]
	
 	/**************************
	 * DATA ITEMS SECTION
	 **************************/  
        
    importJava UcBillRegistration(com.sorrisotech.uc.billstream.UcBillRegistration)    
	importJava UcBillStream(com.sorrisotech.uc.billstream.UcBillStream)
	importJava UcBillStreams(com.sorrisotech.uc.billstream.UcBillStreams)
	importJava Config(com.sorrisotech.utils.AppConfig)
		
    import validation.dateValidation

	import regChecklist.sAccountInfoFailed
	import regChecklist.sUserNameFailed
	import regChecklist.sEmailFailed

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
	
	//flag to set so that the json_is_username_available can be whitelisted
	import utilIsUserNameAvailable.sReqWorkflow
	
	// -- using status call to determine if the customer associated with the
	//			account number provided for self-reg 	is eligible for online access
	//
	//			Note: the account itself may not be eligible but the customer may
	//			have other accounts that are eligible --
   serviceStatus ssIsCustomerEligible
   serviceParam (AccountStatus.IsCustomerEligibleForPortal) spIsCustomerEligible
   serviceResult (AccountStatus.IsCustomerEligibleForPortal) srIsCustomerEligible
   native string sStatusPaymentGroup = Config.get("1ffc.ignore.group")
   native string sAcctForReg = UcBillRegistration.getAccountNum()
   native string sIsEligible
	
	
    string sTitle = "{Registration - Let's Find Your Account (step 1 of 5)}"    
    string sTitleBill = "{Registration - Let's Find Your Account (step 1 of 5)}"
    string sBillDetailsTitle = "{Please make sure you have the following to complete the registration process. You also will need to be able to receive an e-mail from us.}"    
    string sItemEmail  = "{E-mail address}"        
    string sPara1 = "{As part of your contract agreement, you should have received at least one document which includes your account number. If you do not have this information, please contact customer service at 1-800-XXX-XXXX.}"
    string sPara1Bill = "{As part of your contract agreement, you should have received at least one bill which includes your account number. If you do not have this information, please contact customer service at 1-800-XXX-XXXX.}"
    string sPara3 = "{E-mail: contactservice@someplace.com}"
        
    native string sFlag = "false"        
    native string sStreamCount  = UcBillStreams.getBillStreamCount(sAppType)           
    native string sAccountAttr = "account_num"
    native string sSvcNumAttr = "service_num"
    native string sBillDateAttr = "doc_date"
    native string sBillAmountAttr = "doc_amount"
    native string sSelfReg0Attr = "self_reg_1"
    native string sSelfReg1Attr = "self_reg_2"
    native string sSelfReg2Attr = "self_reg_3"
    native string sSelfReg3Attr = "self_reg_4"
    native string sSelfReg4Attr = "self_reg_5"    
    native string sFlagShowAccountNumber = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sAccountAttr)
    native string sFlagShowServiceNumber = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSvcNumAttr)
    native string sFlagShowBillingDate = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sBillDateAttr)    
    native string sFlagShowAmount = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)
    native string sFlagShowSelfReg0 = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSelfReg0Attr)
	native string sFlagShowSelfReg1 = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSelfReg1Attr)
	native string sFlagShowSelfReg2 = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSelfReg2Attr)
	native string sFlagShowSelfReg3 = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSelfReg3Attr)
	native string sFlagShowSelfReg4 = UcBillStream.checkIfRequired(sorrisoLanguage, sorrisoCountry, sSelfReg4Attr)
	native string sAccountLabel = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sAccountAttr)
	native string sServiceLabel = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sSvcNumAttr)
	native string sBillLabel = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sBillDateAttr)  
	native string sAmountLabel = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)
	native string sSelfReg0Label = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sSelfReg0Attr)
	native string sSelfReg1Label = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sSelfReg1Attr)
	native string sSelfReg2Label = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sSelfReg2Attr)  
	native string sSelfReg3Label = UcBillStream.getLabelDisp(sorrisoLanguage, sorrisoCountry, sSelfReg3Attr)
	
	native string sStreamType = UcBillStream.getStreamType(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)
	native string sSingleBillStream = UcBillStreams.getSelectedName(fBillType, sorrisoLanguage, sorrisoCountry)	
	native string sRegType
	native string sDummy = ""
		
	persistent native string sSelectedBillStream	
	persistent native string sCompanyId
    persistent native string sAppType
    native string sLoginAction
	
				
	number nAccountNumberOrder = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sAccountAttr)
    number nServiceNumberOrder = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSvcNumAttr)
    number nBillingDateOrder = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sBillDateAttr)
    number nAmountOrder = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)
    number nSelfReg0Order = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSelfReg0Attr)
    number nSelfReg1Order = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSelfReg1Attr)
    number nSelfReg2Order = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSelfReg2Attr)
    number nSelfReg3Order = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSelfReg3Attr)
    number nSelfReg4Order = UcBillStream.getOrder(sorrisoLanguage, sorrisoCountry, sSelfReg4Attr)
        			
	auto "{Select document or bill type: }" dropDown fBillType [ 
		UcBillStreams.getBillNames(sorrisoLanguage, sorrisoCountry, sAppType)
	]	
	
    field fCheckBoxes [        
    	checkBoxes(control) sField [         
        	Agree: "{I have read and agree to [a style='font-size: 14px;' target='_blank' href='https://www.1ffc.com/wp-content/uploads/2024/03/Online_Services_Agreement.pdf']Online Services Agreement[/a], [a style='font-size: 14px;' target='_blank' href='https://www.1ffc.com/wp-content/uploads/2023/07/1FF_Online_Privacy_Policy-1.pdf'] Online Privacy Notice[/a], [a style='font-size: 14px;' target='_blank' href='https://www.1ffc.com/wp-content/uploads/2024/03/1FFC-Privacy-Notice_2023_Final.pdf'] Privacy Notice[/a], [a style='font-size: 14px;' target='_blank' href='https://www.1ffc.com/wp-content/uploads/2024/03/Text_Use_Policy.pdf'] Text User Policy. [/a]}"          
        ]        
    ]	
        
    persistent field fAccountNumber [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sAccountAttr)
        input(control) pInput = ""
        string(required) sRequired = "{This field is required.}"
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sAccountAttr)       
    ]

	persistent field fServiceNumber [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSvcNumAttr)
        input(control) pInput = ""
        string(required) sRequired = "{This field is required.}"
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSvcNumAttr)       
    ]
    
    persistent field fBillingDate [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sBillDateAttr)          
        date(control) aDate("yyyy-MM-dd", dateValidation)         
        string(required) sRequired = "{This field is required.}"
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sBillDateAttr)       
    ]
        
 	persistent field fAmount [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)
        input(control) pInput("^\\d+(,\\d+)*(\\.\\d+)?$", fAmount.sValidation)
        string(validation) sValidation = "{Document amount format incorrect.}" 
        string(required) sRequired = "{This field is required.}"
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sBillAmountAttr)       
    ]
    
    persistent field fSelfReg0 [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSelfReg0Attr)    
        input(control) pInput = ""    
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSelfReg0Attr)        
    ]
    
    persistent field fSelfReg1 [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSelfReg1Attr)       
        input(control) pInput ("^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d{2}$" , fSelfReg1.sValidation)   
		string(validation) sValidation = "{Birth date must be numeric and appear in a format mm-dd-yyyy.}" 
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSelfReg1Attr)       
    ]
    
    persistent field fSelfReg2 [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSelfReg2Attr) 
        input(control) pInput ("^\\d{4}$" , fSelfReg2.sValidation)    
        string(validation) sValidation = "{Social security number must be 4 digits only.}" 
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSelfReg2Attr)       
    ]
    
    persistent field fSelfReg3 [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSelfReg3Attr)   
        input(control) pInput = ""     
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSelfReg3Attr)       
    ]
    
    persistent field fSelfReg4 [
        native string(label) sLabel = UcBillStream.getLabel(sorrisoLanguage, sorrisoCountry, sSelfReg4Attr) 
        input(control) pInput = ""       
        native string(help) sHelp = UcBillStream.getHelpText(sorrisoLanguage, sorrisoCountry, sSelfReg4Attr)        
    ]
 	
    // -- message strings for display when use case completes.             
    structure(message) msgBillsNotLoadedError [    
        string(title) sTitle = "{Error}"
        string(body) sBody = "{There are no bills loaded for the bill stream you have selected. Please contact your system administrator.}"
    ] 
    
    structure(message) msgEmptyBillDetailsError [    
        string(title) sTitle = "{Error}"
        string(body) sBody = "{Registration field values cannot be empty.}"
    ] 

    structure(message) msgDuplicateAccount [    
        string(title) sTitle = "Failure"
        string(body) sBody = "Your registration cannot be completed because the account has already been registered. For further assistance, contact customer service."
    ]
    
    structure(message) msgInvalidBillDetailsB2CError [    
        string(title) sTitle = "{We can not locate your account based on the information you provided.}"
        string(body) sBody = "{We are unable to locate your account with the information provided. Please double-check your entries and try again. If you continue to experience issues, please contact your local branch.}"
    ]
    
    structure(message) msgInvalidBillDetailsB2BError [    
        string(title) sTitle = "{Error}"
        string(body) sBody = "{The details you provided did not match any existing bills. For further assistance, please contact your local branch.}"
    ]
    
    structure(message) msgCompanyIdNullB2BError [    
        string(title) sTitle = "{Error}"
        string(body) sBody = "{A company is not yet created for this account. For further assistance, please contact your local branch.}"
    ]
	structure(message) msgCustomerNotEligibleB2CError[
		string(title) sTitle ='{Account(s) not eligible for portal}'
		string(body) sBody = "{The account(s) associated with the information provided are not eligible for portal access. For further assistance, please contact your local branch.}"
	]        
    structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
	]

    /**************************************************************************
     * Main Path.
     **************************************************************************/ 
 
 	action startInit [
 		if 	sLoginAction == "signup" then 
 			initRegistrationInfo
 		else
 			checkBillCountFlag
 	]    
 	
	action initRegistrationInfo [
		sAppType = "b2c"
		
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

		groupId="1"
		memberId="1"	
		imageId=""
			
		dSecretQuestion1=""
		dSecretQuestion2=""
		dSecretQuestion3=""
		dSecretQuestion4=""
		fSecretAnswer1.pInput = ""
		fSecretAnswer2.pInput = ""
		fSecretAnswer3.pInput = ""
		fSecretAnswer4.pInput = ""
				
		fUserEmail.pInput = ""
		fUserEmailRetype.pInput = ""
		fMobileNumber.pInput = ""
		fPhoneNumber.pInput = ""

		sAccountInfoFailed = "false"
		sUserNameFailed = "false"
		sEmailFailed = "false"		
						
		sLoginAction = ""
		goto(checkBillCountFlag)
	] 	
    /**************************************************************************
     * 1. Checks the bill stream count.
     */
    action checkBillCountFlag [
		sReqWorkflow = ""
    	
      if sStreamCount  == "one" then
      	setSingleSelectedStream	
      else 
      	gotoScreen	
    ]   
 
    /**************************************************************************
     * 2. Set the flag as single selected stream.
     */     
     action setSingleSelectedStream [
    	sFlag = "true"    	
    	sSelectedBillStream = sSingleBillStream
    	UcBillStream.getRegTypeByBillStream(sSelectedBillStream, sRegType)
    	goto(initialize)
    ]     
        
    /**************************************************************************
	 * 3. Initializes the bill stream list.
	 */
    action initialize [        	 
		 switch UcBillStream.init(sSelectedBillStream) [		 
		 	case "success" displayBillDetailsSection
		 	case "error" genericErrorMsg		 							
			default genericErrorMsg
		]
	] 
	
    /**************************************************************************
     * 4. Displays the bill details section.
     */
    action displayBillDetailsSection [		
		sFlag = "true"
		goto(regBillingInfoScreen)
	]
		
    /**************************************************************************
     * 5. System displays the Registration Billing Info Screen.
     */    
    noMenu xsltScreen regBillingInfoScreen("{Registration - Let's Find Your Account (step 1 of 5)}") [
         
           
        form regBillingInfoForm1 [
            class: "st-login"

            div header [
				class: "row"
				
				h1 headerCol [
					logic: [
	            		if sStreamType  != "document" then "remove"
	            	]
					class: "col-md-12"
				
    				display sTitle
    			]
    			
    			h1 headerColBill [
					logic: [
	            		if sStreamType  != "bill" then "remove"
	            	]
					class: "col-md-12"
				
    				display sTitleBill
    			]
			]
			
			div content1 [
				logic: [
            		if sStreamCount  == "one" || sFlag == "true" then "remove"
            	] 		             					
				div row2 [
					class: "row"
						
					div col2 [
						class: "col-6 col-md-4 col-lg-3"
						
						display fBillType [
							control_attr_tabindex: "1"
							control_attr_autofocus: ""
						]
					]
				]	
			]
			
			div buttons1 [				
				logic: [
            		if sStreamCount  == "one" || sFlag == "true" then "remove"
            	]
            	class: "st-buttons-tight"
				div buttons1Row1 [					
					logic: [
						if sStreamCount != "one" && sFlag == "true" then "remove"
					]
					class: "row"
					div buttons1Col1 [
						class: "col-md-12"
					
						navigation billStreamSubmit(areBillsLoaded, "{Select}") [							
							class: "btn btn-primary"	
														
							require: [
								fBillType
							]
							attr_tabindex: "2"
						]						
						
						navigation billDetailsBack1(gotoRegTermsAndConditions, "{Back}") [    						
    						attr_tabindex: "3"
						]						
					]
				]	
			]	
			
			div borderLine [
				class: "st-border-bottom" 
				logic: [
					if sStreamCount  == "one" || sFlag == "true" then "remove"
					if sStreamCount  != "one" && sFlag == "false" then "remove"
				]  
				display sDummy
			]		
        ]
        
        form regBillingInfoForm2 [        	
        	logic: [
				if sFlag == "false" then "remove"
			]
			class: "row st-login"
			div content2Left [
				class: "col-md-6"
				
				div col26 [
					class: "col-md-12"
					
					div fieldRow [
						class: "row"
						ordered [
    						nAccountNumberOrder => display fAccountNumber [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowAccountNumber == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "4"
								control_attr_autofocus: ""
    						]	
    						nServiceNumberOrder => display fServiceNumber [
			                    class: "col-7"
    							logic: [
    								if sRegType != "service" || sFlagShowServiceNumber != "true" then "remove"	    		                			                		
    		                	]
    		                	control_attr_tabindex: "4"
								control_attr_autofocus: ""
    						]							
                			nBillingDateOrder => display fBillingDate [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowBillingDate == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "5"
    						]
                			nAmountOrder => display fAmount [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowAmount == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "6"
    						]
    						nSelfReg0Order => display fSelfReg0 [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowSelfReg0 == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "7"
    		                	control_attr_autofocus: ""
    						]
    						nSelfReg1Order => display fSelfReg1 [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowSelfReg1 == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "8"
    						]
    						nSelfReg2Order => display fSelfReg2 [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowSelfReg2 == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "9"
    						]
    						nSelfReg3Order => display fSelfReg3 [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowSelfReg3 == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "10"
    						]
    						nSelfReg4Order => display fSelfReg4 [
			                    class: "col-7"
    							logic: [
    		                		if sFlagShowSelfReg4 == "false" then "remove"
    		                	]
    		                	control_attr_tabindex: "11"
    						]	    						
    					]
    				]
    					
					display fCheckBoxes [
						class: "col-7"
            			control_attr_tabindex: "12"
            		]
				]										
			]
			
        	div content2Right [
				class: "col-md-6"
				
				div row21 [
					class: "row"
					
					h2 col21 [
						class: "col-md-12"
						
						display sBillDetailsTitle
					]					
				]
				
				div rowitems [
                    class: "row st-spacing-bottom"
                    
                    ul items [
                       display sAccountLabel [
                       	   class: "st-bullet"
                       	   logic: [
                       	   	if sFlagShowAccountNumber != "true" then "remove"
                       	   ]
                       ]
                       display sServiceLabel [
	                   		class: "st-bullet"
	                   		logic: [		                		
		                		if sRegType != "service" || sFlagShowServiceNumber != "true" then "remove"	    		                
		                	]
                       ]
                       display sBillLabel [
                       	    class: "st-bullet"
                       	    logic: [
                       	   		if sFlagShowBillingDate != "true" then "remove"
                       	   ]                      	    
                       ]
                       display sAmountLabel [
	                       	class: "st-bullet"
	                  	    logic: [
	                   	   		if sFlagShowAmount != "true" then "remove"
	                   	   ]                       	
                       ]                                         
                       display sSelfReg0Label [
	                       	class: "st-bullet"
	                  	    logic: [
	                   	   		if sFlagShowSelfReg0 != "true" then "remove"
	                   	   ]                       	
                       ]
                       display sSelfReg1Label [
	                       	class: "st-bullet"
	                  	    logic: [
	                   	   		if sFlagShowSelfReg1 != "true" then "remove"
	                   	   ]                       	
                       ]
                       display sSelfReg2Label [
	                       	class: "st-bullet"
	                  	    logic: [
	                   	   		if sFlagShowSelfReg2 != "true" then "remove"
	                   	   ]                       	
                       ]                       
                       display sSelfReg3Label [
	                       	class: "st-bullet"
	                  	    logic: [
	                   	   		if sFlagShowSelfReg3 != "true" then "remove"
	                   	   ]                       	
                       ]                       
                       display sItemEmail [class: "st-bullet" ]              
                    ]				    
				]
					
				div row22 [
					class: "row"
					
					p col22 [
						logic: [
		            		if sStreamType  != "document" then "hide"
		            	]
						class: "col-md-12"
						
						display sPara1
					]
					
					p col22Bill [
						logic: [
		            		if sStreamType  != "bill" then "hide"
		            	]
						class: "col-md-12"
						
						display sPara1Bill
					]
				]
								
				div row24 [
					class: "row"
					
					p col24 [
						class: "col-md-12"
						
						display sPara3
					]
				]										
			]
			 
        	div buttons2 [
				class: "st-buttons"
									
				div buttons2Row1 [
					class: "row"
					
					div buttons2Col1 [
						class: "col-md-12"
								
						navigation accountLevelSubmit(validateRegFieldAnswers, "{Next}") [
							class: "btn btn-primary"
							logic: [
			            		if sRegType != "account" then "remove"	    		                
			            	] 
							data: [
								fAccountNumber,
								fBillingDate,
								fAmount,
								fSelfReg0,
								fSelfReg1,
								fSelfReg2,
								fSelfReg3,
								fSelfReg4,
								sFlagShowAccountNumber,
								sFlagShowBillingDate,
								sFlagShowAmount,
								sFlagShowSelfReg0,
								sFlagShowSelfReg1,
								sFlagShowSelfReg2,
								sFlagShowSelfReg3,
								sFlagShowSelfReg4
							]
							attr_tabindex: "13"							
						]       			
						
						navigation serviceLevelSubmit(validateRegFieldAnswers, "{Next}") [
							class: "btn btn-primary"
							logic: [
			            		if sRegType != "service" || sFlagShowServiceNumber != "true" then "remove"	    		                
			            	] 
							data: [
								fAccountNumber,
								fServiceNumber,
								fBillingDate,
								fAmount,
								fSelfReg0,
								fSelfReg1,
								fSelfReg2,
								fSelfReg3,
								fSelfReg4,
								sFlagShowAccountNumber,
								sFlagShowServiceNumber,
								sFlagShowBillingDate,
								sFlagShowAmount,
								sFlagShowSelfReg0,
								sFlagShowSelfReg1,
								sFlagShowSelfReg2,
								sFlagShowSelfReg3,
								sFlagShowSelfReg4
							]
							
							require: [
								fAccountNumber => fAccountNumber.sRequired,
								fServiceNumber => fServiceNumber.sRequired,
								fBillingDate => fBillingDate.sRequired,
								fAmount => fAmount.sRequired,
								fCheckBoxes
							]
							attr_tabindex: "13"							
						]     
						
		                navigation billDetailsCancel(gotoLogin, "{Cancel}") [
							class: "btn btn-secondary"
							attr_tabindex: "14"
						] 							
						
						navigation billDetailsBack2(gotoLogin, "{Back}") [
							attr_tabindex: "15"
						]
					]				
				]		
			]
        ]
    ]   
    
    /**************************************************************************
     * 6. Verify if the bills are loaded for the selected bill stream. 
     */ 
    action areBillsLoaded [      	
    	 UcBillStream.getRegTypeByBillStream(fBillType, sRegType) 
		 switch UcBillStreams.areBillsLoaded(
		 	fBillType
		 ) [		 
		 	case "true" setMultiSelectedStream
		 	case "false" billsNotLoadedErrorMsg		 						
			default genericErrorMsg
		]
	] 
	
	/**************************************************************************
     * 7. Verify if the bills are loaded for the selected bill stream. 
     */ 
	action setMultiSelectedStream [    	
    	sSelectedBillStream = fBillType
    	goto(initialize)
    ]
    
    /**************************************************************************
     * 8. Validate the registration fields. 
     */ 
    action validateRegFieldAnswers [      	 
		 switch UcBillStream.validateRegFieldAnswers(
		 	sAppType,
		 	sSelectedBillStream, 
		 	fAccountNumber.pInput,
		 	fServiceNumber.pInput,  
		 	fBillingDate.aDate, 
		 	fAmount.pInput,		 	
			fSelfReg0.pInput,
			fSelfReg1.pInput,
			fSelfReg2.pInput,
			fSelfReg3.pInput,
			fSelfReg4.pInput,
			sFlagShowAccountNumber,
			sFlagShowServiceNumber,
			sFlagShowBillingDate,
			sFlagShowAmount,
			sFlagShowSelfReg0,
			sFlagShowSelfReg1,
			sFlagShowSelfReg2,
			sFlagShowSelfReg3,
			sFlagShowSelfReg4,
			sCompanyId
		 ) [		 
		 	case "successB2BAccount" determineNextUseCaseB2B
		 	case "successB2BService" determineNextUseCaseB2B
		 	case "successB2CAccount" verifyAccountDetailsB2C		 			 	
		 	case "empty" emptyBillDetailsErrorMsg
		 	case "errorB2BAccount" invalidBillDetailsB2BMsg
		 	case "errorB2BService" invalidBillDetailsB2BMsg							
		 	case "errorB2CAccount" invalidBillDetailsB2CMsg
		 	case "errorB2BCompanyNull" errorCompanyIdNullB2BMsg
			default genericErrorMsg
		]
	] 
	
	/**************************************************************************
     * 9. Checks the flag and determines where to go.
     */
	action determineNextUseCaseB2B [
		sReqWorkflow = "selfReg"
		if sAccountInfoFailed == "true" then
			gotoRegistrationB2B
		else
			gotoRegElectronicTnC	
	]
	    
    /**************************************************************************
     * 10. Go to the b2b registration usecase. 
     */     
    action gotoRegistrationB2B [    
    	gotoUc(registrationB2B)
    ] 
    
	/**************************************************************************
	 * 11. Check if another b2c user has already registered. 
	 */
	action verifyAccountDetailsB2C [
		 UcBillRegistration.init(sSelectedBillStream, fAccountNumber.pInput, fBillingDate.aDate, 
		 	                     fAmount.pInput, fSelfReg0.pInput, fSelfReg1.pInput, 
		 	                     fSelfReg2.pInput, fSelfReg3.pInput, fSelfReg4.pInput)
		
		 switch UcBillRegistration.isAccountAvailable() [		 
		 	case "success" IsThisAccountEligible
		 	case "registered" duplicateAccountMsg
		 	case "duplicate_account" clearFields					
			default invalidBillDetailsB2CMsg
		]
	]     

	/**************************************************************************
	 * 12. System queries for portal eligibility of any accounts associated 
	 * 		with this organization id
	 */
	action IsThisAccountEligible [
		spIsCustomerEligible.account = sAcctForReg
		spIsCustomerEligible.statusPaymentGroup = sStatusPaymentGroup
		switch apiCall AccountStatus.IsCustomerEligibleForPortal(spIsCustomerEligible, srIsCustomerEligible, ssIsCustomerEligible) [
			case apiSuccess  CheckEligibility
			default accountNotEligibleErrorB2CMsg
		]
	]
	
	/**************************************************************************
	 * 12a. System checks checks results
	 */
	action CheckEligibility [
		sIsEligible = srIsCustomerEligible.isEligible
		if sIsEligible == "true" then
			determineNextUseCaseB2C
		else
			accountNotEligibleErrorB2CMsg
	] 

    /**************************************************************************
     * 12. Checks the flag and determines where to go.
     */
	action determineNextUseCaseB2C [
		sReqWorkflow = "selfReg"
		if sAccountInfoFailed == "true" then
			gotoRegistrationB2C
		else
			gotoRegElectronicTnC	
	]

	
	/**************************************************************************
     * 13. Go to the registration usecase. 
     */     
    action gotoRegistrationB2C [    
    	gotoUc(fffcReg99B2C)
    ] 
       
	/**************************************************************************
	 * 14. Go to the registration login info usecase.
	 */    
    action gotoRegElectronicTnC [    
		gotoUc(fffcReg03ElectronicTnC)
    ] 
    
	/**************************************************************************
     * 15. Clears the fields for new inputs
     */
	action clearFields [
		fAccountNumber.pInput = ""
		fServiceNumber.pInput = ""
        fBillingDate.aDate = ""  
        fAmount.pInput = ""  
        fSelfReg0.pInput = ""  
        fSelfReg1.pInput = ""  
        fSelfReg2.pInput = ""  
        fSelfReg3.pInput = ""  
        fSelfReg4.pInput = ""                   
        goto(duplicateAccountMsg) 
	]  
	
    /**************************************************************************
     * 16. Display duplicate account error message
     */ 
    action duplicateAccountMsg [    
        displayMessage(type: "danger" msg: msgDuplicateAccount)                     
        goto(gotoScreen)              
    ]
    
    /**************************************************************************
	 * 17. Shows the registration billing info usecase screen.
	 */ 
	action gotoScreen [
        goto(regBillingInfoScreen)  
    ] 
    
    /**************************************************************************
     * 18. User clicks the cancel button. Go to the login usecase.
     */    
    action gotoLogin [
    	gotoModule(LOGIN)
    ]
    
    /**************************************************************************
     * 19. User clicks the back button. Decides where to go based on the 
     *     bill stream count.
     */
/*     action gotoPreviousScreen [
    	sFlag = "false" 
    	if sStreamCount == "one" then
			gotoRegTermsAndConditions
		else
			checkBillCountFlag
    ]
*/    
    /**************************************************************************
     * 20. User clicks the back button. Go to the registration T&C usecase.
     */
    action gotoRegTermsAndConditions [
    	gotoUc(fffcReg04WebTnC)
    ]  
    
    /**************************************************************************
     * Alternative Paths
     ***************************************************************************/   
     
    /**************************************************************************
     * 6.1 Display bill not loaded error message. 
     */ 
    action billsNotLoadedErrorMsg [    
        displayMessage(type: "danger" msg: msgBillsNotLoadedError)
        goto(gotoScreen)  
    ]   
    
    /**************************************************************************
     * 8.1 Display the empty registration fields input error message 
     */ 
    action emptyBillDetailsErrorMsg [    
        displayMessage(type: "danger" msg: msgEmptyBillDetailsError)
        goto(gotoScreen)  
    ]   
    
    /**************************************************************************
     * 8.2 Display the the b2c invalid bill details error message. 
     */ 
    action invalidBillDetailsB2CMsg [    
        displayMessage(type: "danger" msg: msgInvalidBillDetailsB2CError)
        goto(gotoScreen)  
    ]   
    
    /**************************************************************************
     * 8.3 Display the b2b invalid bill details error message. 
     */ 
    action invalidBillDetailsB2BMsg [    
        displayMessage(type: "danger" msg: msgInvalidBillDetailsB2BError)
        goto(gotoScreen)  
    ] 
    
    /**************************************************************************
     * 8.4 Display the b2b company id null error message. 
     */ 
    action errorCompanyIdNullB2BMsg [    
        displayMessage(type: "danger" msg: msgCompanyIdNullB2BError)
        goto(gotoScreen)  
    ]  
    
    action accountNotEligibleErrorB2CMsg [
    	displayMessage(type:'danger' msg: msgCustomerNotEligibleB2CError)
    	goto (gotoScreen)
    ]
    /**************************************************************************
     * 3.1 Bill stream initializing error.
     * 3.2 Bill stream initializing default error.
     * 6.2 Bills loaded checking error message.
     * 8.5 Validating registration fields error. 
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)
        goto(gotoScreen)
	]	
]