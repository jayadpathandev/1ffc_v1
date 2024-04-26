INSERT INTO db_config (config_key, config_value)
VALUES ('current.balance.type', 'M')
ON CONFLICT (config_key) DO UPDATE SET config_value = EXCLUDED.config_value;

