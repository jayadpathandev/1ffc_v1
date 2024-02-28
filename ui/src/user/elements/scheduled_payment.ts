// (c) Copyright 2021 Sorriso Technologies, Inc(r), All Rights Reserved,
// Patents Pending.
//
// This product is distributed under license from Sorriso Technologies, Inc.
// Use without a proper license is strictly prohibited.  To license this
// software, you may contact Sorriso Technologies at:

// Sorriso Technologies, Inc.
// 400 West Cummings Park
// Suite 1725-184
// Woburn, MA 01801, USA
// +1.978.635.3900

// "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
// Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
// are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
// The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
// "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
// "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
// "Persona E-Service", "Persona Customer Intelligence", "Persona Active
// Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
// Technologies, Inc.
import $ from 'jquery';
import 'jquery-ui';

import { ElementState } from '../../common/basic/forms/element_state';
import { ValidatorBase } from '../../common/basic/forms/validator_base';
import { ValidatorForm } from '../../common/basic/forms/validator_form';

//*****************************************************************************
function handle_source() {
    const surcharge = $('.st-surchargeflag').text();

    function update(id : string|undefined) {
        if (id !== undefined && id !== '') {
            $.ajax(
                'getWalletInfo', { type : "POST",  data: { token: id }, processData : true }
            ).done(function(response) {
                $('div[id^="paymentUpdateAutomaticPayment_paymentSurchargeColumn_"]').addClass('visually-hidden');
                $('div[id^="paymentUpdateAutomaticPayment_eftMessage_"]').addClass('visually-hidden');

                if (response.sPaymentMethodType === 'debit') {            
                    if (surcharge == 'true') {
                        $('div[id^="paymentUpdateAutomaticPayment_paymentSurchargeColumn_"]').removeClass('visually-hidden');
                     }
                } else if (response.sPaymentMethodType === 'bank')                   
                    $('div[id^="paymentUpdateAutomaticPayment_eftMessage_"]').removeClass('visually-hidden');
            });
        }
    }

    $('#paymentUpdateAutomaticPayment_dWalletItems').on('change', function() {
        const val = $(this).val() as string;
        update(val);
    });
    update($('#paymentUpdateAutomaticPayment_dWalletItems').val() as string);
}

//*****************************************************************************
function handle_trigger() {
    $('#paymentUpdateAutomaticPayment_fPayInvoices\\.dPayDate').on('change', function() {
        $('#paymentUpdateAutomaticPayment_fPayInvoices\\.rInput_option1').prop('checked', true);
    });

    $('#paymentUpdateAutomaticPayment_fPayInvoices\\.dPayPriorDays').on('change', function() {
        $('#paymentUpdateAutomaticPayment_fPayInvoices\\.rInput_option2').prop('checked', true);
    });
}

//*****************************************************************************
function pay_effective() {
    //-------------------------------------------------------------------------
    const payNum = $('#paymentUpdateAutomaticPayment_fPayEffective\\.pInput');

    $('input[name="fPayEffective.rInput"]').on('change', function() {
        const selected = $('input[name="fPayEffective.rInput"]:checked').val();

        if (selected !== 'option3') {
            payNum.val('');
            payNum.removeClass('is-valid is-invalid');
        }
    }).trigger('change');

    $('#paymentUpdateAutomaticPayment_fPayEffective\\.aDate_display').on('focus', function() {
        $('#paymentUpdateAutomaticPayment_fPayEffective\\.rInput_option2').prop('checked', true).trigger('change');
    });

    //-------------------------------------------------------------------------
    const payDate = $('#paymentUpdateAutomaticPayment_fPayEffective\\.aDate_display');
    const payActual = $('#paymentUpdateAutomaticPayment_fPayEffective\\.aDate_actual');

    $('input[name="fPayEffective.rInput"]').on('change', function() {
        const selected = $('input[name="fPayEffective.rInput"]:checked').val();

        if (selected !== 'option2') {
            payDate.val('');
            payActual.val('');
            payDate.removeClass('is-valid is-invalid');
        }
    }).trigger('change');

    $('#paymentUpdateAutomaticPayment_fPayEffective\\.pInput').on('focus', function() {
        $('#paymentUpdateAutomaticPayment_fPayEffective\\.rInput_option3').prop('checked', true).trigger('change');
    });
}

