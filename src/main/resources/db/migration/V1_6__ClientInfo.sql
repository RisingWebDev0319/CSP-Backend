--change table fields
ALTER TABLE public.client ADD first_name VARCHAR(80) DEFAULT ' ' NOT NULL;
ALTER TABLE public.client ADD last_name VARCHAR(80) NULL;
ALTER TABLE public.client ADD preferred_name VARCHAR(80) NULL;
ALTER TABLE public.client ADD client_since DATE NULL;
ALTER TABLE public.client ADD birthday DATE NULL;
ALTER TABLE public.client ADD gender SMALLINT DEFAULT 0 NULL;
ALTER TABLE public.client ADD phone_mobile VARCHAR(50) NULL;
ALTER TABLE public.client ADD phone_home VARCHAR(50) NULL;
ALTER TABLE public.client ADD phone_work VARCHAR(50) NULL;
ALTER TABLE public.client ADD city VARCHAR(50) NULL;
ALTER TABLE public.client ADD postcode VARCHAR(15) NULL;
ALTER TABLE public.client ADD country VARCHAR(50) NULL;
ALTER TABLE public.client ADD state VARCHAR(50) NULL;
ALTER TABLE public.client ADD street VARCHAR(80) NULL;
ALTER TABLE public.client ADD email VARCHAR(320) DEFAULT ' ' NOT NULL;
ALTER TABLE public.client ADD cycle TEXT NULL;
ALTER TABLE public.client ADD program_length TEXT NULL;
-- CREATE UNIQUE INDEX client_email_uindex ON public.client (email);
