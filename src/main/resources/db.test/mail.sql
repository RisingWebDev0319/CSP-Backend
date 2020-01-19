--creating types of mails
CREATE TABLE IF NOT EXISTS mail_types
(
  id      BIGSERIAL NOT NULL
    CONSTRAINT email_types_pkey
    PRIMARY KEY,
  key     VARCHAR(20),
  title   VARCHAR(255),
  "order" INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX email_types_id_uindex
  ON mail_types (id);

CREATE UNIQUE INDEX email_types_key_uindex
  ON mail_types (key);

--creating mails
CREATE TABLE IF NOT EXISTS mails
(
  id      BIGSERIAL NOT NULL
    CONSTRAINT mails_pkey
    PRIMARY KEY,
  type    BIGINT
    CONSTRAINT mails_mail_types_id_fk
    REFERENCES mail_types,
  body    TEXT,
  subject VARCHAR(255)
);

CREATE UNIQUE INDEX mails_id_uindex
  ON mails (id);

-- creating mail_expression table
CREATE TABLE IF NOT EXISTS public.mail_expressions
(
  id         BIGSERIAL PRIMARY KEY,
  expression VARCHAR(255) NOT NULL,
  value      TEXT
);
CREATE UNIQUE INDEX mail_expressions_id_uindex
  ON public.mail_expressions (id);
CREATE UNIQUE INDEX mail_expressions_expression_uindex
  ON public.mail_expressions (expression);

--creating relationship expressions & mails
CREATE TABLE public.mails_to_expressions
(
  mail_id       BIGINT,
  expression_id BIGINT,
  CONSTRAINT mails_to_expressions_mail_id_expression_id_pk PRIMARY KEY (mail_id, expression_id),
  CONSTRAINT mails_to_expressions_mails_id_fk FOREIGN KEY (mail_id) REFERENCES mails (id),
  CONSTRAINT mails_to_expressions_mail_expressions_id_fk FOREIGN KEY (expression_id) REFERENCES mail_expressions (id)
);