import React from 'react';

// Define the function to add class to highlight most recent bills.
export function highlightRecentBills(
            parent : HTMLElement
        ): void {

    const currentDate = new Date().getTime();

    $(parent).find('[id^="documentSearchChild_tBillSearch"]').each((_, elem) => {
        $(elem).find('[id^="documentSearchChild_tBillSearch\\.sAfter1"]').each((_, date) => {
            const rowDate = new Date($(date).text()).getTime();
            const currentDate = new Date().getTime();
            const diff = Math.floor((currentDate - rowDate) / (1000 * 60 * 60 * 24));
            if (diff <= 30) {
                $(date).closest('tr').addClass('highlight');
            }
        });
    });
}




