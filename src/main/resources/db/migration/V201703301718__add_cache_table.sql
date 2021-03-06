USE `${BREAKOUT}`;

CREATE TABLE `cache` (
  `cache_key`  VARCHAR(255) NOT NULL,
  `cache_data` TEXT,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`cache_key`)
) ENGINE = InnoDB DEFAULT CHARSET = latin1;

ALTER TABLE `invoice` ADD `purpose_of_transfer` VARCHAR(255) DEFAULT NULL;
