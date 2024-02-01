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
import 'jquery-ui';

import { FormatService } from '../services/format_service';
import { I18nService   } from '../services/i18n_service';

//*****************************************************************************
// This method performs initialization for jquery enabled features.
export function jquery_datepicker(
            parent : HTMLElement
        ) : void {
    const fields = $(parent).find('input.st-date-jquery');

    if (fields.length > 0) {
        var I18N    = new I18nService('formats');
        var FULL    = [ "Januar", "Februar", "Marts", "April", "Maj", "Juni", "Juli", "August", "September", "Oktober", "November", "December" ];
        var SHORT   = [ "Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec" ];

        fields.each(function() {
            var actual = $(this).attr('st-actual-field');
            $(this).attr('st-date-format', 'yy-mm-dd').datepicker({
                altField: '#' + $.escapeSelector(actual ? actual : ''),
                altFormat: 'yy-mm-dd',
                dateFormat: 'yy-mm-dd',
                constrainInput: true
            });
        });

        FormatService.numeric_datepicker((format) => {
            $(parent).find('input.st-date-jquery').attr('st-date-format', format).datepicker(
                "option", "dateFormat", format
            );
        });

        for(var i = 1; i <= 12; ++i) {
            const offset = i-1;
            I18N.get('dateFullMonth' + i, (month:string) => {
                FULL[offset] = month;
                $(parent).find('input.st-date-jquery').datepicker(
                    "option", "monthNames", FULL
                );
            });
            I18N.get('dateMonth' + i, (month:string) => {
                SHORT[offset] = month;
                $(parent).find('input.st-date-jquery').datepicker(
                    "option", "monthNamesShort", SHORT
                );
            });
        }
    }
    $('button.st-date-button').on('click', function() {
        $(this).parent().find('input:first-child').datepicker("show");
    });
}
