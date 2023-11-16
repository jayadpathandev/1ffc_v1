-- SQL scripts to populate default data in auth_topic_channel table for contact preferences.
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('400_marketing', 'email', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('400_marketing', 'postal', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('400_marketing', 'sms', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('500_collections', 'email', 'Y', 'Y', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('500_collections', 'postal', 'Y', 'Y', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('500_collections', 'sms', 'Y', 'Y', 'Y');
