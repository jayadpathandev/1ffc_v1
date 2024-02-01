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
function id_ends_with(
            a      : HTMLElement,
            suffix : string
        ) : boolean {
    const id = $(a).attr('id');

    if (typeof id === 'string') {
        return id.endsWith(suffix);
    }

    return false;
}

//*****************************************************************************
function page_from_id(
            a : HTMLElement
        ) : number {
    const id = $(a).attr('id');

    if (typeof id === 'string') {
        const offset = id.lastIndexOf('_');

        if (offset != -1) {
            return parseInt(id.substring(offset+1));
        }
    }

    return -1;
}

//*****************************************************************************
function show_page(
            table : HTMLElement,
            page  : number,
            size  : number
        ) : void {
    const first = page * size;
    const last  = first + size;
    $(table)
        .find('tbody tr')
        .addClass(HIDE_CLASS)
        .slice(first, last)
        .removeClass(HIDE_CLASS);
}

//*****************************************************************************
function update_nav(
            nav : HTMLElement,
            page  : number
        ) : void {
    let   navs  = $(nav).find('li');
    const pages = navs.length - 2;
    const prev  = navs[0];
    const next  = navs[pages+1];

    //-------------------------------------------------------------------------
    if (page === 0) {
        $(prev).addClass('disabled');
    } else {
        $(prev).removeClass('disabled');
    }

    if (page === pages-1) {
        $(next).addClass('disabled');
    } else {
        $(next).removeClass('disabled');
    }

    //-------------------------------------------------------------------------
    navs = navs.slice(1, -1);
    navs.addClass(HIDE_CLASS).removeClass('active');

    //-------------------------------------------------------------------------
    let first = page - 2;
    let last  = page + 2
    if (first < 0) {
        first = 0;
        last  = Math.min(pages, 4);
    } else if (last >= pages) {
        first = Math.max(pages - 5, 0);
        last  = pages - 1;
    }
    navs = navs.slice(first, last+1);

    navs.removeClass(HIDE_CLASS);

    $(navs[page - first]).addClass('active');
}

//*****************************************************************************
function wrap(
            nav   : HTMLElement,
            table : HTMLElement
        ) {
    const paging  = parseInt($(nav).attr('size') as string);
    let   current = [ 0 ];

    $(nav).find('li a').each(function() {
        const previous = id_ends_with(this, '_page_prev');
        const next     = id_ends_with(this, '_page_next');
        const page     = page_from_id(this);

        $(this).on('click', function($event) {
            $event.preventDefault();
            if (previous == true) {
                current[0] = current[0] - 1;
                show_page(table, current[0], paging);
                update_nav(nav, current[0]);
            } else if (next == true) {
                current[0] = current[0] + 1;
                show_page(table, current[0], paging);
                update_nav(nav, current[0]);
            } else if (page !== current[0]) {
                current[0] = page;
                show_page(table, current[0], paging);
                update_nav(nav, current[0]);
            }
        });
        if (previous) $(this).parent().removeClass(HIDE_CLASS);
        if (next) $(this).parent().removeClass(HIDE_CLASS);
    });
    show_page(table, current[0], paging);
    update_nav(nav, current[0]);
}

//*****************************************************************************
function jquery_table_checkboxes(
            headers : JQuery<any>,
            rows    : JQuery<any>
        ) : void {
    function all_selected() : boolean {
        let retval = true;
        rows.each(function() {
            if ($(this).is(':checked') == false) {
                retval = false;
            }
        });
        return retval;
    }

    rows.on('change', function() {
        if ($(this).is(':checked') && all_selected()) {
            headers.prop('checked', true);
        } else {
            headers.prop('checked', false);
        }
    });

    headers.on('change', function() {
        const state = $(this).is(':checked');

        rows.each(function() {
            $(this).prop('checked', state);
        })
    });
}

//*****************************************************************************
export function jquery_table(
            parent : HTMLElement
        ) {
    $(parent).find('table').each(function() {
        const table = this;
        let   id    = ($(table).attr('id') as string) + '_nav';

        $(parent).find('nav[id="' + id + '"]').each(function() {
            wrap(this, table);
        });
        if ($(parent).find('nav[id="' + id + '"]').length == 0) {
            $(this).find('tbody tr').removeClass(HIDE_CLASS);
        }

        const headerCheck = $(this).find('thead input[st-table-checkbox]');

        if (headerCheck.length > 0) {
            const bodyCheck = $(this).find('tbody input[st-table-checkbox]');
            jquery_table_checkboxes(headerCheck, bodyCheck);
        }
    });
}