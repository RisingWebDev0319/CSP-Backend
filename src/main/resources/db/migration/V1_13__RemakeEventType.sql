-- drop useless columns
ALTER TABLE public.event_types
  DROP time_clean;
ALTER TABLE public.event_types
  DROP time_prep;
ALTER TABLE public.event_types
  DROP time_processing;
ALTER TABLE public.event_types
  DROP price;
-- ALTER TABLE public.event_types DROP archived;
INSERT INTO public.event_types (name, archived) VALUES ('Event', FALSE), ('Class', FALSE);
