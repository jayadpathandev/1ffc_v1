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
import 'jquery-ui';

import { ElementState, Validation } from '../../common/basic/forms/element_state';
import { ValidatorBase } from '../../common/basic/forms/validator_base';
import { ValidatorForm } from '../../common/basic/forms/validator_form';
import { ValidatorRequiredInput } from '../../common/basic/forms/validator_required_input';
import { FormatService } from '../../common/services/format_service';
import { parseAmount } from '../../common/services/parse_amount';
import { PaymentWallet } from '../../common/window';
import { PaymentState } from '../services/payment_state';

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
PMT_ERRORS.set('28', 'editSourceCardDeclinedError');
PMT_ERRORS.set('133', 'editSourceCardNotAcceptedError');

//*****************************************************************************
function get_state() : PaymentState {
    const $elem = $('form[id^="paymentOneTime_paymentForm_"]');
    let   state : PaymentState|undefined = $elem.data('st-state');

    if (state == undefined) {
        if ($('.st-surcharge').text() == 'true') {
            const flex  = $('.st-flexfield').text();
            state = new PaymentState(flex);
        } else {
            state = new PaymentState(undefined);
        }
        $elem.data('st-state', state);
    }

    return state;
}

//*****************************************************************************
function open_correct_method() {
    const sources = $('#paymentOneTime_dWalletItems option').length;

    if (sources > 0) {
        $('div[id^="paymentOneTime_paymentMethodButtonsCol1_"]').removeClass('visually-hidden');
        $('#paymentOneTime_chooseExistingMethod').trigger('focus').trigger('click');
    } else {
        $('div[id^="paymentOneTime_paymentMethodButtonsCol1_"]').addClass('visually-hidden');

        const targets = [
            '#paymentOneTime_addNewBankAccount',
            '#paymentOneTime_addNewDebitCard',
            '#paymentOneTime_addNewCreditCard',
            '#paymentOneTime_addNewSepaAccount',
        ];

        for(let i = 0; i < targets.length; ++i) {
            const link = $(targets[i]);

            if (link.length > 0) {
                link.trigger('focus').trigger('click');
                break;
            }
        }
    }
}

//*****************************************************************************
function step(
            num : number|undefined
        ) {
    $('div[id^="paymentOneTime_paymentSummary_"]').addClass('visually-hidden');
    $('div[id^="paymentOneTime_paymentSummaryComplete_"]').addClass('visually-hidden');
    $('div[id^="paymentOneTime_paymentMethod_"]').addClass('visually-hidden');
    $('div[id^="paymentOneTime_paymentMethodComplete_"]').addClass('visually-hidden');
    $('div[id^="paymentOneTime_confirmPayment_"]').addClass('visually-hidden');
    $('div[id^="paymentOneTime_paymentSuccess_"]').addClass('visually-hidden');

    if (num === 1) {
        $('div[id^="paymentOneTime_paymentSummary_"]').removeClass('visually-hidden');
    } else if (num === 2) {
        $('div[id^="paymentOneTime_messageChangeScheduledDate_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_paymentSummaryComplete_"]').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_paymentMethod_"]').removeClass('visually-hidden');
        open_correct_method();
    } else if (num === 3) {
        $('div[id^="paymentOneTime_paymentSummaryComplete_"]').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_paymentMethodComplete_"]').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_confirmPayment_"]').removeClass('visually-hidden');
        $('#paymentOneTime_fCheckBoxes\\.sField_Agree').
            prop('checked', false).
            removeClass('is-valid').
            addClass('is-invalid').
            trigger('change');
        $('#paymentOneTime_fAutoScheduledConfirm\\.sField_Agree').
            prop('checked', false).
            removeClass('is-valid').
            addClass('is-invalid').
            trigger('change');
    } else {
        $('div[id^="paymentOneTime_paymentSummaryComplete_"]').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_paymentMethodComplete_"]').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_paymentSuccess_"]').removeClass('visually-hidden');
    }
}

