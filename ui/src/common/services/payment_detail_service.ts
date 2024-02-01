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
    INTERNAL_ACCOUNT_NUMBER : string;
    DISPLAY_ACCOUNT_NUMBER  : string;
    PAYMENT_GROUP           : string;
    DOCUMENT_NUMBER         : string;
    AMOUNT                  : number;
    PAY_SURCHARGE           : number;
    PAY_TOTAL_AMOUNT        : number;
    Flex1                   : string;
    FLEX2                   : string;
    FLEX3                   : string;
    FLEX4                   : string;
    FLEX5                   : string;
    FLEX6                   : string;
    FLEX7                   : string;
    FLEX8                   : string;
    FLEX9                   : string;
    FLEX10                  : string;
    FLEX11                  : string;
    FLEX12                  : string;
    FLEX13                  : string;
    FLEX14                  : string;
    FLEX15                  : string;
    FLEX16                  : string;
    FLEX17                  : string;
    FLEX18                  : string;
    FLEX19                  : string;
    FLEX20                  : string;
}

interface InternalGroupingRecord {
    INTERNAL_ACCOUNT_NUMBER : string;
    DISPLAY_ACCOUNT_NUMBER  : string;
    PAYMENT_GROUP           : string;
    DOCUMENT_NUMBER         : string;
    AMOUNT                  : string;
    PAY_SURCHARGE           : string;
    PAY_TOTAL_AMOUNT        : string;
    Flex1                   : string;
    FLEX2                   : string;
    FLEX3                   : string;
    FLEX4                   : string;
    FLEX5                   : string;
    FLEX6                   : string;
    FLEX7                   : string;
    FLEX8                   : string;
    FLEX9                   : string;
    FLEX10                  : string;
    FLEX11                  : string;
    FLEX12                  : string;
    FLEX13                  : string;
    FLEX14                  : string;
    FLEX15                  : string;
    FLEX16                  : string;
    FLEX17                  : string;
    FLEX18                  : string;
    FLEX19                  : string;
    FLEX20                  : string;
}

export interface PaymentDetail {
    PAY_FROM_ACCOUNT    : string;
    PAY_DATE            : Date ;
    PAY_REQ_DATE        : Date | null;
    PAY_CHANNEL         : string;
    PAY_STATUS          : string;
    PAY_AMT             : number;
    PAY_SURCHARGE       : number;
    PAY_TOTAL_AMT       : number;
    FLEX1               : string;
    FLEX2               : string;
    FLEX3               : string;
    FLEX4               : string;
    FLEX5               : string;
    FLEX6               : string;
    FLEX7               : string;
    FLEX8               : string;
    FLEX9               : string;
    FLEX10              : string;
    FLEX11              : string;
    FLEX12              : string;
    FLEX13              : string;
    FLEX14              : string;
    FLEX15              : string;
    FLEX16              : string;
    FLEX17              : string;
    FLEX18              : string;
    FLEX19              : string;
    FLEX20              : string;
    GROUPING            : Grouping[];
}

interface InternalPaymentDetail {
    PAY_FROM_ACCOUNT    : string;
    PAY_DATE            : string;
    PAY_REQ_DATE        : string | undefined;
    PAY_CHANNEL         : string;
    PAY_STATUS          : string;
    PAY_AMT             : string;
    PAY_SURCHARGE       : string;
    PAY_TOTAL_AMT       : string;
    FLEX1               : string | undefined
    FLEX2               : string | undefined;
    FLEX3               : string | undefined;
    FLEX4               : string | undefined;
    FLEX5               : string | undefined;
    FLEX6               : string | undefined;
    FLEX7               : string | undefined;
    FLEX8               : string | undefined;
    FLEX9               : string | undefined;
    FLEX10              : string | undefined;
    FLEX11              : string | undefined;
    FLEX12              : string | undefined;
    FLEX13              : string | undefined;
    FLEX14              : string | undefined;
    FLEX15              : string | undefined;
    FLEX16              : string | undefined;
    FLEX17              : string | undefined;
    FLEX18              : string | undefined;
    FLEX19              : string | undefined;
    FLEX20              : string | undefined;
    GROUPING            : InternalGroupingRecord[];
}

interface Response {
    success : boolean;
    status  : number;
    output  : InternalPaymentDetail;
}

export class PaymentDetailService {
    private static instance: PaymentDetailService;

    private data    = new Map<string, PaymentDetail>();
    private waiting = new Map<string, Array<(data:PaymentDetail|null) => void>>();

    private constructor() {}

