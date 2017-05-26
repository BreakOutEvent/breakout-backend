USE `${BREAKOUT}`;

-- Create new join table for invoice -->* challenges since mappedBy has been removed
CREATE TABLE invoice_challenges
(
  sponsoring_invoice_id BIGINT NOT NULL,
  challenges_id         BIGINT NOT NULL,
  CONSTRAINT UK_51hg3btq3e5hol5rvpyys14r1
  UNIQUE (challenges_id),
  CONSTRAINT FKfnkhpl0h30v11n4icn7knh3cv
  FOREIGN KEY (sponsoring_invoice_id) REFERENCES invoice (id),
  CONSTRAINT FKsf60fk0ktw95k0pkkj3mg8xds
  FOREIGN KEY (challenges_id) REFERENCES challenge (id)
);

CREATE INDEX FKfnkhpl0h30v11n4icn7knh3cv ON invoice_challenges (sponsoring_invoice_id);

-- Create new join table for invoice -->* sponsorings since mappedBy has been removed
CREATE TABLE invoice_sponsorings
(
  sponsoring_invoice_id BIGINT NOT NULL,
  sponsorings_id        BIGINT NOT NULL,
  CONSTRAINT UK_8nsd1iwrmy4odrce8x5owt4iy
  UNIQUE (sponsorings_id),
  CONSTRAINT FKjl4dcu3ltq8kycrbe8wkovp80
  FOREIGN KEY (sponsoring_invoice_id) REFERENCES invoice (id),
  CONSTRAINT FKq7723d5qm1c0twnsn7ly7ydhu
  FOREIGN KEY (sponsorings_id) REFERENCES sponsoring (id)
);

CREATE INDEX FKjl4dcu3ltq8kycrbe8wkovp80 ON invoice_sponsorings (sponsoring_invoice_id);

-- Add values to join tables
INSERT INTO invoice_challenges (SELECT
                                  invoice_id,
                                  id
                                FROM challenge
                                WHERE invoice_id IS NOT NULL);
INSERT INTO invoice_sponsorings (SELECT
                                   invoice_id,
                                   id
                                 FROM sponsoring
                                 WHERE invoice_id IS NOT NULL);

-- Remove unnecessary foreign key column
alter table challenge drop FOREIGN KEY FK_challenge_invoice;
ALTER TABLE challenge DROP COLUMN invoice_id;
ALTER TABLE sponsoring drop FOREIGN KEY FK_sponsoring_invoice;
ALTER TABLE sponsoring DROP COLUMN invoice_id;
