-- Add goals for events
ALTER TABLE public.event ADD client_goals varchar(255) NULL;
ALTER TABLE public.event ADD therapist_goals varchar(255) NULL;