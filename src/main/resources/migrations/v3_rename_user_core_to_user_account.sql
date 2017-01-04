RENAME TABLE user_core TO user_account;

RENAME TABLE user_core_user_roles TO user_account_user_roles;

ALTER TABLE `user_account_user_roles` CHANGE `user_core_id` `user_account_id` BIGINT(20)  NOT NULL;

ALTER TABLE `user_role` CHANGE `core_id` `account_id` BIGINT(20)  NULL  DEFAULT NULL;
