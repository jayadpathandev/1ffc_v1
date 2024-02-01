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
import $ from 'jquery';

import { AuthenticationService } from '../services/authentication_service';

//*****************************************************************************
// Handle a re-authenticate panel.
export function panel_reauth(
            parent : HTMLElement
        ) {
    $(parent).find('form[st-panel-reauth]').each(function() {
        const saveId   = $(this).attr('st-panel-reauth') as string;
        const useCase  = ($(this).attr('id') as string).split('_')[0];
        const $passwd  = $(this).find('input[type="password"]');
        const $save    = $(this).parent().find('#' + useCase + '_' + saveId +' input[type="password"]');
        const $invalid = $(this).find('*[st-panel-reauth-invalid]');
        const $locked  = $(this).find('*[st-panel-reauth-locked]');

        $(this).on('submit', function($event) {
            const form   = this;
            const passwd = $passwd.val() as string;
            $event.preventDefault();
            $save.val(passwd);

            AuthenticationService.verify(passwd, (valid, locked) => {
                if (valid === true) {
                    $invalid.addClass('visually-hidden');
                } else {
                    $invalid.removeClass('visually-hidden');
                }
                if (locked === true) {
                    $locked.removeClass('visually-hidden');
                } else {
                    $locked.addClass('visually-hidden');
                }
                if (valid === true) {
                    $(form).addClass('visually-hidden');
                    $(form).next().removeClass('visually-hidden');
                }
            });
        });
    });
}
