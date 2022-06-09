USE `${BREAKOUT}`;

-- This migration reverts "V202205160800__multiple_teams_per_sponsoring.sql"
-- as this was accidentally built into a develop docker image although it
-- never was merged into the develop branch. Whenever the migration is necessary,
-- it needs to be duplicated and sponsoring_teams needs to be dropped + recreated.

-- drop new unregistered_sponsor relation's team id
ALTER TABLE `unregistered_sponsor` DROP FOREIGN KEY fk_unregistered_sponsor_team_id;
ALTER TABLE `unregistered_sponsor` DROP COLUMN `team_id`;

-- recreate old team id column
ALTER TABLE `sponsoring` ADD COLUMN `team_id` bigint(20) DEFAULT NULL;

-- recreate old team id foreign key constraint
ALTER TABLE `sponsoring` ADD CONSTRAINT FOREIGN KEY fk_sponsoring_team_id (`team_id`) REFERENCES `team` (`id`);
ALTER TABLE `sponsoring` DROP FOREIGN KEY fk_sponsoring_event_id;

-- recreate  new event id
ALTER TABLE `sponsoring` DROP COLUMN `event_id`;

-- revert existing sponsoring -> team relations from new sponsoring_team table

UPDATE `sponsoring` s INNER JOIN `sponsoring_teams` st ON st.`sponsorings_id` = s.`id` SET s.`team_id` = st.`teams_id`;

-- undo new table for Sponsoring <-> Team ManyToMany 
-- !!!! IMPORTANT: THIS IS DISABLED BY DESIGN TO AVOID DATA LOSS !!!!
-- DROP TABLE `sponsoring_teams`; 
