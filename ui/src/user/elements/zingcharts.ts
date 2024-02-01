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
import $         from 'jquery';
import zingchart from 'zingchart';

interface Config {
    id: string;
    height: string;
    width: string;
    data: any;
    defaults: any;
};

function error(msg : string) {
    return {
        labels: [ {
            text:         msg,
            'font-color': '#ff0000',
            'font-family': '"Precision Sans Rg", CenturyGothic, AppleGothic, sans-serif;',
            'font-size':   '20',
            x:             '0%',
            y:             '0%'
        }]
    }
}

export function zingcharts(
            parent : HTMLElement
        ) {
    $(parent).find("[st-bill-zingchart]").each(function() {
        const url   = $(this).attr('st-bill-zingchart') as string;
        let config : Config = {
            id:       $(this).attr('id') as string,
            height:   '100%',
            width:   '100%',
            data:     undefined,
            defaults: undefined
        };

        $.ajax({
            url: 'ftl/charts/themes/chart_theme.json', dataType: 'json'
        }).done((data) => {
            config.defaults = data;
        }).fail(() => {
            config.defaults = { pallet: { vbar: [ ['#B2CCDE', '#D5E3ED'] ] } }
        }).always(() => {
            if (config.data && config.defaults) {
                zingchart.render(config);
            }
        });

        $.ajax({
            url: url + '&sIsMobile=' + (window.innerWidth < 768 ? 'true' : 'false'),
            dataType: 'json'
        }).done((data) => {
            if (data.error === 'CONFIG_NOT_FOUND' || data.error === 'INVALID_PARAMETER') {
                config.data = error('Error loading chart data.');
            } else if (data.error == 'BILL_NOT_FOUND') {
                config.data = error('No billing data found for chart.');
            } else {
                config.data = data;
            }
        }).always(() => {
            if (config.data && config.defaults) {
                zingchart.render(config);
            }
        });

    });
}