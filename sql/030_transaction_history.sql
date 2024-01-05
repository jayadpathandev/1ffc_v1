-- Definition of public.fffc_transactions

-- Drop table
-- DROP TABLE IF EXISTS public.fffc_transactions;

CREATE TABLE if not exists public.fffc_transactions (
	online_id varchar(255) NOT NULL,
	"date" numeric(8,0) NOT NULL,
	account varchar(255) NOT NULL,
	transaction_type varchar(255) NOT NULL,
	description varchar(255) NOT NULL,
	amount numeric(38,3) NULL,
	pay_group varchar(255) NOT NULL,
	CONSTRAINT fffc_transactions_pkey PRIMARY KEY (online_id)
) TABLESPACE @DSS_DB_NAME@_data;

CREATE INDEX if not exists idx_transactions_account
	ON public.fffc_transactions
	USING btree (account)
	tablespace  @DSS_DB_NAME@_idx;

GRANT ALL PRIVILEGES ON TABLE public.fffc_transactions TO @DSS_DB_NAME@;

