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
export interface PaymentWallet {
    responseCode:    string;
    responseMessage: string;
    token:           string;
    transactionId:   string;
    sourceName:      string;
    sourceNickname:  string;
    sourceType:      string;
    sourceNum:       string;
    sourceExpiry:    string;
}

declare global {
    interface Window {
        sorriso_refresh:                        (parent:HTMLElement)=>void;
        handleCancelResponseCallback:           (data:PaymentWallet) => void;
        handleAddSourceSuccessResponseCallback: (data:PaymentWallet) => void;
        handleAddSourceErrorResponseCallback:   (data:PaymentWallet) => void;
        handleEditSourceSuccessResponseCallback:(data:PaymentWallet) => void;
        handleEditSourceErrorResponseCallback:  (data:PaymentWallet) => void;
        handleErrorResponseCallback:            (data:PaymentWallet) => void;
        payment_size:                           (height:number) => void;
    }
}

window.sorriso_refresh = function(parent:HTMLElement) {
    console.error("No refresh function has been defiend yet.");
}