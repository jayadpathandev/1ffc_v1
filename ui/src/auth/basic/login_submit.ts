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
import $ from 'jquery';

export default function login_submit(
            parent : HTMLElement
        ) {
    var BUTTON : JQuery | null = null;
    var FORM   : JQuery | null = null;

    function process($event : JQuery.Event, useCase : string) {
        $event.preventDefault();

        var data = {
            "_internalMovement"          : BUTTON != null ? BUTTON.attr('name') : '<NOT FOUND>',
            "fTempPassword.pInput"       : $("[name='fTempPassword.pInput']").val(),
            "fUserName.pInput"           : $("[name='fUserName.pInput']").val(),
            "fPassword.pInput"           : $("[name='fPassword.pInput']").val(),
            "fConfirmPassword.pInput"    : $("[name='fConfirmPassword.pInput']").val(),
            "fRememberPc.rInput"         : $("[name='fRememberPc.rInput']:checked").val(),
            "fValidationCode.pInput"     : $("[name='fValidationCode.pInput']").val(),
            "fSecretAnswer1.pInput"      : $("[name='fSecretAnswer1.pInput']").val(),
            "fSecretAnswer2.pInput"      : $("[name='fSecretAnswer2.pInput']").val(),
            "fSecretAnswer3.pInput"      : $("[name='fSecretAnswer3.pInput']").val(),
            "fSecretAnswer4.pInput"      : $("[name='fSecretAnswer4.pInput']").val(),
            "f2FAValidationCode.pInput"  : $("[name='f2FAValidationCode.pInput']").val(),
            "f2FAValidationCode1.sValue" : $("[name='f2FAValidationCode1.sValue']").val(),
            "f2FAValidationCode2.sValue" : $("[name='f2FAValidationCode2.sValue']").val()
        };

        $.ajax({
            url     : FORM != null ? FORM.attr('action') : '<NOT FOUND>',
            type    : 'POST',
            data    : data,
            success : function(response, status, request) {
                if (request.getResponseHeader("x-persona-screen-type") === "page") {
                    window.open("refresh.uc?_pagekey=" + request.getResponseHeader("x-persona-pagekey"), "_self");
                } else {
                    var form = document.createElement("form");
                    form.setAttribute("method", "post");
                    form.setAttribute("action", response.forward);

                    var saml = document.createElement("input");
                    saml.setAttribute("type", "hidden");
                    saml.setAttribute("name", "SAMLResponse");
                    saml.setAttribute("value", response.saml);
                    form.appendChild(saml);

                    document.body.appendChild(form);
                    form.submit();
                }
            }
        });
    }

    $(parent).find('input#loginPasswordPcNotRecognized_passwordSubmitLogin').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#loginPasswordPcNotRecognized_passwordSubmitLogin');
        FORM   = $(parent).find('form#loginPasswordPcNotRecognized_loginPasswordPcNotRecognizedForm');
        process($event, "notRecognized");
    });

    $(parent).find('input#loginPasswordPcRecognized_passwordSubmitLogin').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#loginPasswordPcRecognized_passwordSubmitLogin');
        FORM   = $(parent).find('form#loginPasswordPcRecognized_loginPasswordPcNotRecognizedForm');
        process($event, "recognized");
    });

    $(parent).find('input#regCompleteEnrollment_resetPasswordSubmit').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#regCompleteEnrollment_resetPasswordSubmit');
        FORM   = $(parent).find('form#regCompleteEnrollment_askPasswordForm');
        process($event, "recognized");
    });

    $(parent).find('input#loginResetPassword_resetPasswordSubmit').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#loginResetPassword_resetPasswordSubmit');
        FORM   = $(parent).find('form#loginResetPassword_loginResetPasswordForm');
        process($event, "recognized");
    });

    $(parent).find('input#loginCsrResetPassword_resetPasswordSubmit').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#loginCsrResetPassword_resetPasswordSubmit');
        FORM   = $(parent).find('form#loginCsrResetPassword_askResetPasswordForm');
        process($event, "recognized");
    });    

    $(parent).find('input#login2FactorAuthValidation_ValidationCodeSubmit').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#login2FactorAuthValidation_ValidationCodeSubmit');
        FORM   = $(parent).find('form#login2FactorAuthValidation_login2FactorAuthValidationForm');
        process($event, "recognized");
    });  

    $(parent).find('input#login2FASelfRecovery_validateAuthCodeSubmit').on('click', ($event : JQuery.Event)=>{
        BUTTON = $(parent).find('input#login2FASelfRecovery_validateAuthCodeSubmit');
        FORM   = $(parent).find('form#login2FASelfRecovery_enterAuthCodeForm');
        process($event, "recognized");
    });

}