
-- This is configuration for payment automatic purge on failed status.
-- 1st Franklin has decided they don't want that right now as they haven't reviewed the compliance issues associated with it. - By John
INSERT INTO db_config (config_key, config_value)
VALUES ('payment.recurring.purge.on.fail', 'false')
ON CONFLICT (config_key)
DO UPDATE SET config_value = EXCLUDED.config_value;

