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
import DataTable from 'datatables.net-bs5';

import { I18nService } from '../../common/services/i18n_service';

function bill_summary(
            parent : HTMLElement
        ) {
    $(parent).find('a[st-goto-usage-summary]').on('click', function() {
        const id = $(this).attr('st-goto-usage-summary') as string;

        $(".st-hidden-usage-fields .st-services span").each(function() {
            if ($(this).attr('st-servicenum') === id) {
                $.ajax({
                    url: 'getUsageChargesForAsset',
                    data: {
                        sBillingAccount: $(this).attr('st-internalaccountnumber'),
                        sBillDate:       $(this).attr('st-billdate'),
                        sPaymentGroup:   $(this).attr('st-paymentgroup'),
                        sAssetId:        id,
                        sLanguageCode:  'en',
                        sCountryCode:   'us'
                }}).then(() => {
                    window.open('goToUsage', '_self');
                }).fail(() => {
                    window.open('refresh.uc', '_self');
                });
            }
        });

    });
}
function bill_details(
            parent : HTMLElement
        ) {
    let table = $('#billUsage_tUsageDetailsTable');

    if (table.length > 0) {
        let rows = table.find('tr');

        table.each(function() {
            let page = $(this).parents('.modal-content')[0].outerHTML;
            $(this).data('st-print-preview', page);
        });
        table.DataTable({
            ordering:   true,
            paging:     rows.length > 25,
            lengthMenu: rows.length > 25 ? [ 25, 50, 100 ] : undefined,
            pageLength: rows.length > 25 ? 25 : undefined,
            pagingType: "full_numbers",
            searching:  false,
            order:      [[ 0, "asc" ]],
            language: {
                paginate: {
                    first:    "&laquo;",
                    previous: "&lsaquo;",
                    next:     "&rsaquo;",
                    last:     "&raquo;"
                }
            },
            dom: "<\"row\"<\"col-md-3\"l><\"col-md-9\"p>>t"
        });
    }
}


function bill_details_pp(
            parent : HTMLElement
        ) {
    $(parent).find("a.st-print-usage-details").each(function() {
        let i18n = {
            title: '',
            print: '',
            close: ''
        };

        new I18nService('billUsage').get_many(
            [ 'printPreviewTitle', 'printPreviewPrint', 'printPreviewClose' ],
            (text:Map<string, string>) => {
                i18n.title = text.get('printPreviewTitle') as string;
                i18n.print = text.get('printPreviewPrint') as string;
                i18n.close = text.get('printPreviewClose') as string;
            }
        );

        $(this).off("click").on("click", function ($event) {
            $event.preventDefault();

            let content = $('#billUsage_tUsageDetailsTable').data('st-print-preview') as string;

            var html = '<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">';
            html += '<html>';
            html += '<head>';
            html += '<title>' + i18n.title + '</title>';
            html += '<link href="css/print.css" type="text/css" rel="stylesheet"/>';
            html += '</head>';
            html += '<body id="printpreview">';
            html += content;
            html += '<div class="sti-print-buttons">';
            html += '<button onclick="javascript:this.parentNode.style.display = \'none\';window.print();this.parentNode.style.display = \'block\';" class="button">';
            html += '<span>' + i18n.print + '</span>';
            html += '</button>';
            html += '<button onclick="javascript:window.close();" class="button">';
            html += '<span>' + i18n.close + '</span>';
            html += '</button>';
            html += '</div>';
            html += '</body>';
            html += '</html>';

            let root   = $(this).parents('.modal-content');
            let width  = Math.min(root.width() as number, screen.width - 100);
            let height = Math.min(root.height() as number, screen.height - 100);

            let top  = (screen.height - height) / 2;
            let left = (screen.width  - width) / 2;

            let print = window.open(
                '',
                'newWindow_' + new Date().getMilliseconds(),
                'width=' + width + ', top=' + top + ', height=' + height + ', left=' + left +
                ', resizable=yes, scrollbars=yes, status=no, menubar=no, toolbar=no, location=no'
            );

            if(typeof print !== 'undefined' && print !== null) {
                print.document.write(html);
                $(print.document).find('[id]').attr('id', '');
                $(print.document).find('table').DataTable({
                    searching: false,
                    order:     [[ 0, "asc" ]],
                    paging:    false
                });
            }
        });
    });
}

export function bill(
            parent : HTMLElement
        ) {
    if ($().DataTable === undefined) {
        new DataTable('');
    }
    bill_summary(parent);
    bill_details(parent);
    bill_details_pp(parent);
}
