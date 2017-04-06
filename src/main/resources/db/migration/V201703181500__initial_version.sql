/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE SCHEMA IF NOT EXISTS `${BREAKOUT}` DEFAULT CHARACTER SET = utf8 DEFAULT COLLATE = utf8_unicode_ci;

USE `${BREAKOUT}`;

CREATE TABLE IF NOT EXISTS `challenge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `amount` varchar(255) DEFAULT NULL,
  `description` text,
  `status` int(11) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `housenumber` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `zipcode` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `is_hidden` bit(1) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `contract_id` bigint(20) DEFAULT NULL,
  `sponsor_id` bigint(20) DEFAULT NULL,
  `team_id` bigint(20) DEFAULT NULL,
  `invoice_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9tgdub8og1uymldqwvworn043` (`contract_id`),
  KEY `FK_c86a3fmfv2ho8eo09jve4l72x` (`sponsor_id`),
  KEY `FK_rukm7t43ssfuf729taf4i6t4s` (`team_id`),
  KEY `FK_challenge_invoice` (`invoice_id`),
  CONSTRAINT `FK_9tgdub8og1uymldqwvworn043` FOREIGN KEY (`contract_id`) REFERENCES `media` (`id`),
  CONSTRAINT `FK_c86a3fmfv2ho8eo09jve4l72x` FOREIGN KEY (`sponsor_id`) REFERENCES `user_role` (`id`),
  CONSTRAINT `FK_challenge_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`),
  CONSTRAINT `FK_rukm7t43ssfuf729taf4i6t4s` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `text` text,
  `posting_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cb78ra5e8th78whr91f8kl5gg` (`posting_id`),
  KEY `FK_mxoojfj9tmy8088avf57mpm02` (`user_id`),
  CONSTRAINT `FK_cb78ra5e8th78whr91f8kl5gg` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`),
  CONSTRAINT `FK_mxoojfj9tmy8088avf57mpm02` FOREIGN KEY (`user_id`) REFERENCES `user_core` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `body` text,
  `button_text` varchar(255) DEFAULT NULL,
  `button_url` varchar(255) DEFAULT NULL,
  `campaign_code` varchar(255) DEFAULT NULL,
  `is_sent` bit(1) NOT NULL,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email_bcc` (
  `email_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  KEY `FK_3hnhq7ij8pdijprs2162x1cbx` (`email_id`),
  CONSTRAINT `FK_3hnhq7ij8pdijprs2162x1cbx` FOREIGN KEY (`email_id`) REFERENCES `email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email_files` (
  `email_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  KEY `FK_snqhpll0lann0g9wdov6nuj0d` (`email_id`),
  CONSTRAINT `FK_snqhpll0lann0g9wdov6nuj0d` FOREIGN KEY (`email_id`) REFERENCES `email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email_to` (
  `email_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  KEY `FK_12l8to68r43o39kj6qmog74vg` (`email_id`),
  CONSTRAINT `FK_12l8to68r43o39kj6qmog74vg` FOREIGN KEY (`email_id`) REFERENCES `email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `feature` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_enabled` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_jhueeftkn8ve8th8m8a2878dr` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `group_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `group_message_messages` (
  `group_message_id` bigint(20) NOT NULL,
  `messages_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_3jv5hxv98auspp85gx1uujdl6` (`messages_id`),
  KEY `FK_fohg5s4q74u9poalpqgn0nxd` (`group_message_id`),
  CONSTRAINT `FK_3jv5hxv98auspp85gx1uujdl6` FOREIGN KEY (`messages_id`) REFERENCES `message` (`id`),
  CONSTRAINT `FK_fohg5s4q74u9poalpqgn0nxd` FOREIGN KEY (`group_message_id`) REFERENCES `group_message` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `group_message_users` (
  `group_messages_id` bigint(20) NOT NULL,
  `users_id` bigint(20) NOT NULL,
  KEY `FK_seb6gx5m8841qthuujahmy9oi` (`users_id`),
  KEY `FK_bkyeuppkxvqce5v6qjfcr5m7d` (`group_messages_id`),
  CONSTRAINT `FK_bkyeuppkxvqce5v6qjfcr5m7d` FOREIGN KEY (`group_messages_id`) REFERENCES `group_message` (`id`),
  CONSTRAINT `FK_seb6gx5m8841qthuujahmy9oi` FOREIGN KEY (`users_id`) REFERENCES `user_core` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `invitation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `invitation_token` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `team_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_1wxl3pirsa0kq5a9aakh4av76` (`team_id`),
  CONSTRAINT `FK_1wxl3pirsa0kq5a9aakh4av76` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `invoice` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `amount` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `team_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_auiohpywc7yobubt73gtv1is1` (`team_id`),
  CONSTRAINT `FK_auiohpywc7yobubt73gtv1is1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date` datetime DEFAULT NULL,
  `distance` double NOT NULL,
  `is_during_event` bit(1) NOT NULL DEFAULT b'0',
  `team_id` bigint(20) DEFAULT NULL,
  `uploader_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_dkx8c12e2gfrm5lbyx6gi11qv` (`team_id`),
  KEY `FK_buyipo19akx5rel7mn5iibvm4` (`uploader_id`),
  KEY `date` (`date`),
  KEY `distance` (`distance`),
  KEY `is_during_event` (`is_during_event`),
  CONSTRAINT `FK_buyipo19akx5rel7mn5iibvm4` FOREIGN KEY (`uploader_id`) REFERENCES `user_role` (`id`),
  CONSTRAINT `FK_dkx8c12e2gfrm5lbyx6gi11qv` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `location_location_data` (
  `location_id` bigint(20) NOT NULL,
  `location_data_value` varchar(255) DEFAULT NULL,
  `location_data_key` varchar(255) NOT NULL,
  PRIMARY KEY (`location_id`,`location_data_key`),
  CONSTRAINT `FK_heiu4p8xr3lpvh0gyrjww7smq` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `media` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `media_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `media_size` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `length` int(11) DEFAULT NULL,
  `media_type` varchar(255) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `media_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_3r5530ps0aqbqb5896d0e3xx4` (`media_id`),
  CONSTRAINT `FK_3r5530ps0aqbqb5896d0e3xx4` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `text` text,
  `creator_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_8p4uwe711ponlwl6qq28127r2` (`creator_id`),
  CONSTRAINT `FK_8p4uwe711ponlwl6qq28127r2` FOREIGN KEY (`creator_id`) REFERENCES `user_core` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL,
  UNIQUE KEY `authentication_id` (`authentication_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` varchar(30) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(256) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `oauth_client_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `oauth_code` (
  `code` varchar(256) DEFAULT NULL,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `payment` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `amount` varchar(255) DEFAULT NULL,
  `invoice_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_4l6ndm1m1iw9knbdtxd6m6fyc` (`invoice_id`),
  KEY `FK_ctvskou1xh26obtbvta4d2o4l` (`user_id`),
  CONSTRAINT `FK_4l6ndm1m1iw9knbdtxd6m6fyc` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`),
  CONSTRAINT `FK_ctvskou1xh26obtbvta4d2o4l` FOREIGN KEY (`user_id`) REFERENCES `user_core` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `posting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `text` text,
  `challenge_id` bigint(20) DEFAULT NULL,
  `location_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tbk3oedwajpidae8vg37ucfyr` (`challenge_id`),
  KEY `FK_p1kmd1fhqub7ht14cdvqs20oi` (`location_id`),
  KEY `FK_57xdo2wvqyh63f76tvmecm0dy` (`user_id`),
  KEY `date` (`date`),
  CONSTRAINT `FK_57xdo2wvqyh63f76tvmecm0dy` FOREIGN KEY (`user_id`) REFERENCES `user_core` (`id`),
  CONSTRAINT `FK_p1kmd1fhqub7ht14cdvqs20oi` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK_tbk3oedwajpidae8vg37ucfyr` FOREIGN KEY (`challenge_id`) REFERENCES `challenge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `posting_hashtags` (
  `posting_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  KEY `FK_be695e15jv429vgclhldya4th` (`posting_id`),
  CONSTRAINT `FK_be695e15jv429vgclhldya4th` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `posting_media` (
  `posting_id` bigint(20) NOT NULL,
  `media_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_dn760fgmrj2kxa4xjhthh9ss3` (`media_id`),
  KEY `FK_69h5sy9chwmt0vyl76490d6jk` (`posting_id`),
  CONSTRAINT `FK_69h5sy9chwmt0vyl76490d6jk` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`),
  CONSTRAINT `FK_dn760fgmrj2kxa4xjhthh9ss3` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `postinglike` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `posting_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_onwc9cqvi7ikagh8nwj53xyn8` (`posting_id`,`user_id`),
  KEY `FK_29soqmvcbwsarb611h5wrirs5` (`user_id`),
  CONSTRAINT `FK_29soqmvcbwsarb611h5wrirs5` FOREIGN KEY (`user_id`) REFERENCES `user_core` (`id`),
  CONSTRAINT `FK_7crxb84bq2w162t04c6ixbd1y` FOREIGN KEY (`posting_id`) REFERENCES `posting` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `sponsoring` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `amount_per_km` varchar(255) DEFAULT NULL,
  `sponsoring_limit` varchar(255) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `housenumber` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `zipcode` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `is_hidden` bit(1) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `contract_id` bigint(20) DEFAULT NULL,
  `sponsor_id` bigint(20) DEFAULT NULL,
  `team_id` bigint(20) DEFAULT NULL,
  `invoice_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9ej64ywvpb7gx5u17svvx6bqw` (`contract_id`),
  KEY `FK_h5v27ocnlcplf3balxocidh98` (`sponsor_id`),
  KEY `FK_7td609ul214m7gdmr2abnj9nn` (`team_id`),
  KEY `FK_sponsoring_invoice` (`invoice_id`),
  CONSTRAINT `FK_7td609ul214m7gdmr2abnj9nn` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FK_9ej64ywvpb7gx5u17svvx6bqw` FOREIGN KEY (`contract_id`) REFERENCES `media` (`id`),
  CONSTRAINT `FK_h5v27ocnlcplf3balxocidh98` FOREIGN KEY (`sponsor_id`) REFERENCES `user_role` (`id`),
  CONSTRAINT `FK_sponsoring_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `description` text,
  `has_started` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `event_id` bigint(20) DEFAULT NULL,
  `profile_pic_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_seu98gcifrfvg15wfrbpxfetg` (`event_id`),
  KEY `FK_7nrg7w8rpge0e4svy4gc3dcvb` (`profile_pic_id`),
  CONSTRAINT `FK_7nrg7w8rpge0e4svy4gc3dcvb` FOREIGN KEY (`profile_pic_id`) REFERENCES `media` (`id`),
  CONSTRAINT `FK_seu98gcifrfvg15wfrbpxfetg` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_core` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `activation_token` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `is_blocked` bit(1) NOT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `profile_pic_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_a5ejd6edegkmh3pa8mmufa6yu` (`email`),
  KEY `FK_a817rcnx4sgduit0tnq6djrt4` (`profile_pic_id`),
  CONSTRAINT `FK_a817rcnx4sgduit0tnq6djrt4` FOREIGN KEY (`profile_pic_id`) REFERENCES `media` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_core_user_roles` (
  `user_core_id` bigint(20) NOT NULL,
  `user_roles_id` bigint(20) NOT NULL,
  `user_roles_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`user_core_id`,`user_roles_key`),
  UNIQUE KEY `UK_n0vphrful6jh6fkyeutdb2iwp` (`user_roles_id`),
  CONSTRAINT `FK_ihy8d9rfqphnsyi0maq2hxj1k` FOREIGN KEY (`user_core_id`) REFERENCES `user_core` (`id`),
  CONSTRAINT `FK_n0vphrful6jh6fkyeutdb2iwp` FOREIGN KEY (`user_roles_id`) REFERENCES `user_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_role` (
  `role_name` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `housenumber` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `zipcode` varchar(255) DEFAULT NULL,
  `phonenumber` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `emp_tshirtsize` varchar(255) DEFAULT NULL,
  `birthdate` date DEFAULT NULL,
  `emergencynumber` varchar(255) DEFAULT NULL,
  `hometown` varchar(255) DEFAULT NULL,
  `tshirtsize` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `is_hidden` bit(1) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `core_id` bigint(20) DEFAULT NULL,
  `current_team_id` bigint(20) DEFAULT NULL,
  `logo_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_407bb7n4bicixgt567jrw8hat` (`core_id`),
  KEY `FK_ta91meaj05ia0c5bnsfg037y5` (`current_team_id`),
  KEY `FK_edhglrcp9y0ce5ie5361v7fpi` (`logo_id`),
  CONSTRAINT `FK_407bb7n4bicixgt567jrw8hat` FOREIGN KEY (`core_id`) REFERENCES `user_core` (`id`),
  CONSTRAINT `FK_edhglrcp9y0ce5ie5361v7fpi` FOREIGN KEY (`logo_id`) REFERENCES `media` (`id`),
  CONSTRAINT `FK_ta91meaj05ia0c5bnsfg037y5` FOREIGN KEY (`current_team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
