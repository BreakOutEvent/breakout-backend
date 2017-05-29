alter table invoice add column `event_id` bigint(20) DEFAULT NULL;
alter table invoice add KEY `FKlnvggjtamihxdil6v6mc46urg` (`event_id`);
alter table invoice add CONSTRAINT `FKlnvggjtamihxdil6v6mc46urg` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`);