//*****************************************************************************
function state_update_display(
            state : PaymentState
        ) {
    const paygroup = $('.st-pay-group-hidden').text();

    state.surcharge_amount((amount) => {
        FormatService.currency(amount, paygroup, (text) => {
            $('#paymentOneTime_sSurchargeAmountComplete').text(text);
        });
        $('input[name="sSurCharge"]').val(amount.toFixed(2));
    });
    state.total_amount((amount) => {
        FormatService.currency(amount, paygroup, (text) => {
            $('#paymentOneTime_sTotalAmountComplete').text(text);
        });
        $('input[name="sTotalAmount"]').val(amount.toFixed(2));
    });
    state.source_name((nickname) => {
        $('.st-payment-method-nickname').text(nickname);
    });
    state.source_type((type) => {
        $('div[id^="paymentOneTime_messagesCol_"]').addClass('visually-hidden');
        $('#paymentOneTime_sEftNotice').addClass('visually-hidden');
        $('#paymentOneTime_sSurchargeNotice').addClass('visually-hidden');

        if (type === 'bank') {
            $('#paymentOneTime_sEftNotice').removeClass('visually-hidden');
            $('div[id^="paymentOneTime_messagesCol_"]').removeClass('visually-hidden');
        }
        else if (type === 'debit') {
            if ($('.st-surcharge').text() === 'true') {
                $('div[id^="paymentOneTime_messagesCol_"]').removeClass('visually-hidden');
                $('#paymentOneTime_sSurchargeNotice').removeClass('visually-hidden');
             }
       }

    });
    state.source_type_i18n((text) => {
        $('.st-payment-method-type').text(text);
    });
    state.source_account_masked((text) => {
        $('.st-payment-method-account').text(text);
    });
}

//*****************************************************************************
function state_set_amount(
            amount : string
        ) : void {
    const state    = get_state();
    const language = ($('.st-language input').val() as string).toLowerCase();
    const country  = ($('.st-country input').val() as string).toUpperCase();

    let val = parseAmount(amount, language + '-' + country);

    if (isNaN(val) == false) {
        let invoices = state.invoices();
        if (invoices.size == 1) {
            invoices.forEach((_, invoice) => {
                state.set_payment(invoice, val);
                state_update_display(state);
            });
            $('input[name="sTotalPayAmt"]').val(val.toFixed(2));
        } else if (invoices.size == 0) {
            console.error('There are no invoices to pay.');
        } else {
            let sum = 0;
            invoices.forEach((due, invoice) => {
                state.set_payment(invoice, due);
                sum = sum + due;
            });
            if (sum != val) {
                console.warn('Different amount! [' + sum + '] != [' + val + ']');
            }
            $('input[name="sTotalPayAmt"]').val(val.toFixed(2));
        }
    } else {
        console.error('Invalid payment amount: [' + amount + ']');
    }
}

//*****************************************************************************
function state_set_walllet(
            id : string
        ) : void {
    const state = get_state();

    state.set_wallet(id);
    state_update_display(state);
}

//*****************************************************************************
function add_validator(
            id       : string,
            callback : (state:ElementState, field:JQuery<any>)=>Validation[]
        ) {
    const form : ValidatorForm|undefined = $('form[id^="paymentOneTime_paymentForm_"]').data('st-form');
    const field = $('#' + id.replaceAll('.', '\\.'));

    if (form !== undefined && field.length == 1) {
        const state = form.get_state(id);
        if (state !== undefined) {
            for (let validator of callback(state, field)) {
                state.add_custom(validator);
            }
        } else {
            console.error('Could not find validator state for [' + id + '].');
        }
    } else if (field.length == 0) {
        console.error('Could not find field [' + id + '].');
    } else if (field.length > 1) {
        console.error('Multiple elements match a single input field [' + id + '].');
    } else {
        console.error('Could not payment form.');
    }
}

