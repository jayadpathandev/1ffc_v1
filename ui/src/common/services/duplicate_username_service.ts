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
import $ from 'jquery';

interface Response {
    messageType : string;
    message     : string;
};

export class DuplicateUsernameService {

    private static handle_request(
                username : string,
                userId   : string|undefined,
                callback : (unique:boolean) => void
            ) : void {

        $.ajax({
            url: 'json_is_user_name_available',
            type: 'post',
            data: {
                sParamName: username,
                sParamNew : userId === undefined,
                sParamUserId: userId
        }}).done((resp : Response) => {
            callback(
                resp.messageType === 'validation' && resp.message === 'true'
            );
        }).fail(() => {
            callback(true);
        });
    }

    public static verify_new_username(
                username : string,
                callback : (unique:boolean) => void
            ) : void {
        DuplicateUsernameService.handle_request(
            username, undefined, callback
        );
    }

    public static verify_change_username(
                username : string,
                userId   : string,
                callback : (unique:boolean) => void
            ) : void {
        DuplicateUsernameService.handle_request(
            username, userId, callback
        );
    }
}
