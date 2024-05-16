// (c) Copyright 2021 Sorriso Technologies, Inc(r), All Rights Reserved,
// Patents Pending.
//
// This product is distributed under license from Sorriso Technologies, Inc.
// Use without a proper license is strictly prohibited.  To license this
// software, you may contact Sorriso Technologies at:

// Sorriso Technologies, Inc.sPaymentSummaryHeader
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
import React from 'react';

import { PaymentDetail, PaymentDetailService, Grouping } from '../services/payment_detail_service';
import { I18nService } from '../services/i18n_service';
import { FormatService } from '../services/format_service';

export interface Props {
    id: string;
}

export function PaymentStatusTableContent(props: Props) {
    const i18n = new I18nService('paymentHistory');
    const [ sAccountIdLabel,  setAccountIdLabel ] = React.useState('{Account #}');
    const [ sBillNumberLabel, setBillNumberLabel ] = React.useState('{Bill #}');
    const [ sPayAmountLabel, setPayAmountLabel ] = React.useState('{{Pay amount}');
    const [ sPaySurchargeLabel, setPaySurchargeLabel ] = React.useState('{Surcharge}');
    const [ sPayTotalAmountLabel, setPayTotalAmountLabel ] = React.useState('{Total amount}');
    const [ sPaymentSummaryHeader, setPaymentSummaryHeader ] = React.useState('{Payment summary}');
    const [ expandedAccounts, setExpandedAccounts ] = React.useState('{Expand table}');
    const [ collapseAccounts, setCollapseAccounts ] = React.useState('{Collapse table}');
    const [ expanded, setExpanded ] = React.useState(false);
    const [ record, setRecord ] = React.useState<PaymentDetail | null>(null);

    React.useEffect(() => {
        PaymentDetailService.get(props.id, (input: PaymentDetail | null) => { // Retrieve payment detail and set it
            setRecord(input);
            if (input !== null && input.GROUPING.length === 1) {
                setExpanded(true)
            }
        });
    }, [props.id]);

    React.useEffect(()=> {
        let accountIdUnreg = i18n.get('sAccountIdLabel', (text:string) => {
            setAccountIdLabel(text);
        });

        let billNumberUnreg = i18n.get('sBillNumberLabel', (text:string) => {
            setBillNumberLabel(text);
        });

        let payAmountUnreg = i18n.get('sPayAmountLabel', (text:string) => {
            setPayAmountLabel(text);
        });

        let paySurchargeUnreg = i18n.get('sPaySurchargeLabel', (text:string) => {
            setPaySurchargeLabel(text);
        });

        let totalAmountUnreg = i18n.get('sPayTotalAmountLabel', (text:string) => {
            setPayTotalAmountLabel(text);
        });

        let paymentSummaryUnreg = i18n.get('sPaymentSummaryHeader', (text:string) => {
            setPaymentSummaryHeader(text);
        });

        let expandedAccountsUnreg = i18n.get('expandedAccounts', (text:string) => {
            setExpandedAccounts(text);
        });

        let collapseAccountsUnreg = i18n.get('collapseAccounts', (text:string) => {
            setCollapseAccounts(text);
        });
        return ()=>{
            accountIdUnreg();
            billNumberUnreg();
            payAmountUnreg();
            paySurchargeUnreg();
            totalAmountUnreg();
            paymentSummaryUnreg();
            expandedAccountsUnreg();
            collapseAccountsUnreg();
        }
    },[]);

    function toggle() {
        setExpanded(!expanded);
        i18n.get(expanded? 'expandedAccounts' : 'collapseAccounts', (text:string) => {
            setCollapseAccounts
            setExpandedAccounts(text);
        });
    }

    const showLink = record !== null && record.GROUPING.length > 1;
    const surcharge = record !== null && record.PAY_SURCHARGE > 0;
    const col1Class = surcharge ? 'col-3' : 'col-4';
    const col2Class = surcharge ? 'col-3 d-none d-md-table-cell' : 'col-4';
    const col3Class = surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'col-4';
    const col4Class = surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'd-none';
    const col5Class = surcharge ? 'col-2 text-end' : 'd-none';

    // link for expand tables
    const link = <a id="sPaymentDetailsPopinHeader_expand" className="btn btn-link btn-cancel st-padding-top5 st-payment-account-expand" tabIndex={5} onClick={toggle}>{expandedAccounts} </a>

    return (
        <div id="sPaymentDetailsPopin" className="col-12 ng-scope">
            <div id = "PaymentSummaryHeader" className="row">
                <h4 className= "col-6">
                    {sPaymentSummaryHeader}
                </h4>

                <div id="sPaymentsummaryHeader_link" className="col-6">
                        {showLink ? link : null}
                </div>
            </div>

            <div id="sPaymentDetailsPopinHeader" className="row st-payment-onetime-header-row st-payment-onetime-bold-font">
                <div id="st-popin-label" className={col1Class}>
                    <span className="st-paymentDetails-account-number">{sAccountIdLabel}</span>
                </div>
                <div id="st-popin-label" className={col2Class}>
                    <span className="st-paymentDetails-bill-number">{sBillNumberLabel}</span>
                 </div>
                <div id="st-popin-label" className={col3Class}>
                    <span className="st-paymentDetails-pay-amount">{sPayAmountLabel}</span>
                </div>
                <div id="st-popin-label" className={col4Class}>
                    <span className="st-paymentDetails-pay-surcharge">{sPaySurchargeLabel}</span>
                </div>
                <div id="st-popin-label" className={col5Class}>
                    <span className="st-paymentDetails-pay-total-amount">{sPayTotalAmountLabel}</span>
                </div>
            </div>

            {expanded ? <ExpandedAccounts bills={record?.GROUPING} surcharge={surcharge}/> : <CollapsedAccounts record={record} surcharge={surcharge}/>} 

        </div>
    );
}