//*****************************************************************************
class OverBalance extends ValidatorBase {

    private locale  : string = '';
    private balance : number = 0;
    private custom  : JQuery<any> | undefined;
    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        this.custom = this.field.siblings('*[sorriso-error="over"]');

        const language = ($('.st-language input').val() as string).toLowerCase();
        const country  = ($('.st-country input').val() as string).toUpperCase();
        this.locale = language + '-' + country;
        this.balance = parseAmount(
            $('.st-current').text(),
            this.locale
        )
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') {
            this.custom?.addClass('visually-hidden');
            return true;
        };

        const amount = parseAmount(str, this.locale);

        if (isNaN(amount)) {
            this.custom?.addClass('visually-hidden');
            return true;
        }

        if ( this.balance != 0 && amount > this.balance ) {
            this.custom?.removeClass('visually-hidden');
        } else {
            this.custom?.addClass('visually-hidden');
        }
        
        return true;
    }
}

//*****************************************************************************
class AmountIsInLimit extends ValidatorBase {

    private locale  : string = '';
    private minDue : number = 0;
    private maxDue : number = 0;
    private belowMinPrompt  : JQuery<any> | undefined;
    private continueBtn: JQuery<any> | undefined;
    private overMaxPrompt : JQuery<any> | undefined;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        this.belowMinPrompt = this.field.siblings('*[sorriso-error="below-min"]');
        this.overMaxPrompt = this.field.siblings('*[sorriso-error="over-max"]');
        this.continueBtn = $('#paymentOneTime_paymentSummaryContinueLink');

        const language = ($('.st-language input').val() as string).toLowerCase();
        const country  = ($('.st-country input').val() as string).toUpperCase();
        this.locale = language + '-' + country;

        this.minDue = parseAmount(
            $('.st-minimum').text(),
            this.locale
        )

        this.maxDue = parseAmount(
            $('.st-maximum').text(),
            this.locale
        )
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            return true;
        };

        const amount = parseAmount(str, this.locale);

        if (isNaN(amount)) {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            return true;
        }

        if (amount == 0) {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            this.continueBtn?.addClass('disabled');
        } else if (amount < this.minDue) {
            this.belowMinPrompt?.removeClass('visually-hidden');
            this.continueBtn?.addClass('disabled');
        } else if (amount > this.maxDue) {
            this.overMaxPrompt?.removeClass('visually-hidden');
            this.continueBtn?.addClass('disabled');
        } else {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            this.continueBtn?.removeClass('disabled');
        }

        return true;
    }
}

//*****************************************************************************
class NotZero extends ValidatorBase {

    private locale  : string = '';

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        this.set_message('zero');

        const language = ($('.st-language input').val() as string).toLowerCase();
        const country  = ($('.st-country input').val() as string).toUpperCase();
        this.locale = language + '-' + country;
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') return true;

        const amount = parseAmount(str, this.locale);

        if (isNaN(amount)) return true;

        return amount != 0;
    }
}

//*****************************************************************************
class IsAmount extends ValidatorBase {

    private locale  : string = '';

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        this.set_message('validation');

        const language = ($('.st-language input').val() as string).toLowerCase();
        const country  = ($('.st-country input').val() as string).toUpperCase();
        this.locale = language + '-' + country;
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') return true;

        const amount = parseAmount(str, this.locale);

        return !isNaN(amount);
    }
}

