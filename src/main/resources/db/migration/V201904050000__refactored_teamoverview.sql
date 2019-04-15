
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
  `reason` int(11) NOT NULL,
  `comment` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- Add going to sleep column
alter table team add column `asleep` BIT DEFAULT 0;