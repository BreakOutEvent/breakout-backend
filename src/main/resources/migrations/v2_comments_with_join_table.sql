CREATE TABLE `posting_comments` (
  `posting_id` bigint(20) NOT NULL,
  `comments_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_e24nwt6jvxa7kbxnx5i5qjl64` (`comments_id`),
  KEY `FK_614n19qtdyeee4csvp4ql25j` (`posting_id`),
  CONSTRAINT `FK_614n19qtdyeee4csvp4ql25j` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`),
  CONSTRAINT `FK_e24nwt6jvxa7kbxnx5i5qjl64` FOREIGN KEY (`comments_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO posting_comments (SELECT posting_id AS posting_id, id AS comment_id FROM comment);

-- DROP FK for posting_id as they are now in the join column
ALTER TABLE comment DROP FOREIGN KEY FK_cb78ra5e8th78whr91f8kl5gg;

-- DROP column posting_id because we now use a join table
ALTER TABLE comment DROP COLUMN posting_id;
