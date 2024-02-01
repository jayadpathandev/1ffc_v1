// (c) Copyright 2021-2022 Sorriso Technologies, Inc(r), All Rights Reserved,
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
// The N    ew Online Currency", "e-TransPromo", "Persona Enterprise Edition",
// "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
// "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
// "Persona E-Service", "Persona Customer Intelligence", "Persona Active
// Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
// Technologies, Inc.
import { ElementState, Validation } from './element_state';

//*****************************************************************************
export abstract class ValidatorBase implements Validation {

    //-------------------------------------------------------------------------
    private   delay   : number = 50;
    protected state   : ElementState;
    protected field   : JQuery<any>;
    private   message : JQuery<any> | undefined;
    private   valid   : boolean | undefined;
    private   timeout : number = 0;

    //*************************************************************************
    constructor(
                state : ElementState,
                field : JQuery<any>
            ) {
        this.state   = state;
        this.field   = field;
        this.message = undefined;
        this.valid   = undefined;

        this.field.on('keyup click change', () => {
            if (this.timeout > 0) clearTimeout(this.timeout);
            this.timeout = window.setTimeout(() => {
                this.revalidate();
            }, this.get_delay());
        });
    }

    //*************************************************************************
    protected get_delay() : number {
        return this.delay;
    }

    //*************************************************************************
    protected set_message(
                code : string
            ) : void {
        const selector = '*[sorriso-error="' + code + '"]';
        this.message = this.field.siblings(selector);
    }

    //*************************************************************************
    public revalidate() : void {
        let notified = false;
        const valid = this.validate((valid : boolean) => {
            notified = true;
            this.valid = valid;
            if (this.state.is_dirty()) {
                this.update_message();
            }
            this.state.update();
        });
        if (notified == false) {
            this.valid = valid;
            if (this.state.is_dirty()) {
                this.update_message();
            }
            this.state.update();
        }
    }

    //*************************************************************************
    protected abstract validate(
                callback : (valid:boolean) => void
            ) : boolean|undefined;

    //*************************************************************************
    protected as_string() : string {
        let val = this.field.val();

        if (typeof val === 'string') {
            val = val.trim();
        } else {
            val = '';
        }

        return val;
    }

    //*************************************************************************
    public is_valid() : boolean {
        return this.valid === true;
    }

    //*************************************************************************
    public is_ready() : boolean {
        return this.valid !== undefined;
    }

    //*************************************************************************
    public update_message() : void {
        if (this.message !== undefined) {
            if (this.valid !== undefined && this.valid === false) {
                this.message.removeClass('visually-hidden');
            } else {
                this.message.addClass('visually-hidden');
            }
        }
    }

}