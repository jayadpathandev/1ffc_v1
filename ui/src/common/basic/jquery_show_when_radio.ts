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
import { ResetWhenHidden } from './jquery_reset_when_hidden';

//*****************************************************************************
// Show/Hide an element when a radio button is selected.
export function jquery_show_when_radio(
            parent : HTMLElement
        ) : void {
    $(parent).find("[st-show-when-radio]").each(function() {
        const element   = this;
        const attr      = ($(element).attr('st-show-when-radio') as String).split('|');
        const target    = $(parent).find('input[type="radio"][value="' + attr[1] + '"][name^="' + attr[0] + '"]');
        let   whenHidden = $(this).data('st-reset-when-hidden');

        if (whenHidden === undefined) {
            whenHidden = new ResetWhenHidden();
            $(this).data('st-reset-when-hidden', whenHidden);
        }

        $(parent).find('input[type="radio"][name^="' + attr[0] + '"]').on('change', function() {
            if ($(target).is(":checked")) {
                $(element).removeClass('visually-hidden');
            } else {
                $(element).addClass('visually-hidden');
                whenHidden.reset();
            }
        });
        if (target.is(":checked")) {
            $(element).removeClass('visually-hidden');
        } else {
            $(element).addClass('visually-hidden');
            whenHidden.reset();
        }
    });
}
