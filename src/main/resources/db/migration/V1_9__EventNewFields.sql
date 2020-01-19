-- modify Event table
ALTER TABLE public.event ADD internal BOOLEAN DEFAULT FALSE  NULL;
ALTER TABLE public.event ADD tax_id BIGINT NULL;
ALTER TABLE public.event ADD price_purchase INTEGER DEFAULT 0 NULL;
