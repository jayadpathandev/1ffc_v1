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
import { I18nService } from '../../common/services/i18n_service';

interface Bill {
    number          : string;
    numberDisplay   : string;
    paymentGroup    : string;
    invoice         : string;
    interPayWorking : boolean|undefined;
    interPayTransId : string|undefined;
    paymentAmount   : number|undefined;
    surchargeAmt    : number|undefined;
    totalPayAmt     : number|undefined;
    flex1           : string|undefined;
    flex2           : string|undefined;
    flex3           : string|undefined;
    flex4           : string|undefined;
    flex5           : string|undefined;
    flex6           : string|undefined;
    flex7           : string|undefined;
    flex8           : string|undefined;
    flex9           : string|undefined;
    flex10          : string|undefined;
    flex11          : string|undefined;
    flex12          : string|undefined;
    flex13          : string|undefined;
    flex14          : string|undefined;
    flex15          : string|undefined;
    flex16          : string|undefined;
    flex17          : string|undefined;
    flex18          : string|undefined;
    flex19          : string|undefined;
    flex20          : string|undefined;
};

interface UnsavedCard {
    name    : string|undefined;
    type    : string;
    account : string;
    expiry  : string;
};

interface Wallet {
    version   : number;
    loading   : boolean;
    id        : string|undefined;
    name      : string|undefined;
    maskedNum : string|undefined;
    expiry    : string|undefined;
    type      : string|undefined;
    iin       : string|undefined;
    togo      : number;
}

export interface BillPay {
    internalAccountNumber: string;
    displayAccountNumber : string;
    documentNumber       : string;
    amount               : string;
    paymentGroup         : string;
    surcharge            : string;
    totalAmount          : string;
    interPayTransactionId: string;
}

export interface Source {
    nickName : string;
    account  : string;
    type     : string;
    expiry   : string;
    token    : string;
}

export class PaymentState {
    private flexField      : string|undefined;
    private bills          = new Map<string, Bill>();
    private unsavedCards   = new Map<string, UnsavedCard>();
    private wallet         : Wallet;
    private toNotifyBills  : (() => void)[] = [];
    private toNotifyWallet : (() => void)[] = [];

    constructor(
                flexField : string|undefined
            ) {
        $.ajax('getBillsForPayment', {
            processData : false, contentType : false
        }).done ((response) => {
			if (response.success) {
                function safe(val : number|undefined) : number|undefined {
                    if (val === undefined) return undefined;
                    return Math.round(val * 100);
                }

                for(let bill of response.output as Bill[]) {
                    bill.paymentAmount       = safe(bill.paymentAmount);
                    bill.surchargeAmt = safe(bill.surchargeAmt);
                    bill.totalPayAmt  = safe(bill.totalPayAmt);
                    this.bills.set(bill.invoice, bill);
                }
			}
    	});
        this.flexField = flexField;
        this.wallet    = {
            version:   0,
            loading:   false,
            id:        '',
            name:      undefined,
            maskedNum: undefined,
            expiry:    undefined,
            type:      undefined,
            iin:       undefined,
            togo:      0
        };
    }

    private are_surchages_enabled() {
        return this.flexField !== undefined;
    }

    private notify_bills() : void {
        if (this.wallet.loading === false && this.wallet.togo === 0) {
            for(let callback of this.toNotifyBills) {
                callback();
            }
            this.toNotifyBills = [];
        }
    }
    private notify_wallet() : void {
        if (this.wallet.loading === false) {
            for(let callback of this.toNotifyWallet) {
                callback();
            }
            this.toNotifyWallet = [];
        }
    }

    public source_name(
                callback : (nickname:string) => void
            ) : void {
        let respond = () => {
            if (this.wallet.name === undefined || this.wallet.name === '') {
                const i18n = new I18nService('paymentOneTime');
                i18n.get('unsavedName', (text) => {
                    callback(text);
                });
            } else {
                callback(this.wallet.name);
            }
        }

        if (this.wallet.loading) {
            this.toNotifyWallet.push(respond);
        } else {
            respond();
        }
    }

    public source_type(
                callback : (type:string) => void
            ) {
        let respond = () => {
            if (this.wallet.id === undefined || this.wallet.type === undefined) {
                callback('');
                return;
            } else {
                callback(this.wallet.type);
            }
        }

        if (this.wallet.loading) {
            this.toNotifyWallet.push(respond);
        } else {
            respond();
        }
    }

    public source_type_i18n(
                callback : (text:string) => void
            ) {
        let respond = (type:string) => {
            if (type === '') {
                callback('');
            } else {
                const i18n = new I18nService('paymentOneTime');
                i18n.get('type_' + type, (text) => {
                    callback(text);
                })

            }
        }

        if (this.wallet.loading) {
            this.source_type(respond);
        } else {
            respond(this.wallet.type !== undefined ? this.wallet.type : '');
        }
    }

