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
///<reference path="node_modules/@types/jquery/index.d.ts"/>

//*****************************************************************************
// Handle the incredible shrinking window!
$(function() {
    let expanded = true;
    let lastPos  = 0;

    this.addEventListener('scroll', () => {
        let yPos = window.scrollY;

        if (lastPos > yPos) { // User scrolled up
            if (expanded == false) {
                $('header').removeClass('minimize');
                expanded = true;
            }
        } else if (expanded == true && yPos > 30) {
            $('header').addClass('minimize');
            expanded = false;
        }
        lastPos = yPos;
    });
});