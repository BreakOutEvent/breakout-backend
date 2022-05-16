USE `${BREAKOUT}`;

-- create new table for Sponsoring <-> Team ManyToMany
CREATE TABLE `sponsoring_team` (
   `sponsoring_id` bigint(20) NOT NULL,
   `team_id` bigint(20) NOT NULL,
   CONSTRAINT FOREIGN KEY (`sponsoring_id`) REFERENCES `sponsoring` (`id`),
   CONSTRAINT FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
)

-- copy existing sponsoring -> team relations into new sponsoring_team table
INSERT INTO `sponsoring_team` (`sponsoring_id`, `team_id`) SELECT (`id`, `team_id`) FROM sponsorings;

-- add new event id
ALTER TABLE `sponsoring` ADD COLUMN `event_id` bigint(20) DEFAULT NULL; -- TODO add foreign key constraint
ALTER TABLE `sponsoring` ADD CONSTRAINT FOREIGN KEY (`event_id`) REFERENCES `event` (`id`);

-- drop old team id
ALTER TABLE `sponsoring` DROP COLUMN `team_id`;

