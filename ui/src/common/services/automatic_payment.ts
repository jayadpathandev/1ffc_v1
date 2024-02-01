
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

export interface Grouping {
    internalAccountNumber: string;
    displayAccountNumber: string;
    paymentGroup: string;
}

export interface AutomaticPayment {
    payAmountOption: string;
    payDate: number;
    automaticId: number;
    pmtGroupId: string;
    accounts: Grouping[];
    payCount: number;
    payInvoicesOption: string,
    effectiveUntilOption: string,
    payUpto: number,
    payPriorDays: number
}

interface ServerAutomaticPayment {
    PAY_AMOUNT_OPTION: string;
    AUTOMATIC_ID: string;
    PMT_GROUP_ID: string;
    SOURCE_ID: string;
    PAY_DATE: string;
    USER_ID: string;
    EXPIRY_DATE: string;
    GROUPING_JSON: string;
    PAY_COUNT: string;
    PAY_INVOICES_OPTION: string,
    EFFECTIVE_UNTIL_OPTION: string,
    PAY_UPTO: string,
    PAY_PRIOR_DAYS: string
}

interface Response {
    success: boolean;
    status: number;
    output: ServerAutomaticPayment;
}

export class AutomaticPaymentService {

    private constructor() { }

    public static get(
                id: number,
                callback: (data: AutomaticPayment | null) => void
            ): void {

        let success = false;

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        $.ajax("getAutomaticPayment", {
            data: {sAutomaticId: id},
            method: 'POST'
        }).done((data: Response) => {
            if (data.success === true) {
                success = true;
                callback({
                    payAmountOption: data.output.PAY_AMOUNT_OPTION,
                    payDate: parseInt(data.output.PAY_DATE),
                    accounts: JSON.parse(data.output.GROUPING_JSON) as Grouping[],
                    automaticId: parseInt(data.output.AUTOMATIC_ID),
                    pmtGroupId: data.output.PMT_GROUP_ID,
                    payCount:parseInt(data.output.PAY_COUNT),
                    payInvoicesOption: data.output.PAY_INVOICES_OPTION,
                    effectiveUntilOption: data.output.EFFECTIVE_UNTIL_OPTION,
                    payUpto: parseInt (data.output.PAY_UPTO),
                    payPriorDays: parseInt(data.output.PAY_PRIOR_DAYS)
                });
            }
        }).always(() => {
            if (success === false) {
                callback(null);
            }
        });

    }
}
