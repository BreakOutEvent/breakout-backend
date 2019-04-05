
--  (╯°□°）╯︵ ┻━┻ I said a droppa da table
drop table team_overview_location_data;
drop table team_overview_members;
drop table team_overview;

CREATE TABLE `contact_with_headquarters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `team_id` bigint(20) NOT NULL,
  `admin_id` bigint(20) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Add going to sleep column
alter table team add column `asleep` BIT DEFAULT 0;