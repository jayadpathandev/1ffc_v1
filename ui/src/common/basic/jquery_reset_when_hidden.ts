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

//*****************************************************************************
interface Data {
    element : HTMLElement;
    value   : string;
}

//*****************************************************************************
export class ResetWhenHidden {
    private elements : Data[] = [];

    add(
                element : HTMLElement
            ) {
        let value = $(element).attr('value');
        if (value === undefined) {
            value = '';
        }
        this.elements.push({
            element : element,
            value   : value
        });
    }

    //*************************************************************************
    reset() : void {
        this.elements.forEach(data => {
            $(data.element).val(data.value).trigger('change');
        });
    }
};

//*****************************************************************************
export function jquery_reset_when_hidden(
            parent : HTMLElement
        ) : void {
    $(parent).find("[st-reset-when-hidden]").each(function() {
        let whenHidden = $(this).data('st-reset-when-hidden');

        if (whenHidden === undefined) {
            $(this).parents().each(function() {
                if (whenHidden === undefined) {
                    whenHidden = $(this).data('st-reset-when-hidden');
                }
            });
        }

        if (whenHidden !== undefined) {
            whenHidden.add(this);
        } else {
            console.error("Element is not dynamically hiddden: ", this);
        }
    });
}