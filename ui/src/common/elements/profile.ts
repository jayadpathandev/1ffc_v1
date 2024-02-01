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

const HIDE_CLASS = 'visually-hidden';

//*****************************************************************************
// Enable/Disable options in the secret questions drop down based on what has
// been selected.
export function profile_secret_questions(
            parent : HTMLElement
        ) {
    $(parent).find(".st-secret-questions").each(function () {
        const items    = $(this).find('select');
        const selected : string[] = [];

        items.each((i, dd) => {
            const offset = i;

            //---------------------------------------------------------------------
            // Initialize the state.
            $(dd).find('option[value="null"]').attr('value', '').trigger('change');

            const val = $(dd).val();
            selected.push((typeof val === 'string') ? val : '');

            if (selected[offset] !== '') {
                for(var j = 0; j < items.length; ++j) {
                    if (offset !== j) {
                        $(items[j]).find('option[value="' + val + '"]').prop('disabled', true);
                    }
                }
            }

            //---------------------------------------------------------------------
            // Handle changes.
            $(dd).on('change', () => {
                const raw    = $(dd).val();
                const newVal = ((typeof raw === 'string') ? raw : '');

                if (newVal !== selected[offset]) {
                    const oldSelect = 'option[value="' + selected[offset] + '"]';
                    const newSelect = 'option[value="' + newVal + '"]';

                    for(var j = 0; j < items.length; ++j) {
                        if (offset !== j) {
                            if (selected[offset] !== '') {
                                $(items[j]).find(oldSelect).prop('disabled', false);
                            }
                            if (newVal !== '') {
                                $(items[j]).find(newSelect).prop('disabled', true);
                            }
                        }
                    }
                    selected[offset] = newVal;
                }
            });
        });
    });
}

//*****************************************************************************
// Control the dynamic updates of the personal images.
export function profile_personal_image(
            parent : HTMLElement
        ) {
    const groupId  = $(parent).find('input[name="groupId"]');
    const memberId = $(parent).find('input[name="memberId"]');

    $(parent).find('img[st-personal-image-group][st-personal-image-member]').on('click', function() {
        const group  = $(this).attr('st-personal-image-group') as string;
        const member = $(this).attr('st-personal-image-member') as string;
        const image  = group + member;

        groupId.val(group);
        memberId.val(member);

        $(parent).find('img[st-personal-image-group][st-personal-image-member]').removeClass('selected');
        $(parent).find('img[st-personal-image-display]').addClass(HIDE_CLASS);

        $(this).addClass('selected');
        $(parent).find('img[st-personal-image-display="' + image + '"]').removeClass(HIDE_CLASS);
    });

    const image = (groupId.val() as string) + (memberId.val() as string);
    $(parent).find('img[st-personal-image-display="' + image + '"]').removeClass(HIDE_CLASS);
    $(parent).find('img[st-personal-image-group="' + groupId.val() + '"][st-personal-image-member="' + memberId.val() + '"]').addClass('selected');
}
