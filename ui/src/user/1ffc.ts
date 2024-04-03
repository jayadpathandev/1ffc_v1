/**
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved,
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
function esign_spinner(parent : HTMLElement) {
    const declineSignConfirmationPopIn = '#paymentUpdateAutomaticPayment_esignDeclineConfirmationPopIn';
    const createRecurringPaymentBtn = '#paymentUpdateAutomaticPayment_signButton';
    const signDocumentBtn = '#paymentUpdateAutomaticPayment_signDocument';
    const esignPopIn = '#paymentUpdateAutomaticPayment_esignPopIn';
    const spinnerDiv = '[name="pageSpinner"]';
    const paymentAutomaticTable = '#paymentAutomatic_tAutomaticPaymentsTable_container';

    // If clicked on 'SIGN DOCUMENT' then show the loader
    if ($(parent).find(signDocumentBtn).length > 0) {
        $(signDocumentBtn).on('click', () => {
            $(spinnerDiv).removeClass('visually-hidden');
        });
    }

    // If the esignPopIn appears then close the loader
    if ($(parent).find(esignPopIn).length > 0) {
        $(spinnerDiv).addClass('visually-hidden');
    }

    // If clicked on 'CREATE RECURRING PAYMENT' button then show the loader
    if ($(parent).find(createRecurringPaymentBtn).length > 0) {
        $(createRecurringPaymentBtn).on('click', () => {
            $(spinnerDiv).removeClass('visually-hidden');
            $(esignPopIn).parent('div').parent('div').remove();
        });
    }

    // If esign decline confirmation prompt appears then close the loader
    if ($(parent).find(declineSignConfirmationPopIn).length > 0) {
        $(spinnerDiv).addClass('visually-hidden');
    }

    if ($(parent).find(paymentAutomaticTable).length > 0) {
        $(spinnerDiv).addClass('visually-hidden');
    }
}

function edit_payment_date(parent : HTMLElement) {
    const elements = $(parent).find('#paymentHistory_fPayDate\\.aDate_display');

    if (elements.length > 0) {
        const dateWindow = parseInt($('.st-date-window').text() as string);
        const dateNow    = new Date();
        const dateMin    = new Date(dateNow.getTime() + 24 * 60 * 60 * 1000);
        const dateMax    = new Date(dateNow.getTime() + dateWindow * 24 * 60 * 60 * 1000);

        elements.datepicker(
            'option', 'minDate', dateMin
        ).datepicker(
            'option', 'maxDate', dateMax
        );
    }
}

function edit_payment_total(parent : HTMLElement) {
    const INPUT = '#paymentHistory_fPayAmount\\.pInput';
    const FEE   = '#paymentHistory_sConvenienceFee';
    const TOTAL = '#paymentHistory_sTotalAmount';

    function fixed_decimal(
                val : string|string[]|number|undefined
            ) {
        if (typeof val !== 'string') return 0;

        const input = val.trim();
        if (input == '') return 0;

        let retval    = 0;
        let decimal   = false;
        let precision = 0;

        const start = (input.startsWith('$') ? 1 : 0);
        for(let i = start; i < input.length; ++i) {
            if (decimal) precision += 1;
            switch(input.charAt(i)) {
                case '0': retval = retval * 10 + 0; break;
                case '1': retval = retval * 10 + 1; break;
                case '2': retval = retval * 10 + 2; break;
                case '3': retval = retval * 10 + 3; break;
                case '4': retval = retval * 10 + 4; break;
                case '5': retval = retval * 10 + 5; break;
                case '6': retval = retval * 10 + 6; break;
                case '7': retval = retval * 10 + 7; break;
                case '8': retval = retval * 10 + 8; break;
                case '9': retval = retval * 10 + 9; break;
                case '.':
                    if (decimal === true) return 0;
                    decimal = true;
                    break;
                default:
                    return 0;
            }
        }
        if (precision < 2) {
            retval = retval * Math.pow(10, 2 - precision);
        } else if (precision > 2) {
            if (retval % Math.pow(10, precision - 2) == 0) {
                retval = retval / Math.pow(10, precision - 2);
            } else {
                return 0;
            }
        }
        return retval;
    }
    function format_amount(
                input : number
            ) {
        let retval = '$';

        retval += Math.floor(input/100).toString();
        retval += '.';
        if (input % 100 > 10) retval += (input % 100);
        if (input % 100 < 10) retval += '0' + (input % 100);

        return retval;
    }


    $(parent).find(INPUT).on('keyup', function() {
        const base  = fixed_decimal($(INPUT).val());
        const fee   = fixed_decimal($(FEE).text());

        if (base > 0 && fee > 0) {
            $(TOTAL).text(format_amount(base + fee));
        } else  if (base > 0 && fee == 0) {
            $(TOTAL).text(format_amount(base));
        }
    });
}

export function fffc(parent: HTMLElement) {
    esign_spinner(parent);
    edit_payment_date(parent);
    edit_payment_total(parent);
}