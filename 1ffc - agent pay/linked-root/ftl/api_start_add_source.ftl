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
		<#if error != "">
			<div class="alert alert-warning" role="alert alert-danger">
				<h4>
					<span>Data entry error</span>
				</h4>
				<p>
					${error}
				</p>
			</div>
		</#if>
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
				
				<#if !disableAch>
					<div class="col-4">
						<a class="btn btn-primary" href="startChooseNew?itemType=bank">Use a new bank account</a>
					</div>
				</#if>
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
				window.payment_size = function(size) {
					$('iframe').height(size);
				}
			
				$('#apipay_wallet').on('change', function() {
					var item = $(this).val();
					document.location.href = 'startUseSource?walletToken=' + encodeURIComponent(item);
				});
				function success(data) {
					$.ajax({
						url: 'startNewSource' + 
							 '?walletType=' + encodeURIComponent(data.sourceType) +
							 '&walletAccount=' + encodeURIComponent(data.sourceNum) +
							 '&walletExpiry=' + encodeURIComponent(data.sourceExpiry) +
						     '&walletToken=' + encodeURIComponent(data.token),
						type: 'get',
						success: function() {
							document.location.href = "startChooseSource?code=${code?c}"
						}
					});					
				}
				function failure(data) {
					document.location.href = 'startChooseSourceFailure?error=' + data.responseCode					
				}
				function cancel() {
					document.location.href = 'startChooseSource?code=${code?c}'					
				}
				
				window.handleAddSourceSuccessResponseCallback = function(data) { success(data); }
				window.handleAddSourceErrorResponseCallback = function(data) { failure(data); }
				window.handleErrorResponseCallback = function() { failure(); }
				window.handleCancelResponseCallback = function() { cancel(); }
			    
				function receiveCrossOriginMessage(event) {
					const iframe=$('div.apipay').find('iframe');
			        iframe.attr('height', event.data + 'px').css('height', event.data + 'px');
				}
			
				window.addEventListener("message", receiveCrossOriginMessage, false);					
			});
		</script>
	</body>
</html>
