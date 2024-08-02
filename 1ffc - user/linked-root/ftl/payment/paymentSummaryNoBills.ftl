
<#-- 
	   File: paymentSummaryNoBills.ftl
	   Date: December 11, 2023
	   Author: John A. Kowalonek
	   Purpose: summary freemarker template for 1st Franklin application, case
	   		where there are no relevant bills or no bills at all... account is
	   		in status:
	   			newAccount
	   			closedAccount
	   
	   2023-Dec-11	jak-- first iteration
	   2024-Feb-19 	jak --changed wording for old bill
	   2024-Apr-17	jak -- adjusted so customers without bills could pay
	   2024-May-01	jak	-- fixed payment links to properly use offset variable
  -->

<#--  "debug" -- if true will show the status and scheduled payment stuff on the screen for visual validation
	   	 of the settings -->
	   	 
<#assign debug = false>


<#--  Test to ensure the account status stuff came down ok.. its "belt and suspenders" this was already
		done at the use case level but one more time will prevent user seeing something stupid if
		bad stuff happened we need to convert second level variables to first level so we can write 
		to them (this is a limitation of freemarker, you can't change "second level" variable, just
		read them -->

<#--  This template only deals with account status and case where view account is false -->
<#assign accountStatus = "unknown">
<#assign viewAccount = "unknown">

<#if status.accountStatus?has_content>
	<#assign accountStatus = status.accountStatus>
</#if>
<#if status.viewAccount?has_content>
	<#assign viewAccount = status.viewAccount>
</#if>

<#-- ***************************** LET THE GAMES BEGIN ******************************************** -->

<div class="st-payment-summary border border-5 rounded-3 border-primary p-3 mb-3">
		<#if debug> <#--  shows extra varaibles if true -->
	 <table class="table">
	  <thead>
	  	<th scope="col">Item</th>
	  	<th scopy="col">Value</th>
	  </thead>
	  <tbody>
	  	<tr>
	  		<td><span class="fw-bold">Account Status:</span></td>
	  		<td><span class="fw-bold">${accountStatus}</span></td>
	  	</tr>
		<tr>
			<td><span class="fw-bold">View Account:</span></td>
			<td><span class="fw-bold">${viewAccount}</span></td>
		</tr>
	 </table>
	</#if>

	<div class="row">
		<div class="col-7 col-sm-8 col-md-9 col-lg-10">
			<div class="mb-2">
				<span class="fw-bold">Account number:</span> <span class="fw-bold">${nickname.displayAccount}&nbsp</span>
				<#if nickname.url?? && (nickname.url?length > 0)>
					<a class="payment-edit-img st-left-space" target="_blank" "href="#" st-pop-in="${nickname.url}"></a>
				</#if>
				
			</div>
			<div>
				<#--  IF WE'VE DISABLED VIEW ACCOUNTS, THE LINKS DISAPPER, IF Not Disabled then we do it based on
						account status information -->
				<#if "enabled" == viewAccount>
					<#switch accountStatus>
						<#case "newAccount">
							<#--  All links disabled -->
							<a class="me-4 disabled pe-none opacity-50" aria-disabled="true">View statement</a>
							<#-- <a class="me-4 text-nowrap disabled pe-none opacity-50" aria-disabled="true" >Transaction History</a> -->
							<#--  Automatic (recurring) payments enable/disable control driven by status, currency of account, and if there's already
											and automatic payment rule set  -->
							<#if status.bAutoPayLinkEnabled>
								<#--  automatic payment is enabled -->
								<a class="text-nowrap" href="jumpToAutoPay?offset=${jumpToOffset}">Set&nbsp;up&nbsp;recurring&nbsp;payments</a>				
							<#else>
								<#--  automatic payment is disabled -->
								<a class="text-nowrap disabled pe-none opacity-50" aria-disabled="true" "ms-2 text-info float-end" data-bs-toggle="tooltip" data-bs-placement="right" data-bs-trigger="hover focus" title=""
								    data-bs-original-title="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details."
								    aria-label="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details.">
									Set&nbsp;up&nbsp;recurring&nbsp;payment
									<span sorriso="icon-info">
								        <svg aria-hidden="true" role="img" class="octicon octicon-info" viewBox="0 0 16 16"
								            width="16" height="16" fill="currentColor"
								            style="display: inline-block; user-select: none; vertical-align: text-bottom; overflow: visible;">
								            <path fill-rule="evenodd"
								                d="M8 1.5a6.5 6.5 0 100 13 6.5 6.5 0 000-13zM0 8a8 8 0 1116 0A8 8 0 010 8zm6.5-.25A.75.75 0 017.25 7h1a.75.75 0 01.75.75v2.75h.25a.75.75 0 010 1.5h-2a.75.75 0 010-1.5h.25v-2h-.25a.75.75 0 01-.75-.75zM8 6a1 1 0 100-2 1 1 0 000 2z"></path>
								        </svg>
								    </span>
								</a>																				
							</#if>
							<#break>
						<#case "activeAccount">
							<a class="me-4 disabled pe-none opacity-50" aria-disabled="true">View statement</a>
							<#-- <a class="me-4 text-nowrap" href="#" st-pop-in="fffcViewTransactions?offset=${jumpToOffset}">Transaction History</a> -->
								<#--  Automatic (recurring) payments enable/disable control driven by status, currency of account, and if there's already
											and automatic payment rule set  -->
							<#if status.bAutoPayLinkEnabled>
								<#--  automatic payment is enabled -->
								<a class="text-nowrap" href="overviewJumpToAutoPay?offset=${jumpToOffset}">Set&nbsp;up&nbsp;recurring&nbsp;payments</a>				
							<#else>
								<#--  automatic payment is disabled -->
								<a class="text-nowrap disabled pe-none opacity-50" aria-disabled="true" "ms-2 text-info float-end" data-bs-toggle="tooltip" data-bs-placement="right" data-bs-trigger="hover focus" title=""
								    data-bs-original-title="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details."
								    aria-label="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details.">
									Set&nbsp;up&nbsp;recurring&nbsp;payment
									<span sorriso="icon-info">
								        <svg aria-hidden="true" role="img" class="octicon octicon-info" viewBox="0 0 16 16"
								            width="16" height="16" fill="currentColor"
								            style="display: inline-block; user-select: none; vertical-align: text-bottom; overflow: visible;">
								            <path fill-rule="evenodd"
								                d="M8 1.5a6.5 6.5 0 100 13 6.5 6.5 0 000-13zM0 8a8 8 0 1116 0A8 8 0 010 8zm6.5-.25A.75.75 0 017.25 7h1a.75.75 0 01.75.75v2.75h.25a.75.75 0 010 1.5h-2a.75.75 0 010-1.5h.25v-2h-.25a.75.75 0 01-.75-.75zM8 6a1 1 0 100-2 1 1 0 000 2z"></path>
								        </svg>
								    </span>
								</a>			
							</#if>
							<#break>
						<#case "closedAccount">
							<#-- Other links enabled, payment link disabled -->
							<a class="me-4 disabled pe-none opacity-50" aria-disabled="true">View statement</a>
							<#-- <a class="me-4 text-nowrap" href="#" st-pop-in="fffcViewTransactions?offset=${jumpToOffset}">Transaction History</a> -->	
							<a class="text-nowrap disabled pe-none opacity-50" aria-disabled="true" "ms-2 text-info float-end" data-bs-toggle="tooltip" data-bs-placement="right" data-bs-trigger="hover focus" title=""
							    data-bs-original-title="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details."
							    aria-label="If you are unable to set up a recurring payment, it may be because you already have a recurring payment scheduled or due to other reasons. Please contact your branch for further details.">
								Set&nbsp;up&nbsp;recurring&nbsp;payment
								<span sorriso="icon-info">
							        <svg aria-hidden="true" role="img" class="octicon octicon-info" viewBox="0 0 16 16"
							            width="16" height="16" fill="currentColor"
							            style="display: inline-block; user-select: none; vertical-align: text-bottom; overflow: visible;">
							            <path fill-rule="evenodd"
							                d="M8 1.5a6.5 6.5 0 100 13 6.5 6.5 0 000-13zM0 8a8 8 0 1116 0A8 8 0 010 8zm6.5-.25A.75.75 0 017.25 7h1a.75.75 0 01.75.75v2.75h.25a.75.75 0 010 1.5h-2a.75.75 0 010-1.5h.25v-2h-.25a.75.75 0 01-.75-.75zM8 6a1 1 0 100-2 1 1 0 000 2z"></path>
							        </svg>
							    </span>
							</a>			
							<#break>
						<#case "unknown">
						<#default>
							<#--  IF THERE ARE NO STATUS FOR BILL AVAILABLE THAT MAKES SENSE, DON'T SHOW THE LINKS -->
					</#switch>
				</#if>
			</div>
		</div>
		<div class="col-5 col-sm-4 col-md-3 col-lg-2">
			<a class="btn btn-primary <#if !status.bPayEnabled>disabled</#if>" href="overviewJumpToPayment?offset=${jumpToOffset}" 
										<#if !status.bPayEnabled>disabled="true"</#if>>PAY THIS BILL</a>
		</div>
	</div>
	
	<#--  HANDLE THE CASE WHERE THIS ACCOUNT HAS GONE BECAUSE OF FRAUD -->
	<#if !("enabled" == viewAccount)>
		<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
			Your online account access is disabled. Visit or call your local branch immediately to make payment arrangements.
		</div>
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold text-center">
				Account access denied.
			</div>
		</h2>
	<#elseif "newAccount" == accountStatus >
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
	<#elseif "closedAccount" == accountStatus >
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
	<#elseif "activeAccount" == accountStatus >
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col fw-bold text-center">
				We are unable to display a copy of your statement at this time. Please visit or call your local branch at 1-888-504-6520 for a copy of your statement.
			</div>
		</h2>
		<div class="row">
			<div class="col text-center">
				Please visit or call your local branch so we can correct this error.
			</div>
		</div>
	<#else>
		<h2 class="mt-3 pt-3 border-top border-dark row">
			<div class="col text-center">
				Unknown bill status, please contact your local branch.
			</div>
		</h2>
	</#if> <#-- !("enabled" == viewAccount) -->
</div>
