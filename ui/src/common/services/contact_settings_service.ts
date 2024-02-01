// (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved,
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

export interface TopicSetting {
    channel : string;
    selected: boolean;
}
export interface Topic {
    topic: string;
    channels: TopicSetting[];
}

export class ContactSettingsService {
    private static instance: ContactSettingsService;

    private state   : string = 'idle';
    private data    : Topic[]|null = null;
    private waiting : Set<(data:Topic[]|null) => void> = new Set();

    private constructor() {}

    public static get(
                callback : (data:Topic[]|null) => void
            ) : () => void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!ContactSettingsService.instance) {
            ContactSettingsService.instance = new ContactSettingsService();
        }
        var instance = ContactSettingsService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        if (instance.data !== null || instance.state === 'ran') {
            callback(instance.data);
            return () => {};
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        instance.waiting.add(callback);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        if (instance.state === 'idle') {
            instance.state = 'running';

            $.ajax("retrieve_contact_settings").done((data:Topic[]) => {
                instance.data = data;
                instance.data.sort((a, b) => {
					if(a.topic < b.topic) return -1;
					if(a.topic > b.topic) return 1;
					return 0;
				});
				instance.data.forEach(t => {
					t.channels.sort((a, b) => {
						if(a.channel === 'postal') return -1;
						if(a.channel === 'sms') return 1;
						
						if(b.channel === 'postal') return 1;
						if(b.channel === 'sms') return -1;
						
						return 0;
					})
				})
            }).always(() => {
                instance.waiting.forEach((callback) => {
                    callback(instance.data);
                });
                instance.waiting = new Set();
                instance.state   = 'ran';
            });            
        }
        return () => { instance.waiting.delete(callback); }
    }
}
