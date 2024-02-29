useCase paymentRestBusiness [
	
	documentation [
		triggers: [[
			1. after business user login
		]]
		preConditions: [[
			1. business user is logged in successfuly
		]]
		postConditions: [[
			1. the application will use this usecase to run the payment rest calls
		]]
	]
	
	startAt sNoScreen
	
	shortcut internal getPaymentConfig(getPaymentConfig)
	
	shortcut internal getPaymentMethods(getPaymentMethods)
	
	shortcut internal getScheduledPayments(getScheduledPayments)
	
	shortcut internal getPaymentHistory(getPaymentHistory)
	
	shortcut internal deleteScheduledPaymentBusiness(deleteScheduledPayment) [sOnlineTransId]
	
	shortcut internal getPaySourceStatus(getPaySourceStatus) [sPaymentSourceId]
	
	shortcut internal getAutomaticPayment(getAutomaticPayment) [ sAutomaticId]
	shortcut internal getAutomaticPaymentByUserId(getAutomaticPaymentByUserId)
	shortcut internal getAutomaticPaymentByAccount(getAutomaticPaymentByAccount) [ sPayAccountInternal, sPayGroup, sPaymentSourceId, sUserId ]
	shortcut internal getAutomaticPaymentHistory(getAutomaticPaymentHistory) [ sAutomaticId, sUserId ]
	shortcut internal getCurrentBalanceConfig(getCurrentBalanceConfig)
	
	shortcut internal getCheckForScheduledPayments(getCheckForScheduledPayments) [ sSelectedAccountsDataString ]
	
	shortcut internal getPaymentRecord(getPaymentRecord) [sOnlineTransId]
	
	importJava PaymentBusinessAction(com.sorrisotech.uc.payment.PaymentBusinessAction)
	importJava Session(com.sorrisotech.app.utils.Session)
	importJava LocalizedFormat(com.sorrisotech.common.LocalizedFormat)
	importJava DateFormat(com.sorrisotech.common.DateFormat)
	importJava FffcAccountAction(com.sorrisotech.fffc.account.FffcAccountAction)
	
	import billCommon.sPayAccountInternal
    import billCommon.sPayAccountExternal
    import billCommon.sPayGroup
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
    import paymentCommon.sUserName
    import paymentCommon.sAutoPayUpto
    import paymentOneTime.sAutoScheduledFlag
    import paymentOneTime.sPmtScheduledFlag
    
	native string sAutomaticId = ""
	native string sOnlineTransId = ""
	native string sPaymentSourceId = ""
	native string sUserId        = Session.getUserId()
	native string sDeleteStatus  = ""
	native string sSelectedAccountsDataString
	native string sFormat = LocalizedFormat.toJsonString()
	native string sDateFormat = DateFormat.toJsonString()
	
	serviceStatus ssStatus
	
	serviceParam(Payment.GetHistory)  srHistoryParam
    serviceResult(Payment.GetHistory) srHistoryResult
	
	serviceParam(Payment.GetScheduledPayment)  srGetScheduledParam
    serviceResult(Payment.GetScheduledPayment) srGetScheduledResult
        
    serviceParam(Payment.DeleteScheduledPayment)  srDeleteScheduledParam
    serviceResult(Payment.DeleteScheduledPayment) srDeleteScheduledResult
    
    serviceParam(Payment.GetAutomaticPayment)  srGetAutomaticParam
    serviceResult(Payment.GetAutomaticPayment) srGetAutomaticResult
    
    serviceParam(Payment.GetAutomaticPaymentByUserId)  srGetAutomaticByUserIdParam
    serviceResult(Payment.GetAutomaticPaymentByUserId) srGetAutomaticByUserIdResult
    
    serviceParam(Payment.GetAutomaticPaymentByAccount) srGetAutomaticByAccountParam
	serviceResult(Payment.GetAutomaticPaymentByAccount) srGetAutomaticByAccountResult
    
    serviceParam(Payment.GetAutomaticPaymentHistory)  srGetAutomaticHistoryParam
    serviceResult(Payment.GetAutomaticPaymentHistory) srGetAutomaticHistoryResult
    
    serviceParam(Payment.GetPaymentRecord)  srGetPaymentRecordParam
    serviceResult(Payment.GetPaymentRecord) srGetPaymentRecordResult
    
    serviceResult(SystemConfig.GetCurrentBalanceConfig) srGetComm
    
    /* Table for upcoming payments.*/	
	table tScheduledPaymentsTable [
        "ONLINE_TRANS_ID"     => string sOnlineTransId          
        "BILLING_ACCOUNT_ID"  => string sAccId
        "INTERNAL_ACCOUNT_ID"  => string sIntAccId
        "DOCUMENT_NUMBER"     => string sDocumentId        
        "SOURCE_ID"   	      => string sSourceId                
        "PAY_TYPE"			  => string sPayType
        "PAY_AMT"    		  => string sPayAmount
        "PAY_AMT_NUM" 		  => number nPayAmount        
        "PAY_DATE"      	  => string sPayDate
        "PAY_DATE_NUM"   	  => number nPayDate
        "PAY_REQ_DATE"    	  => string sPayReqDate
        "EDIT_DATE"          => string sLastEditDate
        "EDIT_DATE_NUM"      => number nLastEditDate
        "SOURCE_NAME"   	  => string sSourceName
        "SOURCE_TYPE"   	  => string sSourceType
        "SOURCE_NUM"   	      => string sSourceNum 
    ]
    
    /* History table */    
    table tHistoryTable [
        "TRANSACTION_ID"     => string sTransId 
        "BILLING_ACCOUNT_ID" => string sAcctId
        "INTERNAL_ACCOUNT_ID"  => string sIntAccId
        "DOCUMENT_NUMBER"    => string sDocumentId
        "PAY_FROM_ACCOUNT"   => string sPayFrom
        "PAY_CHANNEL"        => string sPayChannel
        "PAY_DATE"           => string sPayDate
        "PAY_DATE_NUM"       => number nPayDate         
        "DUE_DATE"           => string sDueDate        
        "DUE_DATE_NUM"       => number nDueDate
        "PAY_AMT"            => string sPayAmount
        "PAY_AMT_NUM"        => number nPayAmount        
        "PAY_STATUS"         => string sPayStatus
        "USER_ID"            => string sUserId                           
    ]
    
    table tAutomaticPaymentsTable [
 		"AUTOMATIC_ID"       => string sAutomaticId
 		"GROUPING_JSON" 	 => string sGroupingJson
        "PMT_GROUP_ID"       => string sGroupId
        "SOURCE_ID"          => string sSourceId
        "SOURCE_NAME"        => string sSourceName
        "PAY_TRIGGER"        => string sPayTrigger
        "PAY_COUNT"          => string sPayCount
        "PAY_EXPIRY"         => string sPayExpiry
        "PAY_AMOUNT"         => string sPayAmount           
        "EDIT_DATE"          => string sLastEditDate
        "EDIT_DATE_NUM"      => number nLastEditDate
        "AUTO_STATUS"        => string sAutoStatus
        "USER_ID"            => string sUserId
    ]
    
    table tAutomaticPaymentsHistoryTable [
 		"AUTOMATIC_ID"   => string sAutomaticId
 		"CHANGE_DATE"    => string sChangeDate       
        "USERNAME"       => string sUserName
        "CONFIG_CHANGE"  => string sConfigChange
     ]
	
	/**********************************************************************************
	* Data definitions
	**********************************************************************************/
	
	xsltScreen sNoScreen("{Nothing}") [

	]
	
	/**********************************************************************************
	* Actions
	**********************************************************************************/	
	action getPaymentConfig [
		foreignHandler PaymentBusinessAction.getPaymentConfig(
												sorrisoLanguage,
												sorrisoCountry,
												sPayGroup,
												sCurrency,
												sScheduledDateWindow,
												sNumSources,
												sMaxSources,
												sPmtGroupConfigResult,
												sPaymentSourceBankEnabled,
												sPaymentSourceCreditEnabled,
												sPaymentSourceDebitEnabled,
												sPaymentSourceSepaEnabled,
												sAutoScheduledFlag,
												sPmtScheduledFlag,
												sCurrencySymbol,
												sAutoPayUpto)
	]
	
	action getPaymentMethods [
		foreignHandler PaymentBusinessAction.getPaymentMethods(sUserId)
	]
	
	action getPaymentHistory [
		srHistoryParam.USER_ID = sUserId
		srHistoryParam.FORMAT_JSON = sFormat
		switch apiCall Payment.GetHistory(srHistoryParam, srHistoryResult, ssStatus) [
		    case apiSuccess getPaymentHistoryJSON
		    default getPaymentHistoryJSON
		]
    ]
    
    action getPaymentHistoryJSON [
    	tHistoryTable = srHistoryResult.history
        foreignHandler PaymentBusinessAction.getPaymentHistory(tHistoryTable)
    ]
	
	action getScheduledPayments [
		srGetScheduledParam.USER_ID         = sUserId
		srGetScheduledParam.FORMAT_JSON     = sFormat
		switch apiCall Payment.GetScheduledPayment(srGetScheduledParam, srGetScheduledResult, ssStatus) [
		    case apiSuccess getScheduledResultJSON
		    default getScheduledResultJSON
		]
    ]
    
	action getScheduledResultJSON [
		tScheduledPaymentsTable = srGetScheduledResult.schedule
        foreignHandler PaymentBusinessAction.getScheduledPayments(tScheduledPaymentsTable)
    ]

	action getPaymentRecord [
		srGetPaymentRecordParam.USER_ID         = sUserId
		srGetPaymentRecordParam.PAYMENT_ID      = sOnlineTransId   
		switch apiCall Payment.GetPaymentRecord(srGetPaymentRecordParam, srGetPaymentRecordResult, ssStatus) [
		    case apiSuccess getPaymentRecordResultJSON
		    default getPaymentRecordResultJSON
		]
    ]
    
	action getPaymentRecordResultJSON [
        foreignHandler FffcAccountAction.getPaymentRecord(srGetPaymentRecordResult.PAY_DATE,    	srGetPaymentRecordResult.PAY_REQ_DATE,
        													  srGetPaymentRecordResult.PAY_STATUS,  	srGetPaymentRecordResult.PAY_FROM_ACCOUNT,
        													  srGetPaymentRecordResult.PAY_CHANNEL, 	srGetPaymentRecordResult.PAY_AMT,
        													  srGetPaymentRecordResult.PAY_SURCHARGE, 	srGetPaymentRecordResult.PAY_TOTAL_AMT,
        													  srGetPaymentRecordResult.FLEX1, 			srGetPaymentRecordResult.FLEX2, 		
        													  srGetPaymentRecordResult.FLEX3,           srGetPaymentRecordResult.FLEX4, 		
        													  srGetPaymentRecordResult.FLEX5,           srGetPaymentRecordResult.FLEX6, 		
        													  srGetPaymentRecordResult.FLEX7,           srGetPaymentRecordResult.FLEX8, 		
        													  srGetPaymentRecordResult.FLEX9,  		    srGetPaymentRecordResult.FLEX10,		
        													  srGetPaymentRecordResult.FLEX11, 		    srGetPaymentRecordResult.FLEX12, 		
        													  srGetPaymentRecordResult.FLEX13, 		    srGetPaymentRecordResult.FLEX14, 		
        													  srGetPaymentRecordResult.FLEX15, 		    srGetPaymentRecordResult.FLEX16, 		
        													  srGetPaymentRecordResult.FLEX17, 		    srGetPaymentRecordResult.FLEX18, 		
        													  srGetPaymentRecordResult.FLEX19, 		    srGetPaymentRecordResult.FLEX20,		
        													  srGetPaymentRecordResult.GROUPING,        sUserId
        													)
    ]    
        
    action deleteScheduledPayment [
		sDeleteStatus = "error"
		srDeleteScheduledParam.ONLINE_TRANS_ID = sOnlineTransId
		switch apiCall Payment.DeleteScheduledPayment(srDeleteScheduledParam, srDeleteScheduledResult, ssStatus) [
            case apiSuccess deleteScheduledPaymentSuccess
            default deleteScheduledPaymentError
        ]	
    ]
    
    action deleteScheduledPaymentSuccess [
		sDeleteStatus = "success"
        goto (deleteScheduledPaymentResponse)
    ]
    
    action deleteScheduledPaymentError [
        goto (deleteScheduledPaymentResponse)
    ]
    
    action deleteScheduledPaymentResponse [
        foreignHandler PaymentBusinessAction.getDeleteScheduledStatusMessage(sDeleteStatus)
    ]
   
    action getPaySourceStatus [
		foreignHandler PaymentBusinessAction.getPaySourceStatus(sUserId, sPaymentSourceId)
    ]
    
	action getAutomaticPayment [
		srGetAutomaticParam.USER_ID = sUserId
		srGetAutomaticParam.AUTOMATIC_ID = sAutomaticId
		srGetAutomaticParam.FORMAT_JSON  = sFormat
		switch apiCall Payment.GetAutomaticPayment(srGetAutomaticParam, srGetAutomaticResult, ssStatus) [
		    case apiSuccess getAutomaticPaymentJSON
		    default getAutomaticPaymentJSON
		]
    ]
    
    action getAutomaticPaymentJSON [
        foreignHandler PaymentBusinessAction.getCurrentAutomaticPayment(
        	srGetAutomaticResult.AUTOMATIC_ID,
        	srGetAutomaticResult.GROUPING_JSON,
        	srGetAutomaticResult.PAY_INVOICES_OPTION, 
        	srGetAutomaticResult.PAY_DATE,
        	srGetAutomaticResult.PAY_PRIOR_DAYS,
        	srGetAutomaticResult.EFFECTIVE_UNTIL_OPTION,
        	srGetAutomaticResult.EXPIRY_DATE,
        	srGetAutomaticResult.PAY_COUNT,
        	srGetAutomaticResult.PAY_AMOUNT_OPTION,
        	srGetAutomaticResult.PAY_UPTO,
        	sPayGroup,
        	sPaymentSourceId,
        	sUserId
        )
    ]
    
	action getAutomaticPaymentByUserId [
		srGetAutomaticByUserIdParam.USER_ID     = sUserId
		srGetAutomaticByUserIdParam.FORMAT_JSON = sFormat
		switch apiCall Payment.GetAutomaticPaymentByUserId(srGetAutomaticByUserIdParam, srGetAutomaticByUserIdResult, ssStatus) [
		    case apiSuccess getAutomaticPaymentByUserIdJSON
		    default getAutomaticPaymentByUserIdJSON
		]
    ]
    
    action getAutomaticPaymentByUserIdJSON [
    	tAutomaticPaymentsTable = srGetAutomaticByUserIdResult.automatic
        foreignHandler PaymentBusinessAction.getAutomaticPayment(tAutomaticPaymentsTable)
    ]
    
	action getAutomaticPaymentByAccount [
		srGetAutomaticByAccountParam.INTERNAL_ACCOUNT_ID = sPayAccountInternal
		srGetAutomaticByAccountParam.PMT_GROUP_ID = sPayGroup
		srGetAutomaticByAccountParam.SOURCE_ID = sPaymentSourceId
		srGetAutomaticByAccountParam.USER_ID = sUserId
		srGetAutomaticByAccountParam.FORMAT_JSON = sFormat

		switch apiCall Payment.GetAutomaticPaymentByAccount(srGetAutomaticByAccountParam, srGetAutomaticByAccountResult, ssStatus) [
		    case apiSuccess getAutomaticPaymentByAccountJSON
		    default getAutomaticPaymentByAccountJSON
		]
    ]
    
    action getAutomaticPaymentByAccountJSON [
        foreignHandler PaymentBusinessAction.getCurrentAutomaticPayment(
        	srGetAutomaticByAccountResult.AUTOMATIC_ID,
        	srGetAutomaticByAccountResult.GROUPING_JSON,
        	srGetAutomaticByAccountResult.PAY_INVOICES_OPTION, 
        	srGetAutomaticByAccountResult.PAY_DATE,
        	srGetAutomaticByAccountResult.PAY_PRIOR_DAYS,
        	srGetAutomaticByAccountResult.EFFECTIVE_UNTIL_OPTION,
        	srGetAutomaticByAccountResult.EXPIRY_DATE,
        	srGetAutomaticByAccountResult.PAY_COUNT,
        	srGetAutomaticByAccountResult.PAY_AMOUNT_OPTION,
        	srGetAutomaticByAccountResult.PAY_UPTO,
        	sPayGroup,
        	sPaymentSourceId,
        	sUserId
        )
    ]
    
	action getAutomaticPaymentHistory [
		srGetAutomaticHistoryParam.AUTOMATIC_ID = sAutomaticId
    	srGetAutomaticHistoryParam.USER_ID = sUserId
		srGetAutomaticHistoryParam.DATE_FORMAT = sDateFormat
    	switch apiCall Payment.GetAutomaticPaymentHistory(srGetAutomaticHistoryParam, srGetAutomaticHistoryResult, ssStatus) [
		    case apiSuccess getAutomaticPaymentHistoryJSON
		    default getAutomaticPaymentHistoryJSON
		]
    ]
    
    action getAutomaticPaymentHistoryJSON [
    	tAutomaticPaymentsHistoryTable = srGetAutomaticHistoryResult.history
        foreignHandler PaymentBusinessAction.getAutomaticPaymentHistory(tAutomaticPaymentsHistoryTable)
    ]
    
    action getCurrentBalanceConfig [
        switch apiCall SystemConfig.GetCurrentBalanceConfig(srGetComm, ssStatus) [
            case apiSuccess getCurrentBalanceConfigJSON
            default getCurrentBalanceConfigJSON
        ]
    ]
    
    action getCurrentBalanceConfigJSON [
    	foreignHandler PaymentBusinessAction.getCurrentBalanceConfig(srGetComm.RSP_CURBALTYPE)
    ]
    
    action getCheckForScheduledPayments [
		foreignHandler PaymentBusinessAction.getCheckForScheduledPayments(sUserId, sSelectedAccountsDataString)
	]
]