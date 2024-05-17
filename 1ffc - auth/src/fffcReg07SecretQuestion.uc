useCase fffcReg07SecretQuestion [

   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 01-Oct-2015
    *
    *  Primary Goal:
    *       1. System displays Registration- Setup secret question (Step 4 of registration)
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
            1. Users enters correct information on the Registration - Setup login profile page
            ; and selects next button
        ]]
        postConditions: [[
            1. Primary -- System displays Registration - Setup Personal Image
            2. Alternative 1 -- Users selects back link, System take User to the Registration - Setup login page
            3. Alternative 2 -- Users selects cancel button, System take users to the Login page
        ]]
    ]
	startAt initialize

	/**************************
	 * DATA ITEMS SECTION
	 **************************/	
	 
	importJava UcSecretQuestions(com.sorrisotech.uc.secretquestions.UcSecretQuestions)
	
    string sPageName = "{Registration - Choose Secret Questions (step 4 of 5)}"
       
    persistent auto "{* Question 1}" dropDown dSecretQuestion1 [ 
    	null  : "{(Please select a question)}"
        q1: "{In what town/city were you raised?}"        
		q2: "{What is the name of your high school?}"
		q3: "{What is your grandfather's first name?}"
		q4: "{What is your mother's maiden name?}"
		q5: "{When is your grandmother's birthday?}"
		q6: "{Where is your mother's birthplace?}"
		q7: "{What is the name of your childhood best friend?}"
		q8: "{What is the name of your first pet?}"
		q9: "{What was the make of your first car?}"
		q10: "{What is your favorite color?}"
    ]
    
 	persistent field fSecretAnswer1 [ 
 		string(label) sLabel = "{* Answer 1}"       
		input (control) pInput("^[ \\p{L}\\d\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-\\=\\+\\;\\:\\'\\,\\.\\/\\?]{0,50}$", fSecretAnswer1.sValidation)  		 
		string(validation) sValidation = "{Secret question answers can be up to 50 characters. They may contain letters, numbers and any of the following symbols ! @ # $ % ^ & * () _ - = + ; : ' , . / ?}"
		string(required) sRequired = "{This field is required.}" 
    ]
	
	persistent auto "{* Question 2}" dropDown dSecretQuestion2 [ 
		null  : "{(Please select a question)}"
        q1: "{In what town/city were you raised?}"        
		q2: "{What is the name of your high school?}"
		q3: "{What is your grandfather's first name?}"
		q4: "{What is your mother's maiden name?}"
		q5: "{When is your grandmother's birthday?}"
		q6: "{Where is your mother's birthplace?}"
		q7: "{What is the name of your childhood best friend?}"
		q8: "{What is the name of your first pet?}"
		q9: "{What was the make of your first car?}"
		q10: "{What is your favorite color?}"
    ]
   
 	persistent field fSecretAnswer2 [
 		string(label) sLabel = "{* Answer 2}"      
		input (control) pInput("^[ \\p{L}\\d\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-\\=\\+\\;\\:\\'\\,\\.\\/\\?]{0,50}$", fSecretAnswer2.sValidation)  		 
		string(validation) sValidation = "{Secret question answers can be up to 50 characters. They may contain letters, numbers and any of the following symbols ! @ # $ % ^ & * () _ - = + ; : ' , . / ?}"
		string(required) sRequired = "{This field is required.}" 
    ]
    
    persistent auto "{* Question 3}" dropDown dSecretQuestion3 [ 
    	null  : "{(Please select a question)}"
        q1: "{In what town/city were you raised?}"        
		q2: "{What is the name of your high school?}"
		q3: "{What is your grandfather's first name?}"
		q4: "{What is your mother's maiden name?}"
		q5: "{When is your grandmother's birthday?}"
		q6: "{Where is your mother's birthplace?}"
		q7: "{What is the name of your childhood best friend?}"
		q8: "{What is the name of your first pet?}"
		q9: "{What was the make of your first car?}"
		q10: "{What is your favorite color?}"
    ]
   
 	persistent field fSecretAnswer3 [
 		string(label) sLabel = "{* Answer 3}"    
		input (control) pInput("^[ \\p{L}\\d\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-\\=\\+\\;\\:\\'\\,\\.\\/\\?]{0,50}$", fSecretAnswer3.sValidation)  		 
		string(validation) sValidation = "{Secret question answers can be up to 50 characters. They may contain letters, numbers and any of the following symbols ! @ # $ % ^ & * () _ - = + ; : ' , . / ?}"
		string(required) sRequired = "{This field is required.}" 
    ]
    
    persistent auto "{* Question 4}" dropDown dSecretQuestion4 [ 
    	null  : "{(Please select a question)}"
        q1: "{In what town/city were you raised?}"        
		q2: "{What is the name of your high school?}"
		q3: "{What is your grandfather's first name?}"
		q4: "{What is your mother's maiden name?}"
		q5: "{When is your grandmother's birthday?}"
		q6: "{Where is your mother's birthplace?}"
		q7: "{What is the name of your childhood best friend?}"
		q8: "{What is the name of your first pet?}"
		q9: "{What was the make of your first car?}"
		q10: "{What is your favorite color?}"
    ]
   
 	persistent field fSecretAnswer4 [
 		string(label) sLabel = "{* Answer 4}"
		input (control) pInput("^[ \\p{L}\\d\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-\\=\\+\\;\\:\\'\\,\\.\\/\\?]{0,50}$", fSecretAnswer4.sValidation)  		 
		string(validation) sValidation = "{Secret question answers can be up to 50 characters. They may contain letters, numbers and any of the following symbols ! @ # $ % ^ & * () _ - = + ; : ' , . / ?}"
		string(required) sRequired = "{This field is required.}" 
    ]

    // -- message strings for display when use case completes.  		
    structure(message) msgDropdownListError [    
        string(title) sTitle = "{Error}"
        string(body) sBody = "{Cannot load the secret questions. Please contact the system administrator.}"
    ]
    
    structure(message) oMsgQuestion1Error [
		string(title) sTitle = "{Invalid Question 1}"
		string(body) sBody = "{Question 1 is invalid. Please select a question.}"
	]
	
	structure(message) oMsgQuestion2Error [
		string(title) sTitle = "{Invalid Question 2}"
		string(body) sBody = "{Question 2 is invalid. Please select a question.}"
	]
	
	structure(message) oMsgQuestion3Error [
		string(title) sTitle = "{Invalid Question 3}"
		string(body) sBody = "{Question 3 is invalid. Please select a question.}"
	]
	
	structure(message) oMsgQuestion4Error [
		string(title) sTitle = "{Invalid Question 4}"
		string(body) sBody = "{Question 4 is invalid. Please select a question.}"
	]
		
    structure(message) msgGenericError [
		string(title) sTitle = "{Something wrong happened}"
		string(body) sBody = "{An error occurred while trying to fulfill your request. Please try again later.}"
	]
	
    /**************************************************************************
     * Main Path.
     **************************************************************************/ 
     
    /**************************************************************************
	 * 1. Initialize the secret questions list. 
	 */
    action initialize [    
		 switch UcSecretQuestions.init() [		 
		 	case "success" regSecretQuestionScreen
		 	case "error" showDropdownListError		 							
			default genericErrorMsg
		]
	]   
	
    /**************************************************************************
     *  2. System Displays secret questions screen where user selects
     * 	   secret questions and enters answers.
     */  	
    noMenu xsltScreen regSecretQuestionScreen("{Registration - Choose Secret Questions (step 4 of 5)}") [
    	
		form regSecretQuestionForm [
	    	class: "st-login"
	            
	        div header [
				class: "row"
				
				h1 headerCol [
					class: "col-md-12"
	            
    	            display sPageName
	            ]
			]
	    
	    	div content [
				class: "st-secret-questions"
				
				div row2 [
					class: "row"
					
					div col2 [
						class: "col-md-5 st-field-wide"
						
						display dSecretQuestion1 [
							item_q1_ng_disabled:  "form.dSecretQuestion2 == 'q1' || form.dSecretQuestion3 == 'q1' || form.dSecretQuestion4 == 'q1'"
							item_q2_ng_disabled:  "form.dSecretQuestion2 == 'q2' || form.dSecretQuestion3 == 'q2' || form.dSecretQuestion4 == 'q2'"
							item_q3_ng_disabled:  "form.dSecretQuestion2 == 'q3' || form.dSecretQuestion3 == 'q3' || form.dSecretQuestion4 == 'q3'"
							item_q4_ng_disabled:  "form.dSecretQuestion2 == 'q4' || form.dSecretQuestion3 == 'q4' || form.dSecretQuestion4 == 'q4'"     
							item_q5_ng_disabled:  "form.dSecretQuestion2 == 'q5' || form.dSecretQuestion3 == 'q5' || form.dSecretQuestion4 == 'q5'"
							item_q6_ng_disabled:  "form.dSecretQuestion2 == 'q6' || form.dSecretQuestion3 == 'q6' || form.dSecretQuestion4 == 'q6'"
							item_q7_ng_disabled:  "form.dSecretQuestion2 == 'q7' || form.dSecretQuestion3 == 'q7' || form.dSecretQuestion4 == 'q7'"
							item_q8_ng_disabled:  "form.dSecretQuestion2 == 'q8' || form.dSecretQuestion3 == 'q8' || form.dSecretQuestion4 == 'q8'"
							item_q9_ng_disabled:  "form.dSecretQuestion2 == 'q9' || form.dSecretQuestion3 == 'q9' || form.dSecretQuestion4 == 'q9'"     
							item_q10_ng_disabled: "form.dSecretQuestion2 == 'q10' || form.dSecretQuestion3 == 'q10' || form.dSecretQuestion4 == 'q10'"
							control_attr_tabindex: "1"
							control_attr_autofocus: ""
						]
					]
					
					div col3 [
						class: "col-md-5 st-field-wide"
							
			 			display fSecretAnswer1 [
			 				control_attr_tabindex: "2"
			 			]
			 		]
			 	]
			 	
				div row4 [
					class: "row"
					
					div col4 [
						class: "col-md-5 st-field-wide"
				                
						display dSecretQuestion2 [
							item_q1_ng_disabled:  "form.dSecretQuestion1 == 'q1' || form.dSecretQuestion3 == 'q1' || form.dSecretQuestion4 == 'q1'"
							item_q2_ng_disabled:  "form.dSecretQuestion1 == 'q2' || form.dSecretQuestion3 == 'q2' || form.dSecretQuestion4 == 'q2'"
							item_q3_ng_disabled:  "form.dSecretQuestion1 == 'q3' || form.dSecretQuestion3 == 'q3' || form.dSecretQuestion4 == 'q3'"
							item_q4_ng_disabled:  "form.dSecretQuestion1 == 'q4' || form.dSecretQuestion3 == 'q4' || form.dSecretQuestion4 == 'q4'"     
							item_q5_ng_disabled:  "form.dSecretQuestion1 == 'q5' || form.dSecretQuestion3 == 'q5' || form.dSecretQuestion4 == 'q5'"
							item_q6_ng_disabled:  "form.dSecretQuestion1 == 'q6' || form.dSecretQuestion3 == 'q6' || form.dSecretQuestion4 == 'q6'"
							item_q7_ng_disabled:  "form.dSecretQuestion1 == 'q7' || form.dSecretQuestion3 == 'q7' || form.dSecretQuestion4 == 'q7'"
							item_q8_ng_disabled:  "form.dSecretQuestion1 == 'q8' || form.dSecretQuestion3 == 'q8' || form.dSecretQuestion4 == 'q8'"
							item_q9_ng_disabled:  "form.dSecretQuestion1 == 'q9' || form.dSecretQuestion3 == 'q9' || form.dSecretQuestion4 == 'q9'"     
							item_q10_ng_disabled: "form.dSecretQuestion1 == 'q10' || form.dSecretQuestion3 == 'q10' || form.dSecretQuestion4 == 'q10'"
							control_attr_tabindex: "3"
						]
					]
					
					div col5 [
						class: "col-md-5 st-field-wide"
						
						display fSecretAnswer2 [
			 				control_attr_tabindex: "4"
			 			]
			 		]
			 	]
			 	
				div row6 [
					class: "row"
					
					div col6 [
						class: "col-md-5 st-field-wide"
							
						display dSecretQuestion3 [
							item_q1_ng_disabled:  "form.dSecretQuestion1 == 'q1' || form.dSecretQuestion2 == 'q1' || form.dSecretQuestion4 == 'q1'"
							item_q2_ng_disabled:  "form.dSecretQuestion1 == 'q2' || form.dSecretQuestion2 == 'q2' || form.dSecretQuestion4 == 'q2'"
							item_q3_ng_disabled:  "form.dSecretQuestion1 == 'q3' || form.dSecretQuestion2 == 'q3' || form.dSecretQuestion4 == 'q3'"
							item_q4_ng_disabled:  "form.dSecretQuestion1 == 'q4' || form.dSecretQuestion2 == 'q4' || form.dSecretQuestion4 == 'q4'"     
							item_q5_ng_disabled:  "form.dSecretQuestion1 == 'q5' || form.dSecretQuestion2 == 'q5' || form.dSecretQuestion4 == 'q5'"
							item_q6_ng_disabled:  "form.dSecretQuestion1 == 'q6' || form.dSecretQuestion2 == 'q6' || form.dSecretQuestion4 == 'q6'"
							item_q7_ng_disabled:  "form.dSecretQuestion1 == 'q7' || form.dSecretQuestion2 == 'q7' || form.dSecretQuestion4 == 'q7'"
							item_q8_ng_disabled:  "form.dSecretQuestion1 == 'q8' || form.dSecretQuestion2 == 'q8' || form.dSecretQuestion4 == 'q8'"
							item_q9_ng_disabled:  "form.dSecretQuestion1 == 'q9' || form.dSecretQuestion2 == 'q9' || form.dSecretQuestion4 == 'q9'"     
							item_q10_ng_disabled: "form.dSecretQuestion1 == 'q10' || form.dSecretQuestion2 == 'q10' || form.dSecretQuestion4 == 'q10'"
							control_attr_tabindex: "5"
						]
					]
					
					div col7 [
						class: "col-md-5 st-field-wide"
						
			            display fSecretAnswer3 [
			 				control_attr_tabindex: "6"
			 			]
			 		]
			 	]
			 	
				div row8 [
					class: "row"
					
					div col8 [
						class: "col-md-5 st-field-wide"
				 			          
						display dSecretQuestion4 [
							item_q1_ng_disabled:  "form.dSecretQuestion1 == 'q1' || form.dSecretQuestion2 == 'q1' || form.dSecretQuestion3 == 'q1'"
							item_q2_ng_disabled:  "form.dSecretQuestion1 == 'q2' || form.dSecretQuestion2 == 'q2' || form.dSecretQuestion3 == 'q2'"
							item_q3_ng_disabled:  "form.dSecretQuestion1 == 'q3' || form.dSecretQuestion2 == 'q3' || form.dSecretQuestion3 == 'q3'"
							item_q4_ng_disabled:  "form.dSecretQuestion1 == 'q4' || form.dSecretQuestion2 == 'q4' || form.dSecretQuestion3 == 'q4'"     
							item_q5_ng_disabled:  "form.dSecretQuestion1 == 'q5' || form.dSecretQuestion2 == 'q5' || form.dSecretQuestion3 == 'q5'"
							item_q6_ng_disabled:  "form.dSecretQuestion1 == 'q6' || form.dSecretQuestion2 == 'q6' || form.dSecretQuestion3 == 'q6'"
							item_q7_ng_disabled:  "form.dSecretQuestion1 == 'q7' || form.dSecretQuestion2 == 'q7' || form.dSecretQuestion3 == 'q7'"
							item_q8_ng_disabled:  "form.dSecretQuestion1 == 'q8' || form.dSecretQuestion2 == 'q8' || form.dSecretQuestion3 == 'q8'"
							item_q9_ng_disabled:  "form.dSecretQuestion1 == 'q9' || form.dSecretQuestion2 == 'q9' || form.dSecretQuestion3 == 'q9'"     
							item_q10_ng_disabled: "form.dSecretQuestion1 == 'q10' || form.dSecretQuestion2 == 'q10' || form.dSecretQuestion3 == 'q10'"
							control_attr_tabindex: "7"
						]
					]
					
					div col9 [
						class: "col-md-5 st-field-wide"
						
			            display fSecretAnswer4 [
			 				control_attr_tabindex: "8"
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
						
						navigation secretQuestionSubmit(checkQuestion1, "{Next}") [
		                    class: "btn btn-primary"
		                    
		                    data: [
		                    	dSecretQuestion1, 
		                    	fSecretAnswer1, 
		                    	dSecretQuestion2, 
		                    	fSecretAnswer2, 
		                    	dSecretQuestion3, 
		                    	fSecretAnswer3, 
		                    	dSecretQuestion4, 
		                    	fSecretAnswer4
		                    ]
		                    
		                    require: [
		                    	dSecretQuestion1, 
		                    	fSecretAnswer1 => fSecretAnswer1.sRequired, 
		                    	dSecretQuestion2, 
		                    	fSecretAnswer2 => fSecretAnswer2.sRequired, 
		                    	dSecretQuestion3, 
		                    	fSecretAnswer3 => fSecretAnswer3.sRequired, 
		                    	dSecretQuestion4, 
		                    	fSecretAnswer4 => fSecretAnswer4.sRequired
		                    ]
		                    attr_tabindex: "9"
		                ]         			
						
		                navigation secretQuestionCancel(gotoLogin, "{Cancel}") [
							class: "btn btn-secondary"
							attr_tabindex: "10"
		                ]							
						
						navigation secretQuestionBack(gotoRegLoginInfo, "{Back}") [
							attr_tabindex: "11"
						]
					]
				]					
			]			
        ]
    ]      
    
    /************************************************************************** 
     * 3. Check secret question 1. 
     */
    action checkQuestion1 [
    	if dSecretQuestion1 == "null" then
    		question1ErrorMsg
    	else 
    		checkQuestion2	
    ]
    
     /************************************************************************** 
      * 4. Check secret question 2. 
      */
    action checkQuestion2 [
    	if dSecretQuestion2 == "null" then
    		question2ErrorMsg
    	else 
    		checkQuestion3	
    ]
    
     /************************************************************************** 
      * 5. Check secret question 3. 
      */
    action checkQuestion3 [
    	if dSecretQuestion3 == "null" then
    		question3ErrorMsg
    	else 
    		checkQuestion4	
    ]
    
    /************************************************************************** 
     * 6. Check secret question 4. 
     */
    action checkQuestion4 [
    	if dSecretQuestion4 == "null" then
    		question4ErrorMsg
    	else 
    		gotoRegPersonalImage	
    ]
    
	/**************************************************************************
	 * 7. User selects next button, System displays personal image 
	 *    selection page.
	 */
    action gotoRegPersonalImage [    
        gotoUc(fffcReg08PersonalImage)
    ]  
    
    /**************************************************************************
     * 8. User selects Cancel button, System takes user back to the login page.
     */ 
    action gotoLogin [    
    	gotoModule(LOGIN)
    ]
    
    /**************************************************************************
     * 9. User clicks back link, System takes user to the Registration 
     *    Setup login profile screen.
     */
    action gotoRegLoginInfo [
    	gotoUc(fffcReg06LoginInfo)
    ]  
        
    /**************************************************************************
     * Alternative Paths
     **************************************************************************/
        
    /**************************************************************************
     * 1.1 Display the secret question loading error message. 
     */ 
    action showDropdownListError [    
        displayMessage(type: "danger" msg: msgDropdownListError)
        goto(regSecretQuestionScreen)  
    ]   
    
    /**************************************************************************
     * 1.2 Display generic error message. 
     */
	action genericErrorMsg [
		displayMessage(type: "danger" msg: msgGenericError)
        goto(regSecretQuestionScreen)
	]	
		 
    /************************************************************************** 
     * 3.1 Question 1 error message. 
     */
	action question1ErrorMsg [
		displayMessage(type: "danger" msg: oMsgQuestion1Error)
        goto(regSecretQuestionScreen)
	]
	
	/************************************************************************** 
	 * 4.1. Question 2 error message. 
	 */
	action question2ErrorMsg [
		displayMessage(type: "danger" msg: oMsgQuestion2Error)
        goto(regSecretQuestionScreen)
	]
	
	/************************************************************************** 
	 * 5.1. Question 3 error message. 
	 */
	action question3ErrorMsg [
		displayMessage(type: "danger" msg: oMsgQuestion3Error)
        goto(regSecretQuestionScreen)
	]
	
	/************************************************************************** 
	 * 6.1. Question 4 error message. 
	 */
	action question4ErrorMsg [
		displayMessage(type: "danger" msg: oMsgQuestion4Error)
        goto(regSecretQuestionScreen)
	]	 
]