    public source_account_masked(
                callback : (text:string) => void
            ) {
        let respond = () => {
            if (this.wallet.maskedNum == undefined) {
                callback('**** **** **** ****');
            } else {
                callback(this.wallet.maskedNum);
            }
        }

        if (this.wallet.loading) {
            this.toNotifyWallet.push(respond);
        } else {
            respond();
        }
    }

    public source_expired_by(
                date     : Date,
                callback : (text:boolean) => void
            ) {
        let respond = () => {
            if (this.wallet.expiry !== undefined) {
                const expiry = this.wallet.expiry.split('/');

                if (expiry.length === 2) {
                    const month = parseInt(expiry[0]);
                    const year  = parseInt(expiry[1]);

                    if (date.getFullYear() !== year) {
                        callback(date.getFullYear() > year);
                    } else {
                        callback(date.getMonth() >= month);
                    }
                } else {
                    callback(false);
                }
            } else {
                callback(false);
            }
        }

        if (this.wallet.loading) {
            this.toNotifyWallet.push(respond);
        } else {
            respond();
        }
    }

    public source() : Source|undefined {
        if (this.wallet.id        === undefined) return undefined;
        if (this.wallet.maskedNum === undefined) return undefined;
        if (this.wallet.type      === undefined) return undefined;
        if (this.wallet.name      === undefined) return undefined;
        if (this.wallet.expiry    === undefined) return undefined;

        return {
            token    : this.wallet.id,
            account  : this.wallet.maskedNum,
            type     : this.wallet.type,
            nickName : this.wallet.name,
            expiry   : this.wallet.expiry
        };
    }

    public surcharge_amount(
                callback : (amount:number) => void
            ) : void {
        const calculate = (bills : Map<string, Bill>) => {
            let retval = 0;
            for(let [id, bill] of bills) {
                if (bill.surchargeAmt !== undefined) {
                    retval += bill.surchargeAmt;
                }
            }
            callback(retval / 100);
        }

        if (this.wallet.loading === false && this.wallet.togo == 0) {
            calculate(this.bills);
        } else {
            this.toNotifyBills.push(() => {
                calculate(this.bills);
            });
        }
    }

    public total_amount(
                callback : (amount:number) => void
            ) : void {
        function calculate(bills :  Map<string, Bill>) {
            let retval = 0;
            for(let [id, bill] of bills) {
                if (bill.totalPayAmt !== undefined) {
                    retval += bill.totalPayAmt;
                }
            }
            callback(retval / 100);
        }

        if (this.wallet.loading === false && this.wallet.togo == 0) {
            calculate(this.bills);
        } else {
            this.toNotifyBills.push(() => {
                calculate(this.bills);
            });
        }
    }

    private retrieve_wallet_details(
                wallet : Wallet
            ) {
        if (wallet.id !== undefined) {
            $.ajax('getWalletInfo', {
                type : 'POST', data : { token : wallet.id }, processData : true
            }).done ((response) => {
                wallet.type      = response.sPaymentMethodType;
                wallet.name      = response.sPaymentMethodNickName;
                wallet.maskedNum = response.sPaymentMethodAccount;
                wallet.expiry    = response.sSourceExpiry;
            }).always(() => {
                wallet.loading = false;
                if (wallet.version === this.wallet.version) {
                    if (this.are_surchages_enabled() && wallet.type == 'credit') {
                        this.retrieve_wallet_inn(wallet);
                    } else {
                        this.update_surcharges(wallet);
                    }
                    this.notify_wallet();
                }
            });
        } else {
            this.update_surcharges(wallet);
        }
    }

    private retrieve_wallet_inn(
                wallet : Wallet
            ) {
        $.ajax('payment_GetIIN', {
            type : 'POST', data : { token : wallet.id }, processData : true
        }).done ((response) => {
            wallet.iin = response.iin;
        }).always(() => {
            if (wallet.version === this.wallet.version) {
                this.update_surcharges(wallet);
            };
        });
    }