interface CollapsedProps {
    record: PaymentDetail | null;
    surcharge: boolean;
}

function CollapsedAccounts(props: CollapsedProps) {
    const i18n = new I18nService('paymentHistory');
    const [ sAccountNum, setAccountNum ] = React.useState('{accounts}');   
    const [ sBillNum, setBillNum ] = React.useState('{bills}'); 
    const [ sPayAmt,  setPayAmt ]  = React.useState('');
    const [ sPaySurcharge,  setPaySurcharge ]  = React.useState('');
    const [ sPayTotalAmt,  setPayTotalAmt ]  = React.useState('');

    React.useEffect(()=> {
        let accountUnreg = i18n.get('sAccountNum', (text:string) => {
            setAccountNum(text);
        });

        let billUnreg = i18n.get('sBillNum', (text:string) => {
            setBillNum(text);
        });
        return ()=>{
            accountUnreg();
            billUnreg();
        }
    },[]);

    React.useEffect(()=> {
        if (props.record !== null) {
            FormatService.currency(props.record.PAY_AMT, props.record.GROUPING[0].PAYMENT_GROUP, (text:string) => {
                setPayAmt(text);
            });
        
            FormatService.currency(props.record.PAY_SURCHARGE, props.record.GROUPING[0].PAYMENT_GROUP, (text:string) => {
                setPaySurcharge(text);
            });   
            
            FormatService.currency(props.record.PAY_TOTAL_AMT, props.record.GROUPING[0].PAYMENT_GROUP, (text:string) => {
                setPayTotalAmt(text);
            });     
        }
    },[props.record]);
 
    if (props.record === null) return null;
    const count = props.record.GROUPING.length;

    const col1Class = props.surcharge ? 'col-3' : 'col-4';
    const col2Class = props.surcharge ? 'col-3 d-none d-md-table-cell' : 'col-4';
    const col3Class = props.surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'col-4 text-end';
    const col4Class = props.surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'd-none';
    const col5Class = props.surcharge ? 'col-2 text-end' : 'd-none';

    return (
        <div id="step1_collapsedView" className="row st-payment-onetime-border-top">

            <div id="step1_collapsedView_sAccountNumLabel_content" className={col1Class}>
                <span>{count} {sAccountNum}</span>
            </div>

            <div id="step1_collapsedView_sAccountsLabel_content" className={col2Class}>
                <span>{count} {sBillNum}</span>
            </div>

            <div id="st-popin-label" className={col3Class}>
                <span className="st-paymentDetails-pay-amount">{sPayAmt}</span>
            </div>    

            <div id="st-popin-label" className={col4Class}>
                <span className="st-paymentDetails-pay-surcharge">{sPaySurcharge}</span>
            </div>   

            <div id="st-popin-label" className={col5Class}>
                <span className="st-paymentDetails-pay-total-amount">{sPayTotalAmt}</span>
            </div>              
    
        </div>
    );
}

interface LineProps {
    grouping: Grouping;
    surcharge: boolean;
}

function Line(props: LineProps) {

    const [ sPayAmt,  setPayAmt ]  = React.useState('');
    const [ sPaySurcharge,  setPaySurcharge ]  = React.useState('');
    const [ sPayTotalAmt,  setPayTotalAmt ]  = React.useState('');

    React.useEffect(()=> {
  
        FormatService.currency(props.grouping.AMOUNT, props.grouping.PAYMENT_GROUP, (text:string) => {
            setPayAmt(text);
        });
    
        FormatService.currency(props.grouping.PAY_SURCHARGE, props.grouping.PAYMENT_GROUP, (text:string) => {
            setPaySurcharge(text);
        });   
        
        FormatService.currency(props.grouping.PAY_TOTAL_AMOUNT, props.grouping.PAYMENT_GROUP, (text:string) => {
            setPayTotalAmt(text);
        });     
        
    },[props.grouping]);
    
    
    const col1Class = props.surcharge ? 'col-3' : 'col-4';
    const col2Class = props.surcharge ? 'col-3 d-none d-md-table-cell' : 'col-4';
    const col3Class = props.surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'col-4 text-end';
    const col4Class = props.surcharge ? 'col-2 d-none d-md-table-cell text-end' : 'd-none';
    const col5Class = props.surcharge ? 'col-2 text-end' : 'd-none';

    return (
        <div id="step1_expandedView_displayAccountNumber" className="row st-payment-onetime-border-top">
            <div id="step1_collapsedView_sAccountNumLabel_content" className={col1Class}>
                <span>{props.grouping.DISPLAY_ACCOUNT_NUMBER}</span>
            </div>

            <div id="step1_collapsedView_sAccountsLabel_content" className={col2Class}>
                <span>{props.grouping.DOCUMENT_NUMBER}</span>
            </div>

            <div id="st-popin-label" className={col3Class}>
                <span className="st-paymentDetails-pay-amount">{sPayAmt}</span>
            </div>

            <div id="st-popin-label" className={col4Class}>
                <span className="st-paymentDetails-pay-surcharge">{sPaySurcharge}</span>
            </div>

            <div id="st-popin-label" className={col5Class}>
                <span className="st-paymentDetails-pay-total-amount">{sPayTotalAmt}</span>
            </div>
        </div>
    );
}

interface ExpandedProps {
    bills: Grouping[] | undefined;
    surcharge: boolean;
}

function ExpandedAccounts(props: ExpandedProps) {
    const i18n = new I18nService('paymentHistory');
    const [ sAccountNumLabel, setAccountNumLabel ] = React.useState('{Account #}');

    React.useEffect(()=> {
        i18n.get('sAccountNumLabel', (text:string) => {
            setAccountNumLabel(text);
        });
    },[]);

    if (props.bills === undefined) return null;


    const accounts = props.bills.map((account: Grouping) => {
        return (<Line surcharge={props.surcharge} grouping={account} key={account.PAYMENT_GROUP+'_'+account.DOCUMENT_NUMBER}></Line>);
    });


    return (
        <>
            {accounts}
        </>
    );
}
