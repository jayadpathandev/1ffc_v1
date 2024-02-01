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
import React from 'react';

import { ProfileService } from '../../common/services/profile_service';
import { I18nService    } from '../../common/services/i18n_service';

export function UserProfile() {
    const [ username,     	setUsername   ] = React.useState('');
    const [ integrate,    	setIntegrate  ] = React.useState(false);
    const [ profileTxt,   	setProfileTxt ] = React.useState('');
    const [ contactPrefTxt, setContactPrefTxt  ] = React.useState('');
    const [ intTxt,       	setIntTxt     ] = React.useState('');
    const [ logoutTxt,    	setLogoutTxt  ] = React.useState('');

    React.useEffect(()=> {
        const i18nProfile   	= new I18nService('profile');
        const i18nContactPref	= new I18nService('contactPreferences');
        const i18nIntegrate 	= new I18nService('applicationIntegration');
        const i18nSaml      	= new I18nService('saml');

        ProfileService.username((name:string) => {
            setUsername(name);
        });
        ProfileService.actors((actors:string[]) => {
            var enabled = false;

            actors.forEach((actor) => {
                if (actor === 'view_app_integration') {
                    enabled = true;
                }
            });
            setIntegrate(enabled);
        });

        i18nProfile.get('sMenuLink', (text:string) => {
            setProfileTxt(text);
        })
        i18nContactPref.get('sMenuLink', (text:string) => {
            setContactPrefTxt(text);
        })
        i18nSaml.get('sMenuLink', (text:string) => {
            setLogoutTxt(text);
        })
        i18nIntegrate.get('sMenuLink', (text:string) => {
            setIntTxt(text);
        })
    }, []);

    if (username === '') {
        return null;
    }

    return (
        <div className="st-banner-links">
            <div className="dropdown">
                <button id="profileMenu" className="btn dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    {username}
                </button>
                <ul className="dropdown-menu" aria-labelledby="profileMenu">
                    <li><a  className="dropdown-item" href="appProfile" tabIndex={-1}>{profileTxt}</a></li>
                    <li><a  className="dropdown-item" href="appContactPreferences" tabIndex={-1}>{contactPrefTxt}</a></li>
                    { integrate ? <li><a  className="dropdown-item" href="appApplicationIntegration" tabIndex={-1}>{intTxt}</a></li> : null }
                    <li><hr className="dropdown-divider"/></li>
                    <li><a  className="dropdown-item" href="appLogout" tabIndex={-1}>{logoutTxt}</a></li>
                </ul>
            </div>
        </div>
    );
}
