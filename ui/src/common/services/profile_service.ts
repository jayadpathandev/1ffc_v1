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

interface Profile {
    username    : string;
    userId      : string;
    companyId   : string;
    companyName : string;
    actors      : string[];
    reuse       : number;
    orgAdmin    : boolean;
}

interface Response {
    success : boolean;
    status  : number;
    output  : Profile;
}

export class ProfileService {
    private static instance: ProfileService;

    private state   : string = 'idle';
    private data    : Profile|null = null;
    private waiting : Array<(data:Profile|null) => void> = [];

    private constructor() {}

    private static get(
                callback : (data:Profile|null) => void
            ) : void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!ProfileService.instance) {
            ProfileService.instance = new ProfileService();
        }
        var instance = ProfileService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        if (instance.data !== null || instance.state === 'ran') {
            callback(instance.data);
            return;
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        instance.waiting.push(callback);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        if (instance.state === 'idle') {
            instance.state = 'running';

            $.ajax("get_profile").done((data:Response) => {
                if (data.success === true) {
                    instance.data = data.output;
                }
            }).always(() => {
                instance.waiting.forEach((callback) => {
                    callback(instance.data);
                });
                instance.waiting = [];
                instance.state   = 'ran';
            });
        }
    }

    public static username(
                callback : (user:string) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.username);
            } else {
                callback('');
            }
        });
    }

    public static user_id(
                callback : (id:string) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.userId);
            } else {
                callback('');
            }
        });
    }

    public static company_id(
                callback : (id:string) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.companyId);
            } else {
                callback('');
            }
        });
    }

    public static company_name(
                callback : (name:string) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.companyName);
            } else {
                callback('');
            }
        });
    }

    public static actors(
                callback : (actors:string[]) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.actors);
            } else {
                callback([]);
            }
        });
    }

    public static org_admin(
                callback : (orgAdmin:boolean) => void
            ) : void {
        ProfileService.get((data:Profile|null) => {
            if (data !== null) {
                callback(data.orgAdmin);
            } else {
                callback(false);
            }
        });
    }
}
