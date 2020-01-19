-- create table
CREATE TABLE public.event_types
(
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL,
  time_clean INTEGER,
  time_prep INTEGER,
  time_processing INTEGER,
  price VARCHAR(255) NOT NULL,
  archived boolean NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX event_types_id_uindex ON public.event_types (id);

-- modify event table
ALTER TABLE public.event ADD event_type_id BIGINT NULL;
ALTER TABLE public.event
  ADD CONSTRAINT event_event_types_id_fk
FOREIGN KEY (event_type_id) REFERENCES event_types (id);
