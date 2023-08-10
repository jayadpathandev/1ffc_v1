<#setting date_format="yyyyMMdd">		

<div id="payment_summary" class="st-payment-summary border border-3 border-dark p-3">

	<div class="row">
		<div class="col-8">
			<div class="mb-2">
				<span>Account #:</span> <span>${displayAccount}</span>
			</div>
			<div>
				<a class="me-4" target="_blank" href="fffcViewDoc?sAccount=${bill.internalAccountNo}&sDate=${bill.dateNum?c}&sStreamId=${bill.stream}&sDocId=${bill.id?c}&sExtDocId=${bill.extDocId}">View bill</a>
				<a class="me-4" href="startPaymentHistory">Transaction History</a>
				<a class="text-nowrap" href="startAutomaticPayment">Set&nbsp;up&nbsp;automatic&nbsp;payments</a>				
			</div>
		</div>
		<div class="col-4">
			<a class="btn btn-primary float-end" href="startMakePayment">PAY THIS BILL</a>
		</div>
	</div>
	
	<#if overdue>	
		<div class="text-center mt-3 border border-2 rounded-pill border-danger p-3">
			We want to remind you that 
			<span class="fw-bold text-decoration-underline">${formatUtils.formatAmount(amount?number)}</span>
			sum is due for payment since 
			<span class="fw-bold text-decoration-underline">${dueDate}</span>. 
			Please pay now.
		</div>
	</#if>
	
	<h2 class="mt-3 pt-3 border-top border-dark">
		${dueDate}
		<span class="float-end">${formatUtils.formatAmount(amount?number)}</span>
	</h2>
	
	<div class="mb-3 pb-3 border-bottom border-dark row">
		<div class="col">
			Monthly payment due date
		</div>
		<div class="col">
			<span class="float-end">Monthly payment amount</span>
		</div>
	</div>
	
	<div class="row">
		<div class="col-4">
			Personal loan amount $10,0000 <!-- Need to pull this from bill.flex? (Don't know which flex field.) -->
		</div>
		<div class="col-4 text-center">
			Principle Paid $8,0000 <!-- Need to pull this from bill.flex? (Don't know which flex field.) -->
		</div>
		<div class="col-4">
			Principle owed $2,0000 <!-- Need to pull this from bill.flex? (Don't know which flex field.) -->
		</div>
	</div>
</div>