//*****************************************************************************
function payment_summary() {
    //-------------------------------------------------------------------------
    const dateWindow = parseInt($('.st-date-window').text() as string);
    const dateMin    = new Date();
    const dateMax    = new Date(dateMin.getTime() + dateWindow * 24 * 60 * 60 * 1000);

    $('#paymentOneTime_fPayDate\\.aDate_display').datepicker(
        'option', 'minDate', dateMin
    ).datepicker(
        'option', 'maxDate', dateMax
    );

    //-------------------------------------------------------------------------
    const $otherLabel  = $('#paymentOneTime_sOtherAmountLabel');
    const $otherSpacer = $('#paymentOneTime_sDummy');
    const $minSpacer = $('#paymentOneTime_sMinDummy');
    const $otherInput  = $('#paymentOneTime_fOtherAmount');
    const $OtherChargeInfo = $('#paymentOneTime_sAdditionalChargeInfo');

    $('#paymentOneTime_dPayAmount').on('change', function() {
        if ($(this).val() === 'other') {
            $otherLabel.removeClass('visually-hidden');
            $otherSpacer.addClass('visually-hidden');
            $otherInput.removeClass('visually-hidden');
            $OtherChargeInfo.removeClass('visually-hidden');
        } else if ($(this).val() === 'minimum') {
                $otherLabel.addClass('visually-hidden');
                $otherSpacer.removeClass('visually-hidden');
                $otherInput.addClass('visually-hidden');
                $OtherChargeInfo.removeClass('visually-hidden');
                $minSpacer.removeClass('visually-hidden');
        } else {
            $otherLabel.addClass('visually-hidden');
            $otherSpacer.removeClass('visually-hidden');
            $otherInput.addClass('visually-hidden');
            $OtherChargeInfo.addClass('visually-hidden');
        }
    }).trigger('change');

    //-------------------------------------------------------------------------
    add_validator(
        "paymentOneTime_fOtherAmount.pInput",
        (state, field) => { return [
            new ValidatorRequiredInput(state, field),
            new IsAmount(state, field),
            new NotZero(state, field),
            new AmountIsInLimit(state, field),
            new OverBalance(state, field)
        ]}
    );

    //-------------------------------------------------------------------------
    const $amount  = $('#paymentOneTime_fOtherAmount\\.pInput');
    const language = ($('.st-language input').val() as string).toLowerCase();
    const country  = ($('.st-country input').val() as string).toUpperCase();
    const locale   = language + '-' + country;

    function valid_other() {
        const val = ($amount.val() as string).trim();
        if (val === '') return false;
        const amount = parseAmount(val, locale);
        if (isNaN(amount)) return false;
        if (amount <= 0) return false;
        return true;
    }

    const $continue = $('#paymentOneTime_paymentSummaryContinueLink');

    $('#paymentOneTime_dPayAmount').on('change', function() {
        if ($(this).val() == 'other') {
            if (valid_other()) {
                $continue.removeClass('disabled');
            } else {
                $continue.addClass('disabled');
            }
        } else {
            $continue.removeClass('disabled');
        }
    }).trigger('change');

    $amount.on('keyup', function() {
        if (valid_other()) {
            $continue.removeClass('disabled');
        } else {
            $continue.addClass('disabled');
        }
    }).trigger('change');

    $continue.on('click', function($event) {
        if ($(this).hasClass('disabled') == false) {

            step(2);

            FormatService.date_numeric(
                $('#paymentOneTime_fPayDate\\.aDate_actual').val() as string,
                (date) => $('#paymentOneTime_sPaymentDateComplete').text(date)
            );
            FormatService.date_text(
                $('#paymentOneTime_fPayDate\\.aDate_actual').val() as string,
                (date) => $('[id="paymentOneTime_sEstimatedProcessingDate"]').text(date)
            );

            const $payAmt = $('#paymentOneTime_sAmountComplete');
            const from    = $('#paymentOneTime_dPayAmount').val();

            if (from == 'current') {
                $payAmt.text($(".st-current-display").text());
                state_set_amount($(".st-current-display").text());
            } else if (from == 'statement') {
                $payAmt.text($(".st-statement-display").text());
                state_set_amount($(".st-statement-display").text());
            } else if (from == 'minimum') {
                $payAmt.text($(".st-minimum-display").text());
                state_set_amount($(".st-minimum-display").text());
            } else {
                FormatService.currency(
                    parseAmount(($amount.val() as string).trim(), locale),
                    $('.st-pay-group-hidden').text(),
                    (display) => $payAmt.text(display)
                );
                state_set_amount(($amount.val() as string).trim());
            }
        }
        $event.preventDefault();
    });
}

