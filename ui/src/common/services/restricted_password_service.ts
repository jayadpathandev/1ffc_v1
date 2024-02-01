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
import $       from 'jquery';
import XRegExp from 'xregexp';

interface Rule {
    type  : string;
    value : string;
};
interface Request {
    username : string;
    password : string;
    callback :  (valid:boolean) => void
}

export class RestrictedPasswordService {
    private static instance: RestrictedPasswordService;

    private state   : string = 'idle';
    private rules   : Rule[]|undefined;
    private waiting : Array<Request> = [];

    private constructor() {}

    private respond(
                request : Request
            ) : void {
        if (this.rules !== undefined) {
            const valid = this.rules.every(rule => {
                const type  = rule.type;
                const value = rule.value.replace("\\U", request.username);

                if (type === "String") {
                    var lowerPassword = request.password.toLowerCase();
                    var lowerRule     = value.toLowerCase();

                    if (lowerPassword.indexOf(lowerRule) != -1) {
                        return false;
                    }
                } else if (type == "Regex") {
                    var regex = XRegExp(value, 'i');

                    if (regex.test(request.password) == true) {
                        return false
                    }
                }
                return true;
            });
            request.callback(valid);
        } else {
            request.callback(true);
        }
    }

    private static handle_request(
                request : Request
            ) : void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!RestrictedPasswordService.instance) {
            RestrictedPasswordService.instance = new RestrictedPasswordService();
        }
        const instance = RestrictedPasswordService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        if (instance.state == 'ran') {
            instance.respond(request);
            return;
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        instance.waiting.push(request);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        if (instance.state === 'idle') {
            instance.state = 'running';

            $.ajax("invalid_password_list").done((rules:Rule[]) => {
                rules.forEach(rule => {
                    rule.value = rule.value.replace(/\\\[/g, "[").replace(/\\\]/g, "]");
                });
                instance.rules = rules;
            }).always(() => {
                instance.waiting.forEach((request) => {
                    instance.respond(request);
                });
                instance.waiting = [];
                instance.state   = 'ran';
            });
        }
    }

    public static verify(
                username : string,
                password : string,
                callback : (valid:boolean) => void
            ) : void {
        RestrictedPasswordService.handle_request({
            username : username,
            password : password,
            callback : callback
        });
    }
}
