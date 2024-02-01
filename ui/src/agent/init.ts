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
import $     from 'jquery';
import React from 'react';
import { createRoot }  from 'react-dom/client';

import { hydrateRoot              } from 'react-dom/client';
import { UserProfile              } from './elements/user_profile';
import { password_restrictions    } from './elements/password_restrictions';
import { TestPasswordRestrictions } from './elements/test_password_restrictions';

//*****************************************************************************
// This method any special elements that need to be transformed into a react
// component
function specific_elements(
            parent : HTMLElement
        ) : void {
    var e = React.createElement;

    $(parent).find('.header-user-profile').each((_, elem) => {
        hydrateRoot(elem, e(UserProfile))
    });
}

//*****************************************************************************
// This method finds elements with a "sorriso" attribute and transforms
// them into a React component.
function sorriso_elements(
            parent : HTMLElement
        ) : void {
    let e = React.createElement;

    //-------------------------------------------------------------------------
    // Process each element with a "sorriso" attribute.
    $(parent).find('*[sorriso]').each(function () {

        switch($(this).attr('sorriso')) {

        case 'test-password-restrictions':
            createRoot(this).render(
                e(TestPasswordRestrictions)
            );
            break;
        }
    });
};

//*****************************************************************************
// Main entry function, this should be called by the initialization method for
// the application.
export default function agent_elements(
            parent : HTMLElement
        ) : void{
    specific_elements(parent);
    sorriso_elements(parent);
    password_restrictions(parent);
}