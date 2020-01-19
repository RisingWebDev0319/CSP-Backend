-- удаляем метку первичного ключа
ALTER TABLE preliminary_event DROP CONSTRAINT preliminary_event_pkey;
ALTER TABLE preliminary_event
  ADD CONSTRAINT preliminary_event_pkey UNIQUE (datafk_client_id, datafk_session_id, service_id, date);
-- added primary ID key
ALTER TABLE preliminary_event ADD id BIGINT NOT NULL;
CREATE UNIQUE INDEX preliminary_event_id_uindex ON public.preliminary_event (id);
ALTER TABLE preliminary_event ADD CONSTRAINT preliminary_event_id_pk PRIMARY KEY (id);