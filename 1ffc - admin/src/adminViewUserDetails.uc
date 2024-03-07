useCase adminViewUserDetails [

    /**********************************************************************************************
     *  File:                   adminViewUserDetails.uc
     *  Written by:             Yvette Nguyen
     *  Creation date:          2015-September-14
     *  
     *  Description:
     *      A user with system or business unit administration privileges wants to view a list of users that they can manage. 
     * 		     
	 *  Alternative Outcomes:
	 *    	1. User wants to edit user name
	 *		2. User wants to edit password
	 *   	3. User wants to edit user profile
	 *		4. User wants to activate a user
	 *		5. User wants to deactivate a user
	 *		6. User wants to delete a user 
     * 
	 *  Child use cases are:
	 *          None
	 * 
     *  Minimal success guarantees:
     *      The screen displays user details successfully for both account and user profile.
     * 
	 *  Major Versions:
	 *  	2015-September-14 -- first version.  
	 * 		2016-February-19  -- disable business unit
	 *   
	 * 
     **********************************************************************************************/


    documentation [
        preConditions: [[
            1. A System Administrator or Business Unit Administrator have logged into the system.
        ]]
        triggers: [[
            1. User clicks on the user name link from View Users page
        ]]
        postConditions: [[
            1. Primary -- View user profile. This screen displays view user account and view user profile
            2. Alternative 1 -- user selects change username
            3. Alternative 2 -- user selects change password
            4. Alternative 3 -- user selects deactive user
            5. Alternative 4 -- user selects delete user
            6. Alternative 5 -- user selects activate user
        ]]
    ]
	// menuItem itemAdmin

	startAt loadUserDetails [ sParam_user_id ]

	/**************************
	 * DATA ITEMS SECTION
	 **************************/	
	actors [
	    create_user, 
	    create_admin,
	    create_org_admin
	]
	
	importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil)
	importJava I18n(com.sorrisotech.app.common.utils.I18n)	
	importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
	importJava Session(com.sorrisotech.app.utils.Session)		
	importJava ViewUser(com.sorrisotech.uc.admin1ffc.createadmin.Get1FFCAdminUser)
	
    /**********************************************************************************************
     * DECLARE VARIABLES
     *********************************************************************************************/
    string sAppNameSpace = AuthUtil.getAppNameSpace()
    
    string sPageName = "{User Details}"	
    string sTitleAccount  = "{View user account}"			// View user account title   
    string sTitleConfirmDelete = "{Delete User?}"  		    // Delete confirmation title
    string sTitleConfirmActivate = "{Activate User?}" 		// Activate confirmation title
    string sTitleConfirmDeactivate = "{Deactivate User?}"	// Deactivate confirmation title
    string sUserHeading = "{Username & Password}" 
    string sProfileHeading = "{Profile}"
    string sAccessHeading = "{Access}"
     
    native string sMaskedPassword = "*****"                 // Masked password
    native string sActiveFlag 								// variable to store the status of the user: active or inactive
    native string sUserRole									// Current role of the user who logs in the system	          
    native string sParam_user_id							// ID of the selected user. This ID was passed from adminListUser 
    native string sOldRole
    
    native string sUserId       = Session.getUserId()             // ID of the current user  
    native string sMaskedEmail  = NotifUtil.getMaskedEmail(fEmailAddress.pInput)
    
    field fUserNameDisplay [								// Input field for User Name display. 	
        string(label) sLabel = "{User name:}"    
		input (control) pInput      	
    ]
    
    field fPasswordDisplay [								// Input field for password display. 	
        string(label) sLabel = "{Password:}"    
		input (control) pInput      	
    ]

	field fEmailDisplay [								// Input field for password display. 	
        string(label) sLabel = "{E-mail address:}"    
		input (control) pInput      	
    ]
    
    field fUserName [										// Input field for User Name. 	
        string(label) sLabel = "{User name:}"    
		input (control) pInput      	
    ]
	      
 	field fFirstName [										// Input field for First Name	  
        string(label) sLabel = "{First name:}"        
        input(control) pInput    
    ]
         
	field fLastName [										// Input field for Last Name 
	    string(label) sLabel = "{Last name:}"        
	    input(control) pInput 	         
	]
    
    field fPhoneNumber [
        string(label) sLabel = "{Phone number:}"        
        input(control) pInput         
    ]
       
   	field fMobileNumber [
        string(label) sLabel = "{Mobile number:}"        
        input(control) pInput 
    ]
    
	field fEmailAddress [									// Input field for email
	    string(label) sLabel = "{E-mail address:}"
	    input(control) pInput
	]	

	field fRole [											// Input field for role	                
	    string(label) sLabel = "{Role :}"              
	    auto dropDown(control) dRoleLists = Role_Admin_OrganizationAdmin [	        	
	      	ViewUser.populateRole(sUserRole, sOldRole)
	    ]        
	]		    
