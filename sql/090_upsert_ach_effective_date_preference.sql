
-- This is configuration for payment ach generator batch job.
-- This will update the effective date/file creation date of an ach file. 0 represents current day, 1 represents tomorrow...
INSERT INTO db_config (config_key, config_value)
VALUES ('ach.effective.date', '1')
ON CONFLICT (config_key)
DO UPDATE SET config_value = EXCLUDED.config_value;

