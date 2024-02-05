useCase fffcTransactionHistory [
	
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
    shortcut fffcViewTransactions(init) [offset]
	
	importJava TransactionHistory(com.sorrisotech.fffc.transactions.TransactionHistory)
	importJava Session(com.sorrisotech.app.utils.Session)
	
    /*=============================================================================================
     * Data objects used by the use case.
     *===========================================================================================*/
	native string offset   = "not set"
	native string sAccount  = ""
	native string sPayGroup = ""
	
	string szHeader = "{Transaction History}"
	
    table tTable = TransactionHistory.historyFor(sAccount, sPayGroup) [
        emptyMsg: "{There are no transaction details available.}"
 
 		"t_date"    	=> string sDate      
 		"t_date_num" 	=> string sDateSort
		"t_type" 		=> string sType
		"t_desc"		=> string sDesc
		"t_amount" 		=> string sAmount
           
        column dateCol("{Date}") [
            elements: [sDate]   
            sort: [sDate]         
        ]
 
        column descCol("{Description}") [
            elements: [sDesc]                        
        ]
        
        column amountCol("{Amount}") [
            elements: [sAmount]  
            sort: [sAmount]    
            tags: ["text-end"]                   
        ] 
     ]	
	
    /*=============================================================================================
     * 1. System redirects user to an external site.
     *===========================================================================================*/
     
     // -- this sDisplayAccount stuff is just temporary --
     action init[
     	Session.getAccount(offset, sAccount) 
     	Session.getPayGroup(offset, sPayGroup)
     	goto (popin)
     ]
      
   /*-------------------------------------
     *  payment history popin.
     --------------------------------------*/    
    xsltFragment popin [
        div content [
        	class: "modal-content"
        
	        div historyHeading [
	            class: "modal-header"
	            div historyHeadingRow [
	            	class: "row text-center"
	            	
	            	div historyHeadingCol [
	            		class: "col-md-12"
	            		
			            display szHeader [
			                type : "h3"
			            ]
					]
				]
	        ]
	        	
	        div cancelAutomaticBody [
	           class: "modal-body"
	                
	            div tableRow [                	
	                class: "row"
	               	
	               	                
	                display tTable [
	                	class: "col-md-12"    
	                ]                   																				
				]                
	        ]  
	        
	        div historyButton [
	            class: "modal-footer"
	            
	            div historyButtonRow [
	            	class: "row text-center"
	            	
	            	div closeButton [
	            		class: "col-md-12"
	            			
	        			navigation close (init, "{CLOSE}") [
	        				type: "cancel"	
	                    	class: "btn btn-primary"
	                    	attr_tabindex: "10"		                   		                                    
	                	]            		                           		  		                		                
					]
				]
	        ]                                                 
		]
	]
]
    
     

