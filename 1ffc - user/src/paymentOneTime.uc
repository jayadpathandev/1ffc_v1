useCase paymentOneTime [
   /**
    *  author: Maybelle Johnsy Kanjirapallil
    *  created: 19-Apr-2016
    *
    *  Primary Goal:
    *       1. Display payment details.
    *       
    *  Alternative Outcomes:
    *       1. None
    *                     
    *   Major Versions:
    *        1.0 19-Apr-2016 First Version Coded [Maybelle Johnsy Kanjirapallil]
    *        2.0 25-Sept-2019 Support Sepa
    * 		 --------
    * 		 1st Franklin changes
    * 		---------
    * 		1F-1	2024-Feb-2	jak	First full version with changes to reflect 1st Franklin Rules
    * 		1F-2	2024-Feb-07	jak	Lots of changes and restructuring to handle min/max due for 1st Franklin
    */

    documentation [
        preConditions: [[
            1. User is logged in.
        ]]
        triggers: [[
            1. The user clicks the sub menu option OneTimePayments in the Payments page.
        ]]
        postConditions: [[
            1. Primary -- One time payment details are displayed.
        ]]
    ]
    actors [ 
        create_payment
        
    ]
    
    startAt init
    
    /* Returns the url needed to open an add source iframe. */
    shortcut getOnetimePaymentIframeUrl(iframeCreateResponse) [
    	source_type
    ]

    shortcut getOnetimePaymentIframeUrlForEdit(getEditIframeUrl) [
    	sPaymentSourceId
    ]
    
    /* Reset the source type when using the existing payment method */
    shortcut getOnetimePaymentResetSourceType(iframeResetSourceType)     
    
    /* Returns the wallet information for a token. */
    shortcut internal getWalletInfo(getWalletInfo) [
    	token
    ]
    
    shortcut createIframeAddSourceOnetime(createIframeAddSourceOnetime) [
    	sUserId,
    	sUserName,
    	sIframeSourceType,
    	sIframeOnetime
    ]
    
    shortcut createIframeEditSourceOnetime(getWalletInfoForEdit)
    
    /* Submits the form data and executes the payment transaction. */
    shortcut submitOnetimePayment(validatePaymentRequest)
    
    shortcut addSourceSuccess(addSourceSuccessResponse)
    shortcut addSourceError(addSourceErrorResponse) [
    	sErrorMessageType
    ]
    
    shortcut makePaymentError(makePaymentErrorResponse) [
    	sErrorMessageType
    ]
    
    /**
     * Takes a POST json string containing list of bills
     * to be set up for the one-tie payment flow.
     */
    shortcut internal setBillsForPayment(setBillsForPayment)
    shortcut internal getBillsForPayment(getBillsForPayment)
    shortcut internal unsetBillsForPayment(unsetBillsForPayment)
    
    child utilImpersonationActive(utilImpersonationActive)
    
    /*************************
	* DATA ITEMS SECTION
	*************************/ 		
	importJava UcPaymentAction(com.sorrisotech.fffc.user.FffcPaymentAction)    
    importJava AppConfig(com.sorrisotech.utils.AppConfig)
    importJava Session(com.sorrisotech.app.utils.Session)
    importJava SurchargeSession(com.sorrisotech.uc.payment.PaymentSurchargeSession)
    importJava I18n(com.sorrisotech.app.common.utils.I18n)
    importJava TransactionIdGen(com.sorrisotech.svcs.payment.util.RequestTransactionIdUtil)    
    importJava NotifUtil(com.sorrisotech.common.app.NotifUtil)
    importJava ForeignProcessor(com.sorrisotech.app.common.ForeignProcessor)
    importJava Spinner(com.sorrisotech.app.utils.Spinner)
    importJava Format(com.sorrisotech.common.app.Format)
    importJava PaymentAuditIterator(com.sorrisotech.uc.payment.PaymentAuditIterator)
    importJava OneTimePaymentHelperAction(com.sorrisotech.uc.payment.OneTimePaymentHelperAction)
	importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)
	// -- specific to 1st Franklin ... helps calculate current balance --
	importJava CurrentBalanceHelper (com.sorrisotech.fffc.payment.BalanceHelper)
	importJava FlexFieldInformation (com.sorrisotech.fffc.user.FlexFieldInformation)    
            
    import validation.dateValidation
            
    import billCommon.sPayAccountInternal
    import billCommon.sPayAccountExternal
    import billCommon.sPayGroup
    import billCommon.sPaySelectedDate
    import billCommon.sPayIsBill
    
    import paymentCommon.sCurrency
    import paymentCommon.sScheduledDateWindow
    import paymentCommon.sNumSources
    import paymentCommon.sMaxSources
    import paymentCommon.sPmtGroupConfigResult
    import paymentCommon.sPaymentSourceBankEnabled
    import paymentCommon.sPaymentSourceCreditEnabled
    import paymentCommon.sPaymentSourceDebitEnabled
    import paymentCommon.sPaymentSourceSepaEnabled
    import paymentCommon.sCurrencySymbol
    import paymentCommon.sAppType
    import apiPayment.pmtRequest			    	
	import apiPayment.makePaymentUrl	       
    	
	serviceStatus ssStatus
		
	serviceParam (Payment.GetWalletByToken) srGetWalletInfoParam
	serviceResult (Payment.GetWalletByToken) srGetWalletInfoResult
		
	serviceParam (Payment.MakePayment) srMakePaymentParam
	serviceResult (Payment.MakePayment) srMakePaymentResult
	
	serviceParam(Payment.StartPaymentTransaction) srStartPaymentTransactionParam
	serviceResult(Payment.StartPaymentTransaction) srStartPaymentTransactionResult
	
	serviceParam(Payment.SetScheduledPayment) srSetScheduledParam
	serviceResult(Payment.SetScheduledPayment) srSetScheduledResult
    	
	serviceParam (Payment.GetWalletCount) srGetWalletCountParam
	serviceResult (Payment.GetWalletCount) srGetWalletCountResult
	
    serviceStatus srBillOverviewStatus
    serviceParam(Documents.BillOverview) 	srBillOverviewParam
    serviceResult(Documents.BillOverview) 	srBillOverviewResult

    // -- 1st Franklin Specific GetStatus returns the status for all major conditions and business
    //		drivers --
    serviceStatus srAccountStatusCode
    serviceParam (AccountStatus.GetStatus) srGetStatusParams
    serviceResult (AccountStatus.GetStatus) srGetStatusResult
    serviceParam  (AccountStatus.IsMinimumPaymentRequired) srGetMinimumParams
    serviceResult (AccountStatus.IsMinimumPaymentRequired) srGetMinimumResult
    serviceStatus srGetMinimumCode
    native string sLocalAccountStatusDate
    native string sLocalAccountStatusAmount
    native string sLocalAccountBillDate
    native string sLocalAccountBillAmount
	
	string msgStatusError = "Internal System Error, please try again later."
    
    native string sFlexFieldForMax = "16"
    native string sErrorValueReturned = "--"
    
	// -- for now, system takes the max due from where 1st Franklin files placed
	//		it. It will eventually be take from status feed and need to change at that time --
    volatile native string sLocalBillMaxPay = 
    		FlexFieldInformation.getFlexFieldRaw(
 			sUserId, 							  // -- user id
			srBillOverviewParam.account, 		  // -- account number
			srBillOverviewParam.billDate, 		  // -- bill date
			srBillOverviewParam.payGroup,		  // -- payment group
			sFlexFieldForMax,					  // -- flex field nmber
			sErrorValueReturned )				  // -- what returned if error
	
	// -- used in setting the sMinDueEdit variable --		
 	volatile native string sMinDue = 
 			CurrentBalanceHelper.getTrueMinimumDueRaw (
		       srGetMinimumResult.sAmountRequired, // -- minimum due from status
			   sCurrentBalance )				   // -- calculated current balance	
			   									   // -- where minimum due is returned

	// -- use din setting the sMaxDueEdit variable --
	volatile native string sMaxPay = 
			CurrentBalanceHelper.getTrueMaximumPayRaw (
				srBillOverviewParam.payGroup, 	// -- payment group
				srBillOverviewParam.account,	// -- account
				sLocalAccountBillDate,			// -- published date of bill
				sLocalBillMaxPay
			)

	// -- returns a current balance calculated based on either bill or status (whichever is newer) less
	//		payments since that last bill or status date (date inclusive) --
	volatile string sTotalDueDisplay = 
			CurrentBalanceHelper.getCurrentBalanceFormattedAsCurrency (
				srBillOverviewParam.payGroup, 	// -- payment group
				srBillOverviewParam.account,	// -- account
				sLocalAccountBillDate,			// -- published date of bill
				sLocalAccountBillAmount,		// -- amount in bill
				sLocalAccountStatusDate,		// -- published date of acct status
				sLocalAccountStatusAmount )	 	// -- amount in acct status
	volatile native string sCurrentBalance
	
	// -- returns true if current due is <=0, otherwise false --
	volatile string bIsAccountCurrent = 
			CurrentBalanceHelper.isAccountCurrent (
				srBillOverviewParam.payGroup, 	// -- payment group
				srBillOverviewParam.account,	// -- account
				sLocalAccountBillDate,			// -- published date of bill
				sLocalAccountBillAmount,		// -- amount in bill
				sLocalAccountStatusDate,		// -- published date of acct status
				sLocalAccountStatusAmount )	 	// -- amount in acct status
 
 	// -- sets the minimum due used in display on the screen --
 	volatile native string sMinDueDisplay  = 
 			CurrentBalanceHelper.getTrueMinimumDueFormattedAsCurrency (
				srBillOverviewParam.payGroup, 	// -- payment group
				sMinDue,						// -- minimum due from status or bill
				sCurrentBalance)			// -- current balance calculated based on bill/status and payments
				
	// -- sets the maximum payment amount used in display on screen --
	volatile native string sMaxDueDisplay  = 
			CurrentBalanceHelper.getTrueMaximumPayFormattedAsCurrency (
				srBillOverviewParam.payGroup, 	// -- payment group
				srBillOverviewParam.account,	// -- account
				sLocalAccountBillDate,			// -- published date of bill
				sLocalBillMaxPay)				// -- amount in bill
		
	volatile native string sCurrentBalanceEdit =
			CurrentBalanceHelper.getCurrentBalanceRaw (
				srBillOverviewParam.payGroup, 		// -- payment group
				srBillOverviewParam.account,		// -- account
				sLocalAccountBillDate,				// -- published date of bill
				sLocalAccountBillAmount,			// -- amount due in bill
				sLocalAccountStatusDate,			// -- published date of acct status
				sLocalAccountStatusAmount )			// -- current amt due in status
	
    
	volatile native string sAuditIteratorHasNext = PaymentAuditIterator.hasNext()
	volatile native string sAuditIteratorNext = PaymentAuditIterator.next()  
	native string sCompanyId = Session.getUserCompanyId()
	native string sGroupingDataString = UcPaymentAction.getGroupingJsonAsString(sPayData)
	
	//flag indicating whether user has selected bills to pay from dashboard
	native string sHasBillsSelectedForPayment = OneTimePaymentHelperAction.hasBillsForPayment()
	
	input sPayData
	native string sPayDataSourceName    = UcPaymentAction.sourceName(sPayData)
	native string sPayDataSourceAccount = UcPaymentAction.sourceAccount(sPayData)
	native string sPayDataSourceType    = UcPaymentAction.sourceType(sPayData)
	     	
	input sTotalPayAmt
	input sSurCharge = ""
	input sTotalAmount = ""     // pay amount + surcharge
	
	string sDocumentNum

	/* "single" or "multiple" indicator */
	string selectedBillsPlurality
	
	native string sNumberOfBills = OneTimePaymentHelperAction.getNumberOfSelectedBills()
	static sNumberOfBillsDisplayText = "{<1> bills selected}"
	volatile string sNumberOfBillsDisplayLabel = I18n.translate ("paymentOneTime_sNumberOfBillsDisplayText", sNumberOfBills)
	
    static sCurrentBalText = "{Current amount due}"
    static sStatementBalText = "{Bill balance}"
    static sMinimumText = "{Minimum payment}"
    static sOther = "{Other amount}"
    static sExpired = "{(expired)}"
    static account = "{account}"
    static invoice = "{bill}"
    static document = "{document}"
    
	static unsavedName  = "{Unsaved}"
	static type_bank    = "{BANK ACCOUNT}"
	static type_credit  = "{CREDIT CARD}"
	static type_debit   = "{DEBIT CARD}"
	static type_sepa    = "{SEPA ACCOUNT}"
	static type_unsaved = "{UNSAVED METHOD}"
	
    static msgAutoScheduled_body = "{There is an recurring payment set up for this <1>. Recurring payments pay bills on your behalf, but you can choose to proceed with this manual payment if you choose. Recurring payments will then just pay whatever balance remains. If you want to view, edit or cancel the recurring payment, click [a href='<2>']here[/a]}"
    static msgPmtScheduled_body = "{There is a future-scheduled payment configured for this <1>. These amounts should already have been taken into account, but be sure to only pay the 'Current balance' for bills to avoid over-paying. If you want to view or cancel the upcoming payments, click [a href='<2>']here[/a]}"
    
    static addSourceGenericError = "{Failed to add payment source.}"
    static addSourceCardNumberError = "{Failed to add payment source. The card number is invalid.}"
    static addSourceAddressError = "{Failed to add payment source. The address is invalid or does not match your billing address.}"
    static addSourceCvvError = "{Failed to add payment source. CVV code invalid or does not match your card.}"
    static addSourceCardExpiredError = "{Failed to add payment source. The card is expired. }"
    static addSourceLostStolenFraudError = "{Failed to add payment source. The card has been reported lost, stolen, or fraud has been detected.}"
    static addSourceInvalidMethodError = "{Failed to add payment source. The card type is not supported.}"
    static editSourceGenericError = "{Failed to update payment source.}"
    static editSourceCardNumberError = "{Failed to update payment source. The card number is invalid.}"
    static editSourceAddressError = "{Failed to update payment source. The address is invalid or does not match your billing address.}"
    static editSourceCvvError = "{Failed to update payment source. CVV code invalid or does not match your card.}"
    static editSourceCardExpiredError = "{Failed to update payment source. The card is expired. }"
    static editSourceLostStolenFraudError = "{Failed to update payment source. The card has been reported lost, stolen, or fraud has been detected.}"
    static editSourceInvalidMethodError = "{Failed to update payment source. The card type is not supported.}"
    
    static paymentGenericError = "{Failed to make payment.}"
    static paymentDateManipulated = "{Failed to make payment, payment date out of acceptable range.}"
    static paymentInvalidAmountError = "{Failed to make payment. The amount is invalid.}"
    static paymentInsufficientFundsError = "{Failed to make payment. There are insufficient funds on your account.}"

    native string sDueDate                 = Format.formatDateNumeric(srBillOverviewResult.dueDate)
	native string sCurrentBalanceDisplay   = ''
	native string sCurrentBalanceFlag      = ''
	native string sStatementBalanceEdit    = Format.formatAmountForEdit(srBillOverviewResult.totalDue)
	native string sStatementBalanceDisplay = LocalizedFormat.formatAmount(sPayGroup, srBillOverviewResult.totalDue)	
	
	native string sMinAmountDueEdit		// -- value passed to screen
	native string sMaxAmountDueEdit		// -- value passed to screen
    native string sUserId = Session.getUserId()
    native string sMostRecentDocsResult = ""
            
    string sPaymentSummaryHeader = "{Payment summary}"
	string sAccountIdLabel = "{Account ID}"
	
	static sPaymentDateText = "{Payment date (Due <1>)}"
	volatile string sPaymentDateLabel = I18n.translate ("paymentOneTime_sPaymentDateText", sDueDate)
	
	// -- data for constructing heading for payment amount due column --
	static sPayAmountText1 = "{Pay amount (Due}"
	native string sPayAmountText2 = ")"		
	volatile string sPayAmountNewText1 = I18n.translate ("paymentOneTime_sPayAmountText1")
	native string sDisplayAmt = UcPaymentAction.formatAmtText(sCurrentBalance, sPayGroup, "onetime")
	native string sPayAmountLabel
	
	// -- data for constructing heading for other amount column --
	static sOtherAmountText1 = "{Min}"
	static sOtherAmountText2 = "{, Max }"
	volatile string sOtherAmountText1Localized = I18n.translate ("paymentOneTime_sOtherAmountText1")
	volatile string sOtherAmountText2Localized = I18n.translate ("paymentOneTime_sOtherAmountText2")
	native string sOtherAmountLabel
	
	string sAccountIdCompleteLabel = "{PAYMENT FOR}"
	string sPaymentDateCompleteLabel = "{PAYMENT DATE}"
	string sOtherAmountCompleteLabel = "{PAY AMOUNT}"
	string sEstimatedProcessingDateLabel = "{ESTIMATED PROCESSING DATE}"
	string sPaymentRequestReceivedLabel = "{PAYMENT REQUEST RECEIVED}"
	string sPaymentSurChargeLabel = "{SURCHARGE}"
	string sPaymentConvenienceFeeLabel = "{CONVENIENCE FEE}"
	string sPaymentTotalAmountLabel = "{TOTAL AMOUNT}"
    string sPaymentMethodHeader = "{Payment method}"
    string sConfirmPaymentHeader = "{Confirm payment}"
    string sPaymentSuccessHeader = "{Payment confirmed}"
    string sPaymentSuccessText = "{Thank you for your payment!}"
    string sAdditionalChargeInfo = "{Additional charges may apply if you do not pay the current amount due by the due date.}"
	string sMaxPaymentMethodsReached = "{The maximum number of payment methods have already been added to your wallet. Please remove some to add more.}"
	string sAutomaticPaymentsHeader = "{Activate recurring payments for this account}"
	string sAutomaticPaymentsBody = "{Save time and energy by scheduling repeating payments to occur automatically on a specific date, every month.}"
	string sHeaderEdit = "{edit}"
	string fAutoScheduledConfirm_body = "{I acknowledge that there is an automatic payment set up for this account. Making one time payment will override the automatic payment.}"
	
	string sPaySourceEdit = "{edit}"    
	   
    native string sPaymentMethodNickName = ""
    persistent string sPaymentMethodType = ""
    persistent string sPaymentMethodAccount = ""
    string sSourceExpiry = ""
    native string sPaymentDateComplete = ""
    native string sEstimatedProcessingDate = ""
    native string sPaymentRequestReceived = ""
    native string sAmountComplete = ""   
    native string sTotalAmountComplete = ""
    native string sSurchargeAmountComplete = ""
     
    native string sNtfParams = ""
    native string sDummy = ""
    native string sMinDummy = ""
    native string sAutomaticPaymentShortcut = ""
    native string sStepNumber1 = "1"
    native string sStepNumber2 = "2"
    native string sStepNumber3 = "3"
    native string sWalletCreateSuccessEmailFlag     = NotifUtil.isNotificationEnabled(sUserId, "payment_wallet_create_success")
    native string sOneTimeScheduledSuccessEmailFlag = NotifUtil.isNotificationEnabled(sUserId, "payment_onetime_scheduled_success")
    native string sMakePaymentSuccessEmailFlag      = NotifUtil.isNotificationEnabled(sUserId, "payment_make_payment_success")
    native string sPaymentDate
    persistent native string sAutoScheduledFlag = UcPaymentAction.getAutoScheduledFlag(sUserId, sPayAccountInternal)
    persistent native string sPmtScheduledFlag = UcPaymentAction.getPaymentScheduledFlag(sUserId,sPayAccountInternal,srBillOverviewResult.docNumber,"onetime")
    native string sTodaysDate = UcPaymentAction.getTodaysDate()
    native string surchargeFlag = UcPaymentAction.getSurchargeStatus()
    native string sFlexfield = UcPaymentAction.getFlexField()
    native string sBillingType = UcPaymentAction.getBillingBalanceType()
    
    native string flexDefinition = AppConfig.get("1ffc.flex.definition")
    
    persistent native string source_type       = ""
    
    native string sPayAmt = ""
     
    persistent native string sUserName
    persistent native string sNickName = "" 
    persistent native string sPaymentSourceId = ""
    persistent string sDefault = "{}"
                
    field fPayDate [
        date(control) aDate("yyyy-MM-dd", dateValidation)         
    ]
  
	auto dropDown(control) dPayAmount = current [	        	
    	UcPaymentAction.getPayAmountDropdown(sCurrentBalanceDisplay, sStatementBalanceDisplay, sMinDueDisplay, bIsAccountCurrent)
    ] 
    
    field fOtherAmount [
        input(control) pInput("^[+]?[0-9]{1,3}(?:[0-9]*(?:[.,][0-9]{2})?|(?:,[0-9]{3})*(?:\\.[0-9]{2})?|(?:\\.[0-9]{3})*(?:,[0-9]{2})?)$", fOtherAmount.sValidation)            
        string(validation) sValidation = "{Entry must be in standard US or European currency format.}"       
        string(error) sErrorEmpty = "{You must enter an amount}"
        string(error) sErrorOver = "{Warning, entry exceeds current balance.}" 
        string(error) sErrorOverMax = "{Warning, amount exceeds the maximum due}"
        string(error) sErrorBelowMin = "{Warning, amount is less than minimum due}" 
        string(error) sErrorZero = "{Warning, amount should not be zero.}"    
    ]
    
    auto "{Existing account:}" dropDown dWalletItems
    
    field fCheckBoxes [        
    	checkBoxes(control) sField [
        	Agree: "{I have read and agree with the [a href='#' st-pop-in='paymentTerms_en_us.html']terms & conditions[/a] of payment processing.}"            
        ]
        string(required) sRequired = "{This field is required.}"
    ]
    
    static sEftNotice = "{By authorizing this transaction, customer agrees that merchant " +
                        "may convert this transaction into an Electronic Funds Transfer " + 
                        "(EFT) transaction or paper draft, and to debit this account " +
                        "for the amount of the transaction. Additionally, in the event " + 
                        "this draft or EFT is returned unpaid, a service fee, as " +
                        "allowable by law, will be charged to this account via EFT or " + 
                        "draft. In the event you choose to revoke this authorization, " +
                        "please do so by contacting the merchant directly. Please note " +
                        "that processing times may not allow for revocation of this " +
                        "authorization.}"
                        
	static sSurchargeNotice = "{If you decide to pay through Direct Debit, 1st Franklin Financial will charge you a non-refundable Convenience Fee. " + 
							  "This fee is payable in advance along with the Payment Amount and will be charged separately as a line item transaction.}"                        
    
    field fAutoScheduledConfirm [        
    	checkBoxes(control) sField = Agree [
        	UcPaymentAction.getAgreeField(srBillOverviewResult.docLocation, "autoscheduled")            
        ]
        string(required) sRequired = "{This field is required.}"
    ]
    	
    structure(message) msgError [
		string(title) sTitle = "{Payment error}"
		native string(body) sBody = "" 
	]

	structure(message) msgChangeScheduledDate [
		string(title) sTitle = "{Payment warning}"
		string (body) sBody = "{The payment method will expire before the selected payment date. Please schedule the payment before the expiration date, or update your payment method's expiration date.}"
	]
	
	structure(message) msgAutoScheduled [
		string(title) sTitle = "{Payment warning}"
		volatile native string (body) sBody = UcPaymentAction.getOneTimeWarningMsg(srBillOverviewResult.docLocation, "autoscheduled")   
	]
	
	structure(message) msgPmtScheduled [
		string(title) sTitle = "{Payment warning}"
		volatile native string (body) sBody = UcPaymentAction.getOneTimeWarningMsg(srBillOverviewResult.docLocation, "pmtscheduled")
	]
	
	structure(message) msgNoPmtGroupError [
		string(title) sTitle = "{Configuration problem}"
		string(body) sBody = "{There is no payment group configured to your account. Please contact your System Administrator.}"
	]
	
    structure(message) msgMultiplePmtGroupError [
		string(title) sTitle = "{Not supported}"
		string(body) sBody = "{There are more than one payment group configured to your account. We currently do not support multiple payment groups. Please contact your System Administrator.}"
	]
	
	structure(message) msgMultipleInvoicesError [
		string(title) sTitle = "{Not supported}"
		string(body) sBody = "{We currently do not support the payment of multiple bills of the same account that has the same bill date. Please contact your System Administrator.}"
	]
	
	structure(message) msgZeroBalanceError [
		string(title) sTitle = "{Bill amount due}"
		string(body) sBody = "{This account currently has zero balance so no payments can be made.}"
	]
	
    native string sAppUrl           = AppConfig.get("user.app.url")
    native string sAppUsecase       = "paymentOneTime"
    native string sPaymentUrl		= AppConfig.get("epayment.url")
    native string sTransactionIdUrl = ""
    persistent native string responseCode      = ""
    persistent native string responseMessage   = ""
    persistent input transactionId             = TransactionIdGen.getTransactionId(sTransactionIdUrl)
    input token						= ""    
   
    native string methodNickName			= ""
    string (iframe) sCreateIframe
    string (iframe) sEditIframe
	persistent string sIframeSourceType
	persistent string sIframeOnetime
	persistent string sErrorMessageType
	tag hSpinner = Spinner.getSpinnerTemplate("pageSpinner.ftl", "pageSpinner", sorrisoLanguage, sorrisoCountry)


	// -- handling impersonation --
 	import utilImpersonationActive.sImpersonationActive
 	native string bImpersonateActive
    
	/*************************
	* MAIN SUCCESS SCENARIOS
	*************************/
	/* 1. Get most recent document details. */
	action init [
		sUserName = getUserName()
        dPayAmount = ""
        bImpersonateActive = sImpersonationActive
        goto(getDocuments)
	] 
	
	action getDocuments [
		/*-- If it is statement mode, we always need to get the recent document --*/
        if sBillingType == "statement" then
    	    getMostRecent
    	else 
    	    hasBillSelectedForPayment 
	]

	action hasBillSelectedForPayment [
        switch sHasBillsSelectedForPayment [
        	case "true" initPaySelectedBills
        	default getMostRecent
        ]		
	]
	
	action initPaySelectedBills [
		sMostRecentDocsResult = "allow"
		sCurrentBalanceFlag = "valid"
		goto(getWallet)
	]
	
	action getMostRecent [
		
        OneTimePaymentHelperAction.init()
        		
		// first call without location, if only UBF or only Vault, then it should pass
        // if multiple are found, override with UBF
/*         switch apiCall Documents.MostRecent(srLatestReq, srLatestResult, ssStatus) [
           case apiSuccess checkIfMultipleDocsWithoutLocation
           default getBalance
        ] */
        
        srBillOverviewParam.user 		= sUserId
        srBillOverviewParam.account 	= sPayAccountInternal
        srBillOverviewParam.payGroup 	= sPayGroup
        srBillOverviewParam.billDate 	= sPaySelectedDate 
        srBillOverviewParam.isBill	    = sPayIsBill
        
		switch apiCall Documents.BillOverview(srBillOverviewParam, srBillOverviewResult, srBillOverviewStatus) [ 
           case apiSuccess checkIfMultipleDocsWithoutLocation
           default getBalance			
		]       
	]
	
	/* 2. Check if most recent has multiple documents. */
	action checkIfMultipleDocsWithoutLocation [
//		sPayAccountInternal = srLatestResult.internalAccount
//      sPayAccountExternal = srLatestResult.externalAccount
		sMostRecentDocsResult = srBillOverviewResult.result
		sDocumentNum = srBillOverviewResult.docNumber 
		
		switch sMostRecentDocsResult  [
//			case "multipleDocs" multipleDocsUseUbf
			case "multipleDocs" getWallet
		    default getWallet
		]
	]
	
	/* 3. Get most recent again with only UBF. */
