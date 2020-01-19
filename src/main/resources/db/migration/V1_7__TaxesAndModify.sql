-- Create taxes table
CREATE TABLE public.taxes
(
  id BIGSERIAL PRIMARY KEY NOT NULL,
  code VARCHAR(255) NOT NULL,
  title VARCHAR(255),
  archived boolean NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX taxes_id_uindex ON public.taxes (id);
CREATE UNIQUE INDEX taxes_code_uindex ON public.taxes (code);

-- modify client table for taxes

ALTER TABLE public.service ADD taxes_id BIGINT NULL;
ALTER TABLE public.service
  ADD CONSTRAINT service_taxes_id_fk
FOREIGN KEY (taxes_id) REFERENCES taxes (id);

-- modify event table for taxes

ALTER TABLE public.event ADD taxes_id BIGINT NULL;
ALTER TABLE public.event
  ADD CONSTRAINT event_taxes_id_fk
FOREIGN KEY (taxes_id) REFERENCES taxes (id);



