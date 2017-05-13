ALTER DATABASE `${BREAKOUT}` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `cache` CHANGE `cache_key` `cache_key` VARCHAR(191);
ALTER TABLE `cache` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `cache` CHANGE `cache_data` `cache_data` TEXT;

ALTER TABLE `challenge` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `challenge` CHANGE `description` `description` TEXT;

ALTER TABLE `comment` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `comment` CHANGE `text` `text` TEXT;

ALTER TABLE `email` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `email` CHANGE `body` `body` TEXT;

ALTER TABLE `email_bcc` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `email_files` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `email_to` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `event` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `feature` CHANGE `name` `name` VARCHAR(191);
ALTER TABLE `feature` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `group_message` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `group_message_messages` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `group_message_users` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `invitation` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `invoice` CHANGE `dtype` `dtype` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `subject` `subject` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `company` `company` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `amount` `amount` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `firstname` `firstname` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `lastname` `lastname` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `purpose_of_transfer` `purpose_of_transfer` VARCHAR(191);
ALTER TABLE `invoice` CHANGE `purpose_of_transfer_code` `purpose_of_transfer_code` VARCHAR(191);
ALTER TABLE `invoice` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `location` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `location_location_data` CHANGE `location_data_value` `location_data_value` VARCHAR(191);
ALTER TABLE `location_location_data` CHANGE `location_data_key` `location_data_key` VARCHAR(191);
ALTER TABLE `location_location_data` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `media` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `media_size` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `message` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `message` CHANGE `text` `text` TEXT;

ALTER TABLE `oauth_access_token` CHANGE `token_id` `token_id` VARCHAR(191);
ALTER TABLE `oauth_access_token` CHANGE `authentication_id` `authentication_id` VARCHAR(191);
ALTER TABLE `oauth_access_token` CHANGE `user_name` `user_name` VARCHAR(191);
ALTER TABLE `oauth_access_token` CHANGE `client_id` `client_id` VARCHAR(191);
ALTER TABLE `oauth_access_token` CHANGE `refresh_token` `refresh_token` VARCHAR(191);
ALTER TABLE `oauth_access_token` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `oauth_client_details` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `oauth_client_token` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `oauth_code` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `oauth_refresh_token` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `payment` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `posting` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `posting` CHANGE `text` `text` TEXT;

ALTER TABLE `posting_comments` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `posting_hashtags` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `posting_likes` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `posting_media` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `postinglike` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `schema_version` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `sponsoring` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `team` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `team` CHANGE `description` `description` TEXT;

ALTER TABLE `team_members` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `unregistered_sponsor` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `user_account` CHANGE `activation_token` `activation_token` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `email` `email` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `firstname` `firstname` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `gender` `gender` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `lastname` `lastname` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `password_hash` `password_hash` VARCHAR(191);
ALTER TABLE `user_account` CHANGE `preferred_language` `preferred_language` VARCHAR(191);
ALTER TABLE `user_account` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `user_account_user_roles` CHANGE `user_roles_key` `user_roles_key` VARCHAR(191);
ALTER TABLE `user_account_user_roles` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `user_role` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;