CREATE TABLE `team_members` (
  `teams_id` bigint(20) NOT NULL,
  `members_id` bigint(20) NOT NULL,
  PRIMARY KEY (`teams_id`,`members_id`),
  KEY `FK_4adet9n1mfa8wy3wbe963bt36` (`members_id`),
  CONSTRAINT `FK_4adet9n1mfa8wy3wbe963bt36` FOREIGN KEY (`members_id`) REFERENCES `user_role` (`id`),
  CONSTRAINT `FK_gw5prcmqu7a8q2ko0xg23bokx` FOREIGN KEY (`teams_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into team_members (select teams_id as team_id, user_role_id as participant_id from user_role_teams);

drop table user_role_teams;
