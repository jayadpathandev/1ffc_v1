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
import $ from 'jquery';

import { ElementState  } from './element_state';
import { ValidatorBase } from './validator_base';

export class ValidatorSameAs extends ValidatorBase {
    private source : JQuery<any>;
    private timer  : number = 0;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        this.set_message('same-as');

        //---------------------------------------------------------------------
        const id = field.attr('id')?.split('_');
        if (id == undefined || id.length !== 2) throw("Invalid ID: " + field.attr('id'));

        const selector = '#' +
            $.escapeSelector(id[0] + '_' + field.attr('st-same-as')) +
            ' input.form-control';

        this.source = $(selector);

        //---------------------------------------------------------------------
        this.source.on('keyup click change', () => {
            if (this.timer > 0) clearTimeout(this.timer);
            this.timer = window.setTimeout(() => {
                this.revalidate();
            }, this.get_delay());
        });
    }

    //*************************************************************************
    protected validate() : boolean|undefined {
        const target = this.as_string();
        let   source = this.source.val();

        if (typeof source === 'string') source = source.trim();

        return target === source;
    }
}