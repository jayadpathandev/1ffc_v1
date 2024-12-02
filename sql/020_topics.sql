-- SQL scripts to populate default data in auth_topic_channel table for contact preferences.

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'email', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'postal', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'marketing', 'sms', 'N', 'N'
where not exists (select 1 from auth_topic_channel where atc_topic = 'marketing' and atc_channel = 'sms');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'email', 'Y', 'Y', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'postal', 'Y', 'Y', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
select 'collections', 'sms', 'Y', 'N', 'N'
where not exists (select 1 from auth_topic_channel where atc_topic = 'collections' and atc_channel = 'sms');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'account', 'email', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'account' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'account', 'postal', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'account' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'account', 'sms', 'N', 'N'
where not exists (select 1 from auth_topic_channel where atc_topic = 'account' and atc_channel = 'sms');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'paperless', 'email', 'N', 'Y'
where not exists (select 1 from auth_topic_channel where atc_topic = 'paperless' and atc_channel = 'email');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'paperless', 'postal', 'N', 'N'
where not exists (select 1 from auth_topic_channel where atc_topic = 'paperless' and atc_channel = 'postal');

INSERT INTO auth_topic_channel(atc_topic, atc_channel, atc_default, atc_visible)
select 'paperless', 'sms', 'N', 'N'
where not exists (select 1 from auth_topic_channel where atc_topic = 'paperless' and atc_channel = 'sms');

UPDATE auth_topic_channel SET atc_default='N', atc_visible='N' WHERE atc_channel='sms';

UPDATE auth_topic_channel SET atc_default='Y', atc_visible='N' WHERE atc_topic='system' AND atc_channel='email';

UPDATE auth_topic_channel SET atc_default='Y', atc_visible='N' WHERE atc_topic='payment' AND atc_channel='email';

UPDATE auth_topic_channel SET atc_default='Y', atc_visible='N' WHERE atc_topic='bills_documents' AND atc_channel='email';

UPDATE auth_topic_channel SET atc_default='Y', atc_visible='N' WHERE atc_topic='paperless' AND atc_channel='email';
