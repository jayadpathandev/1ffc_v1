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

import { ValidatorBase         } from './validator_base';
import { ElementState          } from './element_state';
import { DuplicateEmailService } from '../../services/duplicate_email_service';

export class ValidatorNewEmail extends ValidatorBase {
    //-------------------------------------------------------------------------
    private regex      = XRegExp('^(?!.{510})([\\d\\p{L}]+([_\\+\\-\\.\\d\\p{L}])*@([\\d\\p{L}\\-_]+\\.)+[\\d\\-\\p{L}]{2,200})$')
    private inProgress : boolean = false;
    private lastEmail  : string  = '';
    private lastResult : boolean = false;
    private userId     : string|undefined = undefined;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        const attr = field.attr('st-new-email');

        if (typeof attr === 'string' && attr !== '') {
            this.userId = attr;
        }

        this.set_message('new-email');
    }

    //*************************************************************************
    protected validate(
                callback : (valid:boolean)=>void
            ) : boolean|undefined {
        //---------------------------------------------------------------------
        const email = this.as_string();

        if (email === '')                    return true;
        if (this.lastEmail === email)        return this.lastResult;
        if (this.inProgress == true)         return undefined;
        if (this.regex.test(email) == false) return true;

        //---------------------------------------------------------------------
        const process = (unique:boolean) => {
            this.inProgress = false;
            this.lastEmail  = email;
            this.lastResult = unique;

            if (email === this.as_string()) {
                callback(unique);
            } else {
                this.revalidate();
            }
        }

        //---------------------------------------------------------------------
        if (this.userId === undefined) {
            DuplicateEmailService.verify_new_email(
                email,
                process
            );
        } else {
            DuplicateEmailService.verify_change_email(
                email,
                this.userId,
                process
            );
        }

        return undefined;
    }
}