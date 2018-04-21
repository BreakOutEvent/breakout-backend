ALTER TABLE `media`
  ADD `url` VARCHAR(255);

ALTER TABLE `posting`
  ADD `media_id` BIGINT(20);

ALTER TABLE `posting`
  ADD CONSTRAINT `posting_media_id` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`);

#TODO migrate media sizes
