INSERT INTO `media` (`id`, `created_at`, `updated_at`, `media_type`) VALUES
(1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', 'IMAGE');
​
INSERT INTO `user_core` (`id`, `created_at`, `updated_at`, `activation_token`, `email`, `firstname`, `gender`, `is_blocked`, `lastname`, `password_hash`, `profile_pic_id`) VALUES
	(1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', NULL, 'admin@break-out.org', NULL, NULL, b'0', NULL, '$2a$10$sZ6KojxX6qvsmGXFGxb6.Os8kiOamXs/56acW1pfUUx4jCq5C3AWC', 1);
​
INSERT INTO `user_core_user_roles` (`user_core_id`, `user_roles_id`, `user_roles_key`) VALUES
	(1, 1, 'backend.model.user.Admin');
​
INSERT INTO `user_role` (`role_name`, `id`, `created_at`, `updated_at`, `phonenumber`, `title`, `emp_tshirtsize`, `emergencynumber`, `hometown`, `tshirtsize`, `address`, `company`, `is_hidden`, `logo`, `url`, `core_id`, `address_id`, `current_team_id`) VALUES
	('ADMIN', 1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
