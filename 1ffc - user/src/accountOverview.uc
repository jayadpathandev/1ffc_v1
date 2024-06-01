useCase accountOverview [
   /**
    *  original author: Maybelle Johnsy Kanjirapallil
    *  refactored and expanded by: John A. Kowalonek
    *  created: 07-Feb-2017
    *  copied to 1st Franklin Project (1ffc-user): 2023-Nov-24
    *
    *  Primary Goal:
    *       1. Display a summary template for the most recent bill associated with an 
    * 			account using data from both the bill stream and the status stream.
    * 		2. Handle situations where there is no bill yet AND where the 
    * 			account is closed and there is no current bill for that account.
    *       
    *  Alternative Outcomes:
    *       1. None 
    * 
    *  NOTES:
    * 	
    * 	FIRST FRANKLIN SPECIFIC --
    * 	
    * 		This is intended to be used as a child use case, called by the overview.uc
    * 		use case on the overview page. A new child is started for each account and
    * 		shows JUST that accounts information in a Freemarker template.
    * 
    * 		The 1st Franklin site will behave somewhat differently than the "standard"
    * 		version of this.
    * 
    * 		1. It is quite possible that an account can have no recent bills
    * 		and no stored documents. Furthermore, an absences of a bill or document indicates
    * 		that we may be dealing with a brand new OR a closed account which we'll need to 
    * 		check in the status stream.
    * 
    * 		2. The status stream provides a lot of information that can be used in the bill
    * 		templates and this information needs to be sent down to the Freemarker template.
    * 
    *   GENERAL CONCEPT
    * 	
    * 		There are four scenarios that are active here
    * 
    * 		1. If we have some bills and its statement based, whether smart bill or smart view, 
    * 				then we show a bill summary for the most recent bill.
    * 		2. If we have some bills, and its invoice based, whether smart bill or smart view,
    * 				we show an account summary based on the open invoices and the payments made
    * 				against those invoices.
    * 		3. If we have only documents, then we show summary information for the most recent
    * 				document and the number of documents available for this account.
    * 		4. If we ONLY have smart pay, we show a bill summary for bills, documents don't get 
    * 				shown.
    * 		5. If there are no bills and no documents, then we are fucked and show nothing
    * 				is available.
    * 
    * 
    *  Arguments passed to the use case include:
    * 		sAccount -- 		internal account number for this account
    * 		sAccountDisplay -- 	account number that's displayed to the user
    * 		sPayGroup -- 		payment group associated with this account
    * 		sBillDate -- 		date for the most recent bill associated with this account
    * 		sBillCount -- 		number of bills available for this user (includes most recent + past)
    * 		sDocDate -- 		date for the most recent document (i.e. non-bill) associated with this account.
    * 		sDocCount -- 		the number of documents associated with this account.
    *                     
    *   Major Versions:
    * 		1.0 07-Feb-2017 First Version Coded [Maybelle Johnsy Kanjirapallil]
    * 		2.0 2023-Nov-24 	JAK	Updated for 1st Franklin and future use.
    * 			2023-Dec-11		JAK Updated to handle no bills situations.
    * 		2.1 2024-Jan-04  	JAK	Updated to use new template builder.
    * 		2.2	2024-Feb-19		JAK	Proper handling of recurring payment link, add test for old bills, and 
    * 								general cleanup of comments and steps 
    * 		2.3 2024-Feb-20		JAK	Add placeholders for nickname support.
    * 		2.4	2024-Apr-17		JAK	Refactored to handle turning links on when there are no bills or
    * 									this is a new account. Removed lots of "dead" code from the
    * 									"core" product so this doen'st have code for invoice mode.
    * 		2.5 2024-Apr-28		JAK	fixed defect where a transitioned new bill didn't check bill age
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user lands on the overview page and this is started.
            2. The user chooses the overview (account summary page) and this is started.
        ]]
        postConditions: [[
            1. Primary -- Summary of the user's account is displayed.
            2. Secondary -- A summary message shows indicating account status info: no bill, old bill, legal reasons.
        ]]
    ]
    
	actors [
		view_overview		
	]

    actorRules [
        if sBillingType == "invoice" removeActor menu_view_bill     
    ]
    
	
    startAt InitializeUseCase [sAccount, sAccountDisplay, sPayGroup, sBillDate, sBillCount, sDocDate, sDocCount]
             
    child utilImpersonationActive(utilImpersonationActive)
    
    /*********************************************************
     * EXTERNAL API CALLS AND DATA ASSOCIATED WITH THOSE CALLS
     **********************************************************/
 
    // -- GetStatus returns the status for all major conditions --
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetStatus) srGetStatusParams
    serviceResult (AccountStatus.GetStatus) srGetStatusResult
    
    //-- template group names --
 	native string sStatusGrp = "status" 
 	native string sScheduledPmtGrp = "scheduledPayment" 
	native string sRootGrp = "root"
	native string sNicknameGrp = "nickname"
 
	// -- Java classes used in this use case --
 	importJava Session(com.sorrisotech.app.utils.Session)
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
    importJava Math(com.sorrisotech.app.utils.Math)
    importJava CurrentBalanceHelper(com.sorrisotech.fffc.payment.BalanceHelper)
	importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava UcBillingAction(com.sorrisotech.uc.bill.UcBillingAction)
    importJava DisplayAccountMasked(com.sorrisotech.fffc.account.DisplayAccountMasked)
    

	// -- BillOverviewStatus provides information used in presenting a bill or document --
    serviceStatus srBillOverviewStatus
    serviceParam(Documents.BillOverview) 	srBillOverviewParam
    serviceResult(Documents.BillOverview) 	srBillOverviewResult
    
    // -- GetScheduledPmtSummaryForAccount gets summary information used in presenting
    //		status and warnings for a bill --
    serviceStatus srGetSchedPmtSummaryStatus
    serviceParam (Payment.GetScheduledPaymentSummaryForAccount) srGetSchedPmtSummaryParam
    serviceResult (Payment.GetScheduledPaymentSummaryForAccount) srGetSchedPmtSummaryResult

	// -- "true" if there's an automatic payment rule, "false" otherwise.  NOTE THAT THIS METHOD
	//		AND THE UNDERLYING SQL WILL NEED TO BE UPDATED TO TAKE THE PAYMENT GROUP INTO CONSIDERATION
	//		ALTHOUGH IT WILL WORK FOR THIS PARTICULAR CASE -- 
 	native string sHasAutomaticPaymentRule = UcPaymentAction.getAutoScheduledFlag(sUserId, sAccount)   

    // -- Simplied FreeMarker template management for situations that
    //		don't require all the bill stuff (although it probably could
    //		be used for that as well --
    importJava FtlTemplate(com.sorrisotech.ftlrender.UcFtlDisplay)
    
    // -- session object gives us the user id and other session based informtion --
    native string sUserId = Session.getUserId()    

	// -- paymentAction and session tell us whether we are statement or invoice based
	//		and whether or not we have smart bill. We will need to use that 
	//		bills enabled piece in the template if we are supporting smart bill --
    native string sBillingType = UcPaymentAction.getBillingBalanceType()
    
    // The app url used to create the edit nickname popin url
    native string sAppUrl = AppConfig.get("user.app.url")
	
	/**
	 * Comment out for now since we aren't using it at 1st Franklin or in the
	 *	1st Franklin Templates  PUT THIS BACK WHEN GENERALIZING
	 *
	 *	native string sBillsEnabledFlag = Session.areBillsEnabled()  
	 */
	 
   /*************************
	* DATA ITEMS SECTION
	*************************/     	
	
	// -- Arguments passed in by the parent use case --
	native string sAccount			// -- account id used internally
	native string sAccountDisplay	// -- account id shown on screens
	native string sPayGroup			// -- payment group
	native string sBillDate			// -- date of most recent bill or "" if none
	native string sBillCount		// -- number of bills available for this account
	native string sDocDate			// -- date of more recent document or "" if none
	native string sDocCount			// -- number of documents available for this account
	
	// -- oldest Bill allowed --
	native string maxBillAge =		AppConfig.get("recent.number.of.months", "3")
	volatile string sIsBillTooOld = UcBillingAction.checkBillAge(maxBillAge, sBillDate )
	
    // this is needed to force Persona to generate i18n files
	string empty = ""
	
	static billingChartTitle = "{Monthly spending comparison}"
	static billingSummaryTitle = "{Billing summary for period }"
	
	static totalOpenBillAmount = "{Total amount outstanding}"
	
	static dashboard = "{Dashboard}"
	static payAll = "{PAY ALL}"
	static withSelected = "{With selected}"
	static deselectAll = "{Deselect all}"
	static payBillSelected = "{Pay now}"
	static payBill = "{Pay now}"
	
	static summaryAndUsage = "{Usage}"
	static selectFromBillOrUsage = "{Select bill details:}"
	static selectBillDate = "{Bill date:}"
	
	static billSummaryAccountPopinHeader = "{Total balance}"
    static billSummaryServicePopinHeader = "{Total Monthly Charge}"
    static dueOnDate = "{due on}"

	/*-- Error messages --*/    
	static noBillsMsg = "{There are no bills for the selected period.}"
	static sInvalidConfig = "{Invalid configuration, you have multiple bills.}"
	static sNotImplemented = "{This has not implemented yet.}"
	static sNoBills = "{You have no bills.}"
	
	
	string sMessage = ""
 	
 				
	tag hPaymentError  = UcPaymentAction.getPaymentErrorTemplate(
        "paymentSummaryError.ftl",
        sorrisoLanguage, 
        sorrisoCountry, 							
        sAccountDisplay,	
        sPayGroup,				
		sMessage) 	

	// -- gets a Template Identifier (session) for the template first time
	//		accessed --
	native string TemplateIdPaymentSummary = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummary.ftl",
											sPayGroup )
	// -- gets a Template Identifier (session) for the template first time
	//		accessed --
	native string TemplateIdNoBills = FtlTemplate.initializeTemplate(
											sorrisoLanguage,
											sorrisoCountry,
											"paymentSummaryNoBills.ftl",
											sPayGroup)
	
	// -- set to one of the two TemplateId's above once system learns if
	//			there is a bill, or not --
	native string sActiveTemplate
 	tag hPaymentSummary = FtlTemplate.renderTemplate(sActiveTemplate)
											
	native string AccountOffset = Session.getAccountOffset(sAccount, sPayGroup) 

	// -- previousAmt does not work for 1st Franklin because we've warped
	//		the meaning of ubf:amountDue to be the principal and we've used ubf:amountDue as the 
	//		amount to be paid including overdue. ubf:minimumDue is the amount of a single standard payment.
	//
	//		You might ask WHY?  Well that's because we can keep payment thinking straight.
	//
	//			ubf.billAmount (totalDue here) -- is the current principal balance on the day the statement was issued
	//			ubf.amountDue  (docAmount here) -- is the amount due now, including overdue amount
	//			ubf.minimumDue (minDue here) -- is the standard monthly payment
	//
	//		So customers can pay up to the TotalDue, but should pay the docAmount to stay current and
	//		need to pay at least the minimumDue when they are in arrears. --
	
    number        previousAmt  = Math.subtract(srBillOverviewResult.totalDue, srBillOverviewResult.docAmount)                
    native string sBillLocalDate
    native string sBillLocalAmountDue
    native string sStatusLocalDate
    native string sStatusLocalAmountDue

    volatile native string sCurrentBalance = CurrentBalanceHelper.getCurrentBalanceRaw(
    									sPayGroup,							// -- payment group
										sAccount,							// -- internal account
										sBillLocalDate,						// -- most recent bill date
										sBillLocalAmountDue,				// -- bill amount due
										sStatusLocalDate,					// -- most recent status date
										sStatusLocalAmountDue	)			// -- status amount due
    

   
   volatile native string bIsAccountCurrent = CurrentBalanceHelper.isAccountCurrent(
    									sPayGroup,							// -- payment group
										sAccount,							// -- internal account
										sBillLocalDate,						// -- most recent bill date
										sBillLocalAmountDue,				// -- bill amount due
										sStatusLocalDate,					// -- most recent status date
										sStatusLocalAmountDue	)			// -- status amount due
    
    
    native string sLocalAccountStatus = "enabled" 						// -- this is work around to a "defect?" in persona.  API return structures appear to be
    											  						// 		appear to be immutable even though we can "assign a new value"... the 
    											  						//		    structure doesn't seem to get that new value . --
    native string bLocalPaymentEnabled = "false" 						// -- System calculates whether payment is enabled and sets this variable 
    																	// 		accordingly. System passes this variable to the template to minimize
    																	//			template based calculations. --
    native string bLocalAutomaticPaymentEnabled = "false"				// -- System calculates whether automatic payment should be enabled and
    																	//			sets this variable accordingly. System passes this variable to the template
    																	//			to minimize template based calculations. -- 	

	// -- variables for holding nickname or last 4 masked display account --
	volatile native string sDisplayAccountNickname = DisplayAccountMasked.displayAccountLookup(sUserId,sAccount,sPayGroup)
	native string sDisplayAccountNicknameUrl
	
	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

	/**************************************************************************************
	 * BEGIN 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	 
	/**
	 *	A1. Initialize this use case by checking for actor updates and then retrieving 
	 * 		the status information associated with this account. This status information
	 * 		will be used to drive the behavior of this sues case and the behavior of 
	 * 		the template that's displayed.  The status information is specific to 1st 
	 * 		Franklin.
	 */ 
	action InitializeUseCase [

		evalActors()
		
		srGetStatusParams.user = sUserId
		srGetStatusParams.paymentGroup = sPayGroup
		srGetStatusParams.account = sAccount
		// -- retrieve the status information --
   		switch apiCall AccountStatus.GetStatus(srGetStatusParams, srGetStatusResult, srAccountStatusCode) [
    		case apiSuccess assignlocalStatusVariable
    		default errorRetrievingStatus
    	]
 	]
 	
 	/**
 	 * A2. System assigns a local variable value from the status result allowing the system to transition from new account to
 	 *		existing account if if the system discovers a bill for a newAccount.
 	 */
 	action assignlocalStatusVariable [
 		sLocalAccountStatus = srGetStatusResult.accountStatus
 		sStatusLocalDate = srGetStatusResult.statusDate
 		sStatusLocalAmountDue = srGetStatusResult.currentAmountDue
 		goto (switchOnViewStatus)
 	]
 	
 	/**
 	 * A3. System determines if user can view the bill. If they can't then system will use the NoBillTemplate...
 	* 				 and it will	have viewAccount disabled so get that sorted
 	 */
 	action switchOnViewStatus [
 		switch srGetStatusResult.viewAccount [
 			case "enabled" 	switchOnAccountStatus
 			default			setNoBillTemplate
 		]
 	]
 
 	/**
 	 * A4. System determines if this is a new or closed account, that's another reason for using  the NoBillTemplate.
 	 */	
 	action switchOnAccountStatus [
 		switch srGetStatusResult.accountStatus [
 			case "activeAccount"	checkActiveAccountNoBills
 			case "newAccount" 		checkForTransition
 			case "closedAccount"	setPaymentDisabledAndNoBillTemplate
 			default					setNoBillTemplate
 		]
 	]
 	
 	/**
 	 * A5. System determined this was a new account.  System checks to see if  a bill has arrived for this account
 	 *				and the account needs  to 	transition to an active account
 	 */
 	action checkForTransition [
	 	if "0" == sBillCount then
	 		setNoBillTemplate
	 	else
	 		transitionNewAccount
	]
	
	/**
	 * A6. System transitions this new account to an active account for display.
	 */
	action transitionNewAccount [
		sLocalAccountStatus = "activeAccount"
		goto(isBillTooOld)
	]

	/**
	 * A7. System checks to see if this active account has bills or not. If an active account doesn't have bills, 
	 *				System will use the NoBillTemplate telling the user to contact their branch.
	 */
	action checkActiveAccountNoBills [
		if "0" == sBillCount then
			setNoBillTemplate
		else
			isBillTooOld
	]
	
	/**
	 * A8. System checks to see if the bill passed in to this overview is too old to use as a base for display.
	 */
	action isBillTooOld [
		if "true" == sIsBillTooOld then
			setNoBillTemplate
		else
			setSummaryTemplate
	]

 	/**
 	 * A9. System chooses no NoBillTemplate.
 	 */
  	action setNoBillTemplate [
  		
  		sActiveTemplate = TemplateIdNoBills
		goto (calculatePaymentEnabled)
 	]	
 
 	/**
 	 *	A10. System chooses PaymentSummaryTemplate
 	 */
 	action setSummaryTemplate [
 		sActiveTemplate = TemplateIdPaymentSummary
  		goto(calculatePaymentEnabled)
	]
	
	/**
	 * A11. System determines if the template should enable payment and/or 
	 *					calculate automatic payment. Default for both paymentEnabled and 
	 *					automatic payment is 'false'.  If payment is NOT enabled, then
	 * 				obviously automatic payment is not.  
	 */
	action calculatePaymentEnabled [
		bLocalPaymentEnabled = 'false'
		bLocalAutomaticPaymentEnabled = 'false'
		switch srGetStatusResult.paymentEnabled [
			case disableDQ		setPaymentEnabledTrueAndAutomaticFalse
			case enabled 		setPaymentEnabledTrueAndAutomaticTrue
			default 			setStatusGroupVariables
		]
	]
		
	
	/**
	 * A12. System sets payment enabled to 'true'. System sets automatic payment 
	 * 				as true for now (variable is temporarily acting as a proxy for !statusDQ).  
	 *					System will check the automatic payment status information AFTER it 
	 *					gets the current balance (much further down this use case)  to determine 
	 *					if there's 	an overriding factor that would prevent this user from having 
	 * 				automatic payment enabled.
	 */
	action setPaymentEnabledTrueAndAutomaticTrue [
		bLocalPaymentEnabled = 'true'
		bLocalAutomaticPaymentEnabled = 'true'
		goto (setStatusGroupVariables)
		
	]

	/**
	 * A13.  System sets payment enabled to 'true'. System sets automatic payment 
	 * 				as false for now (variable is temporarily acting as a proxy for !statusDQ).  
	 *					System will check the automatic payment status information AFTER it 
	 *					gets the current 	balance (much further down this use case)  to determine
	 *			    	if there's an overriding factor that would prevent this user from having 
	 * 				automatic payment enabled.
	 */
	action setPaymentEnabledTrueAndAutomaticFalse [
		bLocalPaymentEnabled = 'true'
		bLocalAutomaticPaymentEnabled = 'false'
		goto (setStatusGroupVariables)
		
	]
	
	/**
	 * A13a The account is closed, so turn off payments and set the no bill 
	 * 			template.
	 */
	action setPaymentDisabledAndNoBillTemplate [
 		sActiveTemplate = TemplateIdNoBills
		bLocalPaymentEnabled = 'false'
		bLocalAutomaticPaymentEnabled = 'false'
		goto (setStatusGroupVariables)
	]
	
 	/**
 	 *	A14. System assigns the status information determined above to the parameters
 	 *			 sent down 	to the currently active bill overview template.
 	 * 
 	 * 		Note that there are two variables that we can't set at this point. First is if
 	 * 				the automatic payment link in the template should be enabled, second is
 	 * 				the current amount due.  This happens after the system retrieves the bill
 	 *				information and calculating the current amount due. 
 	 */ 
	action setStatusGroupVariables [

		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "accountStatus",  "string", sLocalAccountStatus)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "bPayEnabled", 	 "boolean", bLocalPaymentEnabled)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "paymentEnabled", "string", srGetStatusResult.paymentEnabled)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "achEnabled",     "string", srGetStatusResult.achEnabled)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "viewAccount",    "string", srGetStatusResult.viewAccount)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "accountBalance", "number", srGetStatusResult.accountBalance)
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "dueDate", "dateDb", srGetStatusResult.paymentDueDate)
		FtlTemplate.setItemValue(sActiveTemplate, sRootGrp,   "jumpToOffset", "string", AccountOffset)

		// building edit nickname popin url
 		sDisplayAccountNicknameUrl = sAppUrl + "fffcEditDisplayAccounts?offset=" + AccountOffset
 		FtlTemplate.setItemValue(sActiveTemplate, sNicknameGrp, "displayAccount", "string", sDisplayAccountNickname)
		FtlTemplate.setItemValue(sActiveTemplate, sNicknameGrp, "url", "string", sDisplayAccountNicknameUrl)

		goto (fffcIsThereABill)
	]
	
	/**
	 * A15. If there's no bill then don't try to get the statement overiew, there is none
	 */
	action fffcIsThereABill [
		if "0" == sBillCount then
			checkStatusDQ 				// -- we don't have a statement so skip --
		else
			setStatementOverviewInfo	// -- for 1st Franklin the system knows what to do so the 
										// 		bill/document/statement/invoice tests are not necessary --
			
	]

	/**************************************************************************************
	 * END 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	
	/**************************************************************************************
	 * BEGIN GET SUMMARY INFORMATION
	 ************************************************************************************** */
	
	
	/**
	 *  4. System retrieves the statement overview information 
	 */
	action setStatementOverviewInfo [		
		srBillOverviewParam.user = sUserId
		srBillOverviewParam.account = sAccount
		srBillOverviewParam.payGroup = sPayGroup 
		srBillOverviewParam.billDate = sBillDate
		srBillOverviewParam.isBill = "true"
		
	    switch apiCall Documents.BillOverview(srBillOverviewParam, srBillOverviewResult, srBillOverviewStatus) [
		   case apiSuccess calculateCurrentBalanceFor1stFranklin
	       default errorStatementTemplate
	    ]			
	]
		
	/**************************************************************************************
	 * END GET SUMMARY INFORMATION
	 ************************************************************************************** */

	/**************************************************************************************
	 * END CURRENT BALANCE RELATED STEPS
	 ************************************************************************************** */

	/**************************************************************************************
	 * BEGIN GET CURRENT BALANCE FOR 1ST fRANKLIN
	 ************************************************************************************** */
	
	/**
	 * B1. The system performs the 1st Franklin balance calculation based on a reference to 
	 *				sCurrentBalance which is a native string backed by a java calculation.  As a result
	 *				this action is a no-op.
	 */
	action calculateCurrentBalanceFor1stFranklin [
		sBillLocalDate = srBillOverviewResult.docDate
    	sBillLocalAmountDue = srBillOverviewResult.totalDue
		goto(getScheduledPmtInfo)
	]

	/**************************************************************************************
	 * END GET CURRENT BALANCE FOR 1ST fRANKLIN
	 ************************************************************************************** */
	
	/**************************************************************************************
	 * BEGIN GET SCHEDULED AND RECURRING PAYMENT INFORMATION
	 ************************************************************************************** */

	/** 
	 * 13. Get Summary Information on all scheduled and recurring payment rules. Here's
	 *		what we're going to supply to the template:
	 *		
	 *		1. sScheduledPaymentCount -- 	 "none", "one", "many"
	 *		2. dScheduledPaymentDate -- 	 date of the latest schedule payment 
	 *		3. nScheduledPaymentAmount -- 	 total for all scheduled payments
	 *		4. bScheduledPaymentsLate -- 	 true if the total of all scheduled payments
	 *										 don't meet amountDue before due date
	 *		5. bAutomaticPaymentScheduled -- true if an automatic payment is scheduled
	 *		6. dAutomaticPaymentDate -- 	 date for this automatic payment
	 *		7. nAutomaticPaymentAmount -- 	 amount of this automatic payment
	 */									 
	action getScheduledPmtInfo [
		srGetSchedPmtSummaryParam.USER_ID = sUserId
		srGetSchedPmtSummaryParam.ACCOUNT_ID = sAccount
		srGetSchedPmtSummaryParam.PAYMENT_GROUP = sPayGroup
		srGetSchedPmtSummaryParam.PMT_DUEDATE = srGetStatusResult.paymentDueDate
		switch apiCall Payment.GetScheduledPaymentSummaryForAccount(srGetSchedPmtSummaryParam,
																srGetSchedPmtSummaryResult,
																srGetSchedPmtSummaryStatus) [
		case apiSuccess storeScheduledPmtVariables
		default errorFailedScheduledPmtInfo												
		]
	]
	
	/**
	 * 14. System stores scheduled & automatic payment summary information in the template
	 */
	action storeScheduledPmtVariables [
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "oneTimePmtCount", "number", srGetSchedPmtSummaryResult.ONETIMEPMT_COUNT)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "oneTimePmtDate", "timeDb", srGetSchedPmtSummaryResult.ONETIMEPMT_DATE)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "oneTimePmtTotalAmt", "number", srGetSchedPmtSummaryResult.ONETIMEPMT_TOTALAMT)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "automaticPmtCount", "number", srGetSchedPmtSummaryResult.AUTOMATICPMT_COUNT)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "automaticPmtDate", "timeDb", srGetSchedPmtSummaryResult.AUTOMATICPMT_DATE)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "automaticPmtTotalAmt", "number", srGetSchedPmtSummaryResult.AUTOMATICPMT_TOTALAMT)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "scheduledPmtTotalAmt", "number", srGetSchedPmtSummaryResult.SCHEDULEDPMT_TOTALAMT)
		FtlTemplate.setItemValue(sActiveTemplate, sScheduledPmtGrp, "hasAutomaticPmtRule", "boolean", sHasAutomaticPaymentRule)
		goto(checkStatusDQ)
	]
	

	/**************************************************************************************
	 * END GET SCHEDULED AND RECURRING PAYMENT INFORMATION
	 ************************************************************************************** */

	/**************************************************************************************
	 * BEGIN 1ST FRANKLIN SHOULD TEMPLATE SHOW THE AUTOMATIC PAYMENT LINK.
	 ************************************************************************************** */

	/**
	 * C1. The system checks bLocalAutomaticPaymentEnabled which is a proxy for 
	 *					payment status (not)StatusDQ. If we are statusDQ then the system needs
	 * 				to determine if the customer has paid up. In both cases, the next step
	 * 				is to check the automatic payment status with different actions for each.
	 */
	action checkStatusDQ [
		if 'true' == bLocalAutomaticPaymentEnabled then
				checkAutomaticPaymentStatusNoDQ
		else
				checkAutomaticPaymentStatusHasDQ  // -- defensive programming at its finest  
	]

	/**
	 * C2. The system looks at automatic payment status to determine if the user can 
	 *						create an automatic payment, or whether we need to check their balance
	 *						before we determine if they have and automatic payment oustanding. 
	 */
	action checkAutomaticPaymentStatusNoDQ [
		switch srGetStatusResult.automaticPaymentStatus [
			case  disabled					setAutoPayDisabled							// -- cannot enroll for automatic payment
			case  disabledUntilCurrent  	checkIsAccountCurrent						// -- can enroll if they bring their account current
			case  eligible					isThereAnAutomaticPayment					// -- can setup automatic payment so is there one?
			case  enrolled					isThereAnAutomaticPayment
			default							isThereAnAutomaticPayment
		]
	]

	/**
	 * C3. The system looks at automatic payment status to determine if the automatic
	 *						payment has been disabled. If it has, then we need to check their balance
	 *						before we can check their current automatic payment status. 
	 */
	action checkAutomaticPaymentStatusHasDQ [
		switch srGetStatusResult.automaticPaymentStatus [
			case  disabled					setAutoPayDisabled							// -- cannot enroll for automatic payment
			case  disabledUntilCurrent  	checkIsAccountCurrent						// -- can enroll if they bring their account current
			case  eligible					checkIsAccountCurrent											
			case  enrolled					checkIsAccountCurrent
			default							checkIsAccountCurrent
		]
	]
	
	/**
	 *  C4. System checks to see if account is current. The variable bIsAccountCurrent is 
	 * 			automatically set from Java when referenced and decides if the account
	 * 			is current (i.e. nothing overdue).
	 */
	action checkIsAccountCurrent [
		if 'true' == bIsAccountCurrent then
			isThereAnAutomaticPayment
		else
			setAutoPayDisabled
	]

	/**
	 * C5. System checks to see if there's an automatic payment already enabled.
	 * 			If there is none, turns on link. If there's one or more turns off link.
	 */
	action isThereAnAutomaticPayment [
			if "true" != sHasAutomaticPaymentRule then
				setAutoPayEnabled
			else
				setAutoPayDisabled 
	]
	
	/**
	 * C6. System enables automatic payment link.
	 */
	action setAutoPayEnabled [
		bLocalAutomaticPaymentEnabled = "true"
		goto (setAutoPayLinkValueInTemplate)
	]
	
	/**
	 * C7. System disables automatic payment link. 
	 */
	action setAutoPayDisabled [
		bLocalAutomaticPaymentEnabled = "false"
		goto (setAutoPayLinkValueInTemplate)
	]
	
	/**
	 * C8. System sets the automatic payment link enabled value in template  
	 */
	action setAutoPayLinkValueInTemplate [
		FtlTemplate.setItemValue(sActiveTemplate, sStatusGrp, "bAutoPayLinkEnabled", "boolean", bLocalAutomaticPaymentEnabled)
		goto (fffcIsThereABill2)
	]

	/**
	 * C9. if there's no bills, then skip setting Statement Info in the template
	 */
	action fffcIsThereABill2 [
		if "0" == sBillCount then
			showStatementOverview 		// -- we don't have a statement so skip --
		else
			setStatementInfoInTemplate	// -- for 1st Franklin the system knows what to do so the 
										// 		bill/document/statement/invoice tests are not necessary --
			
	]
	/**************************************************************************************
	 * END 1ST FRANKLIN SHOULD TEMPLATE SHOW THE AUTOMATIC PAYMENT LINK.
	 ************************************************************************************** */

	/**************************************************************************************
	 * BEGIN DISPLAY SUMMARY RELATED STEPS
	 ************************************************************************************** */
	 
	/**
	 * 15a. System has collected all the bill/document information needed for the template, assign it. */
	action setStatementInfoInTemplate [
		FtlTemplate.addDocumentInfo(sActiveTemplate,
						sPayGroup,
						sAccount,
						srBillOverviewResult.docDate,
						sCurrentBalance,
						previousAmt,
						srBillOverviewResult.result,
						sHasAutomaticPaymentRule)
		goto (showStatementOverview)
	]

    /* 
     * 16. Show the statement overview page (still called paymentSummery.ftl
     * 
     */
    xsltScreen showStatementOverview("{Overview}") [
    	display hPaymentSummary
    ]
    

	/**********************************************
	 * ALTERNATE (ERROR) SCENARIOS
	 **********************************************/

	// -- error message information --
  	string sErrorBase = "{An error occurred while trying to fulfill your request. Please try again later. }"
    
    structure(message) oMsgErrorTemplate [
		string(title) sTitle = "{Internal Application Error}"
		string(body) sBody = ""
	]
	
    
    /*
     * 2e. Specific to status feed, indicates that we could not retrieve
     *		account status information
     */
   	action errorRetrievingStatus [
    	oMsgErrorTemplate.sBody = sErrorBase + "{Failed to retrieve account status.}"
        displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
	]

    /*
     * 6e. There was an error processing the statement template
     */
    action errorStatementTemplate [
    	oMsgErrorTemplate.sBody = sErrorBase + "{Statement template rendering error.}"
    	displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
    ]
    
    action errorFailedScheduledPmtInfo [
    	oMsgErrorTemplate.sBody = sErrorBase + "{Failed to retrieve scheduled pmt info.}"
    	displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
    	
    ]
     
     /* 
     * 7e. Show the payment screen error. 
     */
    xsltScreen paymentErrorScreen("{Overview}") [

    	display  hPaymentError
    	
    ]    
            
] // -- end of accountOverview