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
import * as bootstrap from 'bootstrap';
import $ from 'jquery';

//*****************************************************************************
// Catch the click events on pop-in links and display the pop-in.
export function jquery_pop_in(
            parent : HTMLElement
        ) : void {
    let lock = { value: false };

    function display_popin(
                link : HTMLElement,
                html : string
            ) : void {
        //---------------------------------------------------------------------
        // Close the old pop-in if it exists.
        const popin = $(link).parents(".modal");
        if (popin.length == 1) {
            bootstrap.Modal.getInstance(popin[0])?.hide();
        }

        //---------------------------------------------------------------------
        // Open the popin.
        const parent = $('<div class="modal" tabindex="-1"></div>').appendTo('main')[0];
        const dialog = $('<div class="modal-dialog"></div>').append(html).appendTo(parent);
        const size   = $(link).attr('st-pop-in-size');
        const backdrop = $(link).attr('st-pop-in-backdrop');
        const keyboard = $(link).attr('st-pop-in-keyboard');

        const modalConfig: {
            backdrop?: 'static' | boolean | undefined,
            keyboard?: boolean | undefined,
            focus?: boolean | undefined,
        } = {}

        switch(backdrop) {
            case 'static':
                modalConfig.backdrop = 'static';
                break;
            case 'false':
                modalConfig.backdrop = false;
                break;
            default:
                modalConfig.backdrop = true;
                break;
        }

        switch(keyboard) {
            case 'false':
                modalConfig.keyboard = false;
                break;
            default:
                modalConfig.keyboard = true;
                break;
        }

        if (typeof size === 'string') {
			dialog.addClass('modal-' + size);
			dialog.addClass('modal-fullscreen-' + size + '-down');
			dialog.addClass('modal-dialog-scrollable');
        }

        var instance = new bootstrap.Modal(parent, modalConfig);

        parent.addEventListener('hidden.bs.modal', function() {
        	instance.dispose();
            parent.remove();
        });
        window.sorriso_refresh(parent);
        instance.show();
    }

    $(parent).find('*[st-pop-in]').each(function() {
        const conditional = $(this).attr('st-pop-in-conditional');

        $(this).on('click', function($event) {
            if (lock.value == false) {
                let post       = ($(this).attr('type') === 'submit')
                let form       = post ? $(this).parents('form') : undefined;
                let staticPage = false;
                let data       = {};

                if (form == undefined || form.length == 0) {
                    data = {
                        url:         $(this).attr('st-pop-in') as string,
                        type:        'get',
                        processData: false,
                        contentType: false
                    };
                    staticPage = conditional === undefined || conditional !== 'true';
                } else {
                    data = {
                        url:         form.attr('action') as string + '&_internalMovement=' + $(this).attr('name'),
                        type:        'post',
                        processData: false,
                        contentType: false,
                        data:        new FormData(form[form.length-1])
                    }
                }

                $event.preventDefault();

                lock.value = true;
                const link = this;
                $.ajax(data).done((data, status, request) => {
                    if (staticPage || request.getResponseHeader("x-persona-screen-type") === "fragment") {
                        display_popin(link, data);
                    } else {
                        window.open("refresh.uc?_pagekey=" + request.getResponseHeader("x-persona-pagekey"), "_self");
                    }
                }).always(() => {
                    lock.value = false;
                });
            }
        });
    });
}