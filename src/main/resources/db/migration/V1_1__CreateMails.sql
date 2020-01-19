CREATE TABLE public.mail_types
(
    id bigint PRIMARY KEY NOT NULL,
    key varchar(20),
    title varchar(255),
    "order" integer DEFAULT 0
);
CREATE UNIQUE INDEX mail_types_id_uindex ON public.mail_types (id);
CREATE UNIQUE INDEX email_types_key_uindex ON public.mail_types (key);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (8, 'event_create_confirm', 'Created new events with confirmations: mail to therapist', 5);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (4, 'event_created', 'Created a new event: mail to therapist', 4);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (6, 'event_removed', 'Removed the event: mail to therapist', 7);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (1, 'basic', 'Basic template', 1);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (9, 'available_delete', 'A request for availability was deleted: mail to therapist ', 9);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (3, 'user_created', 'Registered new user template', 3);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (7, 'available_create', 'A request for availability was created: mail to therapist ', 8);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (5, 'event_changed', 'Changed the event: mail to therapist', 6);
INSERT INTO public.mail_types (id, key, title, "order") VALUES (2, 'password_recovery', 'Recovery password template', 2);

CREATE SEQUENCE mails_id_seq;
CREATE TABLE public.mails
(
    id bigint PRIMARY KEY NOT NULL,
    type bigint,
    body text,
    type_id bigint,
    subject varchar(255),
    CONSTRAINT mails_mail_types_id_fk FOREIGN KEY (type) REFERENCES mail_types (id)
);
CREATE UNIQUE INDEX mails_id_uindex ON public.mails (id);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (2, 2, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (3, 3, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (4, 4, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (5, 5, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (6, 6, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (7, 7, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (8, 8, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (9, 9, null, null, null);
INSERT INTO public.mails (id, type, body, subject, type_id) VALUES (1, 1, null, null, null);

CREATE TABLE public.mail_expressions
(
    id bigint PRIMARY KEY NOT NULL,
    expression varchar(255) NOT NULL,
    value text
);
CREATE UNIQUE INDEX mail_expressions_id_uindex ON public.mail_expressions (id);
CREATE UNIQUE INDEX mail_expressions_expression_uindex ON public.mail_expressions (expression);
INSERT INTO public.mail_expressions (id, expression, value) VALUES (7, 'CONTENT', 'Extremity direction of existence as dashwoods do up. Securing marianne led welcomed offended but offering six raptures. Conveying closed newspaper rapturous oh at. Mrs remain. Occasional continuing possession we insensible an sentiments as is. Law but reasonably motionless principles she. Has six worse down.');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (8, 'PASSWORD', '42pgF7n8e');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (9, 'LOGIN', 'mail@mail.com ');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (10, 'SERVICE_NAME', 'Name of suggested service');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (11, 'ROOM_NAME', 'my big room #4');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (12, 'SERVICE_DATE', '3/‎14/‎2018');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (13, 'SERVICE_TIME', '06‎:‎00‎ ‎AM ');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (14, 'SERVICE_DURATION_FULL', '120');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (15, 'SERVICE_DURATION_PREP', '15');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (16, 'SERVICE_DURATION_CLEAN', '15');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (17, 'SERVICE_DURATION_BASE', '90');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (18, 'LINK_CONFIRM', 'http://localhost:3000/#/settings/mailTemplates');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (19, 'LINK_DECLINE', 'http://localhost:3000/#/settings/mailTemplates');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (20, 'DATE_START', '3/‎14/‎2018');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (21, 'DATE_END', '3/‎20/‎2018');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (22, 'MESSAGE', 'HELLO MY DEAR THERAPIST!');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (23, 'LINK', 'http://localhost/details');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (24, 'SERVICES_START', '24');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (25, 'SERVICES_END', '27');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (26, 'SERVICES_COUNT', '3');
INSERT INTO public.mail_expressions (id, expression, value) VALUES (27, 'SERVICE_NUMBER', '1');


CREATE TABLE public.mails_to_expressions
(
    mail_id bigint NOT NULL,
    expression_id bigint NOT NULL,
    CONSTRAINT mails_to_expressions_mail_id_expression_id_pk PRIMARY KEY (mail_id, expression_id),
    CONSTRAINT mails_to_expressions_mails_id_fk FOREIGN KEY (mail_id) REFERENCES mails (id),
    CONSTRAINT mails_to_expressions_mail_expressions_id_fk FOREIGN KEY (expression_id) REFERENCES mail_expressions (id)
);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (1, 7);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (2, 8);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (3, 9);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (3, 8);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 10);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 11);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 12);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 13);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 14);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 15);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 16);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 17);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 18);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (4, 19);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 24);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 25);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 26);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 11);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 12);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 13);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 14);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 15);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 16);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 17);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (8, 27);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 12);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 13);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 14);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 15);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 16);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 17);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 18);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (5, 19);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 12);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 13);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 14);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 15);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 16);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (6, 17);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (7, 20);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (7, 21);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (7, 22);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (7, 23);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (9, 20);
INSERT INTO public.mails_to_expressions (mail_id, expression_id) VALUES (9, 21);

