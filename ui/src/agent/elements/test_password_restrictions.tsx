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
import React   from 'react';
import XRegExp from 'xregexp';

import { ProfileService } from '../../common/services/profile_service';
import { I18nService    } from '../../common/services/i18n_service';
import { IconInfo       } from '../../common/elements/icons';

interface Rule {
    type  : string;
    value : string;
}

export function TestPasswordRestrictions() {
    const [ username, setUsername   ] = React.useState('');
    const [ i18n,     setI18n       ] = React.useState(new Map<string, string>());
    const [ app,      setApp        ] = React.useState('user');
    const [ password, setPassword   ] = React.useState('');
    const [ user,     setUserRules  ] = React.useState([] as Rule[]);
    const [ agent,    setAgentRules ] = React.useState([] as Rule[]);
    const [ defRegex, setRegex      ] = React.useState('');

    //-------------------------------------------------------------------------
    React.useEffect(()=> {
        const i18nUc     = new I18nService('configPwRestrictionsTest');
        const validation = new I18nService('validation');

        ProfileService.username((name:string) => {
            if (name !== username) {
                setUsername(name);
            }
        });

        i18nUc.get_many([
            'sApp', 'sAppUser', 'sAppAgent', 'sPassword', 'sPasswordHelp',
            'sDeniedByRegex', 'sDeniedByRule', 'sPasswordGood'
        ], (text:Map<string, string>) => setI18n(text) );
        validation.get('passwordRegex', (text : string) => setRegex(text) );

        $.ajax({
            url      : 'invalid_password_list?app=user',
            type     : 'GET',
            dataType : 'json',
            success : function(response) {
                const rules : Rule[] = response;

                rules.forEach((rule : Rule) => {
                    rule.value = rule.value.replace(/\\\[/g, "[").replace(/\\\]/g, "]");
                });

                setUserRules(rules);

            }
        });

        $.ajax({
            url      : 'invalid_password_list?app=agent',
            type     : 'GET',
            dataType : 'json',
            success : function(response) {
                const rules : Rule[] = response;

                rules.forEach((rule : Rule) => {
                    rule.value = rule.value.replace(/\\\[/g, "[").replace(/\\\]/g, "]");
                });

                setAgentRules(rules);
            }
        });

    }, []);

    //-------------------------------------------------------------------------
    if (i18n.size == 0) {
        return null;
    }

    //-------------------------------------------------------------------------
    function test() {
        if (password === '') {
            return [ 'na', '', '' ];
        }

        if (defRegex !== '') {
            if (XRegExp(defRegex).test(password) == false) {
                return [ 'validation', '', '' ];
            }
        }

        const list = (app === 'user' ? user : agent);

        for(let i = 0; i < list.length; ++i) {
			var type = list[i].type;
			var rule = list[i].value.replace("\\U", username);

			if (type == "String") {
				var lowerPassword = password.toLowerCase();
				var lowerRule     = rule.toLowerCase();

				if (lowerPassword.indexOf(lowerRule) != -1) {
                    return [ 'rule', type, list[i].value ];
				}
			} else if (type == "Regex") {
				if (XRegExp(rule, 'i').test(password) == true) {
                    return [ 'rule', type, list[i].value ];
				}
			}
        }
        return [ '', '', ''];
    }

    const [ result, type, rule ] = test();

    //-------------------------------------------------------------------------
    return (
        <div className="row">
            <div className="col-md-3">
                <div className="form-group st-field" onChange={(e:React.ChangeEvent<HTMLInputElement>) => setApp(e.target.value)}>
                    <div className="form-label st-field-label">
                        <label>{i18n.get('sApp')}</label>
                    </div>
                    <div className="st-field-field">
                        <div className="st-radio-item form-check">
                            <input tabIndex={1} value="user" type="radio" className="st-radio-control form-check-input" name="app"  defaultChecked={app==='user'}/>
                            <label className="st-radio-item-label form-check-label">
                                <span className="st-radio-text">{i18n.get('sAppUser')}</span>
                            </label>
                        </div>
                        <div className="st-radio-item form-check">
                            <input tabIndex={2} value="agent" type="radio" className="st-radio-control form-check-input" name="app" defaultChecked={app==='agent'}/>
                            <label className="st-radio-item-label form-check-label">
                                <span className="st-radio-text">{i18n.get('sAppAgent')}</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div className="col-md-9">
                <div className="form-group st-field">
                    <div className="form-label st-field-label">
                        <label>{i18n.get('sPassword')}</label>
                        <span className="ms-2 text-info" data-bs-toggle="tooltip" data-bs-placement="right" data-bs-trigger="hover focus" aria-label={i18n.get('sPasswordHelp')}>
                            <IconInfo size={16}/>
                        </span>
                    </div>
                    <div className="st-field-field">
                        <input tabIndex={3} className="form-control st-input-control" value={password} type="text" onChange={(e:React.ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}/>
                        { result === 'validation' ?
                            <div role="alert" className="st-validation alert alert-danger">
                                <span className="st-validation-text">{i18n.get('sDeniedByRegex')}</span>
                            </div> : null
                        }
                        { result === 'rule' ?
                            <div role="alert" className="st-validation alert alert-danger">
                                <span className=" st-validation-text">{(i18n.get('sDeniedByRule') as string).replace('{{rule}}', rule).replace('{{type}}', type)}</span>
                            </div> : null
                        }
                        { result === '' ?
                            <div role="alert" className="st-validation alert alert-success">
                                <span className=" st-validation-text">{i18n.get('sPasswordGood')}</span>
                            </div> : null
                        }
                    </div>
                </div>
            </div>
        </div>
    );
}
