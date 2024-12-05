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
		 <#if deleteWalletError != "" >
            <div class="alert alert-danger" role="alert">
                <h4>
					<span>Delete wallet error</span>
				</h4>
				<p>
					<#if deleteWalletError == 'scheduled'>
      					Payment method is used in a scheduled payment, cannot delete.
					</#if>
					<#if deleteWalletError == 'auto'>
      					Payment method is used in an auto payment schedule, cannot delete.
					</#if>
					<#if deleteWalletError == 'progress'>
      					Scheduled payment is currently be made with the payment method, cannot delete.
					</#if>
					<#if deleteWalletError == 'unsaved'>
      					Payment method is temporary, cannot delete.
					</#if>
					<#if deleteWalletError == 'unknown'>
      					Payment could not be delete for unknown reasons.
					</#if>
				</p>
            </div>
        </#if>
		<div class="apipay ms-3">
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
				<div class="col-3">
					<#if hasWallet>
						<select class="form-control form-select" name="wallet" id="apipay_wallet">
							<#list wallet as item>
								<option value="${item.val}" <#if walletItem == item.val>selected</#if>>${item.text}</option>
							</#list>
						</select>
					</#if>
				</div>
								
				<!-- Delete Button -->
        		<div class="col-3 row">
            		<#if hasWallet>
                		<div class="col-12">
							<a id="editButton" class="btn btn-primary<#if iframe != ""> disabled</#if>" href="startAgentPayIframe?itemType=edit">Edit</a>
						</div>
            			<div class="col-12">
	                		<a id="deleteButton" class="btn btn-secondary" href="#" 
    	            			data-bs-toggle="modal" data-bs-target="#confirmDeleteModal">Delete</a>
    	            	</div>
            		</#if>
        		</div>
        		
        		 <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-labelledby="confirmDeleteModalLabel" aria-hidden="true">
            		<div class="modal-dialog">
                		<div class="modal-content">
                    		<div class="modal-header">
                        		<h5 class="modal-title" id="confirmDeleteModalLabel">Confirm Delete</h5>
                        		<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    		</div>
                    		<div class="modal-body">
                        		Are you sure you want to delete this item?
                    		</div>
                    		<div class="modal-footer">
                        		<a id="noButton"  class="btn btn-secondary" data-bs-dismiss="modal">No</a>
                        		<a id="confirmDeleteButton" class="btn btn-primary" href="startDeleteWallet">Yes</a>
                    		</div>
                		</div>
            		</div>
        		</div>
				
				<#if !disableAch>
					<div class="col-3">
						<a id="addBankButton" class="btn btn-primary<#if iframe != ""> disabled</#if>" href="startAgentPayIframe?itemType=bank">Use a new bank account</a>
					</div>
				</#if>
				<div class="col-3">
					<a id="addDebitButton" class="btn btn-primary<#if iframe != ""> disabled</#if>" href="startAgentPayIframe?itemType=debit">Use a new debit card</a>
				</div>
			</div>
			<#if iframe == "bank">
				<iframe src="startAddSourceBank" >
				</iframe>
			<#elseif iframe == "debit">
				<iframe src="startAddSourceDebit">
				</iframe>
			<#elseif iframe == "edit">
    			<iframe src="startEditSource">
    			</iframe>
			</#if>
			<a id="internal_error" href="startChooseSourceFailure"></a>
		</div>
		<script>
			$(function() {
				window.payment_size = function(size) {
					$('iframe').height(size);
				<#if iframe != "">
					$('a#editButton').removeClass('disabled');
					$('a#addBankButton').removeClass('disabled');
					$('a#addDebitButton').removeClass('disabled');
				</#if>
				}
				
				// remove the migrated token from the wallet dropdown
		        $('select[name="wallet"] option').each(function() {
		            if ($(this).text().trim().startsWith('Migrated token')) {
		                $(this).remove();
		            }
		        });
			
				$('select[name="wallet"] option').first().prop('selected', true);
				$('#apipay_wallet').on('change', function() {
					var selectedItem = $(this).val();

				    var urlParams = new URLSearchParams(window.location.search);
				    var currentToken = urlParams.get('walletToken');

				    if (currentToken !== selectedItem) {
				        document.location.href = 'startUseSource?walletToken=' + encodeURIComponent(selectedItem);
				    }
				}).trigger('change');
		        
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
				window.handleEditSourceSuccessResponseCallback = function(data) { success(data); }
				window.handleEditSourceErrorResponseCallback = function(data) { failure(data); }
				window.handleErrorResponseCallback = function() { failure(); }
				window.handleCancelResponseCallback = function() { cancel(); }
			    
				function receiveCrossOriginMessage(event) {
					const iframe=$('div.apipay').find('iframe');
			        iframe.attr('height', event.data + 'px').css('height', event.data + 'px');
				}
			
				window.addEventListener("message", receiveCrossOriginMessage, false);
		        
	        <#if iframe != "">
	        	window.setTimeout(function() {
					$('a#editButton').removeClass('disabled');
					$('a#addBankButton').removeClass('disabled');
					$('a#addDebitButton').removeClass('disabled');
	        	}, 30000);
	        </#if>		        
        });
		</script>
	</body>
</html>
