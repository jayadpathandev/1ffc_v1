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
// The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
// "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
// "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
// "Persona E-Service", "Persona Customer Intelligence", "Persona Active
// Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
// Technologies, Inc.
import { ValidatorForm     } from './validator_form';

import { ValidatorDate                   } from './validator_date';
import { ValidatorNewEmail               } from './validator_new_email';
import { ValidatorNewUserName            } from './validator_new_user_name';
import { ValidatorPasswordReused         } from './validator_password_reused';
import { ValidatorPattern                } from './validator_pattern';
import { ValidatorRequiredCheckbox       } from './validator_required_checkbox';
import { ValidatorRequiredInput          } from './validator_required_input';
import { ValidatorRequiredSelect         } from './validator_required_select';
import { ValidatorRestrictedPassword     } from './validator_restricted_password';
import { ValidatorSameAs                 } from './validator_same_as';
import { ValidatorRequiredInputIfVisible } from './validator_required_input_if_visible';

//*****************************************************************************
export interface Validation {
    revalidate() : void;
    is_valid()   : boolean;
    is_ready()   : boolean;
};

//*****************************************************************************
export class ElementState {

    //-------------------------------------------------------------------------
    private form       : ValidatorForm;
    private field      : JQuery<any>;
    private validators : Validation[] = [];
    private dirty      : boolean;
    private valid      : boolean|undefined;
    private initial    : string|number|string[]|boolean|undefined;

    //*************************************************************************
    constructor(
                form  : ValidatorForm,
                field : JQuery<any>
            ) {
        this.form    = form;
        this.field   = field;
        this.dirty   = false;
        this.valid   = true;
        this.initial = this.get_value();

        this.field.on('change click keyup', () => {
            this.dirty = this.dirty || (this.get_value() !== this.initial);
        });

        this.create_validators();

        this.validators.forEach((v) => {
            v.revalidate();
        });
    }
    //*************************************************************************
    public add_custom(
                validator : Validation
            ) {
        this.validators.push(validator);
        validator.revalidate();
    }
    //*************************************************************************
    private create_validators() : void {
        if (this.field.prop('required')) {
            if (this.field.is('input[type="checkbox"]')) {
                this.validators.push(new ValidatorRequiredCheckbox(this, this.field));
            } else if (this.field.is('input')) {
                this.validators.push(new ValidatorRequiredInput(this, this.field));
            } else if (this.field.is('select')) {
                this.validators.push(new ValidatorRequiredSelect(this, this.field));
            } else {
                console.error("We do not handle the required flag on: ", this.field);
            }
        }

        if (this.field.hasClass('st-date-jquery')) {
            this.validators.push(new ValidatorDate(this, this.field));
        }

        if (this.field.attr('st-new-email') !== undefined) {
            this.validators.push(new ValidatorNewEmail(this, this.field));
        }

        if (this.field.attr('st-new-user-name') !== undefined) {
            this.validators.push(new ValidatorNewUserName(this, this.field));
        }

        if (this.field.attr('st-password-reused') !== undefined) {
            this.validators.push(new ValidatorPasswordReused(this, this.field));
        }

        if (this.field.attr('st-pattern') !== undefined) {
            this.validators.push(new ValidatorPattern(this, this.field));
        }

        if (this.field.is('input') && this.field.attr('st-required-if-visible') !== undefined) {
            this.validators.push(new ValidatorRequiredInputIfVisible(this, this.field));
        }

        if (this.field.attr('st-restricted-password') !== undefined) {
            this.validators.push(new ValidatorRestrictedPassword(this, this.field));
        }

        if (this.field.attr('st-same-as') !== undefined) {
            this.validators.push(new ValidatorSameAs(this, this.field));
        }

    }

    //*************************************************************************
    private get_value() : string|number|string[]|boolean|undefined {
        if (this.field.attr('type') === 'checkbox') {
            return this.field.is(':checked');
        }
        return this.field.val();
    }

    //*************************************************************************
    public is_dirty() : boolean {
        return this.dirty;
    }

    //*************************************************************************
    public is_visible() : boolean {
        if (this.field.hasClass("visually-hidden")) return false;
        return this.field.parents('.visually-hidden').length == 0;
    }

    //*************************************************************************
    public revalidate() : boolean {
        this.validators.forEach((v) => {
            v.revalidate();
        });
        return this.valid === true;
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
    public update() : void {
        //---------------------------------------------------------------------
        this.valid = true;

        this.validators.forEach((v) => {
            if (this.valid === true) {
                if (v.is_ready() === false) {
                    this.valid = undefined;
                } else if (v.is_valid() === false) {
                    this.valid = false;
                }
            }
        });

        if (this.dirty) {
            //-----------------------------------------------------------------
            if (this.valid === undefined) {
                this.field.removeClass('is-invalid');
                this.field.removeClass('is-valid');
            } else if (this.valid === true) {
                this.field.addClass('is-valid');
                this.field.removeClass('is-invalid');
            } else {
                this.field.removeClass('is-valid');
                this.field.addClass('is-invalid');
            }
        }

        //---------------------------------------------------------------------
        this.form.update();
    }
}