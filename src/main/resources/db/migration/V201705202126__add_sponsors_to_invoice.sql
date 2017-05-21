USE `${BREAKOUT}`;

alter table invoice add column `registered_sponsor_id` bigint(20) DEFAULT NULL;
alter table invoice add key `FK7ayaixhkmlosiywsoaci2cpo4` (`registered_sponsor_id`);
alter table invoice add CONSTRAINT `FK7ayaixhkmlosiywsoaci2cpo4` FOREIGN KEY (`registered_sponsor_id`) REFERENCES `user_role` (`id`);

alter table invoice add column `unregistered_sponsor_id` bigint(20) DEFAULT NULL;
alter table invoice add KEY `FK46ujje99vpfxmw46n3tf5fa0x` (`unregistered_sponsor_id`);
alter table invoice add CONSTRAINT `FK46ujje99vpfxmw46n3tf5fa0x` FOREIGN KEY (`unregistered_sponsor_id`) REFERENCES `unregistered_sponsor` (`id`);
