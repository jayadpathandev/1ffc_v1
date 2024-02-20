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
    * 			     2023-Dec-11	JAK    Updated to handle no bills situations.
    * 		2.1 2024-Jan-04  	JAK	Updated to use new template builder.
    * 		2.2	2024-Feb-19		JAK	Proper handling of recurring payment link, add test for old bills, and 
    * 																	general cleanup of comments and steps 
    * 		2.3 2024-Feb-20		JAK	Add placeholders for nickname support.
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
    
    // -- group name used for status information in template --
 	native string sStatusGroupName = "status" 
 	// -- group name for scheduled pmt information in template --
 	native string sScheduledPmtGroupName = "scheduledPayment" 
 
	// -- Java classes used in this use case --
 	importJava Session(com.sorrisotech.app.utils.Session)
    importJava UcPaymentAccountBalance(com.sorrisotech.uc.payment.UcPaymentAccountBalance)
    importJava UcPaymentAction(com.sorrisotech.uc.payment.UcPaymentAction)
    importJava Math(com.sorrisotech.app.utils.Math)
    importJava CurrentBalanceHelper(com.sorrisotech.fffc.payment.BalanceHelper)
	importJava AppConfig(com.sorrisotech.utils.AppConfig)
	importJava UcBillingAction(com.sorrisotech.uc.bill.UcBillingAction)
    
    // -- GetCurrentBalanceConfig tells us the source of information for current balance
    serviceStatus srStatus	
    serviceResult(SystemConfig.GetCurrentBalanceConfig) srGetComm

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
	native string sAccount				// -- account id used internally
	native string sAccountDisplay	// -- account id shown on screens
	native string sPayGroup				// -- payment group
	native string sBillDate					// -- date of most recent bill or "" if none
	native string sBillCount				// -- number of bills available for this account
	native string sDocDate				// -- date of more recent document or "" if none
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
	
	string sCurrentBalanceFlag = ""
	
	string sMessage = ""
 	
 	tag hPaymentSummary = FtlTemplate.renderTemplate(TemplateIdPaymentSummary)
 				
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
											
	native string AccountOffset = Session.getAccountOffset(sAccount, sPayGroup) 
	tag hDontShowBills = FtlTemplate.renderTemplate(TemplateIdNoBills)

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
    volatile native string sCurrentBalance = CurrentBalanceHelper.getCurrentBalanceRaw(
    									sPayGroup,																	// -- payment group
										sAccount,																	// -- internal account
										srBillOverviewResult.docDate,						// -- most recent bill date
										srBillOverviewResult.totalDue,						// -- bill amount due
										srGetStatusResult.statusDate,						// -- most recent status date
										srGetStatusResult.currentAmountDue	)	// -- status amount due
    
   volatile native string bIsAccountCurrent = CurrentBalanceHelper.isAccountCurrent(
    									sPayGroup,																	// -- payment group
										sAccount,																	// -- internal account
										srBillOverviewResult.docDate,						// -- most recent bill date
										srBillOverviewResult.totalDue,						// -- bill amount due
										srGetStatusResult.statusDate,						// -- most recent status date
										srGetStatusResult.currentAmountDue	)	// -- status amount due
    
    
    native string sLocalAccountStatus = "enabled" 						// -- this is work around to a "defect?" in persona.  API return structures appear to be
    											  																			// 		appear to be immutable even though we can "assign a new value"... the 
    											  																			//		    structure doesn't seem to get that new value . --
    native string bLocalPaymentEnabled = 'false'	  						// -- System calculates whether payment is enabled and sets this variable 
    																														// 		accordingly. System passes this variable to the template to minimize
    																														//			template based calculations. --
    native string bLocalAutomaticPaymentEnabled = 'false' 	// -- System calculates whether automatic payment should be enabled and
    																														//			sets this variable accordingly. System passes this variable to the template
    																														//			to minimize template based calculations. -- 	

	// -- variables for holding nickname or last 4 masked display account --
	native string sDisplayAccountNickname
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
 	 * A2. System uses this trampoline function that should not be needed... System assigns a local 
 	 *				variable value  from the status result allowing it to transition the result later if the system
 	 *				discovers a bill for a newAccount.
 	 */
 	action assignlocalStatusVariable [
 		sLocalAccountStatus = srGetStatusResult.accountStatus
 		goto (switchOnViewStatus)
 	]
 	
 	/**
 	 * A3. System determines if user can view the bill. If they can't then system will use the NoBillTemplate...
 	* 				 and it will	have viewAccount disabled so get that sorted
 	 */
 	action switchOnViewStatus [
 		switch srGetStatusResult.viewAccount [
 			case "enabled" 	switchOnAccountStatus
 			default			setNoBillTemplateData
 		]
 	]
 
 	/**
 	 * A4. System determins if this is a new or closed account, that's another reason for using  the NoBillTemplate.
 	 */	
 	action switchOnAccountStatus [
 		switch srGetStatusResult.accountStatus [
 			case "activeAccount"	checkActiveAccountNoBills
 			case "newAccount" 		checkForTransition
 			default					setNoBillTemplateData
 		]
 	]
 	
 	/**
 	 * A5. System determined this was a new acount.  System checksto see if  a bill has arrived for this account
 	 *				and the account needs  to 	transition to an active account
 	 */
 	action checkForTransition [
	 	if "0" == sBillCount then
	 		setNoBillTemplateData
	 	else
	 		transitionNewAccount
	]
	
	/**
	 * A6. System transitions this new account to an active account for display.
	 */
	action transitionNewAccount [
		sLocalAccountStatus = "activeAccount"
		goto(oKToAssignStatusToBillTemplate)
	]

	/**
	 * A7. System checks to see if this active account has bills or not. If an active account doesn't have bills, 
	 *				System will use the NoBillTemplate telling the user to contact their branch.
	 */
	action checkActiveAccountNoBills [
		if "0" == sBillCount then
			setNoBillTemplateData
		else
			isBillTooOld
	]
	
	/**
	 * A8. System checks to see if the bill passed in to this overview is too old to use as a base for display.
	 */
	action isBillTooOld [
		if "true" == sIsBillTooOld then
			setNoBillTemplateData
		else
			oKToAssignStatusToBillTemplate
	]

 	/**
 	 * A9. System sets status variables for the NoBillTemplate.
 	 */
  	action setNoBillTemplateData [
 		// -- values for new account --
		FtlTemplate.setItemValue(TemplateIdNoBills, "status", "viewAccount", "string", srGetStatusResult.viewAccount)
		FtlTemplate.setItemValue(TemplateIdNoBills, "status", "accountStatus", "string", sLocalAccountStatus)
		FtlTemplate.setItemValue(TemplateIdNoBills, "root",  "displayAccount", "string", sAccountDisplay)
		FtlTemplate.setItemValue(TemplateIdNoBills, "root", 	"jumpToOffset", "string", AccountOffset)

		// -- values for nickname --
 		sDisplayAccountNickname = sAccountDisplay
 		sDisplayAccountNicknameUrl = ""
 		FtlTemplate.setItemValue(TemplateIdNoBills, "nickname", "displayAccount", "string", sDisplayAccountNickname)
		FtlTemplate.setItemValue(TemplateIdNoBills, "nickname", "url", "string", sDisplayAccountNicknameUrl)
		
		goto (showNoBillTemplate)
 	]	
 
 	/**
 	 * A10. System shows the NoBillTemplate
 	 */
 	xsltScreen showNoBillTemplate [
 		display hDontShowBills
 	]
 	
 	/**
 	 *	A11. System is using the paymentSummary template now... the rest assumes that's
 	 *					the case. Clear the group elements just in case.
 	 */
 	action oKToAssignStatusToBillTemplate [
  		goto(calculatePaymentEnabled)
	]
	
	/**
	 * A112. System determines if the template should enable payment and/or 
	 *					calculate automatic payment. Default for both paymentEnabled and 
	 *					automatic payment is 'false'.  If payment is NOT enabled, then
	 * 				obviously automatic payment is not.  
	 */
	action calculatePaymentEnabled [
		bLocalPaymentEnabled = 'false'
		bLocalAutomaticPaymentEnabled = 'false'
		switch srGetStatusResult.paymentEnabled [
			case disableDQ		SetPaymentEnabledTrueAndAutomaticFalse
			case enabled 		    setPaymentEnabledTrueAndAutomaticTrue
			default 						setStatusGroupVariables
		]
	]
		
	
	/**
	 * A13. System sets payment enabled to 'true'. System sets automatic payment 
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
	 * A14.  System sets payment enabled to 'true'. System sets automatic payment 
	 * 				as false for now (variable is temporarily acting as a proxy for !statusDQ).  
	 *					System will check the automatic payment status information AFTER it 
	 *					gets the current 	balance (much further down this use case)  to determine
	 *			    	if there's an overriding factor that would prevent this user from having 
	 * 				automatic payment enabled.
	 */
	action SetPaymentEnabledTrueAndAutomaticFalse [
		bLocalPaymentEnabled = 'true'
		bLocalAutomaticPaymentEnabled = 'false'
		goto (setStatusGroupVariables)
		
	]
	
 	/**
 	 *	A15. System assigns the status information determined above to the parameters
 	 *			 		 sent down 	to the bill overview template which is named "paymentSummary.ftl"
 	 * 			 	 because originally it was a core part of Smart Pay.
 	 * 
 	 * 		Note that there are two variables that we can't set at this point. First is if
 	 * 				the automatic payment link in the template should be enabled, second is
 	 * 				the current amount due.  This happens after retrieve the bill infomration
 	 * 	   			and calculating the current balance. 
 	 */ 
	action setStatusGroupVariables [

		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "accountStatus",  "string", sLocalAccountStatus)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "bPayEnabled", "boolean", bLocalPaymentEnabled)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "paymentEnabled", "string", srGetStatusResult.paymentEnabled)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "achEnabled",     "string", srGetStatusResult.achEnabled)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "viewAccount",    "string", srGetStatusResult.viewAccount)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "accountBalance", "number", srGetStatusResult.accountBalance)

		goto (setNicknameGroupVariables)
	]

	/**
	 * A16. System assigns the nickname variable values to the template.
	  */
	action setNicknameGroupVariables [
		// -- values for nickname --
 		sDisplayAccountNickname = sAccountDisplay
 		sDisplayAccountNicknameUrl = ""
 		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "nickname",  "displayAccount", "string", sDisplayAccountNickname)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "nickname",  "url", "string", sDisplayAccountNicknameUrl)

		goto (doWeHaveBillsOrJustDocuments)
		
	]
	/**************************************************************************************
	 * END 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	
	/**************************************************************************************
	 * BEGIN BILLS OR DOCUMENTS IN THE SUMMARY?
	 ************************************************************************************** */
	
	/**
	 *	1.  System executes this section in preparation for a day when we summarize even document
	 *				information for a customer. The system needs to determine if this is one of 3 types of
	 *				display:
	 * 
	 * 		- Statement Summary -- 		for statement based account
	 * 		- Document Summary -- 		ok no bills, but documents
	 * 		- Invoice Account Summary --  for invoice based accounts 
	 * 
	 * 		System determines if this account has bills or documents. The distinction between 
	 * 			statements and invoices starts when the system determines how account balances are
	 *				calculated.
	 */
	action doWeHaveBillsOrJustDocuments [
		if "0" != sBillCount then
			areWeProcessingStatementsOrInvoices // -- off to get summary information --
		else
			doWeHaveAnyDocuments
	]
	
	
	/**
	 *  2. System has determined that this account does not have any bills. Does it have documents?
	 *
	 */
	action doWeHaveAnyDocuments [
		if "0" != sDocCount then
			setDocumentSummaryInfo
		else
			errorNoBillsOrDocuments
	]

	/**************************************************************************************
	 * END BILLS OR DOCUMENTS IN THE SUMMARY?
	 ************************************************************************************** */

	/**************************************************************************************
	 * BEGIN GET SUMMARY INFO FOR BILLS OR STATEMENTS
	 ************************************************************************************** */

	// -- NOTE THIS NEEDS TO COME BEFORE THE SYSTEM PERFORMS BALANCE CALCULATIONS
	//		BECAUSE THE AMOUNTDUE IS USED IN CURRENT BALANCE CALCULATIONS IF THE 
	//		CURRENT BALANCE IS BEING CALCULATED INTERNALLY --
	
	/**
	 * 3. System determins if this is an invoice based or statement based account.
	 */
	action areWeProcessingStatementsOrInvoices [
		switch sBillingType [
	 		case "invoice" 		setInvoiceSummaryInfo
	 		case "statement" 	setStatementOverviewInfo
	 		default 			errorBillingTypeConfiguration
		]
	]
	
	/**
	 *  4. System retrieves the statement overview iinformation 
	 */
	action setStatementOverviewInfo [		
		srBillOverviewParam.user = sUserId
		srBillOverviewParam.account = sAccount
		srBillOverviewParam.payGroup = sPayGroup 
		srBillOverviewParam.billDate = sBillDate
		srBillOverviewParam.isBill = "true"
		
	    switch apiCall Documents.BillOverview(srBillOverviewParam, srBillOverviewResult, srBillOverviewStatus) [
		   case apiSuccess getConfigurationForCurrentBalance
	       default errorStatementTemplate
	    ]			
	]
		
	/**
	 * 5. System retrieves Invoice based information. 
	 *		------------------------------------------------------------------------------------
 	 *		NOTE: TODO Write this to support invoices in short order.  
 	 *				It will be a template that simply shows how much you owe on
 	 *				the account, the number of open invoides and the date
 	 *				that the next invoice is due.
 	 * 		------------------------------------------------------------------------------------
	 */
	 action setInvoiceSummaryInfo [
	 	goto (getConfigurationForCurrentBalance)
	 ] 
  
	/**************************************************************************************
	 * END GET SUMMARY INFO FOR BILLS OR STATEMENTS
	 ************************************************************************************** */
	
	/**************************************************************************************
	 * BEGIN CURRENT BALANCE RELATED STEPS
	 ************************************************************************************** */
	/**
	 * 6. This next sequence of steps is targeted at getting the current balance for use
	 *		in the templates. Steps are:
	 * 
	 *			step 6 (this step) -- System calls to see if current balance is configured in system
	 * 				 admin settings. it then branches on that result.
	 * 		step 7 -- If System found a configuration in system admin settings, it uses that
	 *					information to decide where it will get the current balance information.
	 * 		step 8 -- Separate file feed - System retrieves current balance from
	 * 						PMT_ACCOUNT_BALANCE table.
	 *			step 9 -- Internal -  System needs to decide if this account  is invoice or statement based
	 *							since balance is calculated differently for each account type.
	 * 		step 10 -- statement based internal --  calculates balance from the statement
	 *						 and payment history
	 * 		step 11 -- invoice based internal -- calculates balance based on  all open
	 * 						 invoices and accounting for partial payments.
	 * 		step 12 -- web service call -- (to be implemented) system retrieves balance from a web 
	 *							 services call if it can, if not iit presents the last known value using 
	 *							 PMT_ACCOUNT_BALANCE table.
	 */
	action getConfigurationForCurrentBalance [		
		switch apiCall SystemConfig.GetCurrentBalanceConfig (srGetComm, srStatus) [
		    case apiSuccess processCurrentBalanceResults
		    default calculateCurrentBalanceInternallyForStatements
		]
	]
	
	/**
	 * 7. System determines where it will get the current balance information:
	 * 
	 * 			I - Internal calculation -- based on what WE know about payments including
	 * 					external payment history feed
	 * 			F - File -- based on a current balance file coming from the billing system
	 * 			R - Via a web service call to the billing system (future)
	 * 		   default -- no setting ... statement balance model internal.
	 */
	action processCurrentBalanceResults [		
	    switch srGetComm.RSP_CURBALTYPE [	    	
	    	case "I" internalCurrentBalanceStatementOrInvoice
	    	case "F" getCurrentBalanceFromFile
	    	case "R" getCurrentBalanceFromWebSvcCall	 
	    	default calculateCurrentBalanceInternallyForStatements
	    ]
	]  
	
	/* 8. BALANCE FROM FILE FEED -- System retrieves the current account balance from the 
	 * 		PMT_ACCOUNT_BALANCE table. This table is populated by a current balance file feed. 
	 */
	action getCurrentBalanceFromFile [
		UcPaymentAccountBalance.init(sUserId, sAccount, sPayGroup)		
		UcPaymentAccountBalance.getAmountEdit(sCurrentBalance) // balance placed in sCurrentBalance				
		goto(getScheduledPmtInfo)
	]
	
	/**
	 *  9. INTERNAL CALCULATION -- System determines if it is calculating based on invoice or statement 
	 *			based account.
	*
	 * 		------------------------------------------------------------------------------------------------------------
	 *			NOTE that 1st Franklin cacluation is done differently because we use the 
	 *			status feed information to help determine the current balance. This action
	 * 		branches to 1st Franklin specific calculations for statement mode.
	 *			------------------------------------------------------------------------------------------------------------
	 */
	action internalCurrentBalanceStatementOrInvoice [
		switch sBillingType [
	 		case "invoice" 		calculateCurrentBalanceInternallyforInvoices
	 		case "statement" 	calculateCurrentBalanceFor1stFranklin
	 		default 			errorBillingTypeConfiguration
		]
	]

	/* 10. INTERNAL CALCULATION FOR STATEMENTS -- System calculates the current balance using 
	 * 		the current statement less any payments we've seen in the payment history feed (which 
	 *			includes online) since the current statement was created.
	 */
	action calculateCurrentBalanceInternallyForStatements [
	  	UcPaymentAction.getDocumentCurrentBalance(
									sAccount,  			
									srBillOverviewResult.docNumber,         
									sPayGroup,         
									srBillOverviewResult.docAmount,         
									sCurrentBalance, 		// -- this is where the result is placed
									sCurrentBalanceFlag)	// -- this flag indicates if its valid				
		
		goto(getScheduledPmtInfo)
	]
	
	/* 
	 * 11. INTERNAL CALCULATION FOR INVOICES -- System calculates the current balance using 
	 *		the total of allopen invoices and their current open amount; important that we allow 
	 *		partial payment against these invoices.
	 * 
	 */
	 action calculateCurrentBalanceInternallyforInvoices [
	 	// TODO -- create something here... meanwhile..
	 	goto(errorInvoiceCurrentBalanceNotSupported)
	 ]
	
	/* 12. WEB SERVICES CALL -- System retrieves current balance from web services 
	 * 		call (real-time). 	If real-time fails, it retrieves it from the PMT_ACCOUNT_BALANCE
	 *			table which will contain the last known statement balance from a previous web
	 * 		svcs call.
	 */
	 action getCurrentBalanceFromWebSvcCall [
		// ------------------------------------------------------
		// TODO -- Implement this web services call
		// ------------------------------------------------------
		switch sBillingType [
	 		case "invoice" 		calculateCurrentBalanceInternallyforInvoices
	 		case "statement" 	calculateCurrentBalanceInternallyForStatements // -- do internal for now --
	 		default 			errorBillingTypeConfiguration
		]
	 ]

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
		srGetSchedPmtSummaryParam.PMT_DUEDATE = srBillOverviewResult.dueDate
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
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "oneTimePmtCount", "number", srGetSchedPmtSummaryResult.ONETIMEPMT_COUNT)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "oneTimePmtDate", "dateDb", srGetSchedPmtSummaryResult.ONETIMEPMT_DATE)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "oneTimePmtTotalAmt", "number", srGetSchedPmtSummaryResult.ONETIMEPMT_TOTALAMT)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "automaticPmtCount", "number", srGetSchedPmtSummaryResult.AUTOMATICPMT_COUNT)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "automaticPmtDate", "dateDb", srGetSchedPmtSummaryResult.AUTOMATICPMT_DATE)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "automaticPmtTotalAmt", "number", srGetSchedPmtSummaryResult.AUTOMATICPMT_TOTALAMT)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "scheduledPmtTotalAmt", "number", srGetSchedPmtSummaryResult.SCHEDULEDPMT_TOTALAMT)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, sScheduledPmtGroupName, "hasAutomaticPmtRule", "boolean", sHasAutomaticPaymentRule)
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
			case  disabled								setAutoPayDisabled										// -- cannot enroll for automatic payment
			case  disabledUntilCurrent  	checkIsAccountCurrent											// -- can enroll if they bring their account current
			case 	eligible									isThereAnAutomaticPayment					// -- can setup automatic payment so is there one?
			case  enrolled								isThereAnAutomaticPayment
			default											isThereAnAutomaticPayment
		]
	]

	/**
	 * C3. The system looks at automatic payment status to determine if the automatic
	 *						payment has been disabled. If it has, then we need to check their balance
	 *						before we can check their current atuomatic payment status. 
	 */
	action checkAutomaticPaymentStatusHasDQ [
		switch srGetStatusResult.automaticPaymentStatus [
			case  disabled								setAutoPayDisabled										// -- cannot enroll for automatic payment
			case  disabledUntilCurrent  	checkIsAccountCurrent								// -- can enroll if they bring their account current
			case 	eligible									checkIsAccountCurrent											
			case  enrolled								checkIsAccountCurrent
			default											checkIsAccountCurrent
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
			if "0" == srGetSchedPmtSummaryResult.AUTOMATICPMT_COUNT then
				setAutoPayEnabled
			else
				setAutoPayDisabled
	]
	
	/**
	 * C6. System enables automatic payment link.
	 */
	action setAutoPayEnabled [
		bLocalAutomaticPaymentEnabled = 'true'
		goto (setAutoPayLinkValueInTemplate)
	]
	
	/**
	 * C7. System disables automatic payment link. 
	 */
	action setAutoPayDisabled [
		bLocalAutomaticPaymentEnabled = 'false'
		goto (setAutoPayLinkValueInTemplate)
	]
	
	/**
	 * C8. System sets the automatic payment link enabled value in template  
	 */
	action setAutoPayLinkValueInTemplate [
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "bAutoPayLinkEnabled", "boolean", bLocalAutomaticPaymentEnabled)
		goto (shouldWeDisplayStatementOrInvoiceBased)
	]

	/**************************************************************************************
	 * END 1ST FRANKLIN SHOULD TEMPLATE SHOW THE AUTOMATIC PAYMENT LINK.
	 ************************************************************************************** */

	/**************************************************************************************
	 * BEGIN DISPLAY SUMMARY RELATED STEPS
	 ************************************************************************************** */
	 
	/**
	 * 15. System displays summary differently if we are statement
	 *		based or invoice based
	 */
	action shouldWeDisplayStatementOrInvoiceBased [
		switch sBillingType [
	 		case "invoice" 		showInvoiceOverview
	 		case "statement" 	setStatementInfoInTemplate
	 		default 			errorBillingTypeConfiguration
		]
	]
	
	/**
	 * 15a. System has collected all the bill/document information needed for the template, assign it. */
	action setStatementInfoInTemplate [
		FtlTemplate.addDocumentInfo(TemplateIdPaymentSummary,
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
    
 	/*
 	 * 17. Retrieve document information for document summary combined with status information
 	 * 		
	 *		------------------------------------------------------------------------------------
 	 *		NOTE: TODO we will separate this into a different routing other than BillOverview
 	 * 				so we can have a different template that shows document related and not bill
 	 * 				related information.
 	 * 		------------------------------------------------------------------------------------
 	 */
	action setDocumentSummaryInfo [
		srBillOverviewParam.user = sUserId
		srBillOverviewParam.account = sAccount
		srBillOverviewParam.payGroup = sPayGroup 
		srBillOverviewParam.billDate = sDocDate		
		srBillOverviewParam.isBill = "false"
		
	    switch apiCall Documents.BillOverview(srBillOverviewParam, srBillOverviewResult, srBillOverviewStatus) [
		   case apiSuccess setDocumentInfoInTemplate
	       default errorDocumentTemplate
	    ]			
	]

	/**
	 * 17a. The system has collected all the bill/document information needed for the template, assign it. */
	action setDocumentInfoInTemplate [
		FtlTemplate.addDocumentInfo(TemplateIdPaymentSummary,
						sPayGroup,
						sAccount,
						srBillOverviewResult.docDate,
						sCurrentBalance,
						previousAmt,
						srBillOverviewResult.result,
						sHasAutomaticPaymentRule)
		goto (showDocumentOverview)
	]
	/**
	 * 18. Show the document overview page 
	 *		
	 *		SEE NOTE IN STEP 13... THIS WILL CHANGE AS A RESULT 
	 *
	 */
	xsltScreen showDocumentOverview [
		
		display hPaymentSummary
	]	
	
	/**
	 * 19. Show the invoice summary template
	 *		
	 * 		SEE NOTE IN STEP 15 ... THIS WILL CHANGE TO A TEMPLATE AS A RESULT
	 **/
    xsltScreen showInvoiceOverview( "{Billing & Payment}") [
    	css: "css/lib/rzslider.min.css"
    	script: "js/lib/jquery.min.js"
		script: "js/lib/angular.min.js"
        script: "js/lib/angular-route.min.js"
        script: "js/lib/angular-sanitize.min.js"
        script: "js/lib/angular-animate.min.js"
        script: "js/lib/angular-messages.min.js"
        script: "js/lib/angular-bootstrap.js"
        script: "js/lib/angular-ui-tree.min.js"
        script: "js/lib/xregexp.js"
        script: "js/lib/angular-drag-and-drop-lists.min.js"
        script: "js/lib/bootstrap-toggle.min.js"
        script: "js/lib/jquery.dataTables.min.js"
        script: "js/lib/dataTables.bootstrap.min.js"
        script: "js/lib/angular-initial-value.min.js"
        script: "js/lib/angular-validate.min.js"
        script: "js/lib/moment.min.js"
        script: "js/lib/angular-moment.min.js"
        script: "js/lib/angular-spinners.min.js"
        script: "js/lib/zingchart/zingchart.min.js"
        script: "js/lib/zingchart/zingchart-angularjs.js"
        script: "js/lib/numeral/numeral.min.js"
        script: "js/lib/numeral/locales.min.js"
        script: "js/lib/select2.min.js"
        script: "js/lib/angular-translate.min.js"
        script: "js/lib/angular-translate-loader-partial.min.js"
        script: "js/lib/ng-scrollbar.min.js"
        script: "js/lib/angular-ui-notification.min.js"
        script: "js/lib/rzslider.min.js"
        script: "js/lib/ng-table.min.js"
    	
		script: "js/default.js"
		script: "js/userProfile.js"
		script: "js/billBusiness.js"
		script: "js/paymentBusiness.js"
		script: "js/utils.js"
		script: "js/hierarchy.js"
		attr_ng-app: "app"
		
		child utilImpersonationActive
		
		div content [
			class: "billingDashboard"
			attr_ng_controller: "GetI18nCtrl"
			attr_ng_init: "load('accountOverview')"
			
			div main[
				attr_ng_controller: "BDHierarchyPageCtrl"
				attr_ng_init: "init()"
				div billDashboardMenuWrapper [
					class: "mainNav"
					attr_ng_include: "'html/billingConsumer/menu.html'"
					display empty
				]

				div manageBillsWrapper [
					class: "billingDashboardPage"
					attr_ng_show: "!servicesBillingPage.usage"
					attr_ng_include: "'html/billingConsumer/billingDashboard.html'"
					attr_ng_hide: "accountsBillingPage.change || servicesBillingPage.change"
					display empty
				]

				div billingUsageWrapper [
					class: "billingUsagePage"
					attr_ng_if: "accountsBillingPage.change"
					attr_ng_include: "'html/billingConsumer/usage/billingUsage.html'"
					display empty
				]

				div usageWrapper [
					class: "billingUsagePage"
					attr_ng_if: "servicesBillingPage.change"
					attr_ng_include: "'html/billingConsumer/usage/usage.html'"
					display empty
				]
			]
		]
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
     * 1e. Error setting up document template
     */
    action errorDocumentTemplate [ 
    	oMsgErrorTemplate.sBody = sErrorBase + "{Document template rendering failure.}"
        displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
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

	action errorNoBills [
    	oMsgErrorTemplate.sBody = sErrorBase + "{No bills for account.}"
        displayMessage(type: "danger" msg: oMsgErrorTemplate)     
		goto (paymentErrorScreen)
	]
     
	/*
	 * 3e. Billing Type Configuration not configured
	 */
    action errorBillingTypeConfiguration [
    	oMsgErrorTemplate.sBody = sErrorBase + "{Billing type is not configured.}"
        displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
    ]
   
    /*
     * 4e. No bills or documents in this account
     */
    action errorNoBillsOrDocuments [
    	oMsgErrorTemplate.sBody = sErrorBase + "{No bills or documents for account.}"
        displayMessage(type: "danger" msg: oMsgErrorTemplate)     
        goto (paymentErrorScreen)        
    ]
    
    /*
     * 5e. We haven't written current balance internal code
     *		for invoices yet
     */
    action errorInvoiceCurrentBalanceNotSupported [
    	oMsgErrorTemplate.sBody = sErrorBase + "{Internal current balance not supported for invoice based systems.}"
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