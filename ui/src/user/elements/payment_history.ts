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
import { PaymentState } from '../services/payment_state';

//*****************************************************************************
function get_state() : PaymentState {
    const $elem = $('form[id^="paymentHistory_content"]');
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
function add_validator(
            id       : string,
            callback : (state:ElementState, field:JQuery<any>)=>Validation[]
        ) {
    const form : ValidatorForm|undefined = $('form[id^="paymentHistory_content"]').data('st-form');
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
class ValidatorRequiredInput extends ValidatorBase {

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        this.set_message('required_pmt_hist');
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        return this.as_string() !== '';
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
        this.custom = this.field.siblings('*[sorriso-error="over_pmt_hist"]');
        this.balance = parseFloat(
            $('#paymentHistory_fPayAmount\\.pInput').text()
        )
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') {
            this.custom?.addClass('visually-hidden');
            return true;
        };

        const amount = parseFloat(str);

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

    private minDue : number = 0;
    private maxDue : number = 0;
    private belowMinPrompt  : JQuery<any> | undefined;
    private overMaxPrompt : JQuery<any> | undefined;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);
        this.belowMinPrompt = this.field.siblings('*[sorriso-error="below-min_pmt_hist"]');
        this.overMaxPrompt = this.field.siblings('*[sorriso-error="over-max_pmt_hist"]');

        this.minDue = parseFloat(
            $('.st-min-due-amt').text()
        )

        this.maxDue = parseFloat(
            $('.st-max-due-amt').text()
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

        const amount = parseFloat(str);

        if (isNaN(amount)) {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            return true;
        }

        if (amount == 0) {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            return false;
        } else if (amount < this.minDue) {
            this.belowMinPrompt?.removeClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
            return false;
        } else if (amount > this.maxDue) {
            this.overMaxPrompt?.removeClass('visually-hidden');
            this.belowMinPrompt?.addClass('visually-hidden');
            return false;
        } else {
            this.belowMinPrompt?.addClass('visually-hidden');
            this.overMaxPrompt?.addClass('visually-hidden');
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
        this.set_message('zero_pmt_hist');
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') return true;

        const amount = parseFloat(str);

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
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const str = this.as_string();

        if (str === '') return true;

        const amount = parseFloat(str);

        return !isNaN(amount);
    }
}

//*****************************************************************************
function payment_history_elem() {

    $('.sOtherAmountText').css('margin-right', '5px')

    //-------------------------------------------------------------------------
    add_validator(
        "paymentHistory_fPayAmount.pInput",
        (state, field) => { return [
            new ValidatorRequiredInput(state, field),
            new IsAmount(state, field),
            new NotZero(state, field),
            new AmountIsInLimit(state, field),
            new OverBalance(state, field)
        ]}
    );

}

//*****************************************************************************
export function payment_history(
            parent : HTMLElement
        ) {
    if ($(parent).find('#paymentHistory_content').length == 1) {
        get_state();
        payment_history_elem();
    }
}