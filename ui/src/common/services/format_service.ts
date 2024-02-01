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

import { I18nService } from './i18n_service';

interface NumbersResponse {
    before  : boolean;
    lookup  : {[key: string]:string;};
    locale  : string;
    significant : number;
}

export class FormatService {
    private static instance   : FormatService;
    private static i18nFormat : I18nService|null = null;
    private numbersState      : string = 'idle';
    private numbersData       : NumbersResponse|null = null;
    private numberFormats     : Intl.NumberFormat= new Intl.NumberFormat();
    private numbersWaiting    : Array<(data:NumbersResponse|null) => void> = [];

    private constructor() {}

    private static get_numbers(
                callback : (data:NumbersResponse|null) => void
            ) : void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!FormatService.instance) {
            FormatService.instance = new FormatService();
        }
        var instance = FormatService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        if (instance.numbersData !== null || instance.numbersState === 'ran') {
            callback(instance.numbersData);
            return;
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        instance.numbersWaiting.push(callback);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        if (instance.numbersState === 'idle') {
            instance.numbersState = 'running';

            $.ajax("get_currency_symbols").done((data:NumbersResponse) => {
                instance.numbersData = data;
                instance.numberFormats = new Intl.NumberFormat(data.locale, {
                    minimumFractionDigits: data.significant,
                    maximumFractionDigits: data.significant
                });
            }).always(() => {
                instance.numbersWaiting.forEach((callback) => {
                    callback(instance.numbersData);
                });
                instance.numbersWaiting = [];
                instance.numbersState   = 'ran';
            });
        }
    }

    public static currency(
                amount: number,
                paymentGroup : string,
                callback : (value:string) => void
            ) : void {
        FormatService.get_numbers((data:NumbersResponse|null) => {
            if (data !== null) {
                let symbol = data.lookup[paymentGroup];


                if (data.before) {
                    if (symbol === undefined) {
                        callback(this.instance.numberFormats.format(amount).replaceAll(/[0-9]/g, '-'));
                    }
                    else {
                        callback(symbol + this.instance.numberFormats.format(amount));
                    }
                }
                else {
                    if (symbol  === undefined) {
                        callback(this.instance.numberFormats.format(amount).replaceAll(/[0-9]/g, '-'));
                    }
                    callback(this.instance.numberFormats.format(amount) + symbol);
                }
            } else {
                callback('');
            }
        });
    }

    public static text_datepicker(
                callback : (format:string) => void
            ) : void {
        if (FormatService.i18nFormat == null) {
            FormatService.i18nFormat = new I18nService('formats');
        }
        FormatService.i18nFormat.get('dateText', (format:string) => {
            format = format.replace('%y', 'yy').replace('%m', 'M').replace('%d', 'd');
            callback(format);
        });
    }

    public static numeric_datepicker(
                callback : (format:string) => void
            ) : void {
        if (FormatService.i18nFormat == null) {
            FormatService.i18nFormat = new I18nService('formats');
        }
        FormatService.i18nFormat.get('dateNumeric', (format:string) => {
            format = format.replace('%y', 'yy').replace('%m', 'm').replace('%d', 'd');
            callback(format);
        });
    }

    public static date_numeric(
                date     : string,
                callback : (format:string) => void
            ) : void {
        if (FormatService.i18nFormat == null) {
            FormatService.i18nFormat = new I18nService('formats');
        }
        FormatService.i18nFormat.get('dateNumeric', (format:string) => {
            const values = date.split('-');
            const retval = format.replace('%y', values[0]).replace('%m', values[1]).replace('%d', values[2]);
            callback(retval);
        });
    }
    public static date_text(
                date     : string,
                callback : (format:string) => void
            ) : void {
        if (FormatService.i18nFormat == null) {
            FormatService.i18nFormat = new I18nService('formats');
        }
        FormatService.i18nFormat.get_many(
            [ 'dateText',   'dateMonth1', 'dateMonth2', 'dateMonth3', 'dateMonth4',
              'dateMonth5', 'dateMonth6', 'dateMonth7', 'dateMonth8', 'dateMonth9',
              'dateMonth10', 'dateMonth11', 'dateMonth12'
            ], (data : Map<string, string>) => {
                const values = date.split('-');
                const format = data.get('dateText') as string;
                const month  = data.get('dateMonth' + parseInt(values[1])) as string;
                const retval = format.replace('%y', values[0]).replace('%m', month).replace('%d', values[2]);
                callback(retval);
        });
    }
}
