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
    *        1.0 07-Feb-2017 First Version Coded [Maybelle Johnsy Kanjirapallil]
    * 		 2.0 2023-Nov-24 	JAK	Updated for 1st Franklin and future use.
    * 			 2023-Dec-11	JAK Updated to handle no bills situations.
    * 		 2.1 2024-Jan-04	JAK	Updated to use new template builder.
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
            1. Primary -- User summary account information is displayed
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
	native string sAccount			// -- account id used internally
	native string sAccountDisplay	// -- account id shown on screens
	native string sPayGroup			// -- payment group
	native string sBillDate			// -- date of most recent bill or "" if none
	native string sBillCount		// -- number of bills available for this account
	native string sDocDate			// -- date of more recent document or "" if none
	native string sDocCount			// -- number of documents available for this account
	
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
    native string sCurrentBalance
    
    native string sLocalAccountStatus = "enabled" // -- this is work around to a defect in persona.  API return structures appear to be
    											  // 		immutable even though we can "assign a new value"... it seems to screw it up.

	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/

	/**************************************************************************************
	 * BEGIN 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	 
	/**
	 *	1a. Initialize this use case by checking for actor updates and then retrieving 
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
 	 * System uses this trampoline function that should not be needed... System assigns a local variable value
 	 *	from the status result allowing it to transition the result later if the system discovers a bill for a
 	 *	newAccount. This is a workaround for a Persona defect which makes results immutable.
 	 */
 	action assignlocalStatusVariable [
 		sLocalAccountStatus = srGetStatusResult.accountStatus
 		goto (switchOnViewStatus)
 	]
 	
 	/**
 	 * 2a. If we can't view this bill then we're going to use the NoBillTemplate... and it will
 	 *		have viewAccount disabled so get that sorted */
 	action switchOnViewStatus [
 		switch srGetStatusResult.viewAccount [
 			case "enabled" 	switchOnAccountStatus
 			default			setNoBillTemplateData
 		]
 	]
 
 	/**
 	 * 3a. A new or closed account is another reason for the no bill template
 	 */	
 	action switchOnAccountStatus [
 		switch srGetStatusResult.accountStatus [
 			case "activeAccount"	checkActiveAccountNoBills
 			case "newAccount" 		checkForTransition
 			default					setNoBillTemplateData
 		]
 	]
 	
 	/**
 	 * 4a. Check to see if a new account has a bill and needs to
 	 *		transition to an active account
 	 */
 	action checkForTransition [
	 	if "0" == sBillCount then
	 		setNoBillTemplateData
	 	else
	 		transitionNewAccount
	]
	
	/**
	 * 5a. Transition a new account to an active account
	 */
	action transitionNewAccount [
		sLocalAccountStatus = "activeAccount"
		goto(oKToAssignStatusToBillTemplate)
	]

	/**
	 * 6a. If an active account doesn't have bills, we still
	 * 			use the no bill template, it shows a message
	 * 			telling the user to contact their branch.
	 */
	action checkActiveAccountNoBills [
		if "0" == sBillCount then
			setNoBillTemplateData
		else
			oKToAssignStatusToBillTemplate
	]

 	/**
 	 * 7a. If we are going to show the no bill template, set status information
 	 *		and display account in the template
 	 */
  	action setNoBillTemplateData [
 		// -- values for new account --
		FtlTemplate.setItemValue(TemplateIdNoBills, "status",
					"viewAccount", "string", srGetStatusResult.viewAccount)
		FtlTemplate.setItemValue(TemplateIdNoBills, "status",
					"accountStatus", "string", sLocalAccountStatus)
		FtlTemplate.setItemValue(TemplateIdNoBills, "root",
					"displayAccount", "string", sAccountDisplay)
		FtlTemplate.setItemValue(TemplateIdNoBills, "root",
					"jumpToOffset", "string", AccountOffset)
		
		goto (showNoBillTemplate)
 	]	
 
 	/**
 	 * 8a. Show the no bill template
 	 */
 	xsltScreen showNoBillTemplate [
 		display hDontShowBills
 	]
 	
 	/**
 	 *	9a. We're using the paymentSummary template now... the rest assumes that's
 	 *		the case. Clear the group elements just in case.
 	 */
 	action oKToAssignStatusToBillTemplate [
  		goto(setStatusGroupVariables)
	]
	
 	/**
 	 *	10a. System assigns the status information to the parameters sent down
 	 * 		to the bill overview template which is oddly named "payment.ftl"... I don't
 	 * 		know why.
 	 */ 
	action setStatusGroupVariables [

		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "accountStatus",  "string", sLocalAccountStatus)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "paymentEnabled", "string", srGetStatusResult.paymentEnabled)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "achEnabled",     "string", srGetStatusResult.achEnabled)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "viewAccount",    "string", srGetStatusResult.viewAccount)
		FtlTemplate.setItemValue(TemplateIdPaymentSummary, "status", "accountBalance", "number", srGetStatusResult.accountBalance)
		goto (doWeHaveBillsOrJustDocuments)
	]

	/**************************************************************************************
	 * END 1ST FRANKLIN SPECIFIC
	 ************************************************************************************** */
	
	/**************************************************************************************
	 * BEGIN BILLS OR DOCUMENTS IN THE SUMMARY?
	 ************************************************************************************** */
	
	/**
	 *	1. Some of this is preparing for a day when we summarize even document
	 *		information for a customer. We need to determine if this is once
	 *		of three kinds of displays:
	 * 
	 * 		Statement Summary -- 		for statement based account
	 * 		Document Summary -- 		ok no bills, but documents
	 * 		Invoice Account Summary --  for invoice based accounts 
	 * 
	 * 		The first part is just deciding if its bills or documents. The
	 * 		distinction between statements and invoices starts when we determine
	 * 		how account balances are calculated.
	 */
	action doWeHaveBillsOrJustDocuments [
		if "0" != sBillCount then
			areWeProcessingStatementsOrInvoices // -- off to get summary information --
		else
			doWeHaveAnyDocuments
	]
	
	
	/**
	 *  2. We only have documents, do we have any?
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

	// -- NOTE THIS NEEDS TO COME BEFORE THE BALANCE CALCULATION STUFF BECAUSE THE
	//		AMOUNTDUE IS USED IN CURRENT BALANCE CALCULATIONS IF THE CURRENT BALANCE
	//		IS BEING CALCULATED INTERNALLY --
	
	/**
	 * 3. We process account summary differently if we are statement
	 *		based or invoice based
	 */
	action areWeProcessingStatementsOrInvoices [
		switch sBillingType [
	 		case "invoice" 		setInvoiceSummaryInfo
	 		case "statement" 	setStatementOverviewInfo
	 		default 			errorBillingTypeConfiguration
		]
	]
	
	/**
	 * 	4. Retrieve the statement template with combination of bill information, status
	 *		 information, and payment summary information
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
	 * 5. Retrieve Invoice based account summary template and add 
	 *		the information needed to show it
	 *		------------------------------------------------------------------------------------
 	 *		NOTE: TODO we will write this to support invoices in short order.  It will be
 	 * 			a template that simply shows how much you owe on the account, the number of
 	 * 			open invoides and the date that the next invoice is due.
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
	 *			step 6 (this step) -- calls to see if current balance is configured
	 * 				in the system. it then branches on that configuration.
	 * 			step 7 -- decides based on return from step 3, where we'll get the
	 * 				current balance information from
	 * 			step 8 -- finds current balance if it came from a file load and stored
	 * 						in PMT_ACCOUNT_BALANCE table
	 *			step 9 -- for internal calculations, statement and invoice based balances
	 *						are calculated differently so switch on which mode we are in
	 * 			step 10 -- statement based internal calculates balance from the statement
	 *						 and payment history
	 * 			step 11 -- invoice balance based on internal calculation of all open
	 * 						 invoices and accounting for partial payments.
	 * 			step 12 -- gets it from a web services call (real-time) if it can, if not
	 * 						it presents the last known value (using PMT_ACCOUNT_BALANCE)
	 */
	action getConfigurationForCurrentBalance [		
		switch apiCall SystemConfig.GetCurrentBalanceConfig (srGetComm, srStatus) [
		    case apiSuccess processCurrentBalanceResults
		    default calculateCurrentBalanceInternallyForStatements
		]
	]
	
	/**
	 * 7. Decide where we will get the current balance from:
	 * 
	 * 			I - Internal calculation -- based on what WE know about payments including
	 * 					external payment history feed
	 * 			F - File -- based on a current balance file coming from the billing system
	 * 			R - Via a web service call to the billing system (future)
	 */
	action processCurrentBalanceResults [		
	    switch srGetComm.RSP_CURBALTYPE [	    	
	    	case "I" internalCurrentBalanceStatementOrInvoice
	    	case "F" getCurrentBalanceFromFile
	    	case "R" getCurrentBalanceFromWebSvcCall	 
	    	default calculateCurrentBalanceInternallyForStatements
	    ]
	]  
	
	/* 8. BALANCE FROM FILE FEED --Gets the current account balance from the PMT_ACCOUNT_BALANCE table.
	 * 		This table is populated by a current balance file feed 
	 */
	action getCurrentBalanceFromFile [
		UcPaymentAccountBalance.init(sUserId, sAccount, sPayGroup)		
		UcPaymentAccountBalance.getAmountEdit(sCurrentBalance) // balance placed in sCurrentBalance				
		goto(getScheduledPmtInfo)
	]
	
	/**
	 *  9. INTERNAL CALCULATION -- We do internal balance calculation differently if we are statement
	 *		based or invoice based
	 */
	action internalCurrentBalanceStatementOrInvoice [
		switch sBillingType [
	 		case "invoice" 		calculateCurrentBalanceInternallyforInvoices
	 		case "statement" 	calculateCurrentBalanceFor1stFranklin
	 		default 			errorBillingTypeConfiguration
		]
	]

	/* 10. INTERNAL CALCULATION FOR STATEMENTS -- Calculates the current balance using the current
	 * 		statement less any payments we've seen in the payment history feed (which includes online)
	 *		since the current statement was created.
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
	
	/**
	 * 10.A  substitute calculation for 1st Franklin on current balance
	 */
	action calculateCurrentBalanceFor1stFranklin [
		CurrentBalanceHelper.getCurrentBalanceRaw(
										sPayGroup,								// -- payment group
										sAccount,								// -- internal account
										srBillOverviewResult.docDate,			// -- most recent bill date
										srBillOverviewResult.totalDue,			// -- bill amount due
										srGetStatusResult.statusDate,			// -- most recent status date
										srGetStatusResult.currentAmountDue		// -- status amount due
										sCurrentBalance )						// -- current balance for display
		goto(getScheduledPmtInfo)
	]
	
	/* 
	 * 11. INTERNAL CALCULATION FOR INVOICES -- Calculates the current balance using the total of all
	 *		open invoices and their current open amount; important if we allow partial payment
	 *		against and invoice.
	 * 
	 */
	 action calculateCurrentBalanceInternallyforInvoices [
	 	// TODO -- create something here... meanwhile..
	 	goto(errorInvoiceCurrentBalanceNotSupported)
	 ]
	
	/* 12. WEB SERVICES CALL -- Get current balance from web services call (real-time).
	 * 		If real-time fails, it retrieves it from the PMT_ACCOUNT_BALANCE table which 
	 *		will contain the last known statement balance from real-time.
	 */
	 action getCurrentBalanceFromWebSvcCall [
		// ------------------------------------------------------
		// TODO -- Add this call for 1st Franklin (and others)
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
		goto(shouldWeDisplayStatementOrInvoiceBased)
	]

	/**************************************************************************************
	 * END GET SCHEDULED AND RECURRING PAYMENT INFORMATION
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