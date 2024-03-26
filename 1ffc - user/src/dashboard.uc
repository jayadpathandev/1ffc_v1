useCase dashboard [
	
    /**********************************************************************************************
     * author:  Joshu Gentry
     * created: 28-jul-2016 
     *
     * Primary Goal:
     *     This use case is used to place the user at the correct landing page after logging in.
     *     To do this the use case looks at the actors available to the user and decides on the 
     *     correct use case.
     *
     * Major Versions:
     *    1.0      28-jul-2016  First Version Coded
     * 
     * ADAPTED FOR 1ST FRANKLIN
     * 	- as  2023-Dec-11 ADDED INIT FOR CONNECTION TO NLS
     *  - jak 2023-Dec-11 FORCED ACTOR FOR OVERVIEW SINCE THEY CAN VIEW THE OVERVIEW EVEN IF THEY HAVE NO BILLS
     *
     */
     documentation [
		triggers: [[
			1. Once the user logs in this use case determines the landing page for the user.
		]]
		preConditions: [[
			1. The user must be logged into the system for this use case to work.
		]]
		postConditions: [[
			1. The user is directed to the correct landing page.
		]]
	]
	
	startAt actionHasBills
    
    actorRules [
    	if sExtAppFlag == "true"  addActor view_app_integration
        if sExtAppFlag == "false" removeActor view_app_integration 
        if sHasBills   == "Y"     addActor view_overview       
    ]
        
    importJava Session(com.sorrisotech.app.utils.Session)
    importJava UserProfile (com.sorrisotech.app.utils.UserProfile)
    importJava Initialize(com.sorrisotech.fffc.user.Initialize)
	importJava Config(com.sorrisotech.utils.AppConfig)
    
    import billCommon.sBillAccountInternal
    import billCommon.sBillAccountExternal 
    import billCommon.sBillGroup 
    import billCommon.sBillSelectedDate
    import billCommon.sBillIsBill
    import billCommon.sSelectedDropDown
     
    import billCommon.sPayAccountInternal
    import billCommon.sPayAccountExternal
    import billCommon.sPayGroup  
    import billCommon.sPayIsBill
    import billCommon.sPaySelectedDate
    
    serviceStatus srOverviewStatus	
    serviceParam(Documents.AccountOverview) srAcctsParam
    serviceResult(Documents.AccountOverview) srAcctsResult   

 	serviceStatus srStatus	    
    serviceParam(Documents.HasBillsLoaded) srLatestReq
    serviceResult(Documents.HasBillsLoaded) srLatestResult     
    
    serviceStatus srGetOnlineAcctsStatus
    serviceParam(AccountStatus.GetEligibleAssignedAccounts) srGetOnlineAcctsReq
    serviceResult(AccountStatus.GetEligibleAssignedAccounts) srGetOnlineAcctsResult
    native string sStatusPaymentGroup = Config.get("1ffc.ignore.group")
    native string sBillPaymentGroup = Config.get("1ffc.bill.group")

    
 			
    /**********************************************************************************************
     * Data Items Section
     *********************************************************************************************/ 

	native string sUserId = Session.getUserId()  
    native string sExtAppFlag = UserProfile.getExtAppIntegrationFlag()
    native string sHasBills
     
    structure(message) msgAccessDenied [
        string(title) sTitle = "{Access Denied}"
        string(body) sBody = "{You do not have access to this application.}"
    ]

    /**********************************************************************************************
     * Main Success Scenario
     *********************************************************************************************/ 

   /*=============================================================================================
     * 1. System checks if the view overview actor is enabled.
     *===========================================================================================*/
    action actionHasBills [
    	
    	srLatestReq.user = sUserId
    	
	    switch apiCall Documents.HasBillsLoaded(srLatestReq, srLatestResult, srStatus) [
		   case apiSuccess actionCheckBills
	       default actionAccessDenied
	    ]	  	    
    ]

    /*=============================================================================================
     * 2. System checks if the assist bill actor is enabled.
     *===========================================================================================*/
    action actionCheckBills [
//		---------------------------------------------------------------------------
//		jak 2023-Dec-11
//
//		for 1st Franklin, we always set the actor for overview which is controlled
//			by the value of sHasBills
//    	sHasBills = srLatestResult.sFlag
  		sHasBills = "Y"  	
    	Session.setAppType("b2c")
    	evalActors()
    	
        switch on actors [
            has view_bill actionEnableBills
            default actionCheckDocuments
        ]
    ]

    /*=============================================================================================
     * 3. System enables bills.
     *===========================================================================================*/
    action actionEnableBills [
        Session.enableBills()
        goto(actionCheckDocuments)
    ]

    /*=============================================================================================
     * 4. System checks if the assist document actor is enabled.
     *===========================================================================================*/
    action actionCheckDocuments [
        switch on actors [
            has view_document actionEnableDocs
            default actionCheckPayments
        ]
    ]

    /*=============================================================================================
     * 5. System enables documents.
     *===========================================================================================*/
    action actionEnableDocs [
        Session.enableDocs()
        goto(actionCheckPayments)
    ]

    /*=============================================================================================
     * 6. System checks if the assist payment actor is enabled.
     *===========================================================================================*/
    action actionCheckPayments [
        switch on actors [
            has view_payment actionEnablePayments
            default actionChooseDashboard
        ]
    ]

    /*=============================================================================================
     * 7. System enables payments.
     *===========================================================================================*/
    action actionEnablePayments [
        Session.enablePayments()
        goto(getOnlineEligibleAccounts)
    ]    
    
    /**
     *  7a. Get the list of eligible accounts
     */
    action getOnlineEligibleAccounts [
    	srGetOnlineAcctsReq.user = sUserId
    	srGetOnlineAcctsReq.statusPaymentGroup = sStatusPaymentGroup
    	srGetOnlineAcctsReq.billPaymentGroup = sBillPaymentGroup
    	switch apiCall AccountStatus.GetEligibleAssignedAccounts(srGetOnlineAcctsReq, srGetOnlineAcctsResult, srGetOnlineAcctsStatus) [
           case apiSuccess setAccountList
           default getAccounts    // -- if this fails, do without the list --
       ]
    ]

	/**
	 *  7b. Add the list returned to the parameters for Documents.Overview
	 */
	action setAccountList [
		srAcctsParam.jsonAccountList  = srGetOnlineAcctsResult.accountsAsJsonArray
		goto (getAccounts)
	] 
    /*=============================================================================================
     * 8. Get accounts.
     *===========================================================================================*/    
    action getAccounts [
		srAcctsParam.user = sUserId
		
		switch apiCall Documents.AccountOverview(srAcctsParam, srAcctsResult, srOverviewStatus ) [
           case apiSuccess saveAccounts
           default saveAccounts    
        ]       	
    ]

    /*=============================================================================================
     * 9. Save accounts to session.
     *===========================================================================================*/
	action saveAccounts [
		Session.setAccounts(srAcctsResult.accounts)
		
		sSelectedDropDown = "0"
		Session.getAccount(sSelectedDropDown, sBillAccountInternal)
		Session.getAccountDisplay(sSelectedDropDown, sBillAccountExternal)
		Session.getPayGroup(sSelectedDropDown, sBillGroup)
		Session.getLatestBillDate(sSelectedDropDown, sBillSelectedDate)
		Session.getIsBill(sSelectedDropDown, sBillIsBill)
		
		Session.getAccount(sSelectedDropDown, sPayAccountInternal)
		Session.getAccountDisplay(sSelectedDropDown, sPayAccountExternal)
		Session.getPayGroup(sSelectedDropDown, sPayGroup)
		Session.getLatestBillDate(sSelectedDropDown, sPaySelectedDate)
		Session.getIsBill(sSelectedDropDown, sPayIsBill)
		
		goto(actionChooseDashboard)
	]
	
    /*=============================================================================================
     * 10. System checks which actors the user has.
     *===========================================================================================*/
     action actionChooseDashboard [
     	Initialize.init()
        switch on actors [
        	has view_overview actionJumpAccountOverview
        	has view_bill actionJumpBills
            has view_document actionJumpDocument     
            has view_payment actionJumpPayment         
            has ACTOR_SAAS_USER_PROFILE actionJumpProfile
            default actionAccessDenied
        ]
    ]
    
    /*=============================================================================================
     * 10A. If the user has the view_payment actor then they are redirected to the paymentSummary 
           use case.
     *
     *===========================================================================================*/
    action actionJumpAccountOverview [
        gotoUc(overview)
    ]
    
    action actionJumpDocument [
        gotoUc(document)
    ]
    
    action actionJumpBills [
        gotoUc(billSummary)
    ]
        
    action actionJumpPayment [
        gotoUc(payment)
    ]
    
   /*=============================================================================================
     * 10B. If the user has the profile actor then they are redirected to the profile use case
           use case.
     *
     *===========================================================================================*/
    action actionJumpProfile [
        gotoUc(profile)
    ]
    

    /*=============================================================================================
     * 10C. If the user has none of the correct actors they are redirected to the login page with
     *     and error message.
     *===========================================================================================*/
    action actionAccessDenied [
        displayMessage(type: "danger" msg: msgAccessDenied)

        gotoModule(LOGIN)
    ] 
]