/*	 	
	field fBusinessUnit [									// Assigned business unit , only available to sys admin	                
	    string(label) sLabel = "{* Business Unit :}"               
	    auto dropDown(control) dBusinessUnitLists [
	         selectUnit1: "{Business Unit 1}"
	         selectUnit2: "{Business Unit 2}"
	         selectUnit3: "{Business Unit 3}"
	    ]        
	]	
*/		   
	field fDescription [									// Input field for Description
	    string(label) sLabel = "{Description:}"        
		input (control) pInput	        
	]    

    field fUserStatus [										// Input field for status - read only
        string(label) sLabel = "{Status :}" 
		input (control) pInput 
    ]
    
	string sConfirmDeleteQuestion = I18n.translate("adminViewUserDetails_sDeleteUserName", fUserName.pInput)	
	string sDeleteUserName = "{Are you sure you want to delete <1>?}"

	string sConfirmActivateQuestion = I18n.translate("adminViewUserDetails_sActivateUserName", fUserName.pInput)	
	string sActivateUserName = "{Are you sure you want to activate <1>?}"

	string sConfirmDeactivateQuestion = I18n.translate("adminViewUserDetails_sDeactivateUserName", fUserName.pInput)	
	string sDeactivateUserName = "{Are you sure you want to deactivate <1>?}"
	
    // -- message strings for display when use case completes. 	
  	structure(message) msgDeleteSuccess	[
        string(title) sTitle = "{Delete user}"
        string(body) sBody = "{Delete user successfully.}"
    ]

  	structure(message) msgDeleteFailure [
        string(title) sTitle = "{Delete user}"
        string(body) sBody = "{Failed to delete a user. Please contact your system administrator.}"
    ]

   	structure(message) msgActivateUser [
        string(title) sTitle = "{Activate user}"
        string (body) sBody = "{The user account has been activated. Now, the user may log into the system.}"
    ]

   	structure(message) msgDeactivateUser [
        string(title) sTitle = "{Deactivate user}"
        string(body) sBody = "{The user account has been deactivated. The user may no longer log into the system.}"
    ]            

    /**************************************************************************
     * Main Path.
     **************************************************************************/   
    
    /*************************************************************
     *  1. System loads the user's current role and user profile
     */   
 	action loadUserDetails  [	
        loadProfile (
 			user_Role    : sUserRole
 		)
 		
 		loadProfile (
 			userId       : sParam_user_id
 			userName     : fUserName.pInput
 			firstName    : fFirstName.pInput
 			lastName     : fLastName.pInput
 			phoneNumber  : fPhoneNumber.pInput
 			mobileNumber : fMobileNumber.pInput
 			emailAddress : fEmailAddress.pInput
 			user_Role    : sOldRole 			
// 			businessUnit : fBusinessUnit.dBusinessUnitLists
 			description  : fDescription.pInput
 			active		 : sActiveFlag			
 		)
 		
 		fRole.dRoleLists        = sOldRole
 		fUserNameDisplay.pInput = fUserName.pInput
 		fPasswordDisplay.pInput = sMaskedPassword
 		fEmailDisplay.pInput    = sMaskedEmail
 		if sActiveFlag == "true" then actionSetActive else actionSetInactive
 		
 	]   

    /***************************************
	 *  1.A  Action to set status to active
	 */
 	action actionSetActive [
	 	fUserStatus.pInput = "active"
	 	goto (adminViewUserDetails)
	]

    /****************************************
	 *  1.B Action to set status to inactive 
	 */
	action actionSetInactive [
	 	fUserStatus.pInput = "Inactive"
	 	goto (adminViewUserDetails)
	] 
	 
    /******************************************************************
     *  2. System Displays user detail screen which contains 2 sections: 
     * 		* User account: form_account
     *      * User profile: form_profile
     */          
    xsltScreen adminViewUserDetails("{View User Details}") [
    
	    form main_account [									// User account
	        class: "st-view-user-details"
	        
            div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
                
	                display sPageName
				]
            ]
	
	        div subHeading1 [
				class: "row st-border-bottom"
				
				div subHeading1Col1 [
					class: "col-md-8"
					
					h3 subHeading1Col [
			            display sTitleAccount
					]
				]
				
				div subHeading1Col2 [
					class: "col-md-2 col-sm-6 col-12"
					
		            navigation viewAudit(viewAuditLogs, "{View Audit Logs}") [
		                class: "float-end st-padding-top20"
		                attr_tabindex: "1"
		                logic: [
		                	if sAppNameSpace == "saas.user.namespace" then "remove"
		                ]
		            ]
				]
				
				div subHeading1Col3 [
					class: "col-md-2 col-sm-6 col-12"
					
		            navigation backToUserList(userListAction, "{Back}") [
		                class: "float-end st-padding-top20"
		                attr_tabindex: "2"
		            ]
				]
	        ]
	
	        div content [
	        	class : "row "
	        	
    	        div contentCol1 [       
            		class: "col-md-4 col-sm-6"
            		
            		div userWell [
            			class: "well well-sm"	
            		
			            div userHeading [
							class: "row"
							
							div userHeadingCol [
								class: "col-md-12"
			                
				                display sUserHeading [
				                    type: "h4"
				                ]
							]
			            ]
	            		
		            	div userRow1 [
			            	class: "row"
			            	
			            	div userCol1 [
			            		class: "col-md-12"
			            		
				        		display fUserNameDisplay [							
									control_attr_tabindex: "-1"
								]
			        		]
		        		]
		        		
		            	div userRow2 [
			            	class: "row"
			            	
			            	div userCol2 [
			            		class: "col-md-12"
			            		
				                navigation editUserName(editUserName, "{Edit user name}") [
				                    class: "btn btn-primary btn-sm"
				                    type : "popin"
				                    popin_controller: "ChangeAuthCtl"
				                    popin_size: "md"
				                    attr_tabindex: "3"				                    
				                ]
			        		]
			        	]
			        	
			        	div passRow3 [
			            	class: "row"
			            	
			            	div passCol3 [
			            		class: "col-md-12"
			            		
				        		display fPasswordDisplay [							
									control_attr_tabindex: "-1"
								]
			        		]
			        	]
			        	
			        	div passRow4 [
			            	class: "row"
			            	
			            	div passCol4 [
			            		class: "col-md-12"
			            		
				        		navigation editPassword(editPassword, "{Edit password}") [
				                   	class: "btn btn-primary btn-sm"
				                   	type : "popin"
				                   	popin_controller: "ChangeAuthCtl"
				                   	popin_size: "md"
				                   	attr_tabindex: "4"
				                ]
			        		]
			        	]				        		
	        		]
	        	]
	        	
                div contentCol2 [       
            		class: "col-md-4 col-sm-6"
            		
            		div profileWell [
            			class: "well well-sm"	
            		
			            div profileHeading [
							class: "row"
							
							div profileHeadingCol [
								class: "col-md-12"
			                
				                display sProfileHeading [
				                    type: "h4"
				                ]
							]
			            ]
	            		
		            	div profileRow1 [
			            	class: "row"
			            	
			            	div profileCol1 [
			            		class: "col-md-12"
			            		
				        		display fFirstName [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
		        		]
		        		
		            	div profileRow2 [
			            	class: "row"
			            	
			            	div profileCol2 [
			            		class: "col-md-12"
			            		
				                display fLastName [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div profileRow3 [
			            	class: "row"
			            	
			            	div profileCol3 [
			            		class: "col-md-12"
			            		
				                display fPhoneNumber [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div profileRow4 [
			            	class: "row"
			            	
			            	div profileCol4 [
			            		class: "col-md-12"
			            		
				                display fMobileNumber [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div profileRow5 [
			            	class: "row"
			            	
			            	div profileCol5 [
			            		class: "col-md-12"
			            		
				        		display fEmailDisplay [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div profileRow6 [
			            	class: "row"
			            	
			            	div profileCol6 [
			            		class: "col-md-12"
			            		
				        		navigation editProfile(editUserProfile, "{Edit user profile}") [
				                    class: "btn btn-primary btn-sm"
				                    type : "popin"
				                    popin_size: "lg"
				                    attr_tabindex: "5"
				                ]
			        		]
			        	]				        		
	        		]
	        	]
	                      
                div contentCol3 [       
            		class: "col-md-4 col-sm-6"
            		
            		div accessWell [
            			class: "well well-sm"	
            		
			            div accessHeading [
							class: "row"
							
							div accessHeadingCol [
								class: "col-md-12"
			                
				                display sAccessHeading [
				                    type: "h4"
				                ]
							]
			            ]
	            		
		            	div accessRow1 [
			            	class: "row"
			            	
			            	div accessCol1 [
			            		class: "col-md-12"
			            		
				        		display fRole [
			            			control_attr_tabindex: "-1"
			            			logic: [
			            				if "true" == "true" then "dRoleLists_disable"
			            			]		            			
			            		]
			        		]
		        		]
		        		
		            	div accessRow2 [
			            	class: "row"
			            	
			            	div accessCol2 [
			            		class: "col-md-12"
				                
				                display fDescription [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div accessRow3 [
			            	class: "row"
			            	
			            	div accessCol3 [
			            		class: "col-md-12"
			            		
				        		display fUserStatus [
			            			control_attr_tabindex: "-1"
			            		]
			        		]
			        	]
			        	
			        	div accessRow4 [
			            	class: "row"
			            	
			            	div accessCol4 [
			            		class: "col-md-12"
			            		
				        		logic: [
									if sActiveFlag == "false" then "remove"
									if sUserId == sParam_user_id then "remove"
								]
				                navigation deactivateUserAction(deactivateConfirmation, "{Deactivate}") [
									class: "btn btn-primary btn-sm" 
									type: "popin"
									popin_size: "md"
									attr_tabindex: "6"
					            ]
			        		]
			        	]
			        	
			        	div accessRow5 [
			            	class: "row"
			            	
			            	div accessCol5 [
			            		class: "col-md-6"
			            		
				        		logic: [
									if sActiveFlag != "false" then "remove"
									if sUserId == sParam_user_id then "remove"
								]
				                navigation deleteUserAction(deleteConfirmation, "{Delete user}") [
				                    class: "btn btn-danger btn-sm"
									type: "popin"
									popin_size: "md"
									attr_tabindex: "7"
				                ]
			        		]
			        		
			        		div accessCol6 [
			            		class: "col-md-6"
			            		
				        		logic: [
									if sActiveFlag != "false" then "remove"
									if sUserId == sParam_user_id then "remove"
								]
				                navigation activateUserAction(activateConfirmation, "{Activate user}") [
									class: "btn btn-primary btn-sm"
									type: "popin"
									popin_size: "md"
									attr_tabindex: "8"
					            ]
			        		]
			        	]			        				        				        					        		
	        		]
	        	]	        	            
	        ]
	    ]
    ]
    
    /*********************************************************************
     * 2A. User selects Edit user name button to edit user name.   
     */  
	action editUserName [
		gotoUc (adminEditUserName) [
			sUsername_param_userid : sParam_user_id
		]
	]	

    /*********************************************************************
     * 2B. User selects Edit password button to edit user password.   
     */
	action editPassword [
		gotoUc (adminEditPassword)  [
			sPassword_param_userid : sParam_user_id
		]
	]
	
    /*********************************************************************
     * 2C. User selects Edit user profile button to edit user profile.   
     */
	action editUserProfile [
		gotoUc (adminEditUserProfile) [
			sProfile_param_userid : sParam_user_id
		]
	]	  

    /*********************************************************************
     * 2D. User selects Deactivate user button to deactivate a user.  
     *    System display deactivate confirmation popin screen.
     */
    xsltFragment deactivateConfirmation("{Deactivate confirmation}") [
    	
    	form content [
			class: "modal-content"
			
		    div heading [ 
				class: "modal-header"
				
	            div headingRow [
	            	class: "row"
	            	
	            	h4 headingCol [
	            		class: "col-md-12"
	            		
						display sTitleConfirmDeactivate 
					]
				]
	        ]
	        
	        div content [
	            class: "modal-body"
	            
            	div row1 [
                    class: "row"
                    
                    div col1 [
                    	class: "col-md-12"
	        			display sConfirmDeactivateQuestion
	        		]
	        	] 
	        ]                       
	        
	        div buttons [
	            class: "modal-footer"
	            
	            navigation deactivateAnswerYes(deactivateUserAction, "{Yes}") [
	                class: "btn btn-primary"
	                attr_tabindex: "22"
	            ]
	            navigation deactivateAnswerNo(adminViewUserDetails, "{No}") [
	                type: "cancel"
	                class: "btn btn-secondary"
	                attr_tabindex: "23"
	            ]
	        ]            
	    ]
	]

	/*********************************************************************
     * 2D.A. System performs deactivate a user
     */
 	action deactivateUserAction [
 		auditLog(audit_admin.viewUserDetails_deactivated) [
 			secondary: sParam_user_id
 			fUserNameDisplay.pInput
 		]
 		
 		updateProfile(
 			userId: sParam_user_id
 			active: "false"
 			accountStatus: "disabledByAdmin"
 		)
 		
 		sActiveFlag = "false"
 
 		displayMessage(type: "info" msg: msgDeactivateUser)	
 			
		// The active flag has been inactive, need to redisplay the form to reflect the correct status flag
		goto (loadUserDetails)		
 	] 	 
 
	/*********************************************************************
     * 2E. User selects Delete User button to delete a user.  
     *    System display delete confirmation popin screen.
     */
    xsltFragment deleteConfirmation("{Delete confirmation}") [
    		    
	    form contentForm  [
	    	class: "modal-content"
	    
		    div heading [ 
				class: "modal-header"
				
	            div headingRow [
	            	class: "row"
	            	
	            	h4 headingCol [
	            		class: "col-md-12"
	            		
						display sTitleConfirmDelete 
					]
				]
	        ]
        
	        div content [
	            class: "modal-body"
	            
            	div row1 [
                    class: "row"
                    
                    div col1 [
                    	class: "col-md-12"
	        			display sConfirmDeleteQuestion
	        		]
	        	]
	        ]
	        
	        div buttons [
	            class: "modal-footer"

	            navigation deleteAnswerYes(deleteUserAction, "{Yes}") [
	                class: "btn btn-danger"
	                attr_tabindex: "24"
	            ]
	            navigation deleteAnswerNo(adminViewUserDetails, "{No}") [
	                type: "cancel"
	                class: "btn btn-primary"
	                attr_tabindex: "25"
	            ]
	        ]            
	    ]
    ]
    
   /************************************
    * 2E.A. System performs delete a user. 
    */    
	action deleteUserAction [
 		deleteUser(
    		userId: 	sParam_user_id
    		namespace: sAppNameSpace
    	)
    
    	if success then deleteSuccess
    	if failure then deleteFailure
 	]

    /***********************************************
     * 2E.B. Delete user successful, Display a message
     */
    action deleteSuccess [
    	auditLog(audit_admin.viewUserDetails_deleted_success) [
    		secondary: sParam_user_id
    		fUserNameDisplay.pInput
    	]
    	
        displayMessage(type: "warning" msg: msgDeleteSuccess)
        
        gotoUc (adminListUsers)                
    ]  
  	
     /*************************************************
     * 2E.AA. Display error message when failed deleting a user
     */ 	
    action deleteFailure [
    	auditLog(audit_admin.viewUserDetails_deleted_failure) [
    		secondary: sParam_user_id
    		fUserNameDisplay.pInput
    	]
    	
        displayMessage(type: "warning" msg: msgDeleteFailure)
        
        gotoUc (adminListUsers)                
    ]  
        
   	/*********************************************************************
     * 2F. User selects Activate user button to activate a user.  
     *    System display activate confirmation popin screen.
     */
    xsltFragment activateConfirmation("{Activate confirmation}") [
    	
    	form content [
    		class: "modal-content"
    	
	        div heading [ 
				class: "modal-header"
				
	            div headingRow [
	            	class: "row"
	            	
	            	h4 headingCol [
	            		class: "col-md-12"
	            		
						display sTitleConfirmActivate 
					]
				]
	        ]
        
            div body [
                class: "modal-body"
                
            	div row1 [
                    class: "row"
                    
                    div col1 [
                    	class: "col-md-12"
            			display sConfirmActivateQuestion
            		]
            	] 
            ]
            
            div buttons  [
                class: "modal-footer"

                navigation answerYes(activateUserAction, "{Yes}") [
                    class: "btn btn-primary"
                    attr_tabindex: "10"
                ]
                navigation answerNo(adminViewUserDetails, "{No}") [
                    type: "cancel"
                    class: "btn btn-secondary"
                    attr_tabindex: "11"
                ]
            ]
        ]
    ]
 		     
	/*********************************************************************
     * 2F.A. System performs activate a user
     */
 	action activateUserAction [
 		auditLog(audit_admin.viewUserDetails_activated)[
 			secondary: sParam_user_id
 			fUserNameDisplay.pInput
 		]
 		
 		updateProfile(
 			userId: sParam_user_id
 			active: "true"
 			accountStatus: "open"
 		)
 		sActiveFlag = "true"
 
 		displayMessage(type: "info" msg: msgActivateUser)	
 			
		// The active flag has been active, need to redisplay the form to reflect the correct status flag	
		goto (loadUserDetails)
 		
 	]
 	
    /*********************************************************************
     * 2G. User wants to view the audit logs.
     */
    action viewAuditLogs [
        gotoUc(auditView) [
            sArgUserId: sParam_user_id
    ]
       
    ]
    
    /*********************************************************************
     * 2H. User wants to return back to user list.
     */
    action userListAction [
    	gotoUc (adminListUsers)                
    ]
 	
]