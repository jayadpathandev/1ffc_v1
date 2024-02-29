useCase fffcEditDisplayAccount [
	
     documentation [
		triggers: [[
			1. 
		]]
		preConditions: [[
			1. The user must be logged into the system for this use case to work.
		]]
		postConditions: [[
			1. 
		]]
	]
	
	startAt init
    shortcut fffcEditDisplayAccounts(init) [offset]
	
	importJava Session(com.sorrisotech.app.utils.Session) 
	importJava DisplayAccountMasked(com.sorrisotech.fffc.account.DisplayAccountMasked)
	importJava EditDisplayAccountHelper(com.sorrisotech.fffc.account.EditDisplayAccountHelper)
   
    /*=============================================================================================
     * Data objects used by the use case.
     *===========================================================================================*/
	native string offset   = "not set"
	native string sAccount  = ""
	native string sPayGroup = ""
	native string sNicknameAttrName
	native string sUserId = Session.getUserId()
	volatile native string sNickname = EditDisplayAccountHelper.getUsersNicknameValue(sUserId, sNicknameAttrName)
	volatile native string sMaskedDisplayAccount = DisplayAccountMasked.getMaskedDisplayAccount(sAccount)
	
	string sPopinTitle = "{Edit Nickname}"
	
	structure(message) oMsgSaveNicknameSuccess [    
        string(title) sTitle = "{Successfully updated.}"
        string(body) sBody = "{Nickname were successfully updated.}"
    ]
    
    structure(message) oMsgRemoveNicknameSuccess [    
        string(title) sTitle = "{Successfully deleted.}"
        string(body) sBody = "{Nickname was successfully deleted.}"
    ]
    
    structure(message) oMsgGenericError [
        string(title) sTitle = "{Something wrong happened}"
        string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later}"
    ]
    
    structure(message) oMsgNoSmsChangeMade [    
        string(title) sTitle = "{No change was made}"
        string(body) sBody = "{There was no change in the Nickname. The system took no action as a result.}"
    ]
	
	string sAccNumLabel             = "{Account number:}"
	
	field fUserDisplayAccountNickName [  													                                             
	    string(label) sLabel = "{Update Nickname}"
	    input(control) pInput("^[a-zA-Z0-9_]{5,25}$", fUserDisplayAccountNickName.sValidation)
        string(validation) sValidation = "{Please provide a valid nickname. Your nickname must be 5-25 characters, using letters, numbers, and underscores(_).}"
        string(required) sRequired = "{This field is required.}"     
	]
	
    /*=============================================================================================
     * 1. System redirects user to an external site.
     *===========================================================================================*/
     
     // -- 
     action init[
     	Session.getAccount(offset, sAccount) 
     	Session.getPayGroup(offset, sPayGroup)
     	sNicknameAttrName = "Nickname"+"-"+sPayGroup+"-"+sAccount
     	goto (assignValuesToField)
     ]
     
     action assignValuesToField [
     	switch sNickname [
     		case "not set" setMaskedDisplayAccountToField
     		default setNicknameToField
     	]
     ]
     
     action setNicknameToField[
     	fUserDisplayAccountNickName.pInput = sNickname
     	goto(editDisplayAccountPopin)
     ]
     
     action setMaskedDisplayAccountToField[
     	fUserDisplayAccountNickName.pInput = sMaskedDisplayAccount
     	goto(editDisplayAccountPopin)
     ]
     
   /*-------------------------------------
     *  edit Nickname popin.
     --------------------------------------*/    
xsltFragment editDisplayAccountPopin [

    form content [
        class: 'modal-content'

        h4 heading [
            class: 'modal-header'
            display sPopinTitle
        ]

        div updateNickname [

            class: 'modal-body'
            
            div accountCol [
            	display sAccNumLabel [
                	class: "st-dashboard-summary-label"
            		append_space: "true"
            	] 
            	
            	display sMaskedDisplayAccount [
            		class: "st-dashboard-summary-value"
            	]
	        ]
			
			div nickNameCol [
            
        		display fUserDisplayAccountNickName [
           			class: 'st-toggle'
          		    label_class: 'col-10'
            		field_class: 'col-8'
            		control_attr_tabindex: '2'
        		]
           ]
        ]
        
        div nicknameUpdateRemoveAndCancel [
        	class: "modal-footer"
        	attr_style: "flex-wrap: nowrap"
        	
            navigation save (hasChanges, "{Update}") [
            	type: "save"
                class: "btn btn-primary"
                attr_tabindex: "8"
                require: [
                	fUserDisplayAccountNickName
                ]
            ]
                   
            navigation update (removeNicknameNls, "{Remove}") [
            	type: "save"
                class: "btn btn-primary"
                attr_tabindex: "9"
            ]
            
			navigation cancel (init, "{Cancel}") [
				type: "cancel"
				class: "btn btn-secondary"
				attr_tabindex: "10"
			]
        ]
    ]
]

	action hasChanges[
		if sNickname == fUserDisplayAccountNickName.pInput then noSmsChangeMadeMsg
		else saveUpdatedNicknameNls
	]
	
	/*Sending updated nickname to Nls */
	action saveUpdatedNicknameNls[
		switch EditDisplayAccountHelper.sendLoanNicknameToNls(sAccount, fUserDisplayAccountNickName.pInput) [
		case "true" saveNicknameUserProfile
		case "false" genericErrorMsg
		default genericErrorMsg
		]
	]
	
	
	/*Setting user nickname value in user profile. (auth_user_profile table) */
	action saveNicknameUserProfile[
		 switch EditDisplayAccountHelper.upsertNicknameToUserProfile(sUserId, sNicknameAttrName, fUserDisplayAccountNickName.pInput)[
    		case "true" 	genericSaveSuccessMsg
    		case "false" 	genericErrorMsg
    		default genericErrorMsg
    	]
	]
	
	/*Sending empty string as nickname to Nls as user removed nickname*/
	action removeNicknameNls[
		switch EditDisplayAccountHelper.sendLoanNicknameToNls(sAccount,"null") [
		case "true" removeNicknameUserProfile
		case "false" genericErrorMsg
		default genericErrorMsg
		]
	]
    
    /* Setting empty string to update the user nickname value as empty string user profile. (auth_user_profile table)*/
    action removeNicknameUserProfile[
    	switch EditDisplayAccountHelper.upsertNicknameToUserProfile(sUserId, sNicknameAttrName ,"null")[
    		case "true" 	genericRemoveSuccessMsg
    		case "false" 	genericErrorMsg
    		default genericErrorMsg
    	]
	]
    
     /* Display a generic success message. */
    action genericSaveSuccessMsg [
		// TODO Audit event for this 
       displayMessage(type: "success" msg: oMsgSaveNicknameSuccess)               
       gotoUc (overview)
    ]
    
    /* Display a generic success message. */
    action genericRemoveSuccessMsg [
		// TODO Audit event for this 
       displayMessage(type: "success" msg: oMsgRemoveNicknameSuccess)               
       gotoUc (overview)
    ]
    
    /* Display a generic error message. */
    action genericErrorMsg [
    	// TODO Audit event for this 
        displayMessage(type: "danger" msg: oMsgGenericError)            
        gotoUc(overview)
    ]
    
     /* Displays a message that no changes were made to the mobile number. */
    action noSmsChangeMadeMsg [    
        displayMessage(type: "warning" msg: oMsgNoSmsChangeMade)                           
         gotoUc(overview)          
    ]
]
    
