-- Definition of public.fffc_transactions

-- Drop table
-- DROP TABLE IF EXISTS public.fffc_transactions;

CREATE TABLE public.fffc_transactions (
    online_id VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    date TIMESTAMP NOT NULL,
    pmt_group VARCHAR(255) NOT NULL,
    account VARCHAR(255) NOT NULL,
    amount VARCHAR(255) NOT NULL,
    CONSTRAINT fffc_transactions_pkey PRIMARY KEY (online_id)
) TABLESPACE ecare_data;

CREATE INDEX idx_transactions_account ON public.fffc_transactions USING btree (account);