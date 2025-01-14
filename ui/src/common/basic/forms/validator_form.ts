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

import { ElementState } from './element_state';

export class ValidatorForm {
    //*************************************************************************
    private elements : Map<string, ElementState> = new Map<string, ElementState>();
    private submit   : JQuery<any>;
    private customBtns : JQuery<any>[] | null = null;

    //*************************************************************************
    constructor(
                form : HTMLElement,
            ) {
        //---------------------------------------------------------------------
        $(form).off().on('submit', ($event) => {
            if (this.still_valid() == false) {
                $event.preventDefault();
                return false;
            }
            return true;
        });

        //---------------------------------------------------------------------
        this.submit = $(form).find('input[type="submit"]');

        //---------------------------------------------------------------------
        this.create_validators(form);
        this.update();
    }
    //*************************************************************************
    public get_state(
                id : string
            ) : ElementState|undefined {
        return this.elements.get(id);
    }

    public set_btns(btns : JQuery<any>[]) {
        this.customBtns = btns;
    }

    //*************************************************************************
    private create_validators(
                form : HTMLElement
            ) : void {
        $(form).find('input[type!="submit"], select, checkbox').each((i, elem) => {
            this.elements.set(
                $(elem).attr('id') as string,
                new ElementState(this, $(elem))
            );
        });
    }

    //*************************************************************************
    private still_valid() : boolean {
        for (let [ _, value ] of this.elements) {
            if (value.is_visible() && value.revalidate() == false) {
                return false;
            }
        }
        return true;
    }

    //*************************************************************************
    private has_errors() : boolean {
        for (let [ _, value ] of this.elements) {
            if (value.is_visible() && value.is_valid() == false) {
                return true;
            }
        }
        return false;
    }

    //*************************************************************************
    public update() : void {
        const errors = this.has_errors();

        this.submit.each(function() {
            $(this).prop('disabled', errors);
        });

        if (errors) {
            this.customBtns?.forEach(btn => {
                !btn.hasClass('disabled') && btn.addClass('disabled');
            })
        } else {
            this.customBtns?.forEach(btn => {
                btn.hasClass('disabled') && btn.removeClass('disabled');
            })
        }
    }
}