    private static retrieve(
                paymentId : string,
                callback : (data:PaymentDetail|null) => void
            ) : void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!PaymentDetailService.instance) {
            PaymentDetailService.instance = new PaymentDetailService();
        }
        var instance = PaymentDetailService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        var retVal = instance.data.get(paymentId);
        if (retVal !== undefined) {
            callback(retVal);
            return;
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        var waiting = new Array<(data:PaymentDetail|null) => void>();
        var todo = instance.waiting.get(paymentId);
        if (todo === undefined) {
            instance.waiting.set(paymentId, waiting);
        }
        else {
            waiting = todo;
        }

        waiting.push(callback);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.

        $.ajax("getPaymentRecord", {
            method : 'POST',
            data   :  {
                sOnlineTransId : paymentId
            }
        }).done((data:Response) => {
            if (data.success === true) {
                var grouping = new Array<Grouping>();
                data.output.GROUPING.forEach(element => {
                    grouping.push({
                        INTERNAL_ACCOUNT_NUMBER : element.INTERNAL_ACCOUNT_NUMBER,
                        DISPLAY_ACCOUNT_NUMBER  : element.DISPLAY_ACCOUNT_NUMBER,
                        PAYMENT_GROUP           : element.PAYMENT_GROUP,
                        DOCUMENT_NUMBER         : element.DOCUMENT_NUMBER,
                        AMOUNT                  : parseFloat(element.AMOUNT),
                        PAY_SURCHARGE           : parseFloat(element.PAY_SURCHARGE),
                        PAY_TOTAL_AMOUNT        : parseFloat(element.PAY_TOTAL_AMOUNT),
                        Flex1                   : element.Flex1,
                        FLEX2                   : element.FLEX2,
                        FLEX3                   : element.FLEX3,
                        FLEX4                   : element.FLEX4,
                        FLEX5                   : element.FLEX5,
                        FLEX6                   : element.FLEX6,
                        FLEX7                   : element.FLEX7,
                        FLEX8                   : element.FLEX8,
                        FLEX9                   : element.FLEX9,
                        FLEX10                  : element.FLEX10,
                        FLEX11                  : element.FLEX11,
                        FLEX12                  : element.FLEX12,
                        FLEX13                  : element.FLEX13,
                        FLEX14                  : element.FLEX14,
                        FLEX15                  : element.FLEX15,
                        FLEX16                  : element.FLEX16,
                        FLEX17                  : element.FLEX17,
                        FLEX18                  : element.FLEX18,
                        FLEX19                  : element.FLEX19,
                        FLEX20                  : element.FLEX20
                    });

                });

                instance.data.set(paymentId, {
                    PAY_FROM_ACCOUNT    : data.output.PAY_FROM_ACCOUNT,
                    PAY_DATE            : new Date(data.output.PAY_DATE),
                    PAY_REQ_DATE        : data.output.PAY_REQ_DATE !== undefined ? new Date(data.output.PAY_REQ_DATE) : null,
                    PAY_CHANNEL         : data.output.PAY_CHANNEL,
                    PAY_STATUS          : data.output.PAY_STATUS,
                    PAY_AMT             : parseFloat(data.output.PAY_AMT),
                    PAY_SURCHARGE       : parseFloat(data.output.PAY_SURCHARGE),
                    PAY_TOTAL_AMT       : parseFloat(data.output.PAY_TOTAL_AMT),
                    FLEX1               : data.output.FLEX1  !== undefined ? data.output.FLEX1  : "",
                    FLEX2               : data.output.FLEX2  !== undefined ? data.output.FLEX2  : "",
                    FLEX3               : data.output.FLEX3  !== undefined ? data.output.FLEX3  : "",
                    FLEX4               : data.output.FLEX4  !== undefined ? data.output.FLEX4  : "",
                    FLEX5               : data.output.FLEX5  !== undefined ? data.output.FLEX5  : "",
                    FLEX6               : data.output.FLEX6  !== undefined ? data.output.FLEX6  : "",
                    FLEX7               : data.output.FLEX7  !== undefined ? data.output.FLEX7  : "",
                    FLEX8               : data.output.FLEX8  !== undefined ? data.output.FLEX8  : "",
                    FLEX9               : data.output.FLEX9  !== undefined ? data.output.FLEX9  : "",
                    FLEX10              : data.output.FLEX10 !== undefined ? data.output.FLEX10 : "",
                    FLEX11              : data.output.FLEX11 !== undefined ? data.output.FLEX11 : "",
                    FLEX12              : data.output.FLEX12 !== undefined ? data.output.FLEX12 : "",
                    FLEX13              : data.output.FLEX13 !== undefined ? data.output.FLEX13 : "",
                    FLEX14              : data.output.FLEX14 !== undefined ? data.output.FLEX14 : "",
                    FLEX15              : data.output.FLEX15 !== undefined ? data.output.FLEX15 : "",
                    FLEX16              : data.output.FLEX16 !== undefined ? data.output.FLEX16 : "",
                    FLEX17              : data.output.FLEX17 !== undefined ? data.output.FLEX17 : "",
                    FLEX18              : data.output.FLEX18 !== undefined ? data.output.FLEX18 : "",
                    FLEX19              : data.output.FLEX19 !== undefined ? data.output.FLEX19 : "",
                    FLEX20              : data.output.FLEX20 !== undefined ? data.output.FLEX20 : "",
                    GROUPING            : grouping
                });
            }
        }).always(() => {
            var data = instance.data.get(paymentId);
            waiting.forEach((callback) => {
                if (data !== undefined){
                    callback(data);
                }
                else {
                    callback(null);
                }
            });

            instance.waiting.delete(paymentId);
        });
    }


    public static get(
                paymentId : string,
                callback : (record:PaymentDetail | null) => void
            ) : void {
        PaymentDetailService.retrieve(paymentId, callback );
    }


}
