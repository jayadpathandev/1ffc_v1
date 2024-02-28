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

export function fffc(parent: HTMLElement) {
    esign_spinner(parent);
}