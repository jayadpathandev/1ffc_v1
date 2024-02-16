<!DOCTYPE html SYSTEM "about:legacy-compat">
<html>
	<head>
		<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta content="IE=Edge" http-equiv="X-UA-Compatible">
		<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
		<meta charset="utf-8">
		
		<title>Select payment method</title>
		
		<link href="css/lib/bootstrap.min.css" rel="stylesheet" type="text/css"/>
		<link href="css/lib/bootstrap-toggle.min.css" rel="stylesheet" type="text/css"/>
        <link href="css/lib/font-awesome.min.css" rel="stylesheet"/>				
        <link href="css/lib/jquery-ui.min.css" rel="stylesheet"/>
        <link href="css/lib/jquery-ui.theme.min.css" rel="stylesheet"/>
		
		<link href="css/brand.css" rel="stylesheet" type="text/css"/>
		<link href="css/customization.css" rel="stylesheet" type="text/css"/>
		<link href="css/app.css" rel="stylesheet" type="text/css"/>
		<link href="css/print.css" media="print" rel="stylesheet" type="text/css"/>
		<link href="css/userProfile.css" rel="stylesheet" type="text/css"/>
		
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
        <script src="js/lib/bootstrap.bundle.min.js"></script>
	</head>
	<body>
		<div class="apipay">
			<h4>
				Payment method
			</h4>
			<p class="help">
				Choose the payment method for this
				<#if type == 'automatic'>recurring</#if>
				payment.
			</p>
			<div class="row">
				<div class="col-4">
					<label class="form-label">
						Pay using:
					</label>
				</div>
			</div>
			<div class="row mb-3">
				<div class="col-4">
					<#if hasWallet>
						<select class="form-control form-select" name="wallet" id="apipay_wallet">
							<#list wallet as item>
								<option value="${item.val}" <#if walletItem == item.val>selected</#if>>${item.text}</option>
							</#list>
						</select>
					</#if>
				</div>
				<div class="col-4">
					<a class="btn btn-primary" href="startChooseNew?itemType=bank">Use a new bank account</a>
				</div>
				<div class="col-4">
					<a class="btn btn-primary" href="startChooseNew?itemType=debit">Use a new debit card</a>
				</div>
			</div>
			<#if iframe == "bank">
				<iframe src="startAddSourceBank" >
				</iframe>
			<#elseif iframe == "debit">
				<iframe src="startAddSourceDebit">
				</iframe>
			</#if>
			<a id="internal_error" href="startChooseSourceFailure"></a>
		</div>
		<script>
			$(function() {
				$('#apipay_wallet').on('change', function() {
					var item = $(this).val();
					document.location.href = 'startUseSource?walletItem=' + encodeURIComponent(item);
				});
				function success(data) {
					$.ajax({
						url: 'startUseSource?walletItem=' + encodeURIComponent(data.token),
						type: 'get',
						success: function() {
							document.location.href = "startChooseSource?code=${code?c}"
						}
					});					
				}
				function failure() {
					document.location.href = "startChooseSourceFailure"					
				}
				function cancel() {
					document.location.href = "startChooseSource?code=${code?c}"					
				}
				
				window.handleAddSourceSuccessResponseCallback = function(data) { success(data); }
				window.handleAddSourceErrorResponseCallback = function() { failure(); }
				window.handleErrorResponseCallback = function() { failure(); }
				window.handleCancelResponseCallback = function() { cancel(); }						
			});
		</script>
	</body>
</html>
