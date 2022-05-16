USE `${BREAKOUT}`;

-- create new table for Sponsoring <-> Team ManyToMany
CREATE TABLE `sponsoring_team` (
   `sponsoring_id` bigint(20) NOT NULL,
   `team_id` bigint(20) NOT NULL,
   CONSTRAINT FOREIGN KEY fk_sponsoring_team_sponsoring_id (`sponsoring_id`) REFERENCES `sponsoring` (`id`),
   CONSTRAINT FOREIGN KEY fk_sponsoring_team_team_id (`team_id`) REFERENCES `team` (`id`)
);

-- copy existing sponsoring -> team relations into new sponsoring_team table
INSERT INTO `sponsoring_team` (`sponsoring_id`, `team_id`) SELECT `id`, `team_id` FROM `sponsoring`;

-- add new event id
ALTER TABLE `sponsoring` ADD COLUMN `event_id` bigint(20) DEFAULT NULL;
ALTER TABLE `sponsoring` ADD CONSTRAINT FOREIGN KEY fk_sponsoring_event_id (`event_id`) REFERENCES `event` (`id`);

-- drop old team id foreign key constraint
SET @CN := (SELECT CONSTRAINT_NAME
FROM
  information_schema.KEY_COLUMN_USAGE
WHERE
  table_name = 'sponsoring' and referenced_table_name = 'team' and referenced_column_name = 'id'
);

set @sql = 'ALTER TABLE `sponsoring` DROP FOREIGN KEY @CN';
set @sql = replace(@sql, '@CN', @CN);
prepare alterTable from @sql;
execute alterTable;

-- drop old team id column
ALTER TABLE `sponsoring` DROP COLUMN `team_id`;
