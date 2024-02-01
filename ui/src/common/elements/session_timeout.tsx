// (c) Copyright 2021-2022 Sorriso Technologies, Inc(r), All Rights Reserved,
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
import React          from 'react';
import * as bootstrap from 'bootstrap';
import { I18nService } from '../services/i18n_service';

const PRE_DELAY = 5000;

interface Props {
    timeout : number;
}

interface State {
    tillExpire : number;
    i18n       : Map<string, string>;
}

export class SessionTimeout extends React.Component<Props, State> {
    private prepareTimeout = 0;
    private warnTimeout    = 0;
    private tickerInterval = 0;

    //*************************************************************************
    constructor(props : Props) {
        super(props);
        this.state = {
            tillExpire : 60,
            i18n       : new Map<string, string>()
        };
    }

    //*************************************************************************
    componentDidMount() {
        if (window.location.pathname.endsWith("session_expired") == false) {
            if (this.props.timeout > 60) {
                this.prepareTimeout = window.setTimeout(
                    () => this.prepare(),
                    this.props.timeout * 1000 - 60000 - PRE_DELAY
                );
            }
        }
    }

    //*************************************************************************
    componentWillUnmount() {
        if (this.prepareTimeout !== 0) clearTimeout(this.prepareTimeout);
        if (this.warnTimeout    !== 0) clearTimeout(this.warnTimeout);
        if (this.tickerInterval  !== 0) clearInterval(this.tickerInterval);
    }

    //*************************************************************************
    private show() {
        let elem = document.getElementById('expiryDialog');

        if (elem !== null) {
            bootstrap.Modal.getOrCreateInstance(elem, {
                backdrop: 'static',
                keyboard: false
            })?.show();
        }
    }

    //*************************************************************************
    private hide() {
        let elem = document.getElementById('expiryDialog');

        if (elem !== null) {
            bootstrap.Modal.getOrCreateInstance(elem)?.hide();
        }
    }

    //*************************************************************************
    display() {
        this.warnTimeout = 0;
        this.setState({
            tillExpire: 60
        });
        this.tickerInterval = window.setInterval(() => {
            if (this.state.tillExpire == 1) {
                window.open("session_expired", "_self");
            }
            this.setState({
                tillExpire: this.state.tillExpire - 1
            });
        }, 1000);

        this.show();
    }

    //*************************************************************************
    prepare() {
        this.prepareTimeout = 0;
        this.warnTimeout    = window.setTimeout(() => this.display(), PRE_DELAY);

        new I18nService('utilsSessionExpire').get_many([
            'sExpiringTitle', 'sExpiringMsgStart', 'sExpiringMsgEnd', 'sDoNotExpire'
        ], (text:Map<string, string>) => {
            this.setState({
                i18n: text
            });
        });
    }

    //*************************************************************************
    reset() {
        this.hide();
        clearInterval(this.tickerInterval);
        this.tickerInterval = 0;
        this.warnTimeout = window.setTimeout(
            () => this.display(),
            this.props.timeout * 1000 - 60000
        );
        this.setState({
            tillExpire: 60
        });
        $.ajax("refresh.uc");
    }

    render() {
        return(
            <div id="expiryDialog" className="modal" tabIndex={-1}>
                <div className="modal-dialog modal-md modal-fullscreen-sm-down modal-dialog-scrollable">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-tite">{this.state.i18n.get('sExpiringTitle')}</h5>
                            <button className="btn-close" data-bs-dismiss="modal" aria-label="Clsoe" type="button"></button>
                        </div>
                        <div className="modal-body">
                            <p>
                                {this.state.i18n.get('sExpiringMsgStart')} {this.state.tillExpire} {this.state.i18n.get('sExpiringMsgEnd')}
                            </p>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-primary" data-bs-dismiss="modal" onClick={() => this.reset()}>
                                {this.state.i18n.get('sDoNotExpire')}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}