//*****************************************************************************
function pay_amount() {
    //-------------------------------------------------------------------------
    const payUpTo1 = $('#paymentUpdateAutomaticPayment_fPayAmount1\\.pInput');

    $('input[name="fPayAmount1.rInput"]').on('change', function() {
        const selected = $('input[name="fPayAmount1.rInput"]:checked').val();

        if (selected !== 'option3') {
            payUpTo1.val('');
            payUpTo1.removeClass('is-valid is-invalid');
        }
    }).trigger('change');

    $('#paymentUpdateAutomaticPayment_fPayAmount1\\.pInput').on('focus', function() {
        $('#paymentUpdateAutomaticPayment_fPayAmount1\\.rInput_option3').prop('checked', true).trigger('change');
    });

    //-------------------------------------------------------------------------
    const payUpTo2 = $('#paymentUpdateAutomaticPayment_fPayAmount2\\.pInput');

    $('input[name="fPayAmount2.rInput"]').on('change', function() {
        const selected = $('input[name="fPayAmount2.rInput"]:checked').val();

        if (selected !== 'option3') {
            payUpTo2.val('');
            payUpTo2.removeClass('is-valid is-invalid');
        }
    }).trigger('change');

    $('#paymentUpdateAutomaticPayment_fPayAmount2\\.pInput').on('focus', function() {
        $('#paymentUpdateAutomaticPayment_fPayAmount2\\.rInput_option3').prop('checked', true).trigger('change');
    });
}

//*****************************************************************************
class BelowMax extends ValidatorBase {
    private max : number;

    //=========================================================================
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        const val = parseInt($('.st-pay-upto').text() as string);

        this.max = isNaN(val) ? 0 : val;
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const value = parseInt(this.as_string());

        if (isNaN(value)) return true;
        if (value <= this.max) return true;

        return false;
    }
}

//*****************************************************************************
function handle_submit() {
    const max = parseInt($('.st-pay-upto').text() as string);
    const form : ValidatorForm|undefined = $('form[id^="paymentUpdateAutomaticPayment_updateAutomaticPaymentForm"]').data('st-form');

    if (form !== undefined) {
        const state1 = form.get_state('paymentUpdateAutomaticPayment_fPayAmount1.pInput');
        if (state1 !== undefined) {
            state1.add_custom(new BelowMax(state1, $('#paymentUpdateAutomaticPayment_fPayAmount1\\.pInput')));
        }
        const state2 = form.get_state('paymentUpdateAutomaticPayment_fPayAmount2.pInput');
        if (state2 !== undefined) {
            state2.add_custom(new BelowMax(state2, $('#paymentUpdateAutomaticPayment_fPayAmount2\\.pInput')));
        }
    }

    const err1  = $('#paymentUpdateAutomaticPayment_fPayAmount1\\.sError');
    const warn1 = $('#paymentUpdateAutomaticPayment_fPayAmount1\\.sWarning');
    $('#paymentUpdateAutomaticPayment_fPayAmount1\\.pInput').on('keyup', function() {
        const value = parseInt($(this).val() as string);

        if (isNaN(value)) {
            err1.addClass('visually-hidden');
            warn1.addClass('visually-hidden');
        } else if (value > max) {
            err1.removeClass('visually-hidden');
            warn1.addClass('visually-hidden');
        } else if (value > 1000) {
            err1.addClass('visually-hidden');
            warn1.removeClass('visually-hidden');
        }
    }).trigger('keyup');

    const err2  = $('#paymentUpdateAutomaticPayment_fPayAmount2\\.sError');
    const warn2 = $('#paymentUpdateAutomaticPayment_fPayAmount2\\.sWarning');
    $('#paymentUpdateAutomaticPayment_fPayAmount2\\.pInput').on('keyup', function() {
        const value = parseInt($(this).val() as string);

        if (isNaN(value)) {
            err2.addClass('visually-hidden');
            warn2.addClass('visually-hidden');
        } else if (value > max) {
            err2.removeClass('visually-hidden');
            warn2.addClass('visually-hidden');
        } else if (value > 1000) {
            err2.addClass('visually-hidden');
            warn2.removeClass('visually-hidden');
        }
    }).trigger('keyup');
}

//*****************************************************************************
export function scheduled_payment(
            parent : HTMLElement
        ) {
    if ($(parent).find('form[id^="paymentUpdateAutomaticPayment_updateAutomaticPaymentForm_"]').length > 0) {
        handle_source();
        handle_trigger();
        pay_effective();
        pay_amount();
        handle_submit();
        $('#paymentUpdateAutomaticPayment_fPayEffective\\.aDate_display').datepicker(
            'option', 'minDate', new Date()
        );
    }
}