//*****************************************************************************
function payment_summary_edit() {
    $('h4[id^="paymentOneTime_paymentSummaryCompleteHeaderCol_"]').on('click', () => {
        step(1)
    });
}

//-------------------------------------------------------------------------
function payment_method_next() {
    const payOn = ($('#paymentOneTime_fPayDate\\.aDate_actual').val() as string);
    const date  = new Date(payOn + ' 00:00:00');

    get_state().source_expired_by(date, (expired) => {
        if (!expired) {
            step(3);
        } else {
            $('div[id^="paymentOneTime_messageChangeScheduledDate_"]').removeClass('visually-hidden');
            step(1);
        }
    });
}

//*****************************************************************************
function payment_method() {
    //-------------------------------------------------------------------------
    $('#paymentOneTime_dWalletItems').on('change', function() {
        const id = $(this).val();
        if (typeof id === 'string' && id !== '') {
            state_set_walllet(id);
        }
        const option = $('#paymentOneTime_dWalletItems option[value="' + id + '"]');
        if (option.attr('sorriso') === 'unsaved') {
            $('div[id^="paymentOneTime_sPaySourceEditDiv_"]').addClass('visually-hidden');
        } else {
            $('div[id^="paymentOneTime_sPaySourceEditDiv_"]').removeClass('visually-hidden');
        }
    }).trigger('change');

    $('#paymentOneTime_dWalletItems option[value="expired"]').prop('disabled', true);

    //-------------------------------------------------------------------------
    function open_iframe(
                type : string
            ) {
        $.ajax({
            url         : 'getOnetimePaymentIframeUrl?source_type=' + type,
            type        : 'GET',
            processData : false,
            contentType : false
        }).done (function(response) {
            var iframe = $(".st-payment-iframe iframe");
            iframe.attr("src", response.sCreateIframe);
        });
    }

    $('#paymentOneTime_chooseExistingMethod').on('click', function($event) {
        $event.preventDefault();

        if ($('#paymentOneTime_dWalletItems').find('option').length == 0) {
            $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        } else {
            $('div[id^="paymentOneTime_walletRow_"]').removeClass('visually-hidden');   
        }

        $('div[id^="paymentOneTime_iframeDisplayRow_"]').addClass('visually-hidden');
        $('iframe[id="paymentOneTime_sCreateIframe"]').attr('src', '');
    });
    $('div[id^="paymentOneTime_sPaySourceEditDiv_"]').on('click', function($event) {
        $event.preventDefault();
        $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_iframeDisplayRow_"]').removeClass('visually-hidden');

        const id = $('#paymentOneTime_dWalletItems').val();

        $.ajax({
            url         : 'getOnetimePaymentIframeUrlForEdit?sPaymentSourceId=' + id,
            type        : 'GET',
            processData : false,
            contentType : false
        }).done (function(response) {
            var iframe = $(".st-payment-iframe iframe");
            iframe.attr("src", response.sEditIframe);
        });
    });
    $('#paymentOneTime_addNewBankAccount').on('click', function($event) {
        $event.preventDefault();
        $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_iframeDisplayRow_"]').removeClass('visually-hidden');
        open_iframe('bank');
    });
    $('#paymentOneTime_addNewDebitCard').on('click', function($event) {
        $event.preventDefault();
        $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_iframeDisplayRow_"]').removeClass('visually-hidden');
        open_iframe('debit');
    });
    $('#paymentOneTime_addNewCreditCard').on('click', function($event) {
        $event.preventDefault();
        $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_iframeDisplayRow_"]').removeClass('visually-hidden');
        open_iframe('credit');
    });
    $('#paymentOneTime_addNewSepaAccount').on('click', function($event) {
        $event.preventDefault();
        $('div[id^="paymentOneTime_walletRow_"]').addClass('visually-hidden');
        $('div[id^="paymentOneTime_iframeDisplayRow_"]').removeClass('visually-hidden');
        open_iframe('sepa');
    });

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
        }).done((response) => {
            const nickname = response.sPaymentMethodNickName;
            const account  = response.sPaymentMethodAccount;

            const max = parseInt($('.st-max-sources').text());

            if (parseInt(response.sNumSources) >= max) {
                $('div[id^="paymentOneTime_paymentMethodButtonsRow_"]').addClass('visually-hidden');
                $('div[id^="paymentOneTime_paymentMethodButtonsMaxReached_"]').removeClass('visually-hidden');
            }

            $('#paymentOneTime_dWalletItems option[sorriso="unsaved"]').remove();
            let option = '';
            if (nickname !== '') {
                option  = '<option class="st-dropdown-option" value="' +
                          data.token +
                          '">' + nickname + ' ' + account + "</option>";
            } else {
                option  = '<option class="st-dropdown-option" value="' +
                          data.token +
                          '" sorriso="unsaved">' +
                          data.sourceNum + "</option>";
                get_state().add_unsaved(data.token, data.sourceType, data.sourceNum, data.sourceExpiry);
                get_state().update_name(data.token, response.sPaymentMethodNickName);
            }
            const $select = $('#paymentOneTime_dWalletItems');
            $select.append(option);
            $select.val(data.token).trigger('change');

            $('#paymentOneTime_chooseExistingMethod').trigger('click');

            $.ajax('getWalletInfo?token=' + data.token);

            payment_method_next();
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
            $("span[id='paymentOneTime_msgError.sBody']").text(response.sResponseMessage);
        });

        $('#paymentOneTime_chooseExistingMethod').trigger('click');
        $('div[id^="paymentOneTime_messageError_"]').removeClass('visually-hidden');
        step(1);
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
        });
        $('#paymentOneTime_chooseExistingMethod').trigger('click');
    };

    //-------------------------------------------------------------------------
    $('#paymentOneTime_walletContinueLink').on('click', function($event) {
        $event.preventDefault();
        payment_method_next();
    });

    //-------------------------------------------------------------------------
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
            $("span[id='paymentOneTime_msgError.sBody']").text(response.sResponseMessage);
        });

        $('#paymentOneTime_chooseExistingMethod').trigger('click');
        $('div[id^="paymentOneTime_messageError_"]').removeClass('visually-hidden');
        step(1);
    }
}

