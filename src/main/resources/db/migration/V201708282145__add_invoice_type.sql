ALTER TABLE `invoice`
  ADD `type` VARCHAR(255) NULL  DEFAULT NULL
  AFTER `initial_version_sent`;
