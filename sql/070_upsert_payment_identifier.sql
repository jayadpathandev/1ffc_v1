-- This is configuration for payment identifier for ACH files.

INSERT INTO db_config (config_key, config_value)
VALUES ('payment.identifier', 'DISPLAY_ACCOUNT')
ON CONFLICT (config_key)
DO UPDATE SET config_value = EXCLUDED.config_value;