//*****************************************************************************
function payment_source_edit() {
    //-------------------------------------------------------------------------
    $('h4[id^="paymentOneTime_paymentMethodCompleteHeaderCol_"]').on('click', () => {
        step(2)
    });

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
        }).done((response) => {
            const nickname = response.sPaymentMethodNickName + ' ' +
                             response.sPaymentMethodAccount;

            $('#paymentOneTime_dWalletItems option').each(function() {
                if ($(this).attr('value') === data.token) {
                    $(this).text(nickname);
                }
            });
            $('#paymentOneTime_chooseExistingMethod').trigger('click');
            get_state().update_name(data.token, response.sPaymentMethodNickName);
            get_state().source_name((nickname) => {
                $('.st-payment-method-nickname').text(nickname);
            });
            payment_method_next();
        });
    };

    //-------------------------------------------------------------------------
    window.handleEditSourceErrorResponseCallback = (data : PaymentWallet) => {
        const error = PMT_ERRORS.has(data.responseCode) ?
                      PMT_ERRORS.get(data.responseCode) :
                      'editSourceGenericError';

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
            $("span[id='paymentOneTime_msgError.sBody']").text(response.sResponseMessage);
        });

        $('#paymentOneTime_chooseExistingMethod').trigger('click');
        $('div[id^="paymentOneTime_messageError_"]').removeClass('visually-hidden');
        step(1);
    };
}

