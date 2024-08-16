useCase auditView [
   /***********************************************************************************************
    * author:  Joshua Gentry
    * created: 31-Mar-2016
    *
    * Primary Goal:
    *       Display audit messages for a particular user.
    *  
    * Alternative Outcomes:
    *       1. The user can view audit messages for themselves.
    *
    * Major Versions:
    *       1.0      31-Mar-2016  First Version Coded
    */ 
    

    documentation [
        preConditions: [[
            1. The user must be authenticated with the system, or
            2. A user must be selected to view the audit logs for.
        ]]
        triggers: [[
            1. A user selects this use case from the menu.
            2. A user selects to view the audit events for a user.
        ]]
        postConditions: [[
            1. This use case makes no state changes.
        ]]
    ]
    startAt actionChooseTargetUser [
        sArgUserId
    ] 

	// Only business admin and business users are able to see audit tab
	actors[
		view_audit
	]
    
    child manual auditServiceView(auditServiceView)
    
    /**************************
     * Strings used by the JAVA
     **************************/            
    static audit_event_not_found = "{** Internal Error - Audit event could not be found. **}"
    
    /**********************************************************************************************
     * DATA ITEMS SECTION
     *********************************************************************************************/
    importJava Session(com.sorrisotech.app.utils.Session)
    importJava Audit(com.sorrisotech.uc.^audit.Audit)
        
    //---------------------------------------------------------------------------------------------
    // The arguments that can be passed into the user case.
    string sArgUserId = ""
    
    //---------------------------------------------------------------------------------------------
    // Random information we might need
    string sUserId    = Session.getUserId()
    string sUserName  = ""
    string sUserRole  = ""
    string sFirstName = ""
    string sLastName  = ""
    
    //---------------------------------------------------------------------------------------------
    // Static elements displayed on the screen.
    string sPageName = "{Audit for}"
    
    native string sServiceFlag = "false"
    
    //---------------------------------------------------------------------------------------------
    // The audit filters that can be applied.
    field fAuditStart [
        string(label) sLabel = "{Start On:}"
        date(control) aControl("yyyy-MM-dd") = Audit.getFrom()
    ]

    field fAuditEnd [
        string(label) sLabel = "{Until:}"
        date(control) aControl("yyyy-MM-dd") = Audit.getTo()
    ]
    
    field fAuditActionMe [
        radioSet(control) rControl = Audit.getActions() [
            all    : "{Actions involving my account}"
            user   : "{Actions I've done to my account}"
            byUser : "{Actions I've done to other accounts}"
            toUser : "{Actions other's have done to my account}"
        ]       
    ]
    
    field fAuditActionUser [
        radioSet(control) rControl = Audit.getActions() [
            all    : "{Actions involving their account}"
            user   : "{Actions they've done to their account}"
            toUser : "{Actions others have done to their account}"
        ]       
    ]    

    field fAuditActionAdmin [
        radioSet(control) rControl = Audit.getActions() [
            all    : "{Actions involving their account}"
            user   : "{Actions they've done to their account}"
            byUser : "{Actions they've done to other accounts}"
            toUser : "{Actions other's have done to their account}"
        ]       
    ]    
    
    //---------------------------------------------------------------------------------------------
    // The table with the audit events.
    table tEvents = Audit.getTable(sArgUserId, sUserName) [
        "time"      => string sTime
        "time_raw"  => number sRawTime
        "source"    => string sSource
        "primary"   => string sPrimary
        "secondary" => string sSecondary
        "message"   => string sMessage
        "to_class"  => string sToClass
        "for_class" => string sForClass
        
        "" => string sTo = "[span sorriso=\"icon-chevron-left\"][/span]"
        "" => string sFor = "[span sorriso=\"icon-chevron-right\"][/span]"
         

        descending column colTime("Time") [
            elements: [ sTime ]
            sort: [ sRawTime ]
        ]
        
        column colSource("Source") [
            elements: [ sSource ]
            tags: [ "d-none", "d-sm-none" ]
        ]
        
        column colWho("Account") [
            elements: [
                sPrimary 
                sSecondary
                sTo: [
                    ^class: sToClass
                    prepend_space: "true"
                ]
                sFor: [
                    ^class: sForClass
                    prepend_space: "true"
                ]
            ]
            tags: [ "d-none", "d-sm-none" ]
        ]
                                
        column colEvent("Audit Event") [
            elements: [ sMessage ]
        ]
    ]
    
    /**********************************************************************************************
     * Main Success Scenario
     *********************************************************************************************/

    /*=============================================================================================
     * 1. System checks if a user ID was provided.
     *===========================================================================================*/
     action actionChooseTargetUser [
     	stopUc(childId: auditServiceView)
     	
        if sArgUserId == "" then
            actionChooseCurrentUser
        else
            actionAuditAccess            
    ] 
    
    /*=============================================================================================
     * 2. If the use case was not started with a user ID, then the current user's ID is used.
     *===========================================================================================*/
    action actionChooseCurrentUser [
        sArgUserId = sUserId
        auditLog(audit_view.view_myself)
        goto(actionLoadProfileInfo)
    ]

    /*=============================================================================================
     * 2.5. The system logs the viewing of the other audit log.
     *===========================================================================================*/
    action actionAuditAccess [
        auditLog(audit_view.view_other) [
            secondary: sArgUserId
        ]
        
        goto(actionLoadProfileInfo)
    ]

    /*=============================================================================================
     * 3. The system loads information about the user.
     *===========================================================================================*/
    action actionLoadProfileInfo [
        loadProfile(
            userId    : sArgUserId
            ^username : sUserName
            user_role : sUserRole
            firstName : sFirstName
            lastName  : sLastName
        )
        
        goto(screenShowAudits)
    ]
    
    /*=============================================================================================
     * 4. User chooses to filter the audit events in the table.
     *===========================================================================================*/
    xsltScreen screenShowAudits("Audit") [
 
        div main [
			
			logic: [
                if sServiceFlag == "true" then "remove"
            ]
           
            div header [
            	class: "row"
                     
                h1 headerCol1 [
                	class: "col-md-9"
                	
                    display sPageName
                    display sFirstName [
                        prepend_space: "true"
                    ]
                    display sLastName [
                        prepend_space: "true"
                    ]
                ]
                
 /*                div headerCol2 [
                	class: "col-md-3"
                    navigation serviceApi(auditService, "{SERVICE API AUDIT LOGS}") [
	                    class: "btn btn-primary float-end"
	                    attr_tabindex: "6"                            
	                ]  
                ]
*/
            ]
                            
            // Display login profile info
            div content [
                
                form search [
                   class: "row"
                    
                    div left [
                        class: "col-md-6"
                        
                        display fAuditStart [
                        	class: "row"
                        	label_class: "col-2"
                			field_class: "col-8"
                        	control_attr_tabindex: "1"       
                        	control_attr_datepicker-options: "{showWeeks: false}"                 	
                        ]
                        display fAuditEnd [
                       		class: "row"
                        	label_class: "col-2"
                			field_class: "col-8"                        	
                        	control_attr_tabindex: "2"
                        	control_attr_datepicker-options: "{showWeeks: false}"
                        ]
                    ]
                
                    div right [
                        class: "col-md-4"
                        
                        display fAuditActionMe [
                            logic: [
                                if sArgUserId != sUserId then "remove"
                            ]
                            control_attr_tabindex: "3"
                        ]
                        display fAuditActionUser [
                            logic: [
                                if sArgUserId == sUserId then "remove"
                                if sUserRole == "Role_Admin_SystemAdmin" then "remove"
                            ]
                            control_attr_tabindex: "4"
                        ]
                        display fAuditActionAdmin [
                            logic: [
                                if sArgUserId == sUserId then "remove"
                                if sUserRole  != "Role_Admin_SystemAdmin" then "remove"
                            ]
                            control_attr_tabindex: "5"
                        ]
                    ]
                        
                    div far_right [
                        class: "col-md-2"
                                                
                        navigation filter(actionFilter, "{Filter}") [
                            class: "btn btn-primary"
                            data: [ fAuditStart, fAuditEnd, fAuditActionMe, fAuditActionUser, fAuditActionAdmin ]
                            attr_tabindex: "6"                            
                        ]                            
                    ] 
                ]
                
                div tableContent [    
                    display tEvents
                ]
            ]
                
        ]
        
        child auditServiceView
    ]

    /*=============================================================================================
     * 5. System updates the filter and redisplays screen.
     *===========================================================================================*/
    action actionFilter [
        Audit.setFrom(fAuditStart.aControl)
        Audit.setTo(fAuditEnd.aControl)
        Audit.setActions(fAuditActionMe.rControl)       
        Audit.setActions(fAuditActionUser.rControl)
        Audit.setActions(fAuditActionAdmin.rControl)
        
        goto(screenShowAudits)
    ]
    
    action auditService [
    	sServiceFlag = "true"
    	startUc(childId: auditServiceView)    
    	goto(screenShowAudits)
    ] 
    
]