INSERT INTO `media` (`id`, `CREATEd_at`, `updated_at`, `media_type`) VALUES (1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', 'IMAGE');
INSERT INTO `user_core` (`id`, `CREATEd_at`, `updated_at`, `activation_token`, `email`, `firstname`, `gender`, `is_blocked`, `lastname`, `password_hash`, `profile_pic_id`) VALUES (1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', NULL, 'admin@break-out.org', NULL, NULL, b'0', NULL, '$2a$10$sZ6KojxX6qvsmGXFGxb6.Os8kiOamXs/56acW1pfUUx4jCq5C3AWC', 1);
INSERT INTO `user_role` (`role_name`, `id`, `CREATEd_at`, `updated_at`, `phonenumber`, `title`, `emp_tshirtsize`, `emergencynumber`, `hometown`, `tshirtsize`, `address`, `company`, `is_hidden`, `logo`, `url`, `core_id`, `address_id`, `current_team_id`) VALUES ('ADMIN', 1, '2016-04-23 14:06:10', '2016-04-23 14:06:10', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `user_core_user_roles` (`user_core_id`, `user_roles_id`, `user_roles_key`) VALUES (1, 1, 'backend.model.user.Admin');

CREATE TABLE oauth_client_details (client_id VARCHAR(256) PRIMARY KEY, resource_ids VARCHAR(256),  client_secret VARCHAR(256),  scope VARCHAR(256),  authorized_grant_types VARCHAR(256),  web_server_redirect_uri VARCHAR(256),  authorities VARCHAR(256),  access_token_validity INTEGER,  refresh_token_validity INTEGER,  additional_information VARCHAR(4096), autoapprove VARCHAR(256));
CREATE TABLE oauth_client_token (token_id VARCHAR(256),  token BLOB,  authentication_id VARCHAR(256),  user_name VARCHAR(256),  client_id VARCHAR(256));
CREATE TABLE oauth_access_token (token_id VARCHAR(256),  token BLOB,  authentication_id VARCHAR(256),  user_name VARCHAR(256),  client_id VARCHAR(256),  authentication BLOB,  refresh_token VARCHAR(256));
CREATE TABLE oauth_refresh_token (token_id VARCHAR(256),  token BLOB,  authentication BLOB);
CREATE TABLE oauth_code (code VARCHAR(256), authentication BLOB);

INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES	('breakout_app', 'BREAKOUT_BACKEND', '123456789', 'read,write', 'password,refresh_token', '', 'USER', NULL, NULL, '{}', '');
