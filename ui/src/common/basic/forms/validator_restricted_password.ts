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

import { ValidatorBase             } from './validator_base';
import { ElementState              } from './element_state';
import { RestrictedPasswordService } from '../../services/restricted_password_service';

export class ValidatorRestrictedPassword extends ValidatorBase {
    //-------------------------------------------------------------------------
    private userField  : JQuery<any>|undefined;
    private userName   : string;
    private timer      : number = 0;
    private inProgress : boolean = false;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        this.set_message('restricted-password');

        //---------------------------------------------------------------------
        this.userName = field.attr('st-restricted-password') as string;

        if (this.userName.startsWith('#')) {
            const id = field.attr('id')?.split('_');
            if (id == undefined || id.length !== 2) throw("Invalid ID: " + field.attr('id'));

            const selector = '#' +
                $.escapeSelector(id[0] + '_' + this.userName.substring(1)) +
                ' input.form-control';

            this.userField = $(selector);
        }

        //---------------------------------------------------------------------
        if (this.userField !== undefined) {
            this.userField.on('keyup click change', () => {
                if (this.timer > 0) clearTimeout(this.timer);
                this.timer = window.setTimeout(() => {
                    this.revalidate();
                }, this.get_delay());
            });
        }
    }

    //*************************************************************************
    private get_username() : string {
        if (this.userField !== undefined) {
            let username = this.userField.val();
            return typeof username === 'string' ? username : '';
        }
        return this.userName;
    }

    //*************************************************************************
    protected validate(
                callback : (valid:boolean)=>void
            ) : boolean|undefined {
        //---------------------------------------------------------------------
        const username = this.get_username();
        const password = this.as_string();

        if (password === '')         return true;
        if (this.inProgress == true) return undefined;

        //---------------------------------------------------------------------
        RestrictedPasswordService.verify(
            username,
            password,
            (valid:boolean) => {
                this.inProgress = false;
                if (username == this.get_username() && password === this.as_string()) {
                    callback(valid);
                } else {
                    this.revalidate();
                }
        });

        return undefined;
    }
}