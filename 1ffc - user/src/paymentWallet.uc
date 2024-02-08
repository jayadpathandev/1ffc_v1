useCase paymentWallet [
   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 26-Apr-2016
    *
    *  Primary Goal:
    *       1. Display payment wallet.
    *       
    *  Alternative Outcomes:
    *       1. None 
    *                     
    *   Major Versions:
    *        1.0 26-Apr-2016 First Version Coded [Maybelle Johnsy Kanjirapallil]
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the sub menu option Payment Wallet in the Payments page.
        ]]
        postConditions: [[
            1. Primary -- Payment wallet details are displayed.
        ]]
    ]
    actors [ 
        modify_payment
    ]    
		    
    startAt init
        
    shortcut walletPaymentResponse(getWalletInfo) [
    	response_type,
    	error_message_type,
    	response_code,
    	response_message,
    	token,
    	transaction_id
    ]
    
    shortcut createIframeAddSource(createIframeAddSource) [
    	sUserId,
    	sUserName,
    	sIframeSourceType,
    	sIframeOnetime
    ]
    
    shortcut createIframeEditSource(createIframeEditSource) [
    	sUserId,
    	sUserName,
    	sIframeSourceType,
    	sPaymentSourceId,
    	sNickName,
    	sDefault
    ]
    
    child utilImpersonationActive(utilImpersonationActive)
        
    /*************************
	* DATA ITEMS SECTION
	*************************/
	importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava ForeignProcessor(com.sorrisotech.app.common.ForeignProcessor)
	importJava I18n(com.sorrisotech.app.common.utils.I18n)
	importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
    importJava Session(com.sorrisotech.app.utils.Session)    
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
            
    import apiPayment.pmtRequest    			    	
	import apiPayment.getWallet
	import apiPayment.deleteWallet   
    import billCommon.sPayGroup
    import billCommon.sPayAccountExternal  
	import paymentCommon.sNumSources
    import paymentCommon.sMaxSources
    import paymentCommon.sPmtGroupConfigResult    
    import paymentCommon.sPaymentSourceBankEnabled
    import paymentCommon.sPaymentSourceCreditEnabled
    import paymentCommon.sPaymentSourceDebitEnabled
    import paymentCommon.sPaymentSourceSepaEnabled
    import paymentCommon.sSourceCreateMsgFlag
    import paymentCommon.sSourceUpdateMsgFlag    
    import paymentCommon.sSourceDeleteMsgFlag
    import paymentCommon.sSourceErrorMsgFlag
    import paymentCommon.sSourceCancelMsgFlag
    import paymentCommon.sAppType
    
    serviceStatus ssStatus
              
    serviceParam(Payment.GetWallet) srGetWalletParam    
    serviceResult(Payment.GetWallet)srGetWalletResult
        
    serviceParam (Payment.GetWalletByToken) srGetWalletInfoParam
	serviceResult (Payment.GetWalletByToken) srGetWalletInfoResult
	
	serviceParam (Payment.GetWalletCount) srGetWalletCountParam
	serviceResult (Payment.GetWalletCount) srGetWalletCountResult
			
	serviceParam(Payment.DeleteWallet) srDeleteSourceParam	
	serviceResult(Payment.DeleteWallet) srDeleteSourceResult
	
	persistent string sIframeSourceType
	persistent string sIframeOnetime
	string (iframe) sCreateIframe
	string (iframe) sEditIframe
	
	string sMessageDisplay = ""
	string sResponseMessage = ""
    string sPaymentMethodsTitle   = "{My payment methods}"
    string sPaymentMethodsHelp   = "{Edit your credit card info below}"               
        
    string sLabelPaymentEdit      = "{Edit payment information}"
    string sLabelPaymentDelete    = "{Delete payment option}"
   
    string sSourceErrorMsg  = "{An error occurred while trying to fulfill your request. Please try again later}"
    
    static          sSourceCancelI18n = "{Payment action canceled.}" 
    volatile string sSourceCancelMsg  = I18n.translate ("paymentWallet_sSourceCancelI18n")
    
    string sMaxPaymentMethodsReached = "{The maximum number of payment methods have already been added to your wallet. Please remove some to add more.}"
        
    static          sMessageCreate   = "{Payment method <1> has been successfully added.}"        
    volatile string sSourceCreateMsg = I18n.translate ("paymentWallet_sMessageCreate", sNickName)
    static          sMessageEdit     = "{Payment method <1> has been successfully updated.}"      
    volatile string sSourceEditMsg   = I18n.translate ("paymentWallet_sMessageEdit", sNickName)  
    static          sMessageDelete   = "{Payment method <1> has been successfully removed.}"   
    volatile string sSourceDeleteMsg = I18n.translate ("paymentWallet_sMessageDelete", sNickName) 
           
    string sDeleteSourceTitle   = "{CONFIRM REMOVE PAYMENT METHOD}"
    
    static          sScheduledMessage   = "{<1> is currently in use by a future payment.}"
    volatile string sScheduledDeleteMsg = I18n.translate ("paymentWallet_sScheduledMessage", sNickName)

    static          sAutomaticMessage = "{<1> is currently in use by an automatic payment. To avoid cancellation of the automatic payment and all future payments, select NO and select a new payment method for the automatic payment.}"
    volatile string sAutoDeleteMsg    = I18n.translate ("paymentWallet_sAutomaticMessage", sNickName)

    static          sText              = "{Are you sure you want to delete <1>?}"
    volatile string sDeleteSourceText1 = I18n.translate ("paymentWallet_sText", sNickName)        
    string sDeleteSourceText2 = "{This action cannot be undone.}"           
               
    native string sCreateIframeFlag    = "false"    
    native string sEditIframeFlag      = "false"
        
    native string sPaymentMethodNickName = ""
    native string sPaymentMethodType = ""
    native string sPaymentMethodAccount = ""
    input sSourceExpiry = ""    
    native string sAppUrl           = AppConfig.get("user.app.url")
    native string sAppUsecase       = "paymentWallet"
    native string response_type     = ""
    native string error_message_type = ""
    native string response_code     = ""
    native string response_message  = ""
    native string token    			= ""
    native string transaction_id    = ""    
   	native string sUserId          = Session.getUserId()      
    native string sDummy           = ""           
    native string sPaymentType     = ""      
    native string status
    native string sNtfParams	   = ""
    native string sWalletShortcut  = ""
    native string sSendCreateEmailFlag = NotifUtil.isNotificationEnabled(sUserId, "payment_wallet_create_success")
    native string sSendEditEmailFlag   = NotifUtil.isNotificationEnabled(sUserId, "payment_wallet_edit_success")
    native string sSendDeleteEmailFlag = NotifUtil.isNotificationEnabled(sUserId, "payment_wallet_delete_success")
    native string sSourceStatus        = UcPaymentAction.checkSource(sUserId, sPaymentSourceId)
    
    persistent native string sMessageError = ""    
    persistent native string sNickName = ""
    persistent native string sUserName = "" 
    persistent native string sPaymentSourceId = ""
    persistent native string sDefault = ""
    
    structure(message) msgNoPmtGroupError [
		string(title) sTitle = "{Configuration problem}"
		string(body) sBody = "{There is no payment group configured to your account. Please contact your System Administrator.}"
	]
	
    structure(message) msgMultiplePmtGroupError [
		string(title) sTitle = "{Not supported}"
		string(body) sBody = "{There are more than one payment group configured to your account. We currently do not support multiple payment groups. Please contact your System Administrator.}"
	]
		
    table tPaymentWalletTable  [
        emptyMsg: "{You have no stored credit cards, debit cards or bank accounts.}"
        
        "SOURCE_DEFAULT" 	=> radioSet cCardsSelect [
	        true : ""
	        false : ""
        ]

        "SOURCE_NAME"    	=> string sName  
        "SOURCE_TYPE"    	=> string sType  
        "SOURCE_NUM"     	=> string sNum
        "SOURCE_EXPIRY"  	=> string sExpiry
        "SOURCE_EXPIRY_NUM" => string sExpiryNum
        "SOURCE_ID"			=> string sId         
        "CARD_STATUS"       => string sCardStatus
        
        "" => string iPayCredit  = "" 
        "" => string iPayDebit   = "" 
        "" => string iPayBank    = ""  
        "" => string iPaySepa    = ""
        
        "" => string sPayType   = "" 
        
        "" => string  sCardExpired  = "Card expired" 
              
        link "" sourceEdit(checkSourceStatusForEdit) [
           sPaymentSourceId: sId              
           sPaymentType: sType   
           sNickName: sName      
           sDefault:  cCardsSelect
        ]
        
        link "" sourceDelete(checkSourceStatusForDelete) [            
           sPaymentSourceId: sId              
           sPaymentType: sType 
           sNickName: sName
        ]
        
        column defaultCol("{Default}") [
            elements: [cCardsSelect]
        ]       
  
       column nameCol("{Name}") [
       		tags: ["payment-wallet-type",  "d-none", "d-md-table-cell"]
            elements: [            	
            	iPayCredit: [
            		^class: 'payment-wallet-type-credit'
            		^class: sType
        		]
        		iPayDebit: [
            		^class: 'payment-wallet-type-debit'
            		^class: sType
        		]
        		iPayBank: [
            		^class: 'payment-wallet-type-bank'
            		^class: sType
        		]
        		iPaySepa: [
            		^class: 'payment-wallet-type-sepa'
            		^class: sType
        		]        		
        		sName: [
        			^class: 'st-payment-wallet-nickname'
        		]
            ]
        ]        
      
        column numberCol("{Number}") [
            elements: [
            	sNum: [
            		^class: 'payment-wallet-source-num'
        		]
    		]
        ] 
        
        column expiryCol("{Expiry}") [
            elements: [
            	sExpiry: [
            		^class: 'payment-wallet-expiry'
        		]
    		]
            tags: [ "d-none", "d-md-table-cell" ]
        ]  
      
        column actionsCol("{Actions}") [
           tags: ["payment-wallet-status"]	
           elements: [                          		
            	sourceEdit: [           			    			
           			attr_class: "payment-edit-img st-left-space"
           		],
            	
            	sourceDelete: [
           			^type: "popin"    
           			attr_class: "payment-cancel-img st-left-space"       			
           		],  
           		
           		sCardExpired: [
            		^class: 'pay-card-expired'
            		^class: sCardStatus
        		]
           ]
        ]                   
    ]

	// -- handling impersonation --
 	import utilImpersonationActive.sImpersonationActive
 	native string bImpersonateActive
 
    /*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	
	/* 1. Get the most recent document. */
	action init [
		bImpersonateActive = sImpersonationActive		
		sUserName = getUserName()
				
		 goto(getWallet)         
    ]
	
	/* 2. Get wallet details. */
	action getWallet [				
		srGetWalletParam.USER_ID     = sUserId
		srGetWalletParam.DATE_FORMAT = "MM/yyyy"
		
		switch apiCall Payment.GetWallet(srGetWalletParam, srGetWalletResult, ssStatus) [
		    case apiSuccess getResults
		    default paymentWalletScreen
		]            
    ]
    
	/* 3. Store the results from the service to the payment wallet table */
    action getResults [
        tPaymentWalletTable = srGetWalletResult.wallet
                
        goto(paymentWalletScreen)   
    ]
            
    /* 4. Shows the payment wallet details. */
    xsltScreen paymentWalletScreen("{Payment}") [
		
		child utilImpersonationActive
		
		div message1 [
			logic: [						
				if sPmtGroupConfigResult != "nopaygroup" then "hide"						
			]				
			class: "alert alert-danger"
			attr_role: "alert"				
			display msgNoPmtGroupError 
		]
		
		div message2 [
			logic: [						
				if sPmtGroupConfigResult != "manypaygroups" then "hide"						
			]				
			class: "alert alert-danger"
			attr_role: "alert"				
			display msgMultiplePmtGroupError 
		]
		
		div walletContent [
			logic: [
				if sPmtGroupConfigResult == "nopaygroup" then "remove"
				if sPmtGroupConfigResult == "manypaygroups" then "remove"									
			]
								
	        /* Display online payment and late fee messages */
	    	form paymentWalletForm [
	    	    class: "row"
	    	    
	    	    display sAppUrl [
	    	    	class: "d-none st-app-url"
	    	    ]
	    	    
	    	    display sAppUsecase [
	    	    	class: "d-none st-app-usecase"
	    	    ]
	    	       	    
	    	    /* Display the payment wallet table. This includes credit, debit card and bank account */
	    	    div paymentWalletRow [
	    	    	class: "row"
	    	    	
		    	    div PaymentWalletHeaderRow [
		    	    	class: "col-md-12"
		    	    	
		    	    	h4 paymentWalletHeader [	    	    						
							display sPaymentMethodsTitle 
						]	    	    									
		    	    ]		
				
					div paymentWalletHelpRow [
						class: "col-md-12 row"
						
						div paymentWalletHelp [
							class: "col-md-6 st-light-label"
							display sPaymentMethodsHelp						
						]
						
						div messagesCol [	    
		    	    		class: "col-md-6"
		    	    		
		    	    		div messageDisplay [
		    	    			class: "visually-hidden float-end"
		    	    			display sMessageDisplay [
		    	    				class: "st-padding-top"
		    	    			]
		    	    		]
		    	    		
		    	    		div sourceCreateMsg	[
			    	    		logic: [
									if sSourceCreateMsgFlag == "false" then "remove"						
								]
								class: "st-payment-success float-end"
		    	    			
				    	    	display sSourceCreateMsg [
									class: "st-padding-top"
								]
							]
							
							div sourceEditMsg [
			    	    		logic: [
									if sSourceUpdateMsgFlag == "false" then "remove"						
								]
								class: "st-payment-success float-end"
								
								display sSourceEditMsg [
									class: "st-padding-top"
								]
							]
							
							div sourceDeleteMsg [
			    	    		logic: [
									if sSourceDeleteMsgFlag == "false" then "remove"						
								] 	    	    		
								class: "st-payment-success float-end"
								
								display sSourceDeleteMsg [		
									class: "st-padding-top"
								]
							]
							
							div sourceErrorMsg [
			    	    		logic: [
									if sSourceErrorMsgFlag == "false" then "remove"						
								] 	    	    		
								class: "st-payment-error float-end"
									
								display sMessageError [
									class: "st-padding-top"
								]
							]
	
							div sourceCancelMsg [
			    	    		logic: [
									if sSourceCancelMsgFlag == "false" then "remove"						
								]
								class: "st-payment-success float-end"
								
								display sSourceCancelMsg [
									class: "st-padding-top"
								]
							]						
						]	
					]       
								
					div paymentWalletTable [
						class: "col-md-12 st-padding-top"
						display tPaymentWalletTable
					] 		     
				]
			]          
	  
			/* Display help icons */
	    	div helpIcons [
				class: "col-md-12 mb-3"
				
				div helpIconsRow [
					class: "row st-payment-help-icons"
					
					div editIconCol [
						class: "col-6 col-sm-5 text-end "
						
						div editIcon [
							class: "st-payment-edit-details"
							display sLabelPaymentEdit 
						]
					]
					
					div iconsMiddleCol [
						class: "col-sm-2 d-none d-md-table-cell"
						
						display sDummy
					]
					
					div cancelIconCol [
						class: "col-6 col-sm-5 text-start"
						div cancelIcon [
							class: "st-payment-cancel-details"
							display sLabelPaymentDelete 
						]
					]
				]
			]
				 
	 		/* display buttons for Add new credit card, Add new debit card and Add new bank account */
	        div buttons [
	        	class: "row text-center mb-5 pt-4 st-payment-wallet-buttons"
	        	
	        	logic: [
					if sNumSources >= sMaxSources then "remove"
				]
	                
	          	div buttonsCol2 [
	          		class: "col-12 col-md-3 mb-2"
	          		
	            	logic: [
						if sPaymentSourceBankEnabled == "false" then "remove"
					]
	          			            
		            navigation addNewBankAccount(createBankAccountIframeAction, "{ADD NEW BANK ACCOUNT}") [
		                class: "btn btn-primary st-bankaccount-button"
		            ]
		        ]
	          	
	          	div buttonsCol3 [
	          		class: "col-12 col-md-3 mb-2"
	          		
	            	logic: [
						if sPaymentSourceDebitEnabled == "false" then "remove"
					]
	
	          	    navigation addNewDebitCard(createDebitCardIframeAction, "{ADD NEW DEBIT CARD}") [
	                     class: "btn btn-primary st-debitcard-button"		
	          		]
	          	]
	          	
	            div buttonsCol4 [
	            	class: "col-12 col-md-3 mb-2"
	            	
	            	logic: [
						if sPaymentSourceCreditEnabled == "false" then "remove"
					]
	            	
	          	    navigation addNewPmtSource(createCreditCardIframeAction, "{ADD NEW CREDIT CARD}") [
	                     class: "btn btn-primary st-creditcard-button"
	          		]	
	          	]
	          	
	          	div buttonsCol5 [
	          		class: "col-12 col-md-3 mb-2"
	          		
	            	logic: [
						if sPaymentSourceSepaEnabled == "false" then "remove"
					]
	          			            
		            navigation addNewSepaAccount(createSepaAccountIframeAction, "{ADD NEW SEPA ACCOUNT}") [
		                class: "btn btn-primary st-sepaaccount-button"
		            ]
		        ]
	          	
			]
			
			div buttonsMaxReached [
				class: "row"
				
				logic: [
					if sNumSources < sMaxSources then "remove"
				]
				
				div paymentMethodButtonsMaxReached [
					class: "col-md-12"
					display sMaxPaymentMethodsReached [
						class: "st-error text-center float-start st-width100"
					]
				]
			]
			
			div createSourceIframe [
				class: "st-payment-iframe"
				logic: [
					if sCreateIframeFlag == "false" then "remove"	
			    ]
				display sCreateIframe			
			]
			
			div editSourceIframe [
				class: "st-payment-iframe"
				logic: [
					if sEditIframeFlag == "false" then "remove"	
			    ]
				display sEditIframe			
			]
		]
    ]  
    
    /* 5. Check source status before delete.*/
    action checkSourceStatusForDelete [
    	if sSourceStatus == "progress" then 
    		genericErrorMsg
    	else 
    		resetWalletFlags
    ]
    
     /* 6. Resets payment wallet flags. */
    action resetWalletFlags [
    	sSourceCreateMsgFlag = "false"
	    sSourceUpdateMsgFlag = "false"    
	    sSourceDeleteMsgFlag = "false"
	    sSourceErrorMsgFlag = "false"
	    sSourceCancelMsgFlag = "false"	    
	    goto(deleteSourcePopin)
    ]
    	
    /* 7. Cancel payment method popin.*/    
    xsltFragment deleteSourcePopin [
        
        form content [
        	class: "modal-content"
        
	        div deleteSourceHeading [
	            class: "modal-header"
	            
	            div deleteSourceRow [
	            	class: "row text-center"
	            	
	            	h4 deleteSourceCol [
	            		class: "col-md-12"
	            		
			            display sDeleteSourceTitle 
					]
				]
	        ]
	        
            div deleteSourceBody [
               class: "modal-body"
               messages: "top"
                                   
                div deleteSourceRow [                	
                    class: "row text-center"
                   	
                   	div scheduledDeleteMsg [
                   		class: "col-md-12 st-payment-error"
                    	logic: [
							if sSourceStatus != "scheduled" then "hide"
						]
                   		
	                    display sScheduledDeleteMsg [
	                    	class: "st-padding-bottom text-justify"
	                    ]
                    ]
                    
                    div autoDeleteMsg [
                    	class: "col-md-12 st-payment-error"    
                    	logic: [
							if sSourceStatus != "auto" then "hide"
						]
						
						display sAutoDeleteMsg [
							class: "st-padding-bottom text-justify"
						] 
                    ]
                    
                    display sDeleteSourceText1 [
                    	class: "col-md-12"    
                    ]
                    display sDeleteSourceText2 [
                    	class: "col-md-12 text-danger"    
                    ]																					
				]                
            ]
                       
            div deleteSourceButtons [
                class: "modal-footer"

    			navigation yesButton (deleteWallet, "{YES}") [
                	class: "btn btn-primary"
                	attr_tabindex: "10"
                	// -- disabled button shows if agent is impersonating --
                	logic: [if bImpersonateActive == "true" then "remove"]
            	]

    			navigation yesButtonDisabeld (deleteWallet, "{DISABLED FOR AGENT}") [
                	class: "btn btn-primary disabled"
                	attr_tabindex: "10"
                	// -- this button shows if agent is impersonating --
                	logic: [if bImpersonateActive != "true" then "remove"]
            	]
            	
				navigation noButton (paymentWalletScreen, "{NO}") [
					attr_tabindex: "11"
				] 
            ]
        ]        
    ]
    
    /* 8. Create credit card action.*/ 
    action createCreditCardIframeAction [	
    	sCreateIframeFlag	 = "true"	
		sEditIframeFlag      = "false"
		sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		sIframeSourceType = "credit"
		sIframeOnetime = "false"
		goto (getCreateIframeUrl)
	]  
	
	/* 9. Create debit card action.*/ 
    action createDebitCardIframeAction [	
    	sCreateIframeFlag	 = "true"	
    	sEditIframeFlag      = "false"
    	sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		sIframeSourceType = "debit"
		sIframeOnetime = "false"
		goto (getCreateIframeUrl)
	]
	
	/* 10. Create bank account action.*/ 
    action createBankAccountIframeAction [	
    	sCreateIframeFlag    = "true"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		sIframeSourceType = "bank"
		sIframeOnetime = "false"
		goto (getCreateIframeUrl)
	]
	
	/* 11. Create sepa account action.*/ 
    action createSepaAccountIframeAction [	
    	sCreateIframeFlag    = "true"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		sIframeSourceType = "sepa"
		sIframeOnetime = "false"
		goto (getCreateIframeUrl)
	]
	
	
	/* 12. Get Iframe url.*/ 
	action getCreateIframeUrl [
		sCreateIframe = "createIframeAddSource"
		goto (paymentWalletScreen)				
	]

    /* 13. Check source status before edit.*/
    action checkSourceStatusForEdit [
    	if sSourceStatus == "progress" then 
    		genericErrorMsg
    	else 
    		editSourceIframeAction
    ]
    
    /* 14. Get edit iframe url. */	
	action editSourceIframeAction [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "true"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		sIframeSourceType = sPaymentType
		goto (getEditIframeUrl)
    ]
    
    /* 15. Get edit souce iframe url. */
	action getEditIframeUrl [
		sEditIframe = "createIframeEditSource"
		goto(paymentWalletScreen)
	]
	    
	/* 16. Delete wallet action. */	
	action deleteWallet [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
	    
		srDeleteSourceParam.SOURCE_TYPE = sPaymentType
		srDeleteSourceParam.SOURCE_ID   = sPaymentSourceId
	    srDeleteSourceParam.USER_ID     = sUserId
	    
		switch apiCall Payment.DeleteWallet(srDeleteSourceParam, srDeleteSourceResult, ssStatus) [
            case apiSuccess checkDeleteResult
            default genericErrorMsg
        ]	
    ]
    
    /* 17. Check delete wallet result. */
	action checkDeleteResult [		
		if srDeleteSourceResult.RESULT == "success" then
    		setSourceDeleteMsgFlag
    	else 
    		genericErrorMsg	
	]
	
	/* 18. Sets delete source success flag to true. */
	action 	setSourceDeleteMsgFlag [		
		sSourceDeleteMsgFlag = "true"
			
		auditLog(audit_payment.delete_source_success) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch sSendDeleteEmailFlag [
			case "true" sendDeleteSuccessEmail
			case "false" startPaymentWallet			
			default startPaymentWallet
		]		
	]
	
	/* 19. Sends wallet delete success email. */
	action sendDeleteSuccessEmail [
		sNtfParams = "nickName=" + sNickName + "|" + "accountNumber=" + sPayAccountExternal
		NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_wallet_delete_success")		
		goto(startPaymentWallet)
	]
	
	/* 20. Generic error message. */
	action genericErrorMsg [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "true"
		sSourceCancelMsgFlag = "false"
		
        goto(paymentWalletScreen)
	]
	
	/* 21. Get the wallet info for chosen token. */ 
    action getWalletInfo [	
		srGetWalletInfoParam.SOURCE_ID = token
		
		switch apiCall Payment.GetWalletByToken(srGetWalletInfoParam, srGetWalletInfoResult, ssStatus) [
		    case apiSuccess getWalletInfoSuccess
		    default genericErrorMsg
		]  
	]
	
	/* 22. Get wallet info success. */ 
	action getWalletInfoSuccess [
		sNickName = srGetWalletInfoResult.SOURCE_NAME	
		sPaymentMethodNickName = srGetWalletInfoResult.SOURCE_NAME
		sPaymentMethodType = srGetWalletInfoResult.SOURCE_TYPE
		sPaymentMethodAccount = srGetWalletInfoResult.SOURCE_NUM
		sSourceExpiry = srGetWalletInfoResult.SOURCE_EXPIRY
		sDefault  = srGetWalletInfoResult.SOURCE_DEFAULT	
		goto (getWalletCount)
	]
	
	action getWalletCount [
		srGetWalletCountParam.USER_ID = sUserId
		
		switch apiCall Payment.GetWalletCount(srGetWalletCountParam, srGetWalletCountResult, ssStatus) [
		    case apiSuccess setWalletCount
		    default paymentResponse
		]
	]
	
	action setWalletCount [
		sNumSources = srGetWalletCountResult.COUNT
		
		goto(paymentResponse)
	]
	
	/* 23. Checks the payment response. */
	action paymentResponse [
		switch response_type [
			case "addSourceSuccess" paymentResponseAddSourceSuccess
			case "addSourceError" paymentResponseAddSourceError
			case "editSourceSuccess" paymentResponseEditSourceSuccess
			case "editSourceError" paymentResponseEditSourceError
			case "cancel" paymentResponseCancel
			case "error" paymentResponseError
			default paymentResponseError
		]
	]
	
	/* 24. Payment add source success. */
	action paymentResponseAddSourceSuccess [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "true"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		
		sResponseMessage = sSourceCreateMsg
		
		auditLog(audit_payment.add_source_success) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch sSendCreateEmailFlag [
			case "true" sendCreateSuccessEmail
			case "false" jsonPaymentResponse			
			default jsonPaymentResponse
		]
	]
	
	/* 25. Sends wallet create success email. */
	action sendCreateSuccessEmail [
		sNtfParams = "nickName=" + sNickName + "|" + "accountNumber=" + sPayAccountExternal
		NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_wallet_create_success")		
		goto(jsonPaymentResponse)
	]
	
	/* 26. Payment add source error. */
	action paymentResponseAddSourceError [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "true"
		sSourceCancelMsgFlag = "false"
		
		sResponseMessage = sSourceErrorMsg
		
    	auditLog(audit_payment.add_source_failure) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch UcPaymentAction.resolveErrorMessage(error_message_type, sMessageError) [
    		case "success" checkForErrorMsg
    		case "error" jsonPaymentResponse
    		default jsonPaymentResponse
    	]    	
	]
	
	action checkForErrorMsg [
		// Checks if the string is not empty and also not starts with '{'
		if sMessageError ~~ "^(?!\\{).+$" then 
			addGeneratedErrorMessageToResponse
		else
			jsonPaymentResponse
	]
	
	action addGeneratedErrorMessageToResponse [
		sResponseMessage = sMessageError
		goto(jsonPaymentResponse)
	]
	
	/* 27. Payment edit source success. */
	action paymentResponseEditSourceSuccess [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "true"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "false"
		
		sResponseMessage = sSourceEditMsg
		
    	auditLog(audit_payment.edit_source_success) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch sSendEditEmailFlag [
			case "true" sendEditSuccessEmail
			case "false" jsonPaymentResponse			
			default jsonPaymentResponse
		]
	]
	
	/* 28. Send wallet edit success email. */
	action sendEditSuccessEmail [
		sNtfParams = "nickName=" + sNickName + "|" + "accountNumber=" + sPayAccountExternal
		NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_wallet_edit_success")		
		goto(jsonPaymentResponse)
	]
	
	/* 29. Payment edit source error. */
	action paymentResponseEditSourceError [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "true"
		sSourceCancelMsgFlag = "false"
		
		sResponseMessage = sSourceErrorMsg
		
    	auditLog(audit_payment.edit_source_failure) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch UcPaymentAction.resolveErrorMessage(error_message_type, sMessageError) [
    		case "success" checkForErrorMsg
    		case "error" jsonPaymentResponse
    		default jsonPaymentResponse
    	]    	    	
	]
	
	/* 30. Payment cancel response. */
	action paymentResponseCancel [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "false"
		sSourceCancelMsgFlag = "true"
		
		sResponseMessage = sSourceCancelMsg
		
		goto(jsonPaymentResponse)
	]
	
	/* 31. Payment response error. */
	action paymentResponseError [
		sCreateIframeFlag    = "false"	
	    sEditIframeFlag      = "false"
	    sSourceCreateMsgFlag = "false"
		sSourceUpdateMsgFlag = "false"
		sSourceDeleteMsgFlag = "false"
		sSourceErrorMsgFlag  = "true"
		sSourceCancelMsgFlag = "false"
		
		sResponseMessage = sSourceErrorMsg
		
    	auditLog(audit_payment.add_source_failure) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	goto(jsonPaymentResponse)
	]
	
	
	json jsonPaymentResponse [
    	display sResponseMessage
    	display sNumSources
    	display sPaymentMethodNickName
    	display sPaymentMethodType
    	display sPaymentMethodAccount
    	display sSourceExpiry
    	display sDefault
	]
	
	/* 32. Starts payment wallet usecase.*/
	action startPaymentWallet [
		sWalletShortcut = sAppUrl + "startPaymentWallet"
		foreignHandler ForeignProcessor.writeResponse(sWalletShortcut)
	]
	
	/* 33. Write add iframe response to client with freemarker form that autosubmits. */
	action createIframeAddSource [
		foreignHandler UcPaymentAction.writeIframeAddSourceResponse("iframeAddSourceSubmit.ftl", sorrisoLanguage, sorrisoCountry, sUserId, sUserName, sIframeSourceType, sIframeOnetime, sAppType)
	]
	
	/* 34. Write edit iframe response to client with freemarker form that autosubmits. */
	action createIframeEditSource [
		foreignHandler UcPaymentAction.writeIframeEditSourceResponse("iframeEditSourceSubmit.ftl", sorrisoLanguage, sorrisoCountry, sUserId, sUserName, sIframeSourceType, sPaymentSourceId, sNickName, sDefault, sAppType)
	]
]