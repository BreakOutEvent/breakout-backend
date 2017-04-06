USE `${BREAKOUT}`;

CREATE TABLE IF NOT EXISTS `unregistered_sponsor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `housenumber` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `zipcode` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `is_hidden` bit(1) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Copy data from embedded values of challenge
INSERT INTO unregistered_sponsor (created_at, updated_at, city, country, housenumber, street, zipcode, company, firstname, gender, is_hidden, lastname, url)
SELECT created_at, updated_at, city, country, housenumber, street, zipcode, company, firstname, gender, is_hidden, lastname, url FROM challenge WHERE registered_sponsor_id IS NULL;

-- Copy data from embedded values of sponsoring
INSERT INTO unregistered_sponsor (created_at, updated_at, city, country, housenumber, street, zipcode, company, firstname, gender, is_hidden, lastname, url)
SELECT created_at, updated_at, city, country, housenumber, street, zipcode, company, firstname, gender, is_hidden, lastname, url FROM sponsoring WHERE registered_sponsor_id IS NULL;

-- Add fk column to challenge
ALTER TABLE challenge ADD `unregistered_sponsor_id` bigint(20) DEFAULT NULL;
ALTER TABLE challenge ADD CONSTRAINT `FK_r8o50riy3sy8mui5cyfv5r4wu` FOREIGN KEY (`unregistered_sponsor_id`) REFERENCES `unregistered_sponsor` (`id`);

-- Add fk column to sponsoring
ALTER TABLE sponsoring ADD `unregistered_sponsor_id` bigint(20) DEFAULT NULL;
ALTER TABLE sponsoring ADD CONSTRAINT `FK_o9xctgieky22tmu9w8840vt7p` FOREIGN KEY (`unregistered_sponsor_id`) REFERENCES `unregistered_sponsor` (`id`);

-- Add value for foreign key of unregistered_sponsor to challenge
UPDATE challenge c
	INNER JOIN unregistered_sponsor s
	ON c.firstname = s.firstname AND c.lastname = s.lastname AND c.street = s.street AND c.housenumber = s.housenumber AND c.city = s.city AND c.zipcode = s.zipcode AND c.country = s.country AND c.gender = s.gender AND 	c.url = s.url AND c.registered_sponsor_id IS NULL
SET c.unregistered_sponsor_id = s.id
WHERE c.registered_sponsor_id IS NULL;

-- Add value for foreign key of unregistered_sponsor to sponsoring
UPDATE sponsoring c
	INNER JOIN unregistered_sponsor s
	ON c.firstname = s.firstname AND c.lastname = s.lastname AND c.street = s.street AND c.housenumber = s.housenumber AND c.city = s.city AND c.zipcode = s.zipcode AND c.country = s.country AND c.gender = s.gender AND 	c.url = s.url AND c.registered_sponsor_id IS NULL
SET c.unregistered_sponsor_id = s.id
WHERE c.registered_sponsor_id IS NULL;

-- Drop unused columns
ALTER TABLE sponsoring
DROP firstname,
DROP lastname,
DROP street,
DROP housenumber,
DROP city,
DROP zipcode,
DROP country,
DROP company,
DROP gender,
DROP url,
DROP is_hidden;

-- Drop unused columns
ALTER TABLE challenge
DROP firstname,
DROP lastname,
DROP street,
DROP housenumber,
DROP city,
DROP zipcode,
DROP country,
DROP company,
DROP gender,
DROP url,
DROP is_hidden;
