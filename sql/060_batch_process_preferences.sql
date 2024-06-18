-- upsert current balance key
INSERT INTO db_config (config_key, config_value)
VALUES
    ('adjust.current.balance', 'false')
ON CONFLICT (config_key) 
DO UPDATE SET
    config_value = EXCLUDED.config_value;

-- upsert account payment permission key
INSERT INTO db_config (config_key, config_value)
VALUES
    ('check.account.payment.permissions', 'true')
ON CONFLICT (config_key) 
DO UPDATE SET
    config_value = EXCLUDED.config_value;

