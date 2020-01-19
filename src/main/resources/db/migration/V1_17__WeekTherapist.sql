
CREATE TABLE public.therapist_week
(
  therapist_id BIGINT,
  week_id BIGINT
);
ALTER TABLE public.therapist_week
ADD CONSTRAINT therapist_id
FOREIGN KEY (therapist_id) REFERENCES public.therapist(id);

ALTER TABLE public.therapist_week
ADD CONSTRAINT week_id
FOREIGN KEY (week_id) REFERENCES public.week(id);