//*****************************************************************************
function payment_submit() {
    let submitting = false;

    $('#paymentOneTime_submitPaymentLink').on('click', function($event) {
        //---------------------------------------------------------------------
        $event.preventDefault();
        if (submitting == true) return;

        $(this).prop('disabled', true);
        $('spinner').removeClass('visually-hidden');
        $('div[id^="paymentOneTime_messageError_"]').addClass('visually-hidden');

        //---------------------------------------------------------------------
        let payData = {
            payDate              : $('[name="fPayDate.aDate"]').val(),
            paymentGroup         : $('.st-pay-group-hidden').text(),
            payMethod            : get_state().source(),
            grouping             : get_state().payment_data(),
            autoScheduledConfirm : $('[name="fAutoScheduledConfirm.sField"]').is(':checked')
        }
        $('input[name="sPayData"]').val(JSON.stringify(payData));
        if (payData.payMethod !== undefined && payData.payMethod.token !== undefined) {
            $('input[name="token"]').val(payData.payMethod.token);
        }

        //---------------------------------------------------------------------
        const $form = $(this).parents('form');
        const url   = $form.attr('action') + '&_internalMovement=' + $(this).attr('name');

        $.ajax({
            url         : url,
            type        : 'post',
            data		: new FormData($form[0]),
            processData : false,
            contentType : false
        }).done (function(response, status, request) {
            const code           = response.responseCode;
            let errorMessageType = "paymentGenericError";

            if (code == '41') {
                errorMessageType = "paymentGenericError";
            } else if (code == '42') {
                errorMessageType = "paymentInvalidAmountError";
            } else if (code == '43') {
                errorMessageType = "paymentInsufficientFundsError";
            }

            if (code == '40' || code == '1000') { // Success
                step(undefined);
                $.ajax('unsetBillsForPayment');

                const now = new Date();
                FormatService.date_text(
                    now.getFullYear() + '-' + (now.getMonth() + 1) + '-' + now.getDate(),
                    (date) => $('[id="paymentOneTime_sPaymentRequestReceived"]').text(date)
                );
                $('h4[id^="paymentOneTime_paymentSummaryCompleteHeaderCol_"] span').removeClass('st-cursor');
                $('h4[id^="paymentOneTime_paymentSummaryCompleteHeaderCol_"]').off('click');
                $('h4[id^="paymentOneTime_paymentMethodCompleteHeaderCol_"] span').removeClass('st-cursor');
                $('h4[id^="paymentOneTime_paymentMethodCompleteHeaderCol_"]').off('click');
                $('[id="paymentOneTime_sHeaderEdit"]').addClass('visually-hidden');

                //angular.element(document.querySelectorAll(".st-payment-step-complete span")).removeClass("st-cursor");
            } else { // Error
                $.ajax('makePaymentError?sErrorMessageType=' + errorMessageType).done(function(response) {
                    $("span[id='paymentOneTime_msgError.sBody']").text(response.sBody);
                    $('div[id^="paymentOneTime_messageError_"]').removeClass('visually-hidden');
                    step(1);
                });
            }
        }).fail (function() {
            $.ajax('makePaymentError?sErrorMessageType=paymentGenericError').done(function(response) {
                $("span[id='paymentOneTime_msgError.sBody']").text(response.sBody);
                $('div[id^="paymentOneTime_messageError_"]').removeClass('visually-hidden');
                step(1);
            });
        }).always (function() {
            submitting = false;
            $('spinner').addClass('visually-hidden');
        });
    });
}

//*****************************************************************************
function payment_iframe() {
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
export function one_time_payment(
            parent : HTMLElement
        ) {
    if ($('form[id^="paymentOneTime_paymentForm_"]').length == 1) {
        get_state();
        payment_summary();
        payment_summary_edit();
        payment_method();
        payment_source_edit();
        payment_submit();
        payment_iframe();
    }
}
