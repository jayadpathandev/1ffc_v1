-- Definition of public.fffc_transactions

-- Drop table
-- DROP TABLE IF EXISTS public.fffc_transactions;

CREATE TABLE public.fffc_transactions (
	online_id varchar(255) NOT NULL,
	"date" numeric(8,0) NOT NULL,
	account varchar(255) NOT NULL,
	transaction_type varchar(255) NOT NULL,
	description varchar(255) NOT NULL,
	amount numeric(38,3) NULL,
	CONSTRAINT fffc_transactions_pkey PRIMARY KEY (online_id)
) TABLESPACE ecare_data;

CREATE INDEX idx_transactions_account ON public.fffc_transactions USING btree (account);

GRANT ALL PRIVILEGES ON TABLE public.fffc_transactions TO app_ecare;

