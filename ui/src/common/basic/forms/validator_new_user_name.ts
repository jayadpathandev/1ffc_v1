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
import { ValidatorBase            } from './validator_base';
import { ElementState             } from './element_state';
import { DuplicateUsernameService } from '../../services/duplicate_username_service';

export class ValidatorNewUserName extends ValidatorBase {
    //-------------------------------------------------------------------------
    private inProgress : boolean = false;
    private lastName   : string  = '';
    private lastResult : boolean = false;
    private userId     : string|undefined = undefined;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        const attr = field.attr('st-new-user-name');

        if (typeof attr === 'string' && attr !== '') {
            this.userId = attr;
        }

        this.set_message('new-user-name');
    }

    //*************************************************************************
    protected validate(
                callback : (valid:boolean)=>void
            ) : boolean|undefined {
        //---------------------------------------------------------------------
        const name = this.as_string();

        if (name === '')             return true;
        if (this.lastName === name)  return this.lastResult;
        if (this.inProgress == true) return undefined;

        //---------------------------------------------------------------------
        const process = (unique:boolean) => {
            this.inProgress = false;
            this.lastName   = name;
            this.lastResult = unique;

            if (name === this.as_string()) {
                callback(unique);
            } else {
                this.revalidate();
            }
        }

        //---------------------------------------------------------------------
        if (this.userId === undefined) {
            DuplicateUsernameService.verify_new_username(
                name,
                process
            );
        } else {
            DuplicateUsernameService.verify_change_username(
                name,
                this.userId,
                process
            );
        }
        return undefined;
    }
}