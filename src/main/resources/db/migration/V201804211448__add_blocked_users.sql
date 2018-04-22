CREATE TABLE `user_account_blocked_by` (
  `user_account_id` bigint(20) NOT NULL,
  `blocked_by_id` bigint(20) NOT NULL,
  CONSTRAINT FOREIGN KEY (`blocked_by_id`) REFERENCES `user_account` (`id`),
  CONSTRAINT FOREIGN KEY (`user_account_id`) REFERENCES `user_account` (`id`)
)