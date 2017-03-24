USE `${BREAKOUT}`;

ALTER TABLE `challenge` CHANGE `sponsor_id` `registered_sponsor_id` BIGINT(20)  NULL  DEFAULT NULL;
ALTER TABLE `sponsoring` CHANGE `sponsor_id` `registered_sponsor_id` BIGINT(20)  NULL  DEFAULT NULL;
