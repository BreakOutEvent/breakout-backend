ALTER TABLE email
  ADD template VARCHAR(255) NOT NULL DEFAULT '';


CREATE TABLE email_substitutions (
  email_id          BIGINT                      NOT NULL,
  substitutions     VARCHAR(255) DEFAULT 'NULL' NULL,
  substitutions_key VARCHAR(255)                NOT NULL,
  PRIMARY KEY (email_id, substitutions_key),
  CONSTRAINT email_substitution_fk
  FOREIGN KEY (email_id) REFERENCES email (id)
);

