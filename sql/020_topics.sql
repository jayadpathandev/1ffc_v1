-- SQL scripts to populate default data in auth_topic_channel table for contact preferences.

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'email', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'postal', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'sms', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'sms');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'email', 'Y', 'Y', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'postal', 'Y', 'Y', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'sms', 'Y', 'Y', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'sms');
