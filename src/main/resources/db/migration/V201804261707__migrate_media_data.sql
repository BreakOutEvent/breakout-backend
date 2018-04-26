-- Pick biggest size and use its url for the media.url
-- thanks @dogl! You earned yourself that beer ;)
UPDATE media m
JOIN (SELECT m1.id, ms2.url FROM media m1
      JOIN (
        SELECT media_id, MAX(size) AS size FROM media_size mss
        GROUP BY media_id
      ) AS ms ON ms.media_id = m1.id
      JOIN media_size ms2 ON ms.media_id = ms2.media_id AND ms2.size = ms.size)
     mNew
    ON m.id = mNew.id
SET m.url = mNew.url;

-- migrate from previous join table to foreign key
update posting p
join posting_media pm on pm.posting_id = p.id
set p.media_id = pm.media_id;
