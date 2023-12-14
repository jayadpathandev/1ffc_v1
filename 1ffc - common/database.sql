-- SQL scripts to populate default data in auth_topic_channel table for contact preferences.
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('marketing', 'email', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('marketing', 'postal', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('marketing', 'sms', NULL, 'N', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('collections', 'email', 'Y', 'Y', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('collections', 'postal', 'Y', 'Y', 'Y');
INSERT INTO auth_topic_channel
(atc_topic, atc_channel, atc_hardcoded, atc_default, atc_visible)
VALUES('collections', 'sms', 'Y', 'Y', 'Y');
