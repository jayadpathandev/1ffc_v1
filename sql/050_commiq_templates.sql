-- All the listed templates will be sent by Commiq vault.

INSERT INTO db_config (config_key, config_value)
VALUES
  (
    'precisely.vault.templates',
    '{
    	''bill_new_doc_one_account_en'',
	''enroll_user_complete_en'',
	''enroll_validation_en'',
	''login_2fa_recovery_en'',
	''login_2fa_reset_en'',
	''login_forgot_password_en'',
	''login_forgot_username_en'',
	''login_secret_question_reset_en'',
	''payment_automatic_cancelled_en'',
	''payment_automatic_create_success_en'',
	''payment_automatic_delete_success_en'',
	''payment_automatic_edit_success_en'',
	''payment_make_payment_failure_en'',
	''payment_make_payment_success_en'',
	''payment_onetime_scheduled_success_en'',
	''payment_schedule_batch_message_en'',
	''payment_scheduled_cancelled_en'',
	''payment_wallet_create_success_en'',
	''payment_wallet_delete_success_en'',
	''payment_wallet_edit_success_en'',
	''profile_2fa_settings_en'',
	''profile_communication_opt_in_en'',
	''profile_email_change_confirm_en'',
	''profile_paper_billing_en'',
	''profile_updated_en''
    }'
  )
ON CONFLICT (config_key) DO UPDATE
SET config_value = excluded.config_value;

