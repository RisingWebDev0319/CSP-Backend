-- create main table
CREATE TABLE public.report
(
  id BIGSERIAL PRIMARY KEY NOT NULL,
  type VARCHAR(255),
  date_from DATE,
  date_to DATE,
  session_id BIGINT,
  autoupdate BOOLEAN DEFAULT FALSE,
  selected_all BOOLEAN DEFAULT FALSE,
  url VARCHAR(255),
  archived boolean NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX report_id_uindex ON public.report (id);
