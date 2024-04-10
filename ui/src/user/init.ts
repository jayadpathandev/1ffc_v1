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
import React from 'react';

import { hydrateRoot } from 'react-dom/client';
import { fffc } from './1ffc';
import { bill } from './elements/bill';
import { one_time_payment } from './elements/one_time_payment';
import { scheduled_payment } from './elements/scheduled_payment';
import { UserProfile } from './elements/user_profile';
import { payment_wallet } from './elements/wallet';
import { zingcharts } from './elements/zingcharts';
import { highlightRecentBills    } from './elements/document_search';

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
// Main entry function, this should be called by the initialization method for
// the application.
export default function user_elements(
            parent : HTMLElement
        ) : void{
    specific_elements(parent);
    zingcharts(parent);
    bill(parent);
    one_time_payment(parent);
    scheduled_payment(parent);
    payment_wallet(parent);
    fffc(parent);
    highlightRecentBills(parent);
}