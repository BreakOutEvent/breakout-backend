USE `${BREAKOUT}`;

alter table user_account add column `new_email_to_validate` varchar(255) NULL;
alter table user_account add column `change_email_token` varchar(255) NULL;