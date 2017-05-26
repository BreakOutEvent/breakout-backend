USE `${BREAKOUT}`;

ALTER TABLE `invoice` ADD `purpose_of_transfer_code` VARCHAR(255) DEFAULT NULL;

ALTER TABLE `invoice`  ADD UNIQUE `purpose_of_transfer_code_unique` (`purpose_of_transfer_code`);
ALTER TABLE `invoice`  ADD UNIQUE `purpose_of_transfer_unique` (`purpose_of_transfer`);