/* 	action multipleDocsUseUbf [
        srLatestReq.user = sUserId
        srLatestReq.useDocument = "true"
        
        // multiple were found, override with UBF and test again
        // if we now get multipleDocs, then it means two UBF with same date are loaded
        switch apiCall Documents.MostRecent(srLatestReq, srLatestResult, ssStatus) [
           case apiSuccess checkIfMultipleDocsWithLocation
           default getBalance
        ]           	    
	]
	
	/* 4. Check if the UBF only call is still multipleDocs. 
	action checkIfMultipleDocsWithLocation [
		sPayAccountInternal = srLatestResult.internalAccount
        sPayAccountExternal = srLatestResult.externalAccount
		sMostRecentDocsResult = srLatestResult.result
		sDocumentNum = srLatestResult.docNumber 
		
		switch sMostRecentDocsResult  [
			case "multipleDocs" getBalance
		    default getWallet
		]
	]
*/	
	/* 2.1 4.1 Get wallet details. */
	action getWallet [		
//	    sCurrentBalanceEdit = sCurrentBalance
//	    sMinAmountDueEdit   = sMinDue
	    
		fPayDate.aDate = sTodaysDate				
		sTransactionIdUrl = sPaymentUrl + "generateTransactionId"
		sAutomaticPaymentShortcut = sAppUrl + "startAutomaticPayment"
		switch UcPaymentAction.getWalletItems(sUserId, token, dWalletItems) [
			case "success" getBalance
			case "error" genericErrorResponse
			default genericErrorResponse
		]
	]
	
	/* 5 3.1  System needs to decide if we are selecting several bills or just one before
	 * 			retrieving balance information. If many, then the process is different.
	 */
	action getBalance [
		switch sHasBillsSelectedForPayment [
        	case "true" checkSelectedBillsPlurality
        	default getAccountStatusData
        ]
	]
	
	//-----------------------------------------------------------------------------------------
	// 1FFC SPECIFIC STARTS, REPLACES actions that retrieve current balance in core
	//-----------------------------------------------------------------------------------------	
	
	/**
	 * 5.3.1A System has identified this as a single bill process (consumer). It retrieves account status
	 *			 data. This is needed for calculating the current balance since the current balance is
	 *			 based on either bill or status, whichever is newer.
	 */ 
	action getAccountStatusData [
		srGetStatusParams.user = sUserId
		srGetStatusParams.paymentGroup = srBillOverviewParam.payGroup
		srGetStatusParams.account = srBillOverviewParam.account
		// -- retrieve the status information --
   		switch apiCall AccountStatus.GetStatus(srGetStatusParams, srGetStatusResult, srAccountStatusCode) [
    		case apiSuccess assignPaymentInformation
    		default MsgInternalError
    	]
	]
	
	/**
	 * 5.3.1B System assigns parameters from status results and gets the current balance in raw form
	 * 			the localized string form happens when sTotalAmountDue is referenced.. see declaration.
	 */
	action assignPaymentInformation [
	    sLocalAccountStatusDate = srBillOverviewResult.docDate
	    sLocalAccountStatusAmount = srBillOverviewResult.totalDue
	    sLocalAccountBillDate = srGetStatusResult.statusDate
	    sLocalAccountBillAmount = srGetStatusResult.currentAmountDue
		sCurrentBalanceFlag = "valid"				// -- this turns off the flag that prevents overpayment
		goto (getMinimumDueRequired)
	]
	
	/**
	 * 5.3.2A Calculate minimum due (step 1 of 3) -- System gets the minimum due 
	 *			from the status feed. This minimum arrives nightly. 
	 */
	action getMinimumDueRequired [
		srGetMinimumParams.user = sUserId
		srGetMinimumParams.paymentGroup = srBillOverviewParam.payGroup
		srGetMinimumParams.account = srBillOverviewParam.account
		// -- retrieve the status information --
   		switch apiCall AccountStatus.IsMinimumPaymentRequired(srGetMinimumParams, 
   															  srGetMinimumResult, 
   															  srGetMinimumCode) [
    		case apiSuccess isMinimumDueRequired
    		default MsgInternalError
    	]
	]
	
	/**
	 * 5.3.2B Calculate minimum due (step 2 of 3) - System checks status result
	 *			to see if minimum due is required. 
	 */
	action isMinimumDueRequired [
		sMinAmountDueEdit = "0.00"
		if "false" == srGetMinimumResult.bMinimumRequired then
			setMaximumDue
		else
			setMinimumDue
	]

	/**
	 * 5.3.2C Calculate minimum due (step 3 of 3) - System assigns the true
	 *			minimum due based on comparison to current balance.
	 */
	action setMinimumDue [
	    sMinAmountDueEdit   = sMinDue // -- true min due calculated in declaration of sMinDue
		goto (setMaximumDue)	
	]
	
	action setMaximumDue [
	    sMaxAmountDueEdit   = sMaxPay // -- max pay calculated in declaration of sMaxPay
		goto (isAccountCurrent)
	]
	
	action isAccountCurrent [
		if "true" == bIsAccountCurrent then
		 	setCurrentBalanceToZero
		else
			setPayAmtLabels
	]
	
	action setCurrentBalanceToZero [
		sCurrentBalance = "0.00"
		sMinAmountDueEdit = "0.00"
		sPayAmountLabel     = sPayAmountNewText1 + sDisplayAmt + sPayAmountText2
		sOtherAmountLabel = sOtherAmountText1Localized + sMinDueDisplay + sOtherAmountText2Localized + sMaxDueDisplay
		goto (setPayAmtLabels)
	]
	
	action setPayAmtLabels [
		sCurrentBalance = sCurrentBalanceEdit
		sPayAmountLabel     = sPayAmountNewText1 + sDisplayAmt + sPayAmountText2
		sOtherAmountLabel = sOtherAmountText1Localized + sMinDueDisplay + sOtherAmountText2Localized + sMaxDueDisplay
		goto (checkSelectedBillsPlurality)
	]
	
	/**
	 * 5.3.2.D System finds that minimum payment call failed, report internal
	 *			error to the user.
	 */
	action MsgInternalError [
		displayMessage (type: "error" msg: msgStatusError)
		goto(OneTimePaymentScreen)
			
	]

	//-----------------------------------------------------------------------------------------
	// 1FFC SPECIFIC ENDS, REPLACES actions that retrieve current balance in core
	//-----------------------------------------------------------------------------------------	
	    
    /* 6. Check Bills single or multiple */
    action checkSelectedBillsPlurality [
		switch OneTimePaymentHelperAction.getSelectedBillsPlurality() [
			case "single" paySingleBillModeCheck
			case "multiple" preparePayMultipleBills
			default paySingleBillModeCheck
		]
	]

	/**
	 * 6a. System is in pay one bill at a time mode (consumer). Branch flow
	 *		after determining if this is a statement based system or 
	 *		invoice based system that's running.
	 */
    action paySingleBillModeCheck [
    	
    	if sBillingType == "statement" then
    	    setStatementPayment
    	else 
    	    preparePaySingleBill    	
    ]	
    
 	/**
 	 * 6.1a System has identified this as a single bill (consumer), statement based system and passes
 	 *		payment information down to the helper. The helper uses this information when
 	 *		responding to JSON requests from the payment javascript. In this case, specifically
 	 *		a call to get bill information for payment.
 	 */
	action setStatementPayment [

		//-----------------------------------------------------------------------------------------
		// 1FFC SPECIFIC (MINOR) product uses the statement amount due (docAmount) and the statement
		//		minimum due (minDue) and here we've replaced them with amounts calculated based on
		//		information in the status feed and payment history.
		//-----------------------------------------------------------------------------------------	

		// -- The comments to the right of each argument show how they are set in the JSON
		//		sent down to the javascript. Note that some are never used and others are used in
		//		multiple JSON variables (the javascript must be messy).
		OneTimePaymentHelperAction.setBillForPayment(
			sPayAccountInternal,						// -- number, internalAccountNumber, internal_account_number
			sPayAccountExternal,						// -- numberDisplay
			srBillOverviewResult.docDate,				// -- statementDate, statementNum
			srBillOverviewResult.docNumber, 			// -- NOT USED
//			srBillOverviewResult.docAmount,				// -- ORIGINAL LINE! amount, amountNum, total, total_num 
			sCurrentBalance,							// -- amount, amountNum, total, total_num
			srBillOverviewResult.dueDate,				// -- dueDate
			sPayGroup,									// -- paymentGroup
			srBillOverviewResult.totalDue,				// -- NOT USED
//			srBillOverviewResult.minDue,				// -- ORIGINAL LINE! minimumDue, minimumDueNum 
			sMinDue,									// -- minimumDue, minimumDueNum
			sCurrentBalance								// -- paymentAmount, currentBalance, currentBalanceNum
		)
		goto (preparePaySingleBill)
	]    
    
	/**
	 *  6.1b. System sets current balance information for paying a single bill
	 */
	action preparePaySingleBill [

		//-----------------------------------------------------------------------------------------
		// 1FFC SPECIFIC (MINOR) current balance already retrieved and formatted so we don't need
		//		to do that here.
		//-----------------------------------------------------------------------------------------	
		
//		OneTimePaymentHelperAction.populatePaymentAmount(sCurrentBalance, sCurrentBalanceDisplay, sPayGroup)

		sCurrentBalanceDisplay = sTotalDueDisplay		// -- sTotalDueDisplay populated dynamically in declaration
//		sCurrentBalanceEdit = sCurrentBalance			// -- sCurrentBalance populated action assignPaymentInformation
		
		selectedBillsPlurality = "single"
		goto(OneTimePaymentScreen)
	]
	
	//* 6.2a Prepare multiple bills */
	action preparePayMultipleBills [
		OneTimePaymentHelperAction.populatePaymentAmount(sCurrentBalance, sCurrentBalanceDisplay, sPayGroup)
	    
//		sCurrentBalanceEdit = sCurrentBalance
	    
		selectedBillsPlurality = "multiple"
		goto(OneTimePaymentScreen)
	]
    
    /**
     * Supporting action for shortcut setBillsForPayment.
     * Takes a POST json string containing list of bills
     * to be set up for the one-tie payment flow.
     */  
    action setBillsForPayment [
    	foreignHandler OneTimePaymentHelperAction.setBillsForPayment()
    ]
    
    /**
     * Return the list of bills set in 'setBillsForPayment'. 
     */
    action getBillsForPayment [
    	foreignHandler OneTimePaymentHelperAction.getBillsForPayment()
    ]
    
    /**
     * Clear list of bills set in 'setBillsForPayment'.  
     */
    action unsetBillsForPayment [
    	foreignHandler OneTimePaymentHelperAction.unsetBillsForPayment()
    ]
    
    /* 8. Shows the one time payment details. */
    xsltScreen OneTimePaymentScreen("{Payment}") [
		
		child utilImpersonationActive
		
		div messageNoPmtGroupError [
			logic: [						
				if sPmtGroupConfigResult != "nopaygroup" then "hide"						
			]				
			class: "alert alert-danger"
			attr_role: "alert"				
			display msgNoPmtGroupError 
		]
		
		div messageMultiplePmtGroupError [
			logic: [						
				if sPmtGroupConfigResult != "manypaygroups" then "hide"						
			]				
			class: "alert alert-danger"
			attr_role: "alert"				
			display msgMultiplePmtGroupError 
		]
		div messageMultipleInvoicesError [
			logic: [						
				if sPmtGroupConfigResult == "nopaygroup" then "hide"
				if sPmtGroupConfigResult == "manypaygroups" then "hide"		
				if sPmtGroupConfigResult == "manyaccounts" then "hide"					
				if sMostRecentDocsResult != "multipleDocs" then "hide"					
			]				
			class: "alert alert-danger"
			attr_role: "alert"				
			display msgMultipleInvoicesError 
		]
		
		div messageZeroBalanceError [
			logic: [						
				if sCurrentBalanceFlag != "zero" then "hide"						
			]				
			class: "alert alert-info"
//			attr_role: "alert"				
			display msgZeroBalanceError 
		]
		
		div oneTimePaymentContent [
			logic: [
				if sPmtGroupConfigResult == "nopaygroup" then "remove"
				if sPmtGroupConfigResult == "manypaygroups" then "remove"
				if sCurrentBalanceFlag == "zero" then "remove"
				if sPmtGroupConfigResult == "success" &&  sMostRecentDocsResult == "multipleDocs" then "remove"									
			]
			
	        form paymentForm [        	
	        	class: "st-payment-onetime st-payment-onetime-user"
	        	
				display hSpinner
	
				div messageError [
					class: "alert alert-danger visually-hidden"
					attr_role: "alert"
					
					display msgError
				]
	        	
	        	div messageAutoScheduled [
					logic: [
						if sAutoScheduledFlag != "true" then "hide"				
					]			
					class: "alert alert-warning st-future-scheduled-warning"
					attr_role: "alert alert-warning"				
					display msgAutoScheduled 
				]
				
				div messagePmtScheduled [
					logic: [						
						if sPmtScheduledFlag != "true" then "hide"						
					]				
					class: "alert alert-warning st-auto-scheduled-warning"
					attr_role: "alert alert-warning"				
					display msgPmtScheduled 
				]
				
				div messageChangeScheduledDate [
					class: "alert alert-danger visually-hidden"
					attr_role: "alert"				
					display msgChangeScheduledDate 
				]
				
			    display sAppUrl [
			    	class: "visually-hidden st-app-url"
			    ]
			    
			    display sAppUsecase [
			    	class: "visually-hidden st-app-usecase"
			    ]

			    display token [
			    	class: "visually-hidden"
			    ]

				display sorrisoLanguage [
			    	class: "visually-hidden st-language"
			    ]
			    
			    display sorrisoCountry [
			    	class: "visually-hidden st-country"
			    ]
			    			    
			    display sPayGroup [
			    	class: "visually-hidden st-pay-group-hidden"
			    ]
			    
			    display sPayData [
			    	class: "visually-hidden st-pay-data-hidden"
			    ]
			    
			    display sTotalPayAmt [
			    	class: "visually-hidden st-total-amount-hidden"
			    ]
			    			    
			    display sPayAccountInternal [
			    	class: "visually-hidden st-selected-account-hidden"
			    ]
			    
			    display sPayAccountExternal [
			    	class: "visually-hidden st-selected-display-account-hidden"
			    ]
			    
			    display sDocumentNum [
			    	class: "visually-hidden st-document-number-hidden"
			    ]
			    
			    display sAutoScheduledFlag [
			    	class: "visually-hidden st-auto-scheduled"
			    ]
			    
			    display sPmtScheduledFlag [
			    	class: "visually-hidden st-pmt-scheduled"
			    ]
			
			    display sCurrentBalanceEdit [
			    	class: "visually-hidden st-current"
			    ]
			    			    		    
			    display sStatementBalanceEdit [
			    	class: "visually-hidden st-statement"
			    ]
			    
			    display sMinAmountDueEdit [
			    	class: "visually-hidden st-minimum"
			    ]

			    display sMaxAmountDueEdit [
			    	class: "visually-hidden st-maximum"
			    ]
				
				display sCurrentBalanceDisplay [
			    	class: "visually-hidden st-current-display"
			    ]
			    		    
			    display sStatementBalanceDisplay [
			    	class: "visually-hidden st-statement-display"
			    ]
			    
			    display sMinDueDisplay [
			    	class: "visually-hidden st-minimum-display"
			    ]
			    
			    display sMaxDueDisplay [
			    	class: "visually-hidden st-maximum-display"
			    ]
			    
				display sCurrencySymbol[
					class: "visually-hidden st-currency"
				]
				
				display sPayAmt [
					class: "visually-hidden"
				]
				
				display sSurCharge [
					class: "visually-hidden"
				]
				
				display sTotalAmount [
					class: "visually-hidden"
				]
				
				display sScheduledDateWindow [
					class: "visually-hidden st-date-window"
				]
				
				display sNumSources  [
					class: "visually-hidden st-num-sources"
				]
				
				display sMaxSources [
					class: "visually-hidden st-max-sources"
				]
								
				display sPaymentSourceBankEnabled [
					class: "visually-hidden st-source-bank"
				]
				
				display sPaymentSourceCreditEnabled [
					class: "visually-hidden st-source-credit"
				]
				
				display sPaymentSourceDebitEnabled [
					class: "visually-hidden st-source-debit"
				]
				
				display sPaymentSourceSepaEnabled [
					class: "visually-hidden st-source-sepa"
				]
				
				display surchargeFlag [
					class: "visually-hidden st-surcharge"
				]
				
				display sFlexfield [
					class: "visually-hidden st-flexfield"
				]

				display sBillingType [
					class: "visually-hidden st-billing-type"
				]
				
			
									
				/****************************************************************
			     * Step 1 - Payment summary
			     ***************************************************************/
		    	div paymentSummary [
			    	class: "st-payment-padding-bottom"
							    		
			    	div paymentSummaryHeaderRow [
			    		class: "row"
			    		
						h4 paymentSummaryHeaderCol [
							class: "col-md-12"
							
							display sStepNumber1 [
								class: "st-payment-step-number"
							]
							
							display sPaymentSummaryHeader [
								class: "st-payment-onetime-header"
							]
						]
					]
					
					div paymentSummaryContent [
						class: "row st-margin-left45"
						
						div paymentSummaryCol1 [
							class: "col-12 col-sm-6 col-lg-2"
							
							div accountIdLabelRow [
								class: "row st-payment-onetime-header-row"
								
								div accountIdLabel [
									class: "col-md-12"
									display sAccountIdLabel
								]
							]
								
							div accountIdFieldRow [
								class: "row st-payment-onetime-border-top"
								
								div accountIdField [
									logic: [
										if selectedBillsPlurality == "multiple" then "hide"				
									]
									class: "col-md-12 st-payment-onetime-bold-font"
									display sPayAccountExternal									
								]
								div accountIdField2 [
									logic: [
										if selectedBillsPlurality == "single" then "hide"				
									]
									class: "col-md-12 st-payment-onetime-bold-font"
									display sNumberOfBillsDisplayLabel									
								]
							
							]
						]
						
						div paymentSummaryCol2 [
							class: "col-12 col-sm-6 col-lg-4"
							
							div payDateLabelRow [
								class: "row st-payment-onetime-header-row"
								
								div payDateLabel [
									class: "col-md-12"
									display sPaymentDateLabel
								]
							]
							
							div payDateFieldRow [
								class: "row st-payment-onetime-border-top"
								
								div payDateField [
									class: "col-md-12"
									display fPayDate [
									    control_attr_tabindex: "1"
									    readonly: "true"
									]
								]
							]
						]
						
						div paymentSummaryCol3 [
							class: "col-6 col-lg-3"
							
							div payAmountLabelRow [
								class: "row st-payment-onetime-header-row"
								
								div payAmountLabel [
									class: "col-md-12"
									display sPayAmountLabel
								]
							]

				
							div payAmountFieldRow [
								class: "row st-payment-onetime-border-top"
								
								div payAmountField [
									class: "col-md-12"
									
									display dPayAmount [
									    control_attr_tabindex: "2"  
									   									   
									    logic: [
									    	if sBillingType == "invoice" then "hide"
									    ] 									    
									]
									
									display sCurrentBalanceDisplay [
									    logic: [
									    	if sBillingType == "statement" then "hide"
									    ] 										
									]
								]
							]							
						
							display sAdditionalChargeInfo [
								class: "visually-hidden alert alert-warning"
								attr_role: "alert"
					    	]
					    	
			    			display sMinDummy [
								class: "st-space visually-hidden"
							]							
						]
						
						div paymentSummaryCol4 [
							class: "col-6 col-lg-3"
							
							div otherAmountLabelRow [
								class: "row st-payment-onetime-header-row"
								
								div otherAmountLabel [
									class: "col-md-12"
									display sOtherAmountLabel [
										class: "visually-hidden"
									]
									display sDummy [
										class: "st-space visually-hidden"
									]
								]
							]
							
							div otherAmountFieldRow [
								class: "row st-payment-onetime-border-top"
								
								div otherAmountField [
									class: "col-md-12"
									display fOtherAmount [
										class: "visually-hidden"
									    control_attr_tabindex: "3"
									    sErrorEmpty_class_override: "st-amount-validation-msg alert alert-danger"
									    sErrorEmpty_attr_sorriso-error: "required"
									    sErrorOver_class_override: "alert alert-warning visually-hidden"
									    sErrorOver_attr_sorriso-error: "over"
									    sErrorOverMax_class_override: "alert alert-warning visually-hidden"
									    sErrorOverMax_attr_sorriso-error: "over-max"
									    sErrorBelowMin_class_override: "alert alert-warning visually-hidden"
									    sErrorBelowMin_attr_sorriso-error: "below-min"
									    sErrorZero_class_override: "st-amount-validation-msg alert alert-danger visually-hidden"
									    sErrorZero_attr_sorriso-error: "zero"
									]																				
								]							
							]
						]
					]
				
					div paymentSummaryTotal [						
						class: "row st-payment-onetime-border-top st-margin-left45"
													
						div continueButtonCol [
							class: "col-6 col-sm-3 col-lg-2 offset-sm-6 offset-lg-8"
							navigation paymentSummaryContinueLink(OneTimePaymentScreen, "{CONTINUE}") [
			                    class: "btn btn-primary"
			                    attr_tabindex: "4"
							]											
						]
						
						div cancelLinkCol [
							class: "col-6 col-sm-3 col-lg-2 text-center"
							navigation paymentSummaryCancelLink(init, "{CANCEL}") [
								class: "btn btn-link btn-cancel st-padding-top5"
								attr_tabindex: "5"
							]
						]						
					]
				]		
		        
				/****************************************************************
			     * Step 1A - Payment summary complete
			     ***************************************************************/
		    	div paymentSummaryComplete [
			    	class: "st-border-bottom visually-hidden"
			    		
			    	div paymentSummaryCompleteHeaderRow [
			    		class: "row"
			    		
						h4 paymentSummaryCompleteHeaderCol [
							class: "col-md-12 st-payment-step-complete"
							display sPaymentSummaryHeader [
								class: "st-payment-onetime-header st-cursor"
							]
							display sHeaderEdit [
								class: "st-payment-onetime-header st-cursor st-edit"
							]
						]
					]
					
					/*-- Account ID  --*/
					
					div paymentSummaryCompleteContent [
						class: "row st-margin-left45"
						
						div paymentSummaryCompleteCol1 [
							class: "col-md-3"
							
							div accountIdCompleteLabelRow [
								class: "row"
								
								div accountIdCompleteLabel [
									class: "col-md-12"
									display sAccountIdCompleteLabel
								]
							]
							
							div accountIdCompleteRow [
								class: "row st-payment-onetime-bold-font"
								
								h4 accountIdComplete [
									logic: [
										if selectedBillsPlurality == "multiple" then "remove"				
									]
									class: "col-md-12"
									display sPayAccountExternal
								]
								h4 accountIdComplete2 [
									logic: [
										if selectedBillsPlurality == "single" then "remove"				
									]
									class: "col-md-12"
									display sNumberOfBillsDisplayLabel									
								]
							]
						]
	
						/*-- Payment Date --*/
						div paymentSummaryCompleteCol2 [
							class: "col-md-3"
							
							div paymentDateCompleteLabelRow [
								class: "row"
								
								div paymentDateCompleteLabel [
									class: "col-md-12"
									display sPaymentDateCompleteLabel
								]
							]
	
							div paymentDateCompleteRow [
								class: "row st-payment-onetime-bold-font"
								
								h4 paymentDateComplete [
									class: "col-md-12"
									display sPaymentDateComplete
								]
							]
						]
						
						/*-- Amount to Pay --*/
						div paymentSummaryCompleteCol3 [
							class: "col-md-2"
							
							div otherAmountCompleteLabelRow [
								class: "row"
								
								div otherAmountCompleteLabel [
									class: "col-md-12 text-end"
									display sOtherAmountCompleteLabel
								]
							]
	
							div otherAmountCompleteRow [
								class: "row st-payment-onetime-bold-font"
								
								h4 otherAmountComplete [
									class: "col-md-12 text-end"
									display sAmountComplete
							    ]
							]
						]
						
						/*-- Payment Surcharge --*/
						div paymentSummaryCompleteCol4 [
							class: "col-md-2"   
							
							logic: [ if surchargeFlag != "true" then "remove" ]
							
							div paymentSurchargeCompleteLabelRow [
								class: "row"
																
								div paymentSurChargeCompleteLabel [
									class: "col-md-12 text-end"
									display sPaymentConvenienceFeeLabel
								]
							]
	
							div paymentSurChargeCompleteRow [
								class: "row st-payment-onetime-bold-font"
								
							    h4 amountSurCharge [
									class: "col_md-12 text-end"
									display sSurchargeAmountComplete
								]
							]
						]

						/*-- Total amount --*/
						div paymentSummaryCompleteCol5 [
							class: "col-md-2"  
							
							logic: [ if surchargeFlag != "true" then "remove" ]
							
							div paymentTotalAmountCompleteLabelRow [
								class: "row"
								
								div paymentTotalAmountCompleteLabel [
									class: "col-md-12 text-end"
									display sPaymentTotalAmountLabel
								]
							]
	
							div paymentTotalAmountCompleteRow [
								class: "row st-payment-onetime-bold-font"
								
							    h4 totalAmount [
									class: "col_md-12 text-end"
									display sTotalAmountComplete
								]
							]
						]
																		
					]
		        ]
				
				/****************************************************************
			     * Step 2 - Payment method
			     ***************************************************************/
		        div paymentMethod [
	        		class: "st-payment-padding-bottom visually-hidden"
	        		
			    	div paymentMethodHeaderRow [
			    		class: "row"
			    		
						h4 paymentMethodHeaderCol [
							class: "col-md-12"
							
							display sStepNumber2 [
								class: "st-payment-step-number"
							]
							
							display sPaymentMethodHeader [
								class: "st-payment-onetime-header"
							]
						]
					]
	        		
	        		div paymentMethodButtonsRow [
	        			class: "row st-padding-bottom st-margin-left45"

	    	    		logic: [
							if sNumSources >= sMaxSources then "visually-hidden"
						]
	        			
		                div paymentMethodButtonsCol1 [		                	
		                	class: "col st-payment-method-buttons"
		                	
		              	    navigation chooseExistingMethod(OneTimePaymentScreen, "{Use Existing Payment Method}") [
			                     class: "btn btn-primary "
		              		]
		              	]
		              	
		              	div paymentMethodButtonsNotMaxed [
		              		class: "col st-payment-method-buttons"
		              		
		    	    		logic: [
								if sPaymentSourceBankEnabled == "false" then "remove"
							]
															
				            navigation addNewBankAccount(OneTimePaymentScreen, "{Use a new bank account}") [
				                class: "btn btn-primary st-bankaccount-button"
				            ]
				        ]
			              	
		              	div paymentMethodButtonsCol3 [
		              		class: "col"
		
		    	    		logic: [
								if sPaymentSourceDebitEnabled == "false" then "remove"
							]
		
		              	    navigation addNewDebitCard(OneTimePaymentScreen, "{Use a new debit card}") [
			                     class: "btn btn-primary st-debitcard-button"
		              		]
				        ]
					        
		              	div paymentMethodButtonsCol4 [
		                	class: "col"
		                	
		    	    		logic: [
								if sPaymentSourceCreditEnabled == "false" then "remove"
							]
		                	
		              	    navigation addNewCreditCard(OneTimePaymentScreen, "{Use a new credit card}") [
			                     class: "btn btn-primary st-creditcard-button"
		              		]
		              	]
			              	
					        
		              	div paymentMethodButtonsCol5 [
		                	class: "col"
		                	
		    	    		logic: [
								if sPaymentSourceSepaEnabled == "false" then "remove"
							]
		                	
		              	    navigation addNewSepaAccount(OneTimePaymentScreen, "{Use a new sepa credit transfer}") [
			                     class: "btn btn-primary st-sepacard-button"
		              		]
		              	]				        
			        ]
			        									
					div iframeDisplayRow [
						class: "row visually-hidden"
						
						div iframeDisplayCol [
							class: "col-md-12"
		
							div createSourceIframe [
								class: "st-payment-iframe"
	
								display sCreateIframe
							]
						]
					]
					
					div walletRow [
													
						class: "row st-margin-left45"
						
						div walletChoice [
							class: "col-12 col-lg-6"
												
							div walletCol [
								class: "row st-pay-methods-dropdown"
								
								display dWalletItems [
									class: "col-9 col-md-6 col-lg-9 col-xl-6"
								]
								
								div sPaySourceEditDiv [
									class: "col-3 st-pay-method-edit st-edit"
									
									display sPaySourceEdit
								]
							]
						
							div walletSubmit [
								class: "mt-3"
								
			              	    navigation walletContinueLink(OneTimePaymentScreen, "{CONTINUE}") [
				                     class: "btn btn-primary"
								]
			              	    navigation walletCancelLink(init, "{CANCEL}") [
				                     class: "btn btn-link btn-cancel st-padding-top5 ms-4"
			              		]
		              		]
						]
						
		              	div paymentMethodButtonsMaxReached [
		              		class: "col-12 col-lg-6 st-payment-method-buttons-message"
		              		
		    	    		logic: [
								if sNumSources < sMaxSources then "visually-hidden"
							]
							
							div paymentMethodButtonsMaxReached [
								class: "alert alert-info"
								display sMaxPaymentMethodsReached [
									class: "text-center"
								]
							]
						]
					]
				]
				
				/****************************************************************
			     * Step 2A - Payment method complete
			     ***************************************************************/
		        div paymentMethodComplete [
	        		class: "st-border-bottom visually-hidden"
	        		
			    	div paymentMethodCompleteHeaderRow [
			    		class: "row"
			    		
						h4 paymentMethodCompleteHeaderCol [
							class: "col-md-12 st-payment-step-complete"
							display sPaymentMethodHeader [
								class: "st-payment-onetime-header st-cursor"
							]
							display sHeaderEdit [
								class: "st-payment-onetime-header st-cursor st-edit"
							]
						]
					]
	        						
					div paymentMethodCompleteSelection [
						class: "row st-margin-left45"
						
						h4 paymentMethodNickName [
							class: "col-md-3"
							
							display sPaymentMethodNickName [
								class: "st-payment-method-nickname"
							]
						]
						
						
						div paymentMethodDetails [
							class: "col-md-5"
							
							div paymentMethodTypeRow [
								class: "row"
							
								div paymentMethodTypeCol [
									class: "col-md-12"
									
									display sPaymentMethodType [
										class: "st-payment-method-type"
									]
								]
							]
							
							div paymentMethodAccountRow [
								class: "row"
							
								div paymentMethodAccountCol [
									class: "col-md-12"
									
									display sPaymentMethodAccount [
										class: "st-payment-method-account"
									]
								]
							]
						]
					
					]
				]
		        
			    /****************************************************************
			     * Step 3 - Confirm payment
			     ***************************************************************/
		        div confirmPayment [
			    	class: "st-payment-padding-bottom visually-hidden"
			    	
			    	div confirmPaymentHeaderRow [
			    		class: "row"
			    		
						h4 confirmPaymentHeaderCol [
							class: "col-md-12"
							
							display sStepNumber3 [
								class: "st-payment-step-number"
							]
							
							display sConfirmPaymentHeader [
								class: "st-payment-onetime-header"
							]
						]
					]
					
					div estimatedProcessingDateLabelRow [
						class: "row st-margin-left45"
						
						div estimatedProcessingDateLabelCol [
							class: "col-md-12"
							
							display sEstimatedProcessingDateLabel
						]
					]
					
					div estimatedProcessingDateRow [
						class: "row st-margin-left45"
						
						div estimatedProcessingDateCol [
							class: "col-md-12"
							
							display sEstimatedProcessingDate [
								class: "st-payment-onetime-bold-font"
							]
						]
					]
					
					div checkBoxesRow [
						class: "row st-padding-top st-margin-left45"
						
						div messagesCol [
							class: "col-md-12 alert alert-warning"
							
							display sEftNotice
							display sSurchargeNotice

						]
						
						div checboxesCol [
							class: "col-md-12 st-padding-top"
							display fCheckBoxes [
								class: "st-payment-onetime-vertical-align"
							] 
						]
						
						div autoScheduledConfirm [
							logic: [
								if sAutoScheduledFlag != "true" then "visually-hidden"
							]
							class: "col-md-12"
							display fAutoScheduledConfirm [
								class: "st-payment-onetime-vertical-align"
							] 
						]
					]
						
					div confirmPaymentbuttons [
						class: "row st-payment-buttons st-margin-left45"
						div submitPaymentButton [
							class: "col-md-2 col-6"
							navigation submitPaymentLink(validatePayData, "{SUBMIT PAYMENT}") [
			                    class: "btn btn-primary"		                    
			                    data: [
			                    	sPayGroup,
									fPayDate,
									dPayAmount,								
									fOtherAmount,
									token,
									dWalletItems,
									sMinAmountDueEdit,
									sPayData,
									sTotalPayAmt,
									sSurCharge,
									sTotalAmount,
			                    ]
			                	require: [
			                		fCheckBoxes,
			                		fAutoScheduledConfirm
			                	]
			                	// -- disabled button shows if agent is impersonating --
			                	logic: [if bImpersonateActive == "true" then "remove"]
			                	
			                    attr_tabindex: "5"
							]
						] 
						
						// -- disabled button shows if agent is impersonating
						div submitPaymentButtonDisabled [
							class: "col-md-2 col-6"
							navigation submitPaymentLink1( init, "{DISABLED FOR AGENT}") [
			                    class: "btn btn-primary disabled"		                    
			                	logic: [if bImpersonateActive != "true" then "remove"]
							]
						]
						div cancelPaymentButton [ 
							class: "col-md-2 col-6"     			
							navigation cancelPaymentLink(init, "{CANCEL}") [
								class: "btn btn-link btn-cancel st-padding-top5"
								attr_tabindex: "6"
							]
						]
					]
		        ]
		        
				/****************************************************************
			     * Step 3A - Payment success
			     ***************************************************************/
		        div paymentSuccess [
			    	class: "st-payment-padding-bottom visually-hidden"
			    	
			    	div paymentSuccessRow [
			    		class: "row"
			    		
			    		div paymentSuccessContent [
			    			class: "col-md-6"
			    			
							div paymentSuccessHeaderRow [
					    		class: "row"
					    		
								h4 paymentSuccessHeaderCol [
									class: "col-md-12 st-payment-step-complete"
									display sPaymentSuccessHeader [
										class: "st-payment-onetime-header"
									]
								]
							]		    		
					    		
							div paymentSuccessTextRow [
								class: "row st-margin-left45"
			
								div paymentSuccessTextCol [
									class: "col-md-12"
			
									display sPaymentSuccessText [
										class: "st-padding-top st-payment-onetime-bold-font"
									]
								]
							]
							
							div paymentSuccessDates [
								class: "row st-padding-top st-margin-left45"
								
								div paymentSuccessDatesCol1 [
									class: "col-md-6"
									
									div paymentRequestReceivedLabelRow [
										class: "row"
										
										div paymentRequestReceivedLabel [
											class: "col-md-12"
											display sPaymentRequestReceivedLabel
										]
									]
									
									div paymentRequestReceivedRow [
										class: "row"
										
										div paymentRequestReceived [
											class: "col-md-12"
											display sPaymentRequestReceived [
												class: "st-payment-onetime-bold-font"
											]
										]
									]
								]
								
								div paymentSuccessDatesCol2 [
									class: "col-md-6"
									
									div estimatedProcessingDateLabelRow [
										class: "row"
										
										div estimatedProcessingDateLabel [
											class: "col-md-12"
											display sEstimatedProcessingDateLabel
										]
									]
									
									div estimatedProcessingDateRow [
										class: "row"
										
										div estimatedProcessingDate [
											class: "col-md-12"
											
											display sEstimatedProcessingDate [
												class: "st-payment-onetime-bold-font"
											]
										]
									]
								]
							]						
			    		]
			    		
			    		div paymentSuccessAutomatic [
			    			
			    			logic: [
								if sAutoScheduledFlag == "true" then "hide"						
							]
					
			    			class: "col-md-6"
			    			
			    			div paymentSuccessAutomaticRow [
			    				class: "row st-payment-automatic-banner"
			    				
			    				div paymentSuccessAutomaticIcon [
			    					class: "col-md-2 st-payment-automatic-banner-icon"
			    					display sDummy
			    				]
			    			
				    			div paymentSuccessAutomaticContent [
				    				class: "col-12"
				    				
				    				div paymentSuccessAutomaticContentRow1 [
					    				class: "row"
					    				
					    				div paymentSuccessAutomaticContentCol1 [
					    					class: "col-md-12"
					    					display sAutomaticPaymentsHeader [
					    						class: "st-payment-automatic-banner-header"
					    					]
				    					]
				    				]
				    				
				    				div paymentSuccessAutomaticContentRow2 [
					    				class: "row"
					    				
					    				div paymentSuccessAutomaticContentCol2 [
					    					class: "col-md-12"
					    					display sAutomaticPaymentsBody
				    					]
			    					]
					    			
				    				div paymentSuccessAutomaticContentRow3 [
					    				class: "row"
					    				
					    				div paymentSuccessAutomaticContentCol3 [
					    					class: "col-md-12"
											navigation automaticPaymentsLink(gotoAutomaticPayment, "{CREATE RECURRING PAYMENT SCHEDULE}") [
												class: "btn btn-primary st-margin-top10"
												attr_tabindex: "7"
											]
										]
									]
								]
							]
			    		]
			    	]
		        ]
	        ]
	    ]
	]

	action iframeResetSourceType [
		source_type = ""
		sIframeSourceType = ""
		sIframeOnetime = "false"
		goto (getCreateIframeUrl)
		
	]
		
	/* 5. iframe get url shortcut response */
	action iframeCreateResponse [
		sUserName = getUserName()
		switch source_type [
			case "credit" createCreditCardIframeAction
			case "debit" createDebitCardIframeAction
			case "bank" createBankAccountIframeAction
			case "sepa" createSepaAccountIframeAction
			default genericErrorResponse
		]
	]

    /* 6. Create credit card action.*/ 
    action createCreditCardIframeAction [	
		
		sIframeSourceType = "credit"
		sIframeOnetime = "true"
		goto (getCreateIframeUrl)
	]  
	
	/* 7. Create debit card action.*/ 
    action createDebitCardIframeAction [	
		sIframeSourceType = "debit"
		sIframeOnetime = "true"
		goto (getCreateIframeUrl)
	]
	
	/* 8. Create bank account action.*/ 
    action createBankAccountIframeAction [	
		sIframeSourceType = "bank"
		sIframeOnetime = "true"
		goto (getCreateIframeUrl)
	]

	/* 9. Create sepa account action.*/ 
    action createSepaAccountIframeAction [	
		sIframeSourceType = "sepa"
		sIframeOnetime = "true"
		goto (getCreateIframeUrl)
	]
	
	/* 10. Get Iframe url response .*/
	action getCreateIframeUrl [
		 sCreateIframe = "createIframeAddSourceOnetime"
		 goto (jsonCreateIframeResponse)
	]
	
	action getEditIframeUrl [
		 sUserName = getUserName()
		 sEditIframe = "createIframeEditSourceOnetime"
		 goto (jsonEditIframeResponse)
	]
	
	/* 11. Get Iframe url JSON response .*/
    json jsonCreateIframeResponse [
    	display sCreateIframe
    ]
    
    json jsonEditIframeResponse [
    	display sEditIframe
    ]
    
    /* 12. Add source success response.*/
	action addSourceSuccessResponse [
		UcPaymentAction.getWalletItems(sUserId, dWalletItems)
		
    	auditLog(audit_payment.add_source_success) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch sWalletCreateSuccessEmailFlag [
			case "true" sendCreateSuccessEmail
			case "false" getWalletCount			
			default getWalletCount
		]
	]
	
	/* 13. Sends wallet create success email. */
	action sendCreateSuccessEmail [
		sNtfParams = "nickName=" + sNickName
		NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_wallet_create_success")		
		goto(getWalletCount)
	]
	
	/* 14. Gets wallet count.*/
	action getWalletCount [
		srGetWalletCountParam.USER_ID = sUserId
		
		switch apiCall Payment.GetWalletCount(srGetWalletCountParam, srGetWalletCountResult, ssStatus) [
		    case apiSuccess addSourceSuccessResponse2
		    default addSourceSuccessResponse2
		]
	]
	
	/* 15. Add source success response.*/
	action addSourceSuccessResponse2 [
		sNumSources = srGetWalletCountResult.COUNT
		
		goto(jsonAddSourceSuccessResponse)
	]
	
	/* 16. json ass source success response.*/
    json jsonAddSourceSuccessResponse [
    	display sNumSources
    ]
    
    /* 17. Add source error response.*/
	action addSourceErrorResponse [
    	auditLog(audit_payment.add_source_failure) [
    		primary  : sUserId
            secondary: sUserId
    		token
    	]
    	
    	switch UcPaymentAction.resolveErrorMessage(sErrorMessageType, msgError.sBody) [
    		case "success" jsonAddSourceErrorResponse
    		case "error" jsonAddSourceErrorResponse
    		default jsonAddSourceErrorResponse
    	]
	]
	
	/* 18. json add source error response.*/
    json jsonAddSourceErrorResponse [
    	display msgError.sBody
    ]
    
	/* 19. Get the wallet info for chosen token. */ 
    action getWalletInfo [	
		srGetWalletInfoParam.SOURCE_ID = token
		
		switch apiCall Payment.GetWalletByToken(srGetWalletInfoParam, srGetWalletInfoResult, ssStatus) [
		    case apiSuccess getWalletInfoSuccess
		    default genericErrorResponse
		]  
	]
	
	/* 20. Get wallet info success. */ 
	action getWalletInfoSuccess [
		methodNickName = srGetWalletInfoResult.SOURCE_NAME
		sPaymentMethodNickName = srGetWalletInfoResult.SOURCE_NAME
		sPaymentMethodType = srGetWalletInfoResult.SOURCE_TYPE
		sPaymentMethodAccount = srGetWalletInfoResult.SOURCE_NUM
		sSourceExpiry = srGetWalletInfoResult.SOURCE_EXPIRY
		sDefault  = srGetWalletInfoResult.SOURCE_DEFAULT

		goto(jsonGetWalletInfoResponse)
	]
	
	/* 21. Get wallet info json response. */
    json jsonGetWalletInfoResponse [
    	display sPaymentMethodNickName
    	display sPaymentMethodType
    	display sPaymentMethodAccount
    	display sSourceExpiry
    	display sDefault
    ]
	
    /* 22.Validate MakePaymentRequest */	
	action validatePayData [
		UcPaymentAction.setCurrency(sPayData, sCurrency)
		
		switch UcPaymentAction.validateMakePaymentRequest(sPayData) [
			case "valid" validateSurcharge
			case "pmtDateManipulated" makePaymentErrorResponse
			case "invalid" genericErrorResponse
			default genericErrorResponse
		]
	]	
	
    /* 23.validate the payment request if surcharge is true and the paymentMethod is credit */  
	action validateSurcharge [		
		if surchargeFlag == "true" then 
		   isPaymentMethodCredit
		else 
		   checkPayDate
	]    

    /* 24. is the payment Method credit or bank  */
	action isPaymentMethodCredit [
		if sPaymentMethodType == "credit" then 
		   validatePaymentRequest
		else 
			isUsingNewCreditCard
	]
	
	/* check whether this payment is using a new nonsaved credit card */
	action isUsingNewCreditCard [    
		if source_type == "credit" then			
			validatePaymentRequest
		else
			checkPayDate
	]
	
	/* 25. validate surcharge  */
	action validatePaymentRequest [		
		/*-- For a case when a new unsaved credit was used to make a payment, we need to set the sPaymentMethod to credit so notification will
		 *   send out the correct message to users
		 */
		
		sPaymentMethodType= "credit"
		
		switch SurchargeSession.validateRequest(sPayData) [
			case "true" checkPayDate
			case "false" genericErrorResponse
			case "invalid" genericErrorResponse
			default genericErrorResponse
		]				
	]
		
	
	/* 26. Checks the payment date is a future date. */
	action checkPayDate [
		UcPaymentAction.formatPayDate(fPayDate.aDate, sPaymentDate)
		
		switch UcPaymentAction.checkPayDate(sPaymentDate) [
			case "today" checkSurcharge
			case "future" setScheduledPayment
			default genericErrorResponse
		]
	]
	
	action checkSurcharge [
		if surchargeFlag == "true" then
			checkSourceType
		else
			submitPayment
	]
	
	action checkSourceType [
		if sPayDataSourceType == "debit" then
			addConvenienceFee
		else
			submitPayment
	]

	action addConvenienceFee [
//		srMakePaymentParam.FLEX_VALUE = "convenienceFee=true"
//		srMakePaymentParam.FLEX_DEFINITION = flexDefinition
		
		goto(submitPayment)
	]
 	
	/* 27. Submit payment to payment system. */
	action submitPayment [
		srMakePaymentParam.GROUPING_JSON = sPayData
		srMakePaymentParam.ONLINE_TRANS_ID = transactionId
		srMakePaymentParam.AMOUNT = sTotalPayAmt
		srMakePaymentParam.CURRENCY = sCurrency
		srMakePaymentParam.COMPANY_ID = sCompanyId 
		srMakePaymentParam.PMT_DATE = sPaymentDate
		srMakePaymentParam.USER_ID = sUserId
		srMakePaymentParam.TOKEN = token
		
		switch apiCall Payment.MakePayment(srMakePaymentParam, srMakePaymentResult, ssStatus) [
		    case apiSuccess updatePaymentHistorySuccess
		    default updatePaymentHistoryError
		]
	]
	
	/* 28. Insert a payment history record for success response. */
	action updatePaymentHistorySuccess [
    	srStartPaymentTransactionParam.TRANSACTION_ID      = transactionId
		srStartPaymentTransactionParam.ONLINE_TRANS_ID     = transactionId
		srStartPaymentTransactionParam.PMT_PROVIDER_ID     = sPayGroup
		srStartPaymentTransactionParam.GROUPING_JSON   	   = sPayData
		srStartPaymentTransactionParam.PAY_FROM_ACCOUNT    = sPayDataSourceName + "|" + sPayDataSourceType + "|" + sPayDataSourceAccount
		srStartPaymentTransactionParam.PAY_CHANNEL         = "online"
		srStartPaymentTransactionParam.PAY_DATE            = sPaymentDate
		srStartPaymentTransactionParam.PAY_AMT             = sTotalPayAmt		
		srStartPaymentTransactionParam.PAY_STATUS          = "posted"
		srStartPaymentTransactionParam.USER_ID             = sUserId
		
		switch apiCall Payment.StartPaymentTransaction(srStartPaymentTransactionParam, srStartPaymentTransactionResult, ssStatus) [
            case apiSuccess submitPaymentSuccessResponse
            default submitPaymentSuccessResponse
        ]	
    ]
    
	/* 29. Insert a payment history record for error response. */
	action updatePaymentHistoryError [
    	srStartPaymentTransactionParam.TRANSACTION_ID      = transactionId
		srStartPaymentTransactionParam.ONLINE_TRANS_ID     = transactionId
		srStartPaymentTransactionParam.PMT_PROVIDER_ID     = sPayGroup
		srStartPaymentTransactionParam.GROUPING_JSON       = sPayData
		srStartPaymentTransactionParam.PAY_FROM_ACCOUNT    = methodNickName 
		srStartPaymentTransactionParam.PAY_CHANNEL         = "online"
		srStartPaymentTransactionParam.PAY_DATE            = sPaymentDate
		srStartPaymentTransactionParam.PAY_AMT             = sTotalPayAmt		
		srStartPaymentTransactionParam.PAY_STATUS          = "failed"
		srStartPaymentTransactionParam.USER_ID             = sUserId
		
		switch apiCall Payment.StartPaymentTransaction(srStartPaymentTransactionParam, srStartPaymentTransactionResult, ssStatus) [
            case apiSuccess submitPaymentErrorResponse
            default submitPaymentErrorResponse
        ]	
    ]
	
	/* 30. Inserts a payment schedule record. */
	action setScheduledPayment [
		srSetScheduledParam.ONLINE_TRANS_ID     = transactionId
		srSetScheduledParam.GROUPING_JSON       = sPayData
		srSetScheduledParam.SOURCE_ID           = token
		srSetScheduledParam.PAY_TYPE            = "onetime"
		srSetScheduledParam.PAY_AMT             = sTotalPayAmt
		srSetScheduledParam.PAY_DATE            = sPaymentDate				
		srSetScheduledParam.PAY_STATUS          = "scheduled"		
		srSetScheduledParam.USER_ID             = sUserId
		srSetScheduledParam.SOURCE_DETAILS      = sPayDataSourceName + "|" + sPayDataSourceType + "|" + sPayDataSourceAccount
		
		switch apiCall Payment.SetScheduledPayment(srSetScheduledParam, srSetScheduledResult, ssStatus) [
            case apiSuccess setScheduledSuccessResponse
            default setScheduledErrorResponse
        ]	
    ]
    
    /* 31. Set scheduled payment success response. */
    action setScheduledSuccessResponse [
		responseCode = srSetScheduledResult.RESPONSE_CODE
		responseMessage = srSetScheduledResult.RESPONSE_MESSAGE
		transactionId = srSetScheduledResult.ONLINE_TRANS_ID
		
		PaymentAuditIterator.init(sGroupingDataString)
		goto(setScheduledSuccessAuditCheck)
	]
	
	action setScheduledSuccessAuditCheck [
		switch sAuditIteratorHasNext [
			case "true" setScheduledSuccessAudit
			case "false" setScheduledSuccessResponse2			
			default setScheduledSuccessResponse2
		]
	]
	
	action setScheduledSuccessAudit [
    	auditLog(audit_payment.scheduled_payment_success) [
    		primary  : sUserId
            secondary: sUserId
    		transactionId
    		token
    		sTotalPayAmt
    		sPaymentDate
    		sAuditIteratorNext
    	]
		
		goto(setScheduledSuccessAuditCheck)
	]
	
    action setScheduledSuccessResponse2 [
    	switch sOneTimeScheduledSuccessEmailFlag [
			case "true" sendScheduledSuccessEmail
			case "false" jsonSetScheduledSuccessResponse			
			default jsonSetScheduledSuccessResponse
		]
	]
	
	
	/* 29. Sends scheduled payment success email. */
	action sendScheduledSuccessEmail [
		
 		SurchargeSession.emptyMap()
		
		sNtfParams = "transactionId=" + transactionId + "|" + "sourceName=" + sPayDataSourceAccount + "|" +
		              "amount=" + sTotalPayAmt +  "|" + "sourceType=" + sPaymentMethodType + "|" + 
		              "surchargeFlag=" + surchargeFlag + "|"  + "surcharge=" + sSurCharge + "|" + "totalAmount=" + sTotalAmount + "|" +
		              "paymentDate=" + sPaymentDate + "|" + "groupingData=" + sGroupingDataString + "|" + "currency=" + sCurrency
		switch NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_onetime_scheduled_success") [
			case "success" jsonSetScheduledSuccessResponse
			default jsonSetScheduledSuccessResponse
		]
	]
	
	/* 30. Set scheduled payment error response. */
    action setScheduledErrorResponse [
		responseCode = srSetScheduledResult.RESPONSE_CODE
		responseMessage = srSetScheduledResult.RESPONSE_MESSAGE
		transactionId = srSetScheduledResult.ONLINE_TRANS_ID
		
		PaymentAuditIterator.init(sGroupingDataString)
		goto(setScheduledErrorAuditCheck)
	]
	
	action setScheduledErrorAuditCheck [
		switch sAuditIteratorHasNext [
			case "true" setScheduledErrorAudit
			case "false" jsonSetScheduledErrorResponse			
			default jsonSetScheduledErrorResponse
		]
	]
	
	action setScheduledErrorAudit [
    	auditLog(audit_payment.scheduled_payment_failure) [
    		primary  : sUserId
            secondary: sUserId
    		transactionId
    		token
    		sTotalPayAmt
    		sPaymentDate
    		sAuditIteratorNext
    	]
		
		goto(setScheduledErrorAuditCheck)
	]
	
	/* 31. Set scheduled payment success JSON response. */
    json jsonSetScheduledSuccessResponse [
    	display responseCode
    	display responseMessage
    	display transactionId
    ]
    
	/* 32. Set scheduled error JSON response. */
    json jsonSetScheduledErrorResponse [
    	display responseCode
    	display responseMessage
    	display transactionId
    ]

	/* 33. Submit payment success response. */
    action submitPaymentSuccessResponse [
		responseCode = srMakePaymentResult.RESPONSE_CODE
		responseMessage = srMakePaymentResult.RESPONSE_MESSAGE
		transactionId = srMakePaymentResult.ONLINE_TRANS_ID
		
		PaymentAuditIterator.init(sGroupingDataString)
		goto(submitPaymentSuccessAuditCheck)
	]
	
	action submitPaymentSuccessAuditCheck [
		switch sAuditIteratorHasNext [
			case "true" submitPaymentSuccessAudit
			case "false" submitPaymentSuccessResponse2			
			default submitPaymentSuccessResponse2
		]
	]
	
	action submitPaymentSuccessAudit [
		auditLog(audit_payment.ontime_payment_success) [
			primary  : sUserId
            secondary: sUserId
			transactionId
			token
			sTotalPayAmt
			sPaymentDate
			sAuditIteratorNext
		]
		
		goto(submitPaymentSuccessAuditCheck)
	]
	
    action submitPaymentSuccessResponse2 [
		switch sMakePaymentSuccessEmailFlag [
			case "true" sendOneTimeSuccessEmail
			case "false" jsonSubmitPaymentSuccessResponse			
			default jsonSubmitPaymentSuccessResponse
		]
	]
	
	/* 34. Sends one time payment success email. */
	action sendOneTimeSuccessEmail [
		UcPaymentAction.setCurrency(sGroupingDataString, sCurrency)
 		SurchargeSession.emptyMap()
 		
		sNtfParams = "transactionId=" + transactionId + "|" + 
					 "sourceName=" + sPayDataSourceAccount + "|" +
		             "amount=" + sTotalPayAmt + "|" +
					 "sourceType=" + sPaymentMethodType + "|" + 
					 "surchargeFlag=" + surchargeFlag + "|" + 
					 "surcharge=" + sSurCharge + "|" + 
					 "totalAmount=" + sTotalAmount + "|" +
		             "adjusted=false" + "|" + 
		             "payDate=" + sPaymentDate + "|" + 
		             "bills=" + sGroupingDataString + "|" +
		             "currency=" + sCurrency

		switch NotifUtil.sendRegisteredUserEmail(sUserId, sNtfParams, "payment_make_payment_success") [
			case "success" jsonSubmitPaymentSuccessResponse
			default jsonSubmitPaymentSuccessResponse
		]
	]
	
	/* 35. Submit payment success JSON response. */
    json jsonSubmitPaymentSuccessResponse [
    	display responseCode
    	display responseMessage
    	display transactionId
    ]
	
	/* 36. Submit payment error response. */
    action submitPaymentErrorResponse [
		responseCode = srMakePaymentResult.RESPONSE_CODE
		responseMessage = srMakePaymentResult.RESPONSE_MESSAGE
		transactionId = srMakePaymentResult.ONLINE_TRANS_ID
		 
		PaymentAuditIterator.init(sGroupingDataString)
		goto(submitPaymentErrorAuditCheck)
	]
	
	action submitPaymentErrorAuditCheck [
		switch sAuditIteratorHasNext [
			case "true" submitPaymentErrorAudit
			case "false" jsonSubmitPaymentErrorResponse			
			default jsonSubmitPaymentErrorResponse
		]
	]
	
	action submitPaymentErrorAudit [
    	auditLog(audit_payment.ontime_payment_failure) [
    		primary  : sUserId
            secondary: sUserId
    		transactionId
    		token
    		sTotalPayAmt
    		sPaymentDate
    		sAuditIteratorNext
    	]
		
		goto(submitPaymentErrorAuditCheck)
	]
    	
	/* 37. Submit payment error JSON response. */
    json jsonSubmitPaymentErrorResponse [
    	display responseCode
    	display responseMessage
    	display transactionId
    ]
    
    /* 38. Make payment error response. */
	action makePaymentErrorResponse [
    	switch UcPaymentAction.resolveErrorMessage(sErrorMessageType, msgError.sBody) [
    		case "success" jsonMakePaymentErrorResponse
    		case "error" jsonMakePaymentErrorResponse
    		default jsonMakePaymentErrorResponse
    	]
	]
	
	/* 39. Make payment error JSON response. */
    json jsonMakePaymentErrorResponse [
    	display msgError.sBody
    	//display msgError
    ]
	
	/* 40. Generic error response. */
	action genericErrorResponse [
    	switch UcPaymentAction.resolveErrorMessage(sErrorMessageType, msgError.sBody) [
    		case "success" jsonGenericErrorResponse
    		case "error" jsonGenericErrorResponse
    		default jsonGenericErrorResponse
    	]
	]
	
	/* 41. Generic error JSON response. */
	json jsonGenericErrorResponse [
        display msgError.sBody
	]
	
	/* 42. Go to automatic payment.*/
	action gotoAutomaticPayment [
		foreignHandler ForeignProcessor.writeResponse(sAutomaticPaymentShortcut)
	]
	
	/* 43. Write iframe response to client with freemarker form that autosubmits. */
	action createIframeAddSourceOnetime [
		foreignHandler UcPaymentAction.writeIframeAddSourceResponse("iframeAddSourceSubmit.ftl", sorrisoLanguage, sorrisoCountry, sUserId, sUserName, sIframeSourceType, sIframeOnetime, sAppType)
	]
	
	 action getWalletInfoForEdit [	
		srGetWalletInfoParam.SOURCE_ID = sPaymentSourceId
		
		switch apiCall Payment.GetWalletByToken(srGetWalletInfoParam, srGetWalletInfoResult, ssStatus) [
		    case apiSuccess getWalletInfoSuccessForEdit
		    default genericErrorResponse
		]  
	]
	
	/*  Get wallet info success. */ 
	action getWalletInfoSuccessForEdit [
		sNickName = srGetWalletInfoResult.SOURCE_NAME
		sIframeSourceType = srGetWalletInfoResult.SOURCE_TYPE
		sDefault = srGetWalletInfoResult.SOURCE_DEFAULT
		goto (createIframeEditSourceOnetime)
	]
	
	action createIframeEditSourceOnetime [
		foreignHandler UcPaymentAction.writeIframeEditSourceResponse("iframeEditSourceSubmit.ftl", sorrisoLanguage, sorrisoCountry, sUserId, sUserName, sIframeSourceType, sPaymentSourceId, sNickName, sDefault, sAppType)
	]
	
	/* 44. Go to the account overview page. */
    action gotoOverview [
        gotoModule(OVERVIEW)
    ]
    
]
