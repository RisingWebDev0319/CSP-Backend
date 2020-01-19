-- this expressions should be without replace value
UPDATE  mail_expressions SET VALUE = '' WHERE id IN(24,25);
--  add column for descriptions
ALTER TABLE  mail_expressions ADD description TEXT;

UPDATE mail_expressions SET
                        description = CASE id
                        WHEN 7 THEN 'content that contains another template'
                        WHEN 8 THEN 'temporary user password'
                        WHEN 9 THEN 'login or email for a new user'
                        WHEN 10 THEN 'name of the service'
                        WHEN 11 THEN 'the name of the room where the therapy will take place'
                        WHEN 12 THEN 'date of therapy'
                        WHEN 13 THEN 'time of therapy'
                        WHEN 14 THEN 'total duration of therapy in minutes'
                        WHEN 15 THEN 'preparation time for therapy in minutes'
                        WHEN 16 THEN 'clean time for therapy in minutes'
                        WHEN 17 THEN 'base time for therapy in minutes'
                        WHEN 18 THEN 'confirmation link'
                        WHEN 19 THEN 'link for reject'
                        WHEN 20 THEN 'the date of the beginning period for which the request comes'
                        WHEN 21 THEN 'the date of the ending period for which the request comes'
                        WHEN 22 THEN 'message for therapist'
                        WHEN 23 THEN 'link for viewing query details'
                        WHEN 26 THEN 'events number in the array'
                        WHEN 27 THEN 'service number order in the array'
                        WHEN 25 THEN 'end of range array events marker'
                        WHEN 24 THEN 'Mark to begin look over array events'
                        END
WHERE id BETWEEN 7 AND 27;