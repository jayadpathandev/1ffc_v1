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
import $              from 'jquery';
import * as bootstrap from 'bootstrap';
import React          from 'react';
import { createRoot } from 'react-dom/client';

import { IconArchive                    } from './elements/icons';
import { IconCalendar                   } from './elements/icons';
import { IconChevronDown                } from './elements/icons';
import { IconChevronLeft                } from './elements/icons';
import { IconChevronRight               } from './elements/icons';
import { IconDownload                   } from './elements/icons';
import { IconChevronUp                  } from './elements/icons';
import { IconInfo                       } from './elements/icons';
import { IconTrash                      } from './elements/icons';
import { IconTriangleDown               } from './elements/icons';
import { IconTriangleLeft               } from './elements/icons';
import { IconTriangleUp                 } from './elements/icons';
import { ScheduleStep1Content           } from './elements/schedule_step1_content';
import { SessionTimeout                 } from './elements/session_timeout';
import { PaymentStatusTableContent      } from './elements/payment_status_table';
import { default_forms                  } from './basic/forms/forms';
import { jquery_auto_submit             } from './basic/jquery_auto_submit';
import { jquery_datepicker              } from './basic/jquery_datepicker';
import { jquery_disable_when_set        } from './basic/jquery_disable_when_set';
import { jquery_goto_on_change          } from './basic/jquery_goto_on_change';
import { jquery_pop_in                  } from './basic/jquery_pop_in';
import { jquery_reset_when_hidden       } from './basic/jquery_reset_when_hidden';
import { jquery_show_when_checkbox      } from './basic/jquery_show_when_checkbox';
import { jquery_show_when_radio         } from './basic/jquery_show_when_radio';
import { jquery_table                   } from './basic/jquery_table';
import { profile_secret_questions       } from './elements/profile';
import { profile_personal_image         } from './elements/profile';
import { panel_reauth                   } from './elements/panels';
import { shared_dropdowns_except_first  } from './elements/shared_dropdowns_except_first';
import { ProfileTopicConfig 			} from './elements/profile_topic_config';
import { geoip_elements                 } from './geolocation';

//*****************************************************************************
// This method performs initialization for bootstrap javascript enabled
// features.
function bootstrap_elements(
            parent : HTMLElement
        ) : void {
    const tooltips = '[data-bs-toggle="tooltip"]';
    $(parent).find(tooltips).each(function() {
        new bootstrap.Tooltip(this);
    });

    const tabs = 'ul.nav.nav-tabs > li.nav-item > button';
    $(parent).find(tabs).each(function() {
        const trigger = new bootstrap.Tab(this);
        this.addEventListener('click', ($event) => {
            $event.preventDefault();
            trigger.show();
        });
    });

    const firstTab = 'ul.nav.nav-tabs > li.nav-item:first-child > button';
    $(parent).find(firstTab).each(function() {
        bootstrap.Tab.getInstance(this)?.show();
    });

}

//*****************************************************************************
// This method performs initialization for jquery enabled features.
function jquery_elements(
            parent : HTMLElement
        ) : void {
    jquery_auto_submit(parent);
    jquery_datepicker(parent);
    jquery_pop_in(parent);
    jquery_table(parent);
    jquery_show_when_checkbox(parent);
    jquery_show_when_radio(parent);
    jquery_disable_when_set(parent);
    jquery_goto_on_change(parent);
    default_forms(parent);
    jquery_reset_when_hidden(parent);
    shared_dropdowns_except_first(parent);
}

//*****************************************************************************
// This method finds all the elements with a "sorriso" attribute and transforms
// them into a React component.
function sorriso_elements(
            parent : HTMLElement
        ) : void {
    let e = React.createElement;

    //-------------------------------------------------------------------------
    // Process each element with a "sorriso" attribute.
    $(parent).find('*[sorriso]').each(function () {

        function attr_number(elem:HTMLElement, attr:string) : number {
            const input = $(elem).attr(attr);
            if (typeof input === 'string') {
                return parseInt(input);
            }
            return 0;
        }

        switch($(this).attr('sorriso')) {
        case 'expires':
            createRoot(this).render(
                e(SessionTimeout, {
                    timeout: attr_number(this, 'at')
                })
            );
            break;
        case 'element-topics-config':
		    createRoot(this).render(
		        e(ProfileTopicConfig, {})
		    );
		    break;
        case 'icon-archive':
            createRoot(this).render(
                e(IconArchive, {
                    size: 24
                })
            );
            break;
        case 'icon-calendar':
            createRoot(this).render(
                e(IconCalendar, {
                    size: 24
                })
            );
            break;
        case 'icon-chevron-down':
            createRoot(this).render(
                e(IconChevronDown, {
                    size: 24
                })
            );
            break;
        case 'icon-chevron-left':
            createRoot(this).render(
                e(IconChevronLeft, {
                    size: 24
                })
            );
            break;
        case 'icon-chevron-right':
            createRoot(this).render(
                e(IconChevronRight, {
                    size: 24
                })
            );
            break;
        case 'icon-chevron-up':
            createRoot(this).render(
                e(IconChevronUp, {
                    size: 24
                })
            );
            break;
        case 'icon-download':
            createRoot(this).render(
                e(IconDownload, {
                    size: 16
                })
            );
            break;
        case 'icon-info':
            createRoot(this).render(
                e(IconInfo, {
                    size: 16
                })
            );
            break;
        case 'icon-trash':
            createRoot(this).render(
                e(IconTrash, {
                    size: 16
                })
            );
            break;
        case 'icon-triangle-down':
            createRoot(this).render(
                e(IconTriangleDown, {
                    size: 24
                })
            );
            break;
        case 'icon-triangle-left':
            createRoot(this).render(
                e(IconTriangleLeft, {
                    size: 24
                })
            );
            break;
        case 'icon-triangle-up':
            createRoot(this).render(
                e(IconTriangleUp, {
                    size: 24
                })
            );
            break;
        case 'element-schedule-step1-content':
            const scheduleId = $(this).attr('scheduleId');
            const accounts   = $(this).attr('accounts');

            createRoot(this).render(
                e(ScheduleStep1Content, {
                    id: scheduleId !== undefined && scheduleId !== '' ? parseInt(scheduleId) : -1,
                    accounts: accounts != undefined && accounts !== '' ? JSON.parse(accounts): []
                })
            );
            break;
        case 'element-payment-status-table':
            const onlineTransId = $(this).attr('onlineTransId');

            createRoot(this).render(
                e(PaymentStatusTableContent, {
                    id: onlineTransId !== undefined && onlineTransId !== '' ? onlineTransId : ''
                })
            );
            break;
        }

    });
}

//*****************************************************************************
function basic_elements(
            parent : HTMLElement
        ) : void {
    profile_secret_questions(parent);
    profile_personal_image(parent);
    panel_reauth(parent);
    geoip_elements(parent);
}

//*****************************************************************************
// Main entry function, this should be called by the initialization method for
// the application.
export default function common_elements(
            parent : HTMLElement
        ) : void{
    bootstrap_elements(parent);
    jquery_elements(parent);
    sorriso_elements(parent);
    basic_elements(parent);
}