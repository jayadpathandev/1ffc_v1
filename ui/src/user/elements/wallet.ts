/**
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park
 * Suite 1725-184
 * Woburn, MA 01801, USA
 * +1.978.635.3900
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
import $ from 'jquery';
import { PaymentWallet } from '../../common/window';

//*****************************************************************************
const PMT_ERRORS = new Map<String, String>();
PMT_ERRORS.set('11', 'addSourceGenericError');
PMT_ERRORS.set('12', 'addSourceCardNumberError');
PMT_ERRORS.set('13', 'addSourceAddressError');
PMT_ERRORS.set('14', 'addSourceCvvError');
PMT_ERRORS.set('15', 'addSourceCardExpiredError');
PMT_ERRORS.set('16', 'addSourceLostStolenFraudError');
PMT_ERRORS.set('17', 'addSourceInvalidMethodError');
PMT_ERRORS.set('18', 'addSourceCardDeclinedError');
PMT_ERRORS.set('132', 'addSourceCardNotAcceptedError');
PMT_ERRORS.set('21', 'editSourceGenericError');
PMT_ERRORS.set('22', 'editSourceCardNumberError');
PMT_ERRORS.set('23', 'editSourceAddressError');
PMT_ERRORS.set('24', 'editSourceCvvError');
PMT_ERRORS.set('25', 'editSourceCardExpiredError');
PMT_ERRORS.set('26', 'editSourceLostStolenFraudError');
PMT_ERRORS.set('27', 'editSourceInvalidMethodError');
PMT_ERRORS.set('133', 'editSourceCardNotAcceptedError');

//*****************************************************************************
function wallet_callback() {
    function display_msg(
                error : boolean,
                text : string
            ) {
        const $elem = $("[id='paymentWallet_sMessageDisplay']");

        $elem.parent().removeClass("visually-hidden");
        if (error) {
            $elem.parent().removeClass("st-payment-success");
            $elem.parent().addClass("st-payment-error");
        } else {
            $elem.parent().addClass("st-payment-success");
            $elem.parent().removeClass("st-payment-error");
        }
        $elem.text(text);

        $(".st-payment-iframe").remove();
    }
    //-------------------------------------------------------------------------
    window.handleAddSourceSuccessResponseCallback = (data : PaymentWallet) => {
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "addSourceSuccess",
                error_message_type: null,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done(() => {
            window.open('startPaymentWallet', '_self');
        });
    }

    //-------------------------------------------------------------------------
    window.handleAddSourceErrorResponseCallback = (data : PaymentWallet) => {
        const error = PMT_ERRORS.has(data.responseCode) ?
                        PMT_ERRORS.get(data.responseCode) :
                        'addSourceGenericError';
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "addSourceError",
                error_message_type: error,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done((response) => {
            display_msg(true, response.sResponseMessage);
        });
    }
    //-------------------------------------------------------------------------
    window.handleEditSourceSuccessResponseCallback = (data : PaymentWallet) => {
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "editSourceSuccess",
                error_message_type: null,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done(() => {
            window.open('startPaymentWallet', '_self');
        });
    }
    //-------------------------------------------------------------------------
    window.handleEditSourceErrorResponseCallback = (data : PaymentWallet) => {
        const error = PMT_ERRORS.has(data.responseCode) ?
                        PMT_ERRORS.get(data.responseCode) :
                        'addSourceGenericError';
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "editSourceError",
                error_message_type: error,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done((response) => {
            display_msg(true, response.sResponseMessage);
        });
    }

    window.handleCancelResponseCallback = (data : PaymentWallet) => {
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "cancel",
                error_message_type: null,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done((response) => {
            display_msg(false, response.sResponseMessage);
        });
    }
    window.handleErrorResponseCallback = (data : PaymentWallet) => {
        $.ajax('walletPaymentResponse', {
            type: 'post', processData: true, data : {
                response_type:      "error",
                error_message_type: null,
                response_code:      data.responseCode,
                response_message:   data.responseMessage,
                token:              data.token,
                transaction_id:     data.transactionId
            }
        }).done((response) => {
            display_msg(true, response.sResponseMessage);
        });
    }

    window.payment_size = (height:number) => {
        $('iframe').height(height);
    }
}

//*****************************************************************************
function wallet_iframe() {
	const iframe=$('div.st-payment-iframe').find('iframe')[0];

    if (iframe) {
        iframe.onload = function() {
            return function() {
                iframe.contentWindow?.postMessage('what is your size?', iframe.src);
            }
        }();
    }

    function receiveCrossOriginMessage(event : MessageEvent) {
		var iframe=$('div.st-payment-iframe').find('iframe');
        iframe.attr('height', event.data + 'px').css('height', event.data + 'px');
	}

	window.addEventListener("message", receiveCrossOriginMessage, false);
}

//*****************************************************************************
export function payment_wallet(
            parent : HTMLElement
        ) {
    const $create = $(parent).find('#paymentWallet_sCreateIframe');
    const $edit   = $(parent).find('#paymentWallet_sEditIframe');
    if ($create.length > 0 || $edit.length > 0) {
        wallet_callback();
        wallet_iframe();
    }
}