ALTER TABLE `whitelist_email_entry` ADD UNIQUE (`event_id`, `value`);

ALTER TABLE `whitelist_domain_entry` ADD UNIQUE (`event_id`, `domain`);