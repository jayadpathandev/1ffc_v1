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
import { PasswordReusedService    } from '../../services/password_reused_service';

export class ValidatorPasswordReused extends ValidatorBase {
    //-------------------------------------------------------------------------
    private inProgress : boolean = false;
    private lastPass   : string  = '';
    private lastResult : boolean = false;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        super(state, field);

        this.set_message('password-reused');
    }

    //*************************************************************************
    protected validate(
                callback : (valid:boolean)=>void
            ) : boolean|undefined {
        //---------------------------------------------------------------------
        const password = this.as_string();

        if (password === '')            return true;
        if (this.lastPass === password) return this.lastResult;
        if (this.inProgress == true)    return undefined;

        //---------------------------------------------------------------------
        const process = (reused:boolean) => {
            this.inProgress = false;
            this.lastPass   = password;
            this.lastResult = !reused;

            if (password === this.as_string()) {
                callback(!reused);
            } else {
                this.revalidate();
            }
        }

        //---------------------------------------------------------------------
        PasswordReusedService.is_password_reused(
            password,
            process
        );
        return undefined;
    }
}