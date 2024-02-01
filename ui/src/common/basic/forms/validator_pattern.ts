// (c) Copyright 2022 Sorriso Technologies, Inc(r), All Rights Reserved,
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
import XRegExp from 'xregexp';

import { ElementState  } from './element_state';
import { ValidatorBase } from './validator_base';

export class ValidatorPattern extends ValidatorBase {
    private regex : RegExp;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        const pattern = field.attr('st-pattern');
        if (pattern === undefined)  throw("No pattern defined!");

        this.regex = XRegExp(pattern);

        this.set_message('validation');
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const value = this.as_string();

        if (value === '') return true;

        return this.regex.test(value);
    }
}