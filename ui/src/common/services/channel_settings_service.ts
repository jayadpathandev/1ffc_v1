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

export interface Channel {
    name 			: string;
    visible 		: boolean;
    configurable	: boolean;
    hardcoded 		: string;
    default 		: string;
}
export interface Topic {
    name	: string;
    channels: Channel[];
}

export class ChannelSettingsService {
    private static instance: ChannelSettingsService;

    private state   : string = 'idle';
    private data    : Map<string, Map<string, Channel >>|null = null;
    private waiting : Array<() => void> = [];

    private constructor() {}

    private static get(
                callback : () => void
            ) : void {
        //---------------------------------------------------------------------
        // Create the instance if needed.
        if (!ChannelSettingsService.instance) {
            ChannelSettingsService.instance = new ChannelSettingsService();
        }
        var instance = ChannelSettingsService.instance;

        //---------------------------------------------------------------------
        // We have the data, or tired to get the data.
        if (instance.data !== null || instance.state === 'ran') {
            callback();
            return;
        }

        //---------------------------------------------------------------------
        // Add the callback to the waiting list.
        instance.waiting.push(callback);

        //---------------------------------------------------------------------
        // If we haven't queried for the data do so now.
        if (instance.state === 'idle') {
            instance.state = 'running';

            $.ajax("retrieve_channel_settings").done((data:Topic[]) => {
                instance.data = new Map<string, Map<string, Channel>>();
                data.forEach((topic:Topic) => {
			    	let m = new Map<string, Channel>();
			    	topic.channels.forEach((chan:Channel) => {
			        	m.set(chan.name, chan);
			    	});
					instance.data?.set(topic.name, m);
				});
            }).always(() => {
                instance.waiting.forEach((callback) => {
                    callback();
                });
                instance.waiting = [];
                instance.state   = 'ran';
            });
        }
    }
    
    private static get_channel(
				topic 	 : string,
				channel  : string,
				callback : (chan:Channel) => void
			) : void {
		ChannelSettingsService.get(() => {
		    if (ChannelSettingsService.instance.data == null) {
				throw("Could not retrieve channel settings.")
			}
			
			let cmap = ChannelSettingsService.instance.data.get(topic);
			
			if (cmap === undefined) {
				throw ("Could not find topic [" + topic + "].");
			}
			
			let chan = cmap.get(channel);
			
			if (chan === undefined) {
				throw ("Could not find channel [" + channel + "] in topic [" + topic + "].");
			}
			
			callback(chan);
		});	
	}
	
	public static is_visible(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void { 
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.visible);
		});
	}
		
	public static is_configurable(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void { 
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.configurable);
		});
	}
		
	public static is_hardcoded(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void {
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.hardcoded !== '');
		});	
	}
		
	public static hardcoded_value(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void {
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.hardcoded.toLowerCase() === 'on');
		});		
	}
		
	public static has_default(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void {
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.default !== '');
		});		
	}
		
	public static default_value(
			topic 	 : string,
			channel  : string,
			callback : (enabled:boolean) => void
		) : void {
		ChannelSettingsService.get_channel(topic, channel, (chan:Channel) => {
		    callback(chan.default.toLowerCase() === 'on');
		});	
	}
}
