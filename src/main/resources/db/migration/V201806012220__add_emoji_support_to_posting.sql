alter database `${BREAKOUT}` character set = utf8mb4 collate = utf8mb4_unicode_ci;

alter table posting convert to character set utf8mb4 collate utf8mb4_unicode_ci;
alter table posting change `text` `text` text character set utf8mb4 collate utf8mb4_unicode_ci;

alter table cache convert to character set utf8mb4 collate utf8mb4_unicode_ci;
alter table cache change `cache_key` `cache_key` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;
alter table cache change `cache_data` `cache_data` text character set utf8mb4 collate utf8mb4_unicode_ci;

alter table challenge convert to character set utf8mb4 collate utf8mb4_unicode_ci;
alter table challenge change `amount` `amount` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;
alter table challenge change `description` `description` text character set utf8mb4 collate utf8mb4_unicode_ci;

alter table comment convert to character set utf8mb4 collate utf8mb4_unicode_ci;
alter table comment change `text` `text` text character set utf8mb4 collate utf8mb4_unicode_ci;

alter table email convert to character set utf8mb4 collate utf8mb4_unicode_ci;
alter table email change `body` `body` longtext character set utf8mb4 collate utf8mb4_unicode_ci;
alter table email change `button_text` `button_text` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;
alter table email change `button_url` `button_url` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;
alter table email change `campaign_code` `campaign_code` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;
alter table email change `subject` `subject` varchar(255) character set utf8mb4 collate utf8mb4_unicode_ci;

alter table email_bcc convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table email_files convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `email_files` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table email_to convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `email_to` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table event convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `event` CHANGE `city` `city` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `event` CHANGE `title` `title` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table feature convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `feature` CHANGE `name` `name` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table group_message convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table group_message_messages convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table group_message_users convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table invitation convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `invitation` CHANGE `invitation_token` `invitation_token` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invitation` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `invitation` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table invoice convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `invoice` CHANGE `dtype` `dtype` VARCHAR(31)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `invoice` CHANGE `amount` `amount` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `subject` `subject` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `company` `company` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `firstname` `firstname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `lastname` `lastname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `purpose_of_transfer` `purpose_of_transfer` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `purpose_of_transfer_code` `purpose_of_transfer_code` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `invoice` CHANGE `type` `type` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table invoice_challenges convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table invoice_sponsorings convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table location convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table location_location_data convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `location_location_data` CHANGE `location_data_value` `location_data_value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `location_location_data` CHANGE `location_data_key` `location_data_key` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';

alter table media convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `media` CHANGE `media_type` `media_type` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `media` CHANGE `url` `url` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table message convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `message` CHANGE `text` `text` TEXT  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table oauth_access_token convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `oauth_access_token` CHANGE `token_id` `token_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_access_token` CHANGE `authentication_id` `authentication_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_access_token` CHANGE `user_name` `user_name` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_access_token` CHANGE `client_id` `client_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_access_token` CHANGE `refresh_token` `refresh_token` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table oauth_client_details convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `oauth_client_details` CHANGE `client_id` `client_id` VARCHAR(30)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `oauth_client_details` CHANGE `resource_ids` `resource_ids` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `client_secret` `client_secret` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `scope` `scope` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `authorized_grant_types` `authorized_grant_types` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `web_server_redirect_uri` `web_server_redirect_uri` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `authorities` `authorities` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `additional_information` `additional_information` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_details` CHANGE `autoapprove` `autoapprove` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table oauth_client_token convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `oauth_client_token` CHANGE `token_id` `token_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_token` CHANGE `authentication_id` `authentication_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_token` CHANGE `user_name` `user_name` VARCHAR(256)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `oauth_client_token` CHANGE `client_id` `client_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table oauth_code convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `oauth_code` CHANGE `code` `code` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table oauth_refresh_token convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `oauth_refresh_token` CHANGE `token_id` `token_id` VARCHAR(256)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table payment convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `payment` CHANGE `dtype` `dtype` VARCHAR(31)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `payment` CHANGE `amount` `amount` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table posting_comments convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table posting_hashtags convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `posting_hashtags` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table posting_likes convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table posting_media convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table postinglike convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table schema_version convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `schema_version` CHANGE `version` `version` VARCHAR(50)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `schema_version` CHANGE `description` `description` VARCHAR(200)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `schema_version` CHANGE `type` `type` VARCHAR(20)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `schema_version` CHANGE `script` `script` VARCHAR(1000)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `schema_version` CHANGE `installed_by` `installed_by` VARCHAR(100)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';

alter table sponsoring convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `sponsoring` CHANGE `amount_per_km` `amount_per_km` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `sponsoring` CHANGE `sponsoring_limit` `sponsoring_limit` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table team convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `team` CHANGE `description` `description` TEXT  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team` CHANGE `name` `name` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `team` CHANGE `name` `name` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table team_members convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table team_overview convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `team_overview` CHANGE `name` `name` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview` CHANGE `comment` `comment` VARCHAR(600)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview` CHANGE `team_name` `team_name` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview_location_data` CHANGE `last_location_data_value` `last_location_data_value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview_location_data` CHANGE `last_location_data_key` `last_location_data_key` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `team_overview_members` CHANGE `contact_phone` `contact_phone` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview_members` CHANGE `emergency_phone` `emergency_phone` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview_members` CHANGE `firstname` `firstname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `team_overview_members` CHANGE `lastname` `lastname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table team_overview_location_data convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table team_overview_members convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table unregistered_sponsor convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `unregistered_sponsor` CHANGE `city` `city` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `country` `country` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `housenumber` `housenumber` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `street` `street` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `zipcode` `zipcode` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `company` `company` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `firstname` `firstname` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `gender` `gender` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `lastname` `lastname` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `url` `url` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `email` `email` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `supporter_type` `supporter_type` VARCHAR(31)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `zipcode` `zipcode` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `company` `company` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `firstname` `firstname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `gender` `gender` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `lastname` `lastname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `url` `url` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `email` `email` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `unregistered_sponsor` CHANGE `supporter_type` `supporter_type` VARCHAR(31)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table user_account convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `user_account` CHANGE `activation_token` `activation_token` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_account` CHANGE `email` `email` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `user_account` CHANGE `firstname` `firstname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_account` CHANGE `gender` `gender` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_account` CHANGE `lastname` `lastname` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_account` CHANGE `password_hash` `password_hash` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `user_account` CHANGE `preferred_language` `preferred_language` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT 'DE';
ALTER TABLE `user_account` CHANGE `notification_token` `notification_token` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;

alter table user_account_blocked_by convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table user_account_user_roles convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table user_role convert to character set utf8mb4 collate utf8mb4_unicode_ci;
ALTER TABLE `user_role` CHANGE `role_name` `role_name` VARCHAR(31)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NOT NULL  DEFAULT '';
ALTER TABLE `user_role` CHANGE `city` `city` VARCHAR(255)  CHARACTER SET utf8mb4  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `city` `city` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `country` `country` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `housenumber` `housenumber` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `street` `street` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `zipcode` `zipcode` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `phonenumber` `phonenumber` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `title` `title` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `emp_tshirtsize` `emp_tshirtsize` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `emergencynumber` `emergencynumber` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `hometown` `hometown` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `tshirtsize` `tshirtsize` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `company` `company` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
ALTER TABLE `user_role` CHANGE `value` `value` VARCHAR(255)  CHARACTER SET utf8mb4  COLLATE utf8mb4_unicode_ci  NULL  DEFAULT NULL;
