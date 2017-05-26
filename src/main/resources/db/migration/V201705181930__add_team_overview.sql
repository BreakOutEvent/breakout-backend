USE `${BREAKOUT}`;

CREATE TABLE `team_overview` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `event_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `location_id` bigint(20) DEFAULT NULL,
  `posting_id` bigint(20) DEFAULT NULL,
  `posting_timestamp` datetime DEFAULT NULL,
  `team_id` bigint(20) NOT NULL,
  `team_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `team_overview_members` (
  `team_overview_id` bigint(20) NOT NULL,
  `contact_phone` varchar(255) DEFAULT NULL,
  `emergency_phone` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  KEY `FKgpulbx3n1qxurb5crpb5er7y8` (`team_overview_id`),
  CONSTRAINT `FKgpulbx3n1qxurb5crpb5er7y8` FOREIGN KEY (`team_overview_id`) REFERENCES `team_overview` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
