
<#-- 
	   File: paymentSummary.ftl
	   Date: September 11, 2023
	   Updated by: John A. Kowalonek
	   Purpose: summary freemarker template for 1st Franklin application
	   
	   2023-Sep-11	jak-- first major iteration, handles all the messages we can
	   						think of today.. unit tested without all the use case data
	   						but with 1st Franklin styled bill data. 

  -->

<#--  setting the date format here for just this template... was having a problem with the date 
		handed down from the use case so using bill.dueDate instead of the dueDate stuffed into
		the variables directly. There was some kind of locale problem with my setup (I'm guessing) 
		because all dueDate had curly braces around it. -->
<#setting date_format="MM/dd/yyyy">

<#--  ********************************************************************************************
		The settings here are used during testing and should be passed down from the use case.
		But...we haven't built that yet so they are currently set to NOT interfere with a normal bill
		operation... 
	  ******************************************************************************************** -->

<#--   ** CONTROLS ACCOUNT RESTRICTION BEHAVIOR **  passed in to the template by use case and obtained from the status feed-->
<#assign bAccessDisabledCollections = false> 	<#-- true if account is in collections -->
<#assign bACHDisabled = false>					<#-- true if ACH is disabled -->
<#assign bPaymentDisabled = false>				<#-- true if payment is disabled -->
<#assign sPaymentDisabledReason = "none">  		<#--  valid reasons are 'none', 'delinquency', or 'lastpayment' -->


<#--  ** CONTROLS SCHEDULED AND AUTOMATIC PAYMENT MESSAGES ** passed in to the template by use case -->
<#assign sScheduledPaymentCount = "none"> 			<#-- not really a count but an enum "none", "one", "multiple" -->
<#assign dScheduledPaymentDate = "09/11/2023">		<#-- the last schedule payment date for all one time scheduled payments before due date-->
<#assign nScheduledPaymentAmount = "1022.00">		<#-- the total amount of all scheduled payments -->
<#assign bScheduledPaymentsLate = true> 			<#-- true if the total of all scheduled payments before payment due date + the automatic payment won't work -->
<#assign bAutomaticPaymentScheduled = false>		<#-- true if there's an automatic payment scheduled for this account -->
<#assign dAutomaticPaymentDate = "09/15/2023">		<#-- date of the automatic payment -->
<#assign nAutomaticPaymentAmount = "995.10">		<#-- amount of the automatic payment -->

<#--  ** ACCOUNT STATUS INFORMATION FOR STATEMENT MESSAGES ** passed nto the template by use case -->
<#assign sBillAvailable = "yes"> <#-- "yes", "no-NewAccount", "no-PaidOff", "no-Unknown" -->

<#--  ********************************************************************************************
	  END - Settings for testing that should come from use case 
	  ******************************************************************************************** -->
	  
<#--   ** VARIABLES THAT CONTROL STATEMENT MESSAGE BEHAVIOR ..

	   This odd conversion of .now to a string with date format and then back to a date 
		allowS us to compare our "pure" date formatted dueDate when other mathmatical forms do
		not. So yeah... if for some reason, .now can't convert to a comparison string directly.. same date but keeps time.

		The test returns 0(false) if due date is is today or in the future, 1(true) if we are passed the due date -->
<#assign bPastDueDate = bill.dueDate?date < .now?string["MM/dd/yyyy"]?date />
<#assign bBillHasOverdue = overdue>


<#--  ** DATA VALUES USED IN THE TEMPLATE BUT MAY CHANGE WHERE THEY COME FROM IN THE FUTURE ** -->
<#assign nAmountDue = amount> 				<#--  probably want to change what the use case sends down -->
<#assign dDueDate = bill.dueDate?date>		<#--  we use this everywhere so make it a variable -->
<#if bill.flex12?has_content && bill.flex12?string?trim != ''>  <#-- checks to see if loan amount exists -->
	<#assign nLoanAmount = bill.flex12>
<#else>
	<#assign nLoanAmount = "0.00">
</#if>

<#-- ***************************** LET THE GAMES BEGIN ******************************************** -->

<div class="st-payment-summary border border-5 rounded-3 border-primary p-3">

	<div class="row">
		<div class="col-10">
			<div class="mb-2">
				<span class="fw-bold">Account #:</span> <span class="fw-bold">${displayAccount}</span>

			</div>
			<div>
				<#--  IF WE'VE DISABLED THE ACCOUNTS, THE LINKS DISAPPER, IF Not Disabled then we do it based on
						account status information -->
				<#if !bAccessDisabledCollections>
					<#switch sBillAvailable>
						<#case "yes">
							<#-- All links enabled -->
							<a class="me-4" target="_blank" href="fffcViewDoc?sAccount=${bill.internalAccountNo}&sDate=${bill.dateNum?c}&sStreamId=${bill.stream}&sDocId=${bill.id?c}&sExtDocId=${bill.extDocId}">View statement</a>
							<a class="me-4 text-nowrap" href="startPaymentHistory">Transaction history</a>
							<a class="text-nowrap" href="startAutomaticPayment">Set&nbsp;up&nbsp;automatic&nbsp;payments</a>				
							<#break>
						<#case "no-NewAccount">
							<#-- All links disabled -->
							<a class="me-4 disabled" aria-disabled="true" target="_blank" href="fffcViewDoc?sAccount=${bill.internalAccountNo}&sDate=${bill.dateNum?c}&sStreamId=${bill.stream}&sDocId=${bill.id?c}&sExtDocId=${bill.extDocId}">View statement</a>
							<a class="me-4 text-nowrap disabled" aria-disabled="true" href="startPaymentHistory">Transaction History</a>
							<a class="text-nowrap disabled" aria-disabled="true"href="startAutomaticPayment">Set&nbsp;up&nbsp;automatic&nbsp;payments</a>				
							<#break>
						<#case "no-PaidOff">
							<#--  bill linke and automatic payment link disabled -->
							<a class="me-4 disabled" aria-disabled="true" target="_blank" href="fffcViewDoc?sAccount=${bill.internalAccountNo}&sDate=${bill.dateNum?c}&sStreamId=${bill.stream}&sDocId=${bill.id?c}&sExtDocId=${bill.extDocId}">View statement</a>
							<a class="me-4 text-nowrap" href="startPaymentHistory">Transaction History</a>
							<a class="text-nowrap disabled" aria-disabled="true"href="startAutomaticPayment">Set&nbsp;up&nbsp;automatic&nbsp;payments</a>				
							<#break>
						<#case "unknown">
						<#default>
							<#--  IF THERE ARE NO STATUS FOR BILL AVAILABLE THAT MAKES SENSE, DON'T SHOW THE LINKS -->
					</#switch>
				</#if>
			</div>
		</div>
		<div class="col-2">
			<a class="btn btn-primary float-end <#if bPaymentDisabled>disabled</#if>" href="startMakePayment" <#if bPaymentDisabled>aria-disabled="true"</#if>>PAY THIS BILL</a>
		</div>
	</div>
	
	<#--  HANDLE THE CASE WHERE THIS ACCOUNT HAS GONE INTO COLLECTIONS -->
	<#if bAccessDisabledCollections>
		<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
			Your account is in collections and online access is disabled. Visit or call your local branch immediately to make payment arrangements.
		</div>
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold text-center">
				Account access denied.
			</div>
		</h2>
	<#elseif "yes" == sBillAvailable>
		<#-- NORMAL CASE WHERE THERE IS A BILL AND WE NEED TO MESSAGE IT --> 
		
		<#--  HANDLE CASE WHERE PAYMENT HAS BEEN DISABLED FOR THIS ACCOUNT -->
		<#if bPaymentDisabled>

			<#switch sPaymentDisabledReason>
				<#case "delinquency">
					<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
						Your account is currently delinquent and online payments including any scheduled or recurring payments are disabled.
						Visit or contact your local branch now to make a payment.
					</div>
					<#break>
	
				<#case "lastpayment">
					<#if !bBillHasOverdue>
						<#--  last payment with no overdue amount -->
						<div class="text-center mt-3 border border-2 rounded-pill border-info p-3">
							Contratulations! your final payment of 
							<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span>
							is due on <span class="fw-bold text-decoration-underline">${dDueDate?date}</span>. Visit your local
							branch to make this payment and close your account.
						</div>
					<#else>
					<#--  last payment with an overdue amount -->
						<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
							Contratulations! your final payment of 
							<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span>
							is due on <span class="fw-bold text-decoration-underline">${dDueDate?date}</span> and includes an
							overdue amount of <span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(bill.flex17?number)}</span>.
							Visit your local branch now to make this payment, close your account, and avoid addiitonal charges.
						</div>
					</#if>
					<#break>
				
				<#default>
					<#--  ok.. we don't know why payments been disabled -->
					<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
						Payment has been disabled for this account. Visit or contact your local branch now to arrange payment.
					</div>
			</#switch>
		<#else>
		<#--  HANDLE CASES WHERE PAYMENT HAS NOT BEEN DISABLED -->
		
			<#-- HANDLE CASE WHERE ACH IS DISABLED -->
			<#if bACHDisabled> <#-- This message shows when ach is disabled along with other payment and bill messages -->
				<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
					Payments made using your bank account number have failed several times, so direct debit (ACH)
					by by bank account is disabled and any associated scheduled and recurring payments are cancelled.
					Contact your local branch to resolve this. You can pay by debit card if there are sufficient funds
					in your account.
				</div>
			</#if>				

			<#-- HANDLE ONE TIME PAYMENTS SCHEDULED -->
			<#switch sScheduledPaymentCount>	<#--  The customer has one or more scheduled payments in the queue -->
				<#case "one">
					<#--  Note that not enough turn it into danger from info message -->
					<div class="text-center mt-3 border border-2 rounded-pill <#if bScheduledPaymentsLate>border-danger<#else>border-info</#if> p-3">
						You have scheduled a payment of
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nScheduledPaymentAmount?number)}</span>
						for this account on <span class="fw-bold text-decoration-underline">${dScheduledPaymentDate?date}</span>.
						<#if bScheduledPaymentsLate> <#--  if true, this payment and automatic aren't enough -->
							This payment and any automatic payment currently scheduled will not meet your obligation to pay
							<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span> by
							<span class="fw-bold text-decoration-underline">${dDueDate?date}</span>.
						</#if>
					</div>
					<#break>

				<#case "multiple">
					<#--  Note that not enough turn it into danger from info message -->
					<div class="text-center mt-3 border border-2 rounded-pill <#if bScheduledPaymentsLate>border-danger<#else>border-info</#if> p-3">
						You have scheduled multiple payments totaling
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nScheduledPaymentAmount?number)}</span>
						for this account with last payment on <span class="fw-bold text-decoration-underline">${dScheduledPaymentDate?date}</span>.
						<#if bScheduledPaymentsLate> <#--  if true these payments and scheduled aren't enough -->
							These payments and any automatic payment currently scheduled will not meet your obligation to pay
							<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span> by
							<span class="fw-bold text-decoration-underline">${dDueDate?date}</span>.
						</#if>
					</div>
					<#break>
				
				<#case "none">
					<#break>
				
				<#case default>
					<#--  covers the case where someone screwed up the use case should I put up an error message?-->
			</#switch>
			
			<#--  HANDLE AUTOMATIC PAYMENT SCHEDULES -->
			<#if bAutomaticPaymentScheduled> <#--  The customer has an automatic payment that's scheduled -->
				<div class="text-center mt-3 border border-2 rounded-pill <#if bScheduledPaymentsLate>border-danger<#else>border-info</#if> p-3">
					You have an automatic payment of
					<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAutomaticPaymentAmount?number)}</span>
					scheduled for <span class="fw-bold text-decoration-underline">${dAutomaticPaymentDate?date}</span>.
					<#if bScheduledPaymentsLate>
						This payment and any other payments currently scheduled will not meet your obligation to pay
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span> by
						<span class="fw-bold text-decoration-underline">${dDueDate?date}</span>.
					
					</#if>
				</div>
			</#if>
			
			<#-- HANDLE THE CASE WHERE THERE'S NO PAYMENTS OF ANY KIND SCHEDULED -->
			<#if ("none" == sScheduledPaymentCount && !bAutomaticPaymentScheduled) > 
				
				<#--  HANDLE THE CASE WHERE THE CURRENT BILL IS PAST ITS DUE DATE BUT A NEW BILL HASN'T ARRIVED -->
				<#if bPastDueDate> 
					<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
						We want to remind you that 
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span>
						was due for payment on <span class="fw-bold text-decoration-underline">${dDueDate?date}</span>.
						Please pay now.
					</div>
				<#-- HANDLE THE CASE WHERE THERE'S AN OVERDUE AMOUNT ON THE CURRENT BILL -->
				<#elseif bBillHasOverdue>	
					<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
						We want to remind you that 
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span>
						is due for payment on <span class="fw-bold text-decoration-underline">${dDueDate?date}</span> 
						and includes an overdue amount of 
						<span class="fw-bold text-decoration-underline">
							<#if bill.flex17??>
								${formatUtils.formatAmount(bill.flex17?number)}
							<#else>
								Flex 17
							</#if>								
						</span>.
						Please pay now to avoid additional charges.
					</div>
				<#--  HANDLE GOOD OLD BILL THAT'S NOT LATE AND DOESN'T CONTAIN ANY OVERDUE AMOUNT -->
				<#else> 
					<div class="text-center mt-3 border border-2 rounded-pill border-info p-3">
						Your next payment of 
						<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(nAmountDue?number)}</span>
						is due on <span class="fw-bold text-decoration-underline">${dDueDate?date}</span>. 
					</div>
				</#if>	
			</#if> <#--  ("none" == sScheduledPaymentCount && !bAutomaticPaymentScheduled) -->
		</#if> <#-- bPaymentDisabled -->
		
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold">
				${formatUtils.formatAmount(nLoanAmount?number)} <!--  original loan amount -->
			</div>
			<div class="col fw-bold text-center">
				${dDueDate?date}
			</div>
			<div class="col">
				<span class="float-end fw-bold">${formatUtils.formatAmount(nAmountDue?number)}</span>
			</div>
		</h2>
		
		<div class="row">
			<div class="col">
				Original personal loan amount
			</div>
			<div class="col text-center">
				Monthly payment due date
			</div>
			<div class="col">
				<span class="float-end">Statement amount due</span>
			</div>
		</div>
	<#elseif "no-NewAccount" == sBillAvailable >
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold text-center">
				Congratulations, and thank you for opening your loan account with 1st Franklin! 
			</div>
		</h2>
		<div class="row">
			<div class="col text-center">
				You don't have a statement yet, we'll notify you by email when your first statement is available.
			</div>
		</div>
	<#elseif "no-PaidOff" == sBillAvailable >
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold text-center">
				Congratulations! You've paid off this loan and the account is now closed.
			</div>
		</h2>
		<div class="row">
			<div class="col text-center">
				Your account history is still available.
			</div>
		</div>
	<#else>
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col text-center">
				Unknown bill status, please contact your local branch.
			</div>
		</h2>
	</#if> <#-- bAccessDisabledcollections -->
</div>
