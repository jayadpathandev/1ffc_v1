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
import React from 'react';

import { ContactSettingsService, Topic, TopicSetting } from '../services/contact_settings_service';
import { I18nService    } from '../../common/services/i18n_service';
import { ChannelSettingsService } from '../services/channel_settings_service';

interface CheckboxProps {
    selected 	: boolean;
    topic 		: string;
    channel 	: string;
}

function CheckBox(props : CheckboxProps) {
	const [ visible, 		setVisible 		] = React.useState(false);
	const [ configurable, 	setConfigurable ] = React.useState(false);
	const [ enabled, 		setEnabled 		] = React.useState(props.selected);

	React.useEffect(()=> {
		ChannelSettingsService.is_visible(
			props.topic,
			props.channel,
			(value:boolean) => {
				setVisible(value);
		})

		ChannelSettingsService.is_configurable(
			props.topic,
			props.channel,
			(value:boolean) => {
				setConfigurable(value);
		})

	}, [props.topic, props.channel]);

	if(visible === false) {
		return null;
	}

    // Return the HTML to display a checkbox.
    return(
	  	<div className="form-check form-switch st-toggle-item">
	  		<div className="st-field st-toggle">
				<div className="col-md-8 st-toggle-field">
					<input st-toggle-control="" value="true" type="checkbox" role="switch" className="form-check-input st-toggle-field" disabled={!configurable} checked={enabled} onChange={toggle}/>
					<label className="form-check-label st-toggle-item-label">
						<span className="st-toggle-text"></span>
					</label>
				</div>
			</div>
	 	</div>
    )

    function toggle() {
		if(configurable  === true) {
			setEnabled(!enabled);

			$.ajax({
			    url: 'set_contact_setting',
			    type: 'post',
			    data: {
			       topic : props.topic,
			       channel: props.channel,
			       enabled: !enabled
			    }
            });
		}
	}
}

function Topic(props : Topic) {
	const [ topicTxt, 		setTopicTxt 	] = React.useState('');
	const [ topicDesc, 		setTopicDesc 	] = React.useState('');

	React.useEffect(()=> {
		const i18nTopic = new I18nService('topics');

		i18nTopic.get(props.topic, (text:string) => {
		    setTopicTxt(text);
		})

		i18nTopic.get(props.topic + '_desc', (text:string) => {
		    setTopicDesc(text);
		})

	}, []);

    // Create the checkboxes for each channel.
    const list = props.channels.map((setting : TopicSetting) => {
	    return (
	    	<div className="col-4 col-sm-2" key={props.topic + '_' + setting.channel}>
				<div>
		        	<CheckBox topic={props.topic} channel={setting.channel} selected={setting.selected}/>
		        </div>
			</div>
	    );
	});

	// Return the HTML to display the topic.
    return (
	    <div className="row st-border-bottom pb-4 mb-4">
		    <div className="col-12 col-sm-6">
				<div className="topic-header">{topicTxt}</div>
				<div className="topic-desc">{topicDesc}</div>
			</div>
		    {list}
	    </div>
    );
}

export function ProfileTopicConfig() {
    const [ topics, setTopics ] = React.useState<Topic[] | null>(null);
	const [ sort,   setSort ] = React.useState<string | null>(null);

    // Load the contact Settings
    React.useEffect(() => {
		ContactSettingsService.get(
			(data:Topic[]|null) => {
				setTopics(data);
		});

		const i18nTopic = new I18nService('topics');
		i18nTopic.get('order', (text:string) => {
			setSort(text);
		});
    }, [  ]);

    // Don't do anything until we have the contact settings.
    if (topics == null || sort == null) return null;

	const sorted = topics.filter((v) => sort.indexOf(v.topic) !== -1).sort((a, b) => {
		const ao = sort.indexOf(a.topic);
		const bo = sort.indexOf(b.topic);

		if (ao < bo) return -1;
		if (bo < ao) return 1;
		return 0;
	});

    // Create the HTML for each topic.
    const list = sorted.map((topic:Topic) => {
        return (
            <div key={topic.topic}>
                <Topic topic={topic.topic} channels={topic.channels}/>
            </div>
        );
    });

    // Output the HTML.
    return (
        <div>
            {list}
        </div>
    );
}
