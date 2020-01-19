--добавляем таблицу therapist_info
CREATE TABLE public.therapist_info
(
  id SERIAL PRIMARY KEY NOT NULL,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80),
  preferred_name VARCHAR(80),
  email_personal VARCHAR(320),
  phone_mobile VARCHAR(50),
  phone_home VARCHAR(50),
  phone_work VARCHAR(50),
  birthday DATE,
  gender SMALLINT,
  company VARCHAR(100),
  street VARCHAR(80),
  city VARCHAR(20),
  postcode VARCHAR(15),
  state VARCHAR(50),
  country VARCHAR(50)
);
CREATE UNIQUE INDEX therapist_info_id_uindex ON public.therapist_info (id);

--добавляем поле therapist_info терапевтам и связываем эти таблицы

ALTER TABLE public.therapist ADD therapist_info_id BIGINT NULL;
CREATE UNIQUE INDEX therapist_therapist_info_id_uindex ON public.therapist (therapist_info_id);
ALTER TABLE public.therapist
  ADD CONSTRAINT therapist_therapist_info_id_fk
FOREIGN KEY (therapist_info_id) REFERENCES therapist_info (id) ON DELETE CASCADE ON UPDATE CASCADE DEFERRABLE;
