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
// Show/Hide the Test Valid/Invalid messages.
function test_rule(
            parent : HTMLElement
        ) : void {
    const $passes   = $(parent).find('#configPwRestrictionsApp_fTest\\.sValid');
    const $rejects  = $(parent).find('#configPwRestrictionsApp_fTest\\.sInvalid');
    const $username = $(parent).find('#configPwRestrictionsApp_username');
    const $typeR    = $(parent).find('#configPwRestrictionsApp_fType\\.rControl_R');
    const $typeS    = $(parent).find('#configPwRestrictionsApp_fType\\.rControl_S');
    const $rule     = $(parent).find('#configPwRestrictionsApp_fValue\\.pInput');
    const $test     = $(parent).find('#configPwRestrictionsApp_fTest\\.pInput');

    $typeR.add($typeS).add($rule).add($test).on('change keyup click', function() {
        const username = $username.val() as string;
        const type = $typeR.is(":checked") ? 'R' : 'S';
        const rule = ($rule.val() as string).replace('\\U', username).toLowerCase();
        const test = $test.val() as string;

        if (rule !== '' && test !== '') {
            let passes = false;

            if (type === 'S') {
                passes = (test.indexOf(rule) == -1);
            } else if (type == 'R') {
                var regex = new RegExp(rule, 'i');
                passes = (regex.test(test) == false);
            }

            if (passes) {
                $passes.removeClass('visually-hidden');
                $rejects.addClass('visually-hidden');
            } else {
                $passes.addClass('visually-hidden');
                $rejects.removeClass('visually-hidden');
            }

        } else {
            $passes.addClass('visually-hidden');
            $rejects.addClass('visually-hidden');
        }
    });
}

export function password_restrictions(
            parent : HTMLElement
        ) {
    $(parent).find('form[id^="configPwRestrictionsApp_main_"]').each(function() {
        test_rule(this);
    });
}