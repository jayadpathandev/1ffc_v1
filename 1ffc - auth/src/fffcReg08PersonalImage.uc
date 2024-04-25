useCase fffcReg08PersonalImage [
 
   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 01-Oct-2015
    *
    *  Primary Goal:
    *       1. System displays Registration- Select Personal Image Screen (Step 5 of registration)
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
            1. Users enters correct information on the Registration - Setup secret question screen
            ; and selects next button
        ]]
        postConditions: [[
            1. Primary -- System displays Registration - Setup email address
            2. Alternative 1 -- Users selects back link, System take User to the Registration - Setup secret question
            3. Alternative 2 -- Users selects cancel button, System take users to the Login page
        ]]
    ]
	startAt initialize

	/**************************
	 * DATA ITEMS SECTION
	 **************************/	
	 
	importJava UcSecurityImage(com.sorrisotech.uc.securityimage.UcSecurityImage) 
    
    string sPageName = "{Registration - Choose Your Image (step 5 of 5)}"        
    string sMessage = "{Your personal image is used to help you recognize when a hacker has created a fake log-in and is \"phishing\" for your credentials. If you arrive at the password entry page and your personal image is not shown, DO NOT enter your password. Close your browser window and try to connect to the application again.}"                      
       
    persistent input groupId  = "1"       		
    persistent input memberId = "1"    
    persistent input imageId
    
    image securityImage11 = UcSecurityImage.loadSecurityImage("1","1") 
    image securityImage12 = UcSecurityImage.loadSecurityImage("1","2") 
    image securityImage13 = UcSecurityImage.loadSecurityImage("1","3") 
    image securityImage14 = UcSecurityImage.loadSecurityImage("1","4") 
    image securityImage15 = UcSecurityImage.loadSecurityImage("1","5") 
    image securityImage16 = UcSecurityImage.loadSecurityImage("1","6") 
    image securityImage17 = UcSecurityImage.loadSecurityImage("1","7") 
    image securityImage18 = UcSecurityImage.loadSecurityImage("1","8") 
    image securityImage19 = UcSecurityImage.loadSecurityImage("1","9")
    
    image securityImage21 = UcSecurityImage.loadSecurityImage("2","1") 
    image securityImage22 = UcSecurityImage.loadSecurityImage("2","2") 
    image securityImage23 = UcSecurityImage.loadSecurityImage("2","3") 
    image securityImage24 = UcSecurityImage.loadSecurityImage("2","4") 
    image securityImage25 = UcSecurityImage.loadSecurityImage("2","5") 
    image securityImage26 = UcSecurityImage.loadSecurityImage("2","6") 
    image securityImage27 = UcSecurityImage.loadSecurityImage("2","7") 
    image securityImage28 = UcSecurityImage.loadSecurityImage("2","8") 
    image securityImage29 = UcSecurityImage.loadSecurityImage("2","9")
    
    image securityImage31 = UcSecurityImage.loadSecurityImage("3","1") 
    image securityImage32 = UcSecurityImage.loadSecurityImage("3","2") 
    image securityImage33 = UcSecurityImage.loadSecurityImage("3","3") 
    image securityImage34 = UcSecurityImage.loadSecurityImage("3","4") 
    image securityImage35 = UcSecurityImage.loadSecurityImage("3","5") 
    image securityImage36 = UcSecurityImage.loadSecurityImage("3","6") 
    image securityImage37 = UcSecurityImage.loadSecurityImage("3","7") 
    image securityImage38 = UcSecurityImage.loadSecurityImage("3","8") 
    image securityImage39 = UcSecurityImage.loadSecurityImage("3","9")
    
    image securityImage41 = UcSecurityImage.loadSecurityImage("4","1") 
    image securityImage42 = UcSecurityImage.loadSecurityImage("4","2") 
    image securityImage43 = UcSecurityImage.loadSecurityImage("4","3") 
    image securityImage44 = UcSecurityImage.loadSecurityImage("4","4") 
    image securityImage45 = UcSecurityImage.loadSecurityImage("4","5") 
    image securityImage46 = UcSecurityImage.loadSecurityImage("4","6") 
    image securityImage47 = UcSecurityImage.loadSecurityImage("4","7") 
    image securityImage48 = UcSecurityImage.loadSecurityImage("4","8") 
    image securityImage49 = UcSecurityImage.loadSecurityImage("4","9")

    // -- message strings for display when use case completes.     
    structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later}"
	]

    /**************************************************************************
     * Main Path.
     **************************************************************************/ 
     
	/**************************************************************************
	 * 1. Initialize and loads all security images.
	 */
	action initialize [    
		 switch UcSecurityImage.init() [		 
		 	case "success" regPersonalImageScreen
		 	case "error" genericErrorMsg						
			default genericErrorMsg
		]
	] 
			   
    /**************************************************************************
    * 2. System displays the personal image screen.
    */        
    noMenu xsltScreen regPersonalImageScreen("{Registration - Your Personal Image (step 5 of 5)}") [
    	        
		form regPersonalImageForm [
	    	class: "st-login"
	            
	        div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
	            
	                display sPageName
	            ]
			]
	    
	    	div content [
	    		class: "personalimg"
				
				div row2 [
					class: "row"
					
					div col2 [
						class: "col-md-12"
						
						ul tabs [
		                	class: "nav nav-tabs"
		                	
		                	li animal_tab [ 
		                		class: "nav-item"
			                	navigation selectAnimals(selectAnimals, "{Animals}") [
			                		type: "button"
			                		class: "nav-link"
			                		attr_data-bs-toggle: "tab"
			                		attr_data-bs-target: "#fffcReg08PersonalImage_animal_panel"
			                		attr_role: "tab"
			                		attr_aria-selected: "true"
								    attr_tabindex: "1"
								]
		                	]
							
		                	li nature_tab [ 
		                		class: "nav-item"
								navigation selectNature(selectNature, "{Nature}") [
			                		type: "button"
			                		class: "nav-link"
			                		attr_data-bs-toggle: "tab"
			                		attr_data-bs-target: "#fffcReg08PersonalImage_nature_panel"
			                		attr_role: "tab"
			                		attr_aria-selected: "false"
								    attr_tabindex: "2"
								]
							]
							
		                	li object_tab [ 
		                		class: "nav-item"
								navigation selectObjects(selectObjects, "{Objects}") [
			                		type: "button"
			                		class: "nav-link"
			                		attr_data-bs-toggle: "tab"
			                		attr_data-bs-target: "#fffcReg08PersonalImage_object_panel"
			                		attr_role: "tab"
			                		attr_aria-selected: "false"
								    attr_tabindex: "3"
								]
							]
														
		                	li people_tab [ 
		                		class: "nav-item"
								navigation selectPeople(selectPeople, "{People}") [
			                		type: "button"
			                		class: "nav-link"
			                		attr_data-bs-toggle: "tab"
			                		attr_data-bs-target: "#fffcReg08PersonalImage_people_panel"
			                		attr_role: "tab"
			                		attr_aria-selected: "false"
								    attr_tabindex: "4"
								]
							]
		                ]
					]
				]
				
				div row3 [
					class: "row"
					
					div col3 [
						class: "col-md-4 tab-content"
		                
		                div animal_panel [
		                	class: "tab-pane thumbnails"
		                	 attr_role: "tabpanel"
		                	 attr_aria-labelledby: "regPersonalImage_animal_tab"
		                	 			                		                
							display securityImage11 [	
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "1"								
								attr_tabindex: "5"
								attr_autofocus: ""
							] 
							
							display securityImage12 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "2"								
								attr_tabindex: "6"
							] 
							
							display securityImage13 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "3"								
								attr_tabindex: "7"
							] 
							
							display securityImage14 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "4"								
								attr_tabindex: "8"
							] 
							
							display securityImage15 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "5"								
								attr_tabindex: "9"
							] 
							
							display securityImage16 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "6"								
								attr_tabindex: "10"
							]    
							
							display securityImage17 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "7"								
								attr_tabindex: "11"
							]  
							
							display securityImage18 [									
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "8"								
								attr_tabindex: "12"
							]  
							
							display securityImage19 [
								attr_st-personal-image-group: "1"
								attr_st-personal-image-member: "9"								
								attr_tabindex: "13"
							]   
						]	  
							
						div nature_panel [
		                	class: "tab-pane thumbnails"
		                	 attr_role: "tabpanel"
		                	 attr_aria-labelledby: "regPersonalImage_nature_tab"
		                	 							                		                
							display securityImage21 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "1"								
								attr_tabindex: "14"
								attr_autofocus: ""
							] 
							
							display securityImage22 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "2"								
								attr_tabindex: "15"
							] 
							
							display securityImage23 [							
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "3"								
								attr_tabindex: "16"
							] 
							
							display securityImage24 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "4"								
								attr_tabindex: "17"
							] 
							
							display securityImage25 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "5"								
								attr_tabindex: "18"
							] 
							
							display securityImage26 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "6"								
								attr_tabindex: "19"
							]    
							
							display securityImage27 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "7"								
								attr_tabindex: "20"
							]  
							
							display securityImage28 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "8"								
								attr_tabindex: "21"
							]  
							
							display securityImage29 [									
								attr_st-personal-image-group: "2"
								attr_st-personal-image-member: "9"								
								attr_tabindex: "22"
							]   
						]	  
							
						div object_panel [
		                	class: "tab-pane thumbnails"
		                	 attr_role: "tabpanel"
		                	 attr_aria-labelledby: "regPersonalImage_object_tab"
		                	 							                		                
							display securityImage31 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "1"								
								attr_tabindex: "23"
								attr_autofocus: ""
							] 
							
							display securityImage32 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "2"								
								attr_tabindex: "24"
							] 
							
							display securityImage33 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "3"								
								attr_tabindex: "25"
							] 
							
							display securityImage34 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "4"								
								attr_tabindex: "26"
							] 
							
							display securityImage35 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "5"								
								attr_tabindex: "27"
							] 
							
							display securityImage36 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "6"								
								attr_tabindex: "28"
							]    
							
							display securityImage37 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "7"								
								attr_tabindex: "29"
							]  
							
							display securityImage38 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "8"								
								attr_tabindex: "30"
							]  
							
							display securityImage39 [									
								attr_st-personal-image-group: "3"
								attr_st-personal-image-member: "9"								
								attr_tabindex: "31"
							]   
						]	  
							
						div people_panel [
		                	class: "tab-pane thumbnails"
		                	 attr_role: "tabpanel"
		                	 attr_aria-labelledby: "fffcReg08PersonalImage_people_tab"
		                	 							                		                
							display securityImage41 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "1"								
								attr_tabindex: "32"
								attr_autofocus: ""
							] 
							
							display securityImage42 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "2"								
								attr_tabindex: "33"
							] 
							
							display securityImage43 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "3"								
								attr_tabindex: "34"
							] 
							
							display securityImage44 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "4"								
								attr_tabindex: "35"
							] 
							
							display securityImage45 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "5"								
								attr_tabindex: "36"
							] 
							
							display securityImage46 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "6"								
								attr_tabindex: "37"
							]    
							
							display securityImage47 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "7"								
								attr_tabindex: "38"
							]  
							
							display securityImage48 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "8"								
								attr_tabindex: "39"
							]  
							
							display securityImage49 [									
								attr_st-personal-image-group: "4"
								attr_st-personal-image-member: "9"								
								attr_tabindex: "40"
							]   
						]	  
					]
					
					div col4 [
						class: "col-md-2"
							
						div imagePreview[
							class: "preview"
															
							display securityImage11 [		
				        		class: "visually-hidden"
								attr_st-personal-image-display: "11"							
							]
							
							display securityImage12 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "12"							
							]
							
							display securityImage13 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "13"							
							]
							
							display securityImage14 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "14"							
							]
							
							display securityImage15 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "15"							
							]
							
							display securityImage16[									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "16"							
							]
							
							display securityImage17 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "17"							
							]
							
							display securityImage18 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "18"							
							]
							
							display securityImage19 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "19"							
							] 
							
							display securityImage21 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "21"							
							]
							
							display securityImage22 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "22"							
							]
							
							display securityImage23 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "23"							
							]
							
							display securityImage24 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "24"							
							]
							
							display securityImage25 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "25"							
							]
							
							display securityImage26[									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "26"							
							]
							
							display securityImage27 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "27"							
							]
							
							display securityImage28 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "28"							
							]
							
							display securityImage29 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "29"							
							] 
							
							display securityImage31 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "31"							
							]
							
							display securityImage32 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "32"							
							]
							
							display securityImage33 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "33"							
							]
							
							display securityImage34 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "34"							
							]
							
							display securityImage35 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "35"							
							]
							
							display securityImage36[									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "36"							
							]
							
							display securityImage37 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "37"							
							]
							
							display securityImage38 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "38"							
							]
							
							display securityImage39 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "39"							
							]  
							
							display securityImage41 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "41"							
							]
							
							display securityImage42 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "42"							
							]
							
							display securityImage43 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "43"							
							]
							
							display securityImage44 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "44"							
							]
							
							display securityImage45 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "45"							
							]
							
							display securityImage46[									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "46"							
							]
							
							display securityImage47 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "47"							
							]
							
							display securityImage48 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "48"							
							]
							
							display securityImage49 [									
				        		class: "visually-hidden"
								attr_st-personal-image-display: "49"							
							]  
						]
					]
					
					div col5 [
						class: "col-md-6"
						
						p message1 [
		        			display sMessage
		        		]
			        											        		
			        	display groupId [
			        		class: "visually-hidden"
			        	]
			        	
			        	display memberId [
			        		class: "visually-hidden"
			    		]	        	
			    	]		
	        	]	 
			]
			
			div buttons [
				class: "st-buttons"
				
				div buttonsRow1 [
					class: "row"
						
					div buttonsCol1 [
						class: "col-md-12"
						navigation personalImageSubmit(getSelectedImageId, "{Next}") [
							class: "btn btn-primary"		
							
							require: [
								groupId,
								memberId
							]
							attr_tabindex: "41"
						]      			
		                navigation personalImageCancel(gotoLogin, "{Cancel}") [
							class: "btn btn-secondary"
							attr_tabindex: "42"
						]							
						navigation personalImageBack(gotoRegSecretQuestion, "{Back}") [
							attr_tabindex: "43"
						]	
					]
				]					
			]			
        ]               
	]

	/**************************************************************************
     * 3. User selects the Animals tab.
     */
    action selectAnimals [    
    	groupId = "1"    	
        goto(regPersonalImageScreen)
    ]  
    
    /**************************************************************************
     * 4. User selects the Nature tab.
     */
    action selectNature [  
    	groupId= "2"
        goto(regPersonalImageScreen)
    ]
    
    /**************************************************************************
     * 5. User selects the Objects tab.
     */
    action selectObjects [ 
    	groupId = "3"
        goto(regPersonalImageScreen)
    ]
    
    /**************************************************************************
     * 6. User selects the People tab.
     */
    action selectPeople [ 
    	groupId = "4"
        goto(regPersonalImageScreen)
    ]
    
    /**************************************************************************
     * 7. Gets selected image id by group id and member id.
     */
    action getSelectedImageId [
    	switch UcSecurityImage.getSelectedImageId(groupId, memberId, imageId) [
	    	case "success" gotoCompleteRegistration
			case "error" genericErrorMsg						
			default genericErrorMsg
		]   	
    ]
 
    /**************************************************************************
	 * 8. Go to the registration contact info usecase.
	 */    
    action gotoCompleteRegistration [    
        gotoUc(fffcReg99B2C)
    ]    
     
    /**************************************************************************
	 * 9. Go to the login usecase.
	 */     
    action gotoLogin [    
    	gotoModule(LOGIN)
    ] 

	/**************************************************************************
     * 10. Go to the registration secret question usecase.
     */
    action gotoRegSecretQuestion [
    	gotoUc(fffcReg07SecretQuestion)
    ]  
    
    /**************************************************************************
     * Alternative Paths
     **************************************************************************/
	
    /**************************************************************************
     * 1.1 Personal image initializing error. 
     * 1.2 Personal image initializing default error.
     * 7.1 Selected image id retrieval error.
     * 7.2 Selected image id retrieval default error.    
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)
        goto(regPersonalImageScreen)
	]	
]