    private retrieve_surcharge(
                wallet : Wallet,
                bill   : Bill,
                amount : number
            ) {
        if ((bill.interPayWorking === undefined || bill.interPayWorking === false) && wallet.type === 'debit') {
            type BillKey = keyof typeof bill;

            bill.interPayWorking =  true;
            wallet.togo          += 1;

            $.ajax('getConvenienceFee', {
                type        : 'POST',
                data        : {
                    pay_group          : bill.paymentGroup,
                    pay_account_number  : bill.number
                },
                processData : true
            }).done ((response) => {

                if (wallet.version == this.wallet.version && bill.paymentAmount == amount) {
                    bill.surchargeAmt = Number(response.convenienceFeeAmount) * 100 ;
                    bill.totalPayAmt  = amount + bill.surchargeAmt;
                }

                bill.interPayWorking = false;

            }).always(() => {
                // this.wallet.iin !== undefined &&  (temproarily removing from condition)
                if ((wallet.version != this.wallet.version || bill.paymentAmount != amount)) {
                    if (bill.paymentAmount !== undefined) {
                        this.retrieve_surcharge(this.wallet, bill, bill.paymentAmount);
                    }
                }
                wallet.togo -= 1;
                this.notify_bills();
            });
        }
    }

    private update_surcharges(
                wallet : Wallet
            ) {
        for(let [id, bill] of this.bills) {
            bill.surchargeAmt = 0;
            bill.totalPayAmt  = bill.paymentAmount;

            // wallet.iin !== undefined &&  (temproarily removing from condition)
            if (bill.paymentAmount !== undefined) {
                this.retrieve_surcharge(wallet, bill, bill.paymentAmount);
            }
        }
        this.notify_bills();
    }

    public update_name(
                id   : string,
                name : string
            ) {
        const apply = (name:string) => {
            if (this.wallet.id === id) {
                this.wallet.name = name;
            }
            const unsaved = this.unsavedCards.get(id);

            if (unsaved !== undefined) {
                unsaved.name = name;
            }
        }

        if (name === '') {
            const i18n = new I18nService('paymentOneTime');
            i18n.get('unsavedName', apply);
        } else {
            apply(name);
        }
    }

    public invoices() : Map<string, number> {
        let retval = new Map<string, number>();

        for(let [ id, bill ] of this.bills) {
            if (bill.paymentAmount !== undefined) {
                retval.set(id, bill.paymentAmount / 100);
            } else {
                retval.set(id, 0);
            }
        }

        return retval;
    }

    public add_unsaved(
                id      : string,
                type    : string,
                account : string,
                expiry  : string
            ) {
        this.unsavedCards.set(id, {
            name    : undefined,
            type    : type,
            account : account,
            expiry  : expiry
        });
    }

    public set_wallet(
                id : string|undefined
            ) {
        if (id === '') id = undefined;

        if (id !== this.wallet.id) {
            this.wallet = {
                version   : this.wallet.version + 1,
                loading   : true,
                id        : id,
                name      : undefined,
                maskedNum : undefined,
                expiry    : undefined,
                type      : undefined,
                iin       : undefined,
                togo      : 0
            };
            if (id !== undefined && this.unsavedCards.has(id)) {
                const unsaved = this.unsavedCards.get(id);
                this.wallet.name      = unsaved?.name;
                this.wallet.type      = unsaved?.type;
                this.wallet.maskedNum = unsaved?.account;
                this.wallet.expiry    = unsaved?.expiry;
                this.wallet.loading   = false;

                if (this.are_surchages_enabled() && this.wallet.type == 'credit') {
                    this.retrieve_wallet_inn(this.wallet);
                } else {
                    this.update_surcharges(this.wallet);
                }
            } else {
                this.retrieve_wallet_details(this.wallet);
            }
        }
    }

    public set_payment(
                invoice : string,
                amount  : number
            ) {
        const bill   = this.bills.get(invoice);
        const actual = amount > 0 ? Math.round(amount * 100) : undefined;

        if (bill !== undefined && bill.paymentAmount != actual) {
            bill.paymentAmount       = actual;
            bill.surchargeAmt = 0;
            bill.totalPayAmt  = actual;

            // && this.wallet.iin !== undefined (temprorily removing it from condition)
            if (bill.paymentAmount !== undefined) {
                this.retrieve_surcharge(this.wallet, bill, bill.paymentAmount);
            } else {
                bill.interPayTransId = undefined;
            }
        } else if (bill === undefined) {
            console.error('Could not find bill [' + invoice + '].')
        }
    }

    public payment_data() : BillPay[] {
        let retval : BillPay[] = [];

        for(let [ id, bill ] of this.bills) {
            function safe(val : number|undefined) : string {
                if (val === undefined) return '0.00';
                return (val / 100).toFixed(2);
            }

            retval.push({
                internalAccountNumber: bill.number,
                displayAccountNumber : bill.numberDisplay,
                documentNumber       : bill.invoice,
                amount               : safe(bill.paymentAmount),
                paymentGroup         : bill.paymentGroup,
                surcharge            : safe(bill.surchargeAmt),
                totalAmount          : safe(bill.totalPayAmt),
                interPayTransactionId: bill.interPayTransId === undefined ? 'N/A' : bill.interPayTransId
            });
        }

        return retval;
    }
}