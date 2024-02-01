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

import { AutomaticPayment, AutomaticPaymentService, Grouping } from '../services/automatic_payment';
import { I18nService } from '../services/i18n_service';

export interface Props {
    id: number;
    accounts: Grouping[];
}

export function ScheduleStep1Content(props: Props) {
    const i18n = new I18nService('paymentUpdateAutomaticPayment');
    const [ sPaymentSummaryHeaderPrefix, setPaymentSummaryHeaderPrefix ] = React.useState('{Payment summary - }');
    const [ sPaymentSummaryHeaderSuffix, setPaymentSummaryHeaderSuffix ] = React.useState('{selected accounts}');
    const [ expandedAccounts, setExpandedAccounts ] = React.useState('{Expand table}');
    const [ collapseAccounts, setCollapseAccounts ] = React.useState('{Collapse table}');
    const [ expanded, setExpanded ] = React.useState(false);
    const [ accounts, setAccounts ] = React.useState<Grouping[] | null>(null);

    React.useEffect(() => {
        if (props.id !== -1) {
            AutomaticPaymentService.get(props.id, (schedule: AutomaticPayment | null) => {
                if (schedule !== null) {
                    setAccounts(schedule.accounts);  // If there is only one acount then the expanded view is shown
                    if (schedule.accounts.length === 1)
                        setExpanded(true)
                }
                else {
                    setAccounts([]);
                }
            });
        }
        else {
            setAccounts(props.accounts);  // if there is one account, then the link collapse/expand will be removed
            if (props.accounts === null || props.accounts.length === 1)
                setExpanded(true)
        }
    }, [props.id, props.accounts]);

    React.useEffect(()=> {
        i18n.get('sPaymentSummaryHeaderPrefix', (text:string) => {
            setPaymentSummaryHeaderPrefix(text);
        });

        i18n.get('sPaymentSummaryHeaderSuffix', (text:string) => {
            setPaymentSummaryHeaderSuffix(text);
        });

        i18n.get('expandedAccounts', (text:string) => {
            setExpandedAccounts(text);
        });

        i18n.get('collapseAccounts', (text:string) => {
            setCollapseAccounts(text);
        });

    },[]);

    function toggle() {
        setExpanded(!expanded);
        i18n.get(expanded? 'expandedAccounts' : 'collapseAccounts', (text:string) => {
            setCollapseAccounts
            setExpandedAccounts(text);
        });
    }
    function remove(e: React.MouseEvent<HTMLAnchorElement, MouseEvent>, account:Grouping) {
        setAccounts([]);
    }

    const single = accounts === null || accounts.length === 1;
    // link for expand table
    const link = <a id="step1_sPaymentSummaryHeader_expand" className="btn btn-link btn-cancel st-padding-top5 st-payment-account-expand" tabIndex={5} onClick={toggle}>{expandedAccounts} </a>

    return (
        <div id="step1_container_content" className="col-md-12 ng-scope">
            <div id="step1_sPaymentSummaryHeader" className="row">
                <h4 className=" col-md-12">
                    <span className="st-payment-step-number">1</span>
                    <span className="st-payment-onetime-header">{sPaymentSummaryHeaderPrefix} </span>
                    <span>{accounts !== null ? accounts.length : 0}</span>
                    <span>&nbsp;</span>
                    <span className="st-payment-onetime-header">{sPaymentSummaryHeaderSuffix}</span>
                </h4>

                <div id="step1_sPaymentSummaryHeader_content" className=" col-md-6">
                    {!single ? link : null}
                </div>
            </div>

            {expanded ? <ExpandedAccounts accounts={accounts} remove={remove}/> : <CollapsedAccounts accounts={accounts}/>}

        </div>
    );
}

interface CollapsedProps {
    accounts: Grouping[] | null;
}

function CollapsedAccounts(props: CollapsedProps) {
    const i18n = new I18nService('paymentUpdateAutomaticPayment');
    const [ sAccountNumLabel, setAccountNumLabel ] = React.useState('{Account #}');
    const [ sAccountsLabel, setAccountsLabel ] = React.useState('{account(s)}');

    React.useEffect(()=> {
        i18n.get('sAccountNumLabel', (text:string) => {
            setAccountNumLabel(text);
        });

        i18n.get('sAccountsLabel', (text:string) => {
            setAccountsLabel(text);
        });
    },[]);


    if (props.accounts === null) return null;

    return (
        <div id="step1_collapsedView" className="row st-margin-left45">
            <div id="step1_collapsedView_content" className="col-md-12">
                <div id="step1_collapsedView_sAccountNumLabel" className="row st-payment-onetime-header-row">
                    <div id="step1_collapsedView_sAccountNumLabel_content" className="col-md-12">
                        <span>{sAccountNumLabel}</span>
                    </div>
                </div>
                <div id="step1_collapsedView_sAccountsLabel" className="row st-payment-onetime-border-top">
                    <div id="step1_collapsedView_sAccountsLabel_content" className="col-md-12 st-payment-onetime-bold-font">
                        <span>{props.accounts.length} {sAccountsLabel}</span>
                    </div>
                </div>
            </div>
        </div>
    );
}

interface ExpandedProps {
    accounts: Grouping[] | null;
    remove  : (e: React.MouseEvent<HTMLAnchorElement, MouseEvent>, account:Grouping) => void
}

function ExpandedAccounts(props: ExpandedProps) {
    const i18n = new I18nService('paymentUpdateAutomaticPayment');
    const [ sAccountNumLabel, setAccountNumLabel ] = React.useState('{Account #}'); 

    React.useEffect(()=> {
        i18n.get('sAccountNumLabel', (text:string) => {
            setAccountNumLabel(text);
        });
    },[]);

    if (props.accounts === null) return null;

    const accounts = props.accounts.map((account: Grouping) => {
        const single = props.accounts === null || props.accounts.length === 1;
        const link = <a id="step1_expandedView_actionsCol_contentHolder_link" className="cancelPaymentIcon st-left-space" href="#" onClick={(e) => props.remove(e, account)}></a>;
        return (
            <div id="step1_expandedView_displayAccountNumber" className="row st-payment-onetime-border-top" key={account.paymentGroup+'_'+account.internalAccountNumber}>
                <div id="step1_expandedView_displayAccountNumber_text" className="col-md-12 st-payment-onetime-bold-font">
                    <span className=" ">{account.displayAccountNumber}</span>
                    <span>
                        {!single ? link : null}
                    </span>
                </div>
            </div>
        );
    });


    return (
        <div id="step1_expandedView" className="row st-margin-left45 st-pay-expanded-view">
            <div id="step1_expandedView_accountNum" className="col-md-12">
                <div id="step1_expandedView_accountNumRow" className="row">
                    <div id="step1_expandedView_accountNumCol" className="col-md-12">
                        <div id="step1_expandedView_accountNumContent" className="row st-payment-onetime-header-row">
                            <div id="step1_expandedView_accountNumText" className="col-md-12 ">
                                <span>{sAccountNumLabel}</span>
                            </div>
                        </div>
                        {accounts}
                    </div>
                </div>
            </div>
         </div>
    );
}
