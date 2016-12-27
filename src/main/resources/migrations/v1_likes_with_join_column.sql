CREATE TABLE `posting_likes` (
  `posting_id` bigint(20) NOT NULL,
  `like_id` bigint(20) NOT NULL,
  PRIMARY KEY (`posting_id`,`like_id`),
  UNIQUE KEY `UK_gaocgs95qcekgflnkj26ncsjn` (`like_id`),
  CONSTRAINT `FK_gaocgs95qcekgflnkj26ncsjn` FOREIGN KEY (`like_id`) REFERENCES `postinglike` (`id`),
  CONSTRAINT `FK_jvv3g4ua42fsog6017dtog0eb` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO posting_likes (SELECT posting_id as posting_id, id as like_id FROM postinglike);

-- Drop FK for posting_id as they are now in the join column
ALTER TABLE `postinglike` DROP FOREIGN KEY `FK_7crxb84bq2w162t04c6ixbd1y`;

-- Drop UK for user as this will be handled in model now
ALTER TABLE `postinglike` DROP KEY `UK_onwc9cqvi7ikagh8nwj53xyn8`;

-- Drop column posting_id because we now use a join table
ALTER TABLE `postinglike` DROP COLUMN posting_id;
