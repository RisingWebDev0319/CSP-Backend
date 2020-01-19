-- For events
ALTER TABLE public.event ADD billing_description TEXT NULL;
-- For services
ALTER TABLE public.service ADD billing_description TEXT NULL;
ALTER TABLE public.service ADD price_purchase INTEGER DEFAULT 0 NULL;
