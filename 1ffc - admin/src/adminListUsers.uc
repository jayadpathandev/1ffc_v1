useCase adminListUsers [ 
	
   /******************************************************************************************************************************
     *  File:                   adminListUsers.uc
     *  Written by:             Yvette Nguyen
     *  Creation date:          2015-September-14
     *  
     *  Description:
     *      A user with system or business unit administration privileges wants to view a list of users that they can manage. 
     * 		     
	 *  Alternative Outcomes:
	 *      1. User will go to new user enroll screen when there is no user created in the system
	 *      2. User will click on the user name link to go to view user details
	 * 		3. User will enter search criteria and click on the search button
	 *
     * 
	 *  Child use cases are:
	 *          None
	 * 
     *  Minimal success guarantees:
     *      The screen displays a list of user who enrolled in the application
     * 
	 *  Major Versions:
	 *  	2015-September-14 -- first version.     
	 * 
     **********************************************************************************************/	
     

    documentation [
        preConditions: [[
            1. A System Administrator or Business Unit Administrator have logged into the system.
        ]]
        triggers: [[
            1. There are some users who already enrolled in the system
        ]]
        postConditions: [[
            1. Primary -- View list of users.
            2. There is no user enrolled in the system
            3. User click on the user name link
        ]]
    ]
    startAt initAction
   
   // Only SysAdmin and Business Admin are able to see Manage users tab 
	actors [
        create_user, 
        create_admin,
        create_org_admin
    ]    

    /**************************
     * Strings used by the JAVA
     **************************/            
    static Status_Active = "{Active}"
    static Status_Inactive = "{Inactive}"

	/**************************
	 * DATA ITEMS SECTION
	 **************************/	        

//	importJava User(com.sorrisotech.uc.aaa.User)
	importJava User1FFC(com.sorrisotech.uc.admin1ffc.createadmin.User1FFC)
    importJava AuthUtil(com.sorrisotech.app.common.utils.AuthUtil)
    
    string sAppNameSpace = AuthUtil.getAppNameSpace()   
    string sPageName = "{Users}"    
    string sUserRole
    string sSelectedUser    
    string sSelectedId = User1FFC.getId(sSelectedUser)
    native string sDocumentController = "ROLE_ADMIN_DOCUMENT_CONTROLLER"
   
    field fUserAttribute [                 
        string(label) sLabel = "{User attribute}"
        input (control) pInput ("(^[\\p{L}\\d _\\.\\-\\,]{3,}\\*?$)|(^[\\p{L}\\d _\\.\\-\\,]+$)", fUserAttribute.sValidation)
        string(validation) sValidation = "{User attribute may contain letters, numbers, spaces, hyphens (-), underscores (_), commas (,), and a wildcard *.  At least three characters are needed before the wildcard, such as 'abc*'.}"
        string(required) sRequired = "{This field is required.}"  
        string(help) sHelp = "{Please enter a company name or first name or last name or email. Search criteria may consist of one or more characters and followed by an asterisk (*) and click Search button.}"
    ]
    
    // -- message strings for display when use case completes. 	
	structure(message) msgNoUser [
        string(title) sTitle = "{Enroll new user}"
        string(body) sBody = "{There is no user in the system, please enroll a new user.}"
		        
    ]
    
    structure(message) oMsgInvalidInput [
        string(title) sTitle = "{Invalid input}"
        string(body) sBody = "{Please provide an input matching any of the column in list: Username, Name, E-mail.}"
    ]
    
    structure(message) msgTooManyUsersInList [
        string(title) sTitle = "{Too many users}"
        string(body) sBody = "{There are over 50 users, please use search to list desired users.}"
		        
    ]
    
    structure(message) msgTooManyUsersInSearchResult [
        string(title) sTitle = "{Too many results}"
        string(body) sBody = "{There are over 50 results, please update the user attribute and search again for desired results.}"
		        
    ]
    
    table userProfile = User1FFC.listUsers() [
    
        "row" => string sRow
        "username" => string sName
        "businessunit" => string sBusinessUnit
      	// "namespace" => string sNameSpace

		"" => string sSysAdmin = "{System Administrator}"
		"" => string sBusinessAdmin = "{Business Unit Administrator}"
		"" => string sBusinessUser = "{Business Unit User}"
	
		"role" => string sRole           			     
        "status" => string sStatus
        "email"     => string sEmail
        "description" =>string sDescription
        "" => string sIcon
        "fullname" => string sFullName
        
         // Define a link for user name, will take user to view user details
         "username" => link usernameLink(viewUserAction) [
             sSelectedUser: sRow 
         ]
              
        ascending column nameColumn("{User name}")  [
        	
            elements: [ 
            	usernameLink,
            	sIcon:  [
            	   attr_class: "glyphicon glyphicon-user st-left-space"
            		] 
            ]
            sort    : [sStatus, sName]
        ]
        
 		/*  column spaceColumn("Name Space")
        	[
            	elements: [ sNameSpace ]
        	]
		      
		  
        column businessUnitColumn("Business Unit") [
            elements: [ sBusinessUnit ]
            sort    : [ sBusinessUnit ]
        ]
       
        */
		column fullNameColumn("{Name}") [
			elements: [ sFullName ]
			sort	  : [ sFullName ]
		]
        
        column roleColumn("Role") [
			elements: [ sRole ]
			sort	  : [ sRole ]
        ]

        column statusColumn("Status") [
            elements: [ sStatus ]
            sort	: [ sStatus ]
            tags: [ "d-none" ]
        ]

        /*column emailColumn("E-mail") [
            elements: [sEmail ]
            sort	: [sEmail ]
            tags: [ "d-none" ]
        ]*/
         
        column descriptionColumn("Description") [
            elements: [sDescription:[attr_class:"ten_chars"] ]
            tags: [ "d-none", "d-sm-none" ]
        ]
    ]
    
    /** I18N values used by the JAVA codee */
    static Role_Admin_SystemAdmin = "{System Administrator}"
    static Role_Admin_Company = "{Company Administrator}"
    static Role_Admin_OrganizationAdmin = "{Agent Administrator}"
    static Role_Admin_Agent = "{Agent}"
    static Role_Admin_DocumentController = "{Agent Document Controller}"
    static Role_Biz_OrganizationAdmin = "{Organization Administrator}"
    static Role_Biz_Admin = "{Manager}"
    static Role_Biz_EndUser = "{End User}"
    
    /**************************************************************************
     * Main Path.
     **************************************************************************/   
    
    /*************************************************************
     *  1. System loads the user's current role and user details
     */   
    action initAction [
//    	User1FFC.addRole(sDocumentController, "1")
    	
		loadProfile (
 			user_Role    : sUserRole
 		) 		
 		switch User1FFC.init (sUserRole) [
 			case "no_user"         actionGotoCreateUser
 			case "user"            showUsers
 			case "tooManyResults"  tooManyUsersInListMsg
 		]
 	]    	
 	
    /****************************************************************************
     *  2. System Displays a table of all users that this user can view or edit. 
     */          	    
    xsltScreen showUsers("{Users}") [    
        
        form main [
            class: "st-list-users"

            //-------------------------------------------------------------------------------------
            // Heading            
            div header [
				class: "row"
				
				h1 headerColH1 [
					class: "row col-12"
					
	                div headerCol1 [
						class: "col-md-10"
	                
	                	display sPageName
					]
					
	                div headerCol2 [
						class: "col-md-2"
	                
                    	navigation enrollUsers(actionEnroll, "{Enroll}") [
	                        class: "btn btn-primary float-end"
	                        type: "popin"
							popin_size: "xl"
							attr_tabindex: "10"
						]
					]
				]
            ]
            
            //-------------------------------------------------------------------------------------
            // Content
            div content [
            	
            	div row1 [    
                	class: "row st-field-row"
                    logic: [ if "saas.csr.namespace" == sAppNameSpace then "remove"]
                    
					div searchcol1 [  
						class: "col-md-3"
		                display fUserAttribute [
		                	control_attr_tabindex: "11"
						    control_attr_autofocus: ""
						]
					]
                    
					div searchcol2 [  
						class: "col-md-3"
		                navigation search(verifyInputData, "{SEARCH}") [
		                    class: "btn btn-primary st-search-button"
		                    data: [fUserAttribute]
		                    attr_tabindex: "12"
		                ]
					]
				]
				
                div row2 [    
                	class: "row"
                    
					div col1 [  
						class: "col-md-12"
		                display userProfile [
		                	class: "st-user-table"
		                	table_nav_before: "false"
		                	table_nav_after: "true"
                            page_size: "10"
                            control_attr_tabindex: "13"
		                ]
					]
				]
            ]
        ]
    ]

	
    /**************************************************************************
     * Alternative Paths
     **************************************************************************/    

 	/*********************************************************************
     * 1A. User logs in the system. When there is no user created, system
     *     will redirect user to the enroll screen.
     */     
    action actionGotoCreateUser [	
 		displayMessage(type: "info" msg: msgNoUser)
 		
 		gotoUc (adminCreateUser)
 	]
 	
 	/* 1B. System displays the too many users message on the screen. */ 
    action tooManyUsersInListMsg [
        displayMessage(type: "info" msg: msgTooManyUsersInList)         
        goto(showUsers)
    ]
 	     
  	/*********************************************************************
     * 2A. User selects a user name link, system passes that user id to
     *     adminViewUserDetails use case.  
     */
    action viewUserAction  [  	
 		gotoUc(adminViewUserDetails) [ sParam_user_id : sSelectedId ]
 	]   

    /***********************************************************************************
     * 2B. User clicks on enroll button, system will redirect user to the enroll screen
     */
	action actionEnroll [
		gotoUc (adminCreateUser)
	]
	
	 /*=============================================================================================
     * 3A. System verifies the input data for user search.
     */ 
	action verifyInputData [
	   switch User1FFC.verifyInputData(fUserAttribute.pInput) [
            case "error" invalidInputMsg 
            default performSearch           
        ]	          
	]
	
	/*=============================================================================================
     * 3B. System performs the user search.
     */ 
	action performSearch [		
		User1FFC.setUserAppSearchAttribute(fUserAttribute.pInput)
		
		switch User1FFC.performUserAppSearch (sUserRole) [
 			case "no_user"         showUsers
 			case "user"            showUsers
 			case "tooManyResults"  tooManyUsersInSearchResultMsg
 		] 		
	]
    
    /* 3C. System displays the invalid input message on the screen. */ 
    action invalidInputMsg [
        displayMessage(type: "danger" msg: oMsgInvalidInput)         
        goto(showUsers)
    ] 
    
    /* 3D. System displays too many results message on the screen. */ 
    action tooManyUsersInSearchResultMsg [
        displayMessage(type: "info" msg: msgTooManyUsersInSearchResult)         
        goto(showUsers)
    ]
]