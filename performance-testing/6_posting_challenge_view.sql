select posting0_.id as id1_20_, posting0_.created_at as created_2_20_, posting0_.updated_at as updated_3_20_, posting0_.challenge_id as challeng6_20_, posting0_.date as date4_20_, posting0_.location_id as location7_20_, posting0_.text as text5_20_, posting0_.user_id as user_id8_20_ from posting posting0_ where posting0_.id=?
select challenge0_.id as id1_1_0_, challenge0_.created_at as created_2_1_0_, challenge0_.updated_at as updated_3_1_0_, challenge0_.amount as amount4_1_0_, challenge0_.contract_id as contract7_1_0_, challenge0_.description as descript5_1_0_, challenge0_.invoice_id as invoice_8_1_0_, challenge0_.registered_sponsor_id as register9_1_0_, challenge0_.status as status6_1_0_, challenge0_.team_id as team_id10_1_0_, challenge0_.unregistered_sponsor_id as unregis11_1_0_, posting1_.id as id1_20_1_, posting1_.created_at as created_2_20_1_, posting1_.updated_at as updated_3_20_1_, posting1_.challenge_id as challeng6_20_1_, posting1_.date as date4_20_1_, posting1_.location_id as location7_20_1_, posting1_.text as text5_20_1_, posting1_.user_id as user_id8_20_1_, location2_.id as id1_14_2_, location2_.created_at as created_2_14_2_, location2_.updated_at as updated_3_14_2_, location2_.latitude as latitude4_14_2_, location2_.longitude as longitud5_14_2_, location2_.date as date6_14_2_, location2_.distance as distance7_14_2_, location2_.is_during_event as is_durin8_14_2_, location2_.team_id as team_id9_14_2_, location2_.uploader_id as uploade10_14_2_, useraccoun3_.id as id1_30_3_, useraccoun3_.created_at as created_2_30_3_, useraccoun3_.updated_at as updated_3_30_3_, useraccoun3_.activation_token as activati4_30_3_, useraccoun3_.email as email5_30_3_, useraccoun3_.firstname as firstnam6_30_3_, useraccoun3_.gender as gender7_30_3_, useraccoun3_.is_blocked as is_block8_30_3_, useraccoun3_.lastname as lastname9_30_3_, useraccoun3_.password_hash as passwor10_30_3_, useraccoun3_.preferred_language as preferr11_30_3_, useraccoun3_.profile_pic_id as profile12_30_3_ from challenge challenge0_ left outer join posting posting1_ on challenge0_.id=posting1_.challenge_id left outer join location location2_ on posting1_.location_id=location2_.id left outer join user_account useraccoun3_ on posting1_.user_id=useraccoun3_.id where challenge0_.id=?
select media0_.id as id1_16_0_, media0_.created_at as created_2_16_0_, media0_.updated_at as updated_3_16_0_, media0_.media_type as media_ty4_16_0_, sizes1_.media_id as media_i10_17_1_, sizes1_.id as id1_17_1_, sizes1_.id as id1_17_2_, sizes1_.created_at as created_2_17_2_, sizes1_.updated_at as updated_3_17_2_, sizes1_.height as height4_17_2_, sizes1_.length as length5_17_2_, sizes1_.media_id as media_i10_17_2_, sizes1_.media_type as media_ty6_17_2_, sizes1_.size as size7_17_2_, sizes1_.url as url8_17_2_, sizes1_.width as width9_17_2_ from media media0_ left outer join media_size sizes1_ on media0_.id=sizes1_.media_id where media0_.id=?
select sponsoring0_.id as id2_13_0_, sponsoring0_.created_at as created_3_13_0_, sponsoring0_.updated_at as updated_4_13_0_, sponsoring0_.amount as amount5_13_0_, sponsoring0_.purpose_of_transfer as purpose_6_13_0_, sponsoring0_.purpose_of_transfer_code as purpose_7_13_0_, sponsoring0_.company as company8_13_0_, sponsoring0_.firstname as firstnam9_13_0_, sponsoring0_.lastname as lastnam10_13_0_, sponsoring0_.subject as subject11_13_0_, sponsoring0_.team_id as team_id12_13_0_, team1_.id as id1_27_1_, team1_.created_at as created_2_27_1_, team1_.updated_at as updated_3_27_1_, team1_.description as descript4_27_1_, team1_.event_id as event_id7_27_1_, team1_.has_started as has_star5_27_1_, team1_.name as name6_27_1_, team1_.profile_pic_id as profile_8_27_1_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team1_.id) as formula0_1_, event2_.id as id1_7_2_, event2_.created_at as created_2_7_2_, event2_.updated_at as updated_3_7_2_, event2_.city as city4_7_2_, event2_.date as date5_7_2_, event2_.duration as duration6_7_2_, event2_.is_current as is_curre7_7_2_, event2_.latitude as latitude8_7_2_, event2_.longitude as longitud9_7_2_, event2_.title as title10_7_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_ from invoice sponsoring0_ left outer join team team1_ on sponsoring0_.team_id=team1_.id left outer join event event2_ on team1_.event_id=event2_.id left outer join media media3_ on team1_.profile_pic_id=media3_.id where sponsoring0_.id=? and sponsoring0_.dtype='SponsoringInvoice'
select teamentryf0_.id as id2_13_3_, teamentryf0_.created_at as created_3_13_3_, teamentryf0_.updated_at as updated_4_13_3_, teamentryf0_.amount as amount5_13_3_, teamentryf0_.purpose_of_transfer as purpose_6_13_3_, teamentryf0_.purpose_of_transfer_code as purpose_7_13_3_, teamentryf0_.team_id as team_id12_13_3_, team1_.id as id1_27_0_, team1_.created_at as created_2_27_0_, team1_.updated_at as updated_3_27_0_, team1_.description as descript4_27_0_, team1_.event_id as event_id7_27_0_, team1_.has_started as has_star5_27_0_, team1_.name as name6_27_0_, team1_.profile_pic_id as profile_8_27_0_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team1_.id) as formula0_0_, event2_.id as id1_7_1_, event2_.created_at as created_2_7_1_, event2_.updated_at as updated_3_7_1_, event2_.city as city4_7_1_, event2_.date as date5_7_1_, event2_.duration as duration6_7_1_, event2_.is_current as is_curre7_7_1_, event2_.latitude as latitude8_7_1_, event2_.longitude as longitud9_7_1_, event2_.title as title10_7_1_, media3_.id as id1_16_2_, media3_.created_at as created_2_16_2_, media3_.updated_at as updated_3_16_2_, media3_.media_type as media_ty4_16_2_ from invoice teamentryf0_ left outer join team team1_ on teamentryf0_.team_id=team1_.id left outer join event event2_ on team1_.event_id=event2_.id left outer join media media3_ on team1_.profile_pic_id=media3_.id where teamentryf0_.team_id=? and teamentryf0_.dtype='TeamEntryFeeInvoice'
select sponsor0_.id as id2_32_0_, sponsor0_.created_at as created_3_32_0_, sponsor0_.updated_at as updated_4_32_0_, sponsor0_.account_id as account20_32_0_, sponsor0_.city as city5_32_0_, sponsor0_.country as country6_32_0_, sponsor0_.housenumber as housenum7_32_0_, sponsor0_.street as street8_32_0_, sponsor0_.zipcode as zipcode9_32_0_, sponsor0_.company as company17_32_0_, sponsor0_.is_hidden as is_hidd18_32_0_, sponsor0_.logo_id as logo_id22_32_0_, sponsor0_.value as value19_32_0_, useraccoun1_.id as id1_30_1_, useraccoun1_.created_at as created_2_30_1_, useraccoun1_.updated_at as updated_3_30_1_, useraccoun1_.activation_token as activati4_30_1_, useraccoun1_.email as email5_30_1_, useraccoun1_.firstname as firstnam6_30_1_, useraccoun1_.gender as gender7_30_1_, useraccoun1_.is_blocked as is_block8_30_1_, useraccoun1_.lastname as lastname9_30_1_, useraccoun1_.password_hash as passwor10_30_1_, useraccoun1_.preferred_language as preferr11_30_1_, useraccoun1_.profile_pic_id as profile12_30_1_, media2_.id as id1_16_2_, media2_.created_at as created_2_16_2_, media2_.updated_at as updated_3_16_2_, media2_.media_type as media_ty4_16_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_, sizes4_.media_id as media_i10_17_4_, sizes4_.id as id1_17_4_, sizes4_.id as id1_17_5_, sizes4_.created_at as created_2_17_5_, sizes4_.updated_at as updated_3_17_5_, sizes4_.height as height4_17_5_, sizes4_.length as length5_17_5_, sizes4_.media_id as media_i10_17_5_, sizes4_.media_type as media_ty6_17_5_, sizes4_.size as size7_17_5_, sizes4_.url as url8_17_5_, sizes4_.width as width9_17_5_ from user_role sponsor0_ left outer join user_account useraccoun1_ on sponsor0_.account_id=useraccoun1_.id left outer join media media2_ on useraccoun1_.profile_pic_id=media2_.id left outer join media media3_ on sponsor0_.logo_id=media3_.id left outer join media_size sizes4_ on media3_.id=sizes4_.media_id where sponsor0_.id=? and sponsor0_.role_name='S'
select posting0_.id as id1_20_6_, posting0_.created_at as created_2_20_6_, posting0_.updated_at as updated_3_20_6_, posting0_.challenge_id as challeng6_20_6_, posting0_.date as date4_20_6_, posting0_.location_id as location7_20_6_, posting0_.text as text5_20_6_, posting0_.user_id as user_id8_20_6_, challenge1_.id as id1_1_0_, challenge1_.created_at as created_2_1_0_, challenge1_.updated_at as updated_3_1_0_, challenge1_.amount as amount4_1_0_, challenge1_.contract_id as contract7_1_0_, challenge1_.description as descript5_1_0_, challenge1_.invoice_id as invoice_8_1_0_, challenge1_.registered_sponsor_id as register9_1_0_, challenge1_.status as status6_1_0_, challenge1_.team_id as team_id10_1_0_, challenge1_.unregistered_sponsor_id as unregis11_1_0_, location2_.id as id1_14_1_, location2_.created_at as created_2_14_1_, location2_.updated_at as updated_3_14_1_, location2_.latitude as latitude4_14_1_, location2_.longitude as longitud5_14_1_, location2_.date as date6_14_1_, location2_.distance as distance7_14_1_, location2_.is_during_event as is_durin8_14_1_, location2_.team_id as team_id9_14_1_, location2_.uploader_id as uploade10_14_1_, locationda3_.location_id as location1_15_8_, locationda3_.location_data_value as location2_15_8_, locationda3_.location_data_key as location3_8_, team4_.id as id1_27_2_, team4_.created_at as created_2_27_2_, team4_.updated_at as updated_3_27_2_, team4_.description as descript4_27_2_, team4_.event_id as event_id7_27_2_, team4_.has_started as has_star5_27_2_, team4_.name as name6_27_2_, team4_.profile_pic_id as profile_8_27_2_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team4_.id) as formula0_2_, participan5_.id as id2_32_3_, participan5_.created_at as created_3_32_3_, participan5_.updated_at as updated_4_32_3_, participan5_.account_id as account20_32_3_, participan5_.birthdate as birthda13_32_3_, participan5_.current_team_id as current21_32_3_, participan5_.emergencynumber as emergen14_32_3_, participan5_.hometown as hometow15_32_3_, participan5_.phonenumber as phonenu10_32_3_, participan5_.tshirtsize as tshirts16_32_3_, useraccoun6_.id as id1_30_4_, useraccoun6_.created_at as created_2_30_4_, useraccoun6_.updated_at as updated_3_30_4_, useraccoun6_.activation_token as activati4_30_4_, useraccoun6_.email as email5_30_4_, useraccoun6_.firstname as firstnam6_30_4_, useraccoun6_.gender as gender7_30_4_, useraccoun6_.is_blocked as is_block8_30_4_, useraccoun6_.lastname as lastname9_30_4_, useraccoun6_.password_hash as passwor10_30_4_, useraccoun6_.preferred_language as preferr11_30_4_, useraccoun6_.profile_pic_id as profile12_30_4_, media7_.id as id1_16_5_, media7_.created_at as created_2_16_5_, media7_.updated_at as updated_3_16_5_, media7_.media_type as media_ty4_16_5_ from posting posting0_ left outer join challenge challenge1_ on posting0_.challenge_id=challenge1_.id left outer join location location2_ on posting0_.location_id=location2_.id left outer join location_location_data locationda3_ on location2_.id=locationda3_.location_id left outer join team team4_ on location2_.team_id=team4_.id left outer join user_role participan5_ on location2_.uploader_id=participan5_.id left outer join user_account useraccoun6_ on posting0_.user_id=useraccoun6_.id left outer join media media7_ on useraccoun6_.profile_pic_id=media7_.id where posting0_.challenge_id=?
select posting0_.id as id1_20_6_, posting0_.created_at as created_2_20_6_, posting0_.updated_at as updated_3_20_6_, posting0_.challenge_id as challeng6_20_6_, posting0_.date as date4_20_6_, posting0_.location_id as location7_20_6_, posting0_.text as text5_20_6_, posting0_.user_id as user_id8_20_6_, challenge1_.id as id1_1_0_, challenge1_.created_at as created_2_1_0_, challenge1_.updated_at as updated_3_1_0_, challenge1_.amount as amount4_1_0_, challenge1_.contract_id as contract7_1_0_, challenge1_.description as descript5_1_0_, challenge1_.invoice_id as invoice_8_1_0_, challenge1_.registered_sponsor_id as register9_1_0_, challenge1_.status as status6_1_0_, challenge1_.team_id as team_id10_1_0_, challenge1_.unregistered_sponsor_id as unregis11_1_0_, location2_.id as id1_14_1_, location2_.created_at as created_2_14_1_, location2_.updated_at as updated_3_14_1_, location2_.latitude as latitude4_14_1_, location2_.longitude as longitud5_14_1_, location2_.date as date6_14_1_, location2_.distance as distance7_14_1_, location2_.is_during_event as is_durin8_14_1_, location2_.team_id as team_id9_14_1_, location2_.uploader_id as uploade10_14_1_, locationda3_.location_id as location1_15_8_, locationda3_.location_data_value as location2_15_8_, locationda3_.location_data_key as location3_8_, team4_.id as id1_27_2_, team4_.created_at as created_2_27_2_, team4_.updated_at as updated_3_27_2_, team4_.description as descript4_27_2_, team4_.event_id as event_id7_27_2_, team4_.has_started as has_star5_27_2_, team4_.name as name6_27_2_, team4_.profile_pic_id as profile_8_27_2_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team4_.id) as formula0_2_, participan5_.id as id2_32_3_, participan5_.created_at as created_3_32_3_, participan5_.updated_at as updated_4_32_3_, participan5_.account_id as account20_32_3_, participan5_.birthdate as birthda13_32_3_, participan5_.current_team_id as current21_32_3_, participan5_.emergencynumber as emergen14_32_3_, participan5_.hometown as hometow15_32_3_, participan5_.phonenumber as phonenu10_32_3_, participan5_.tshirtsize as tshirts16_32_3_, useraccoun6_.id as id1_30_4_, useraccoun6_.created_at as created_2_30_4_, useraccoun6_.updated_at as updated_3_30_4_, useraccoun6_.activation_token as activati4_30_4_, useraccoun6_.email as email5_30_4_, useraccoun6_.firstname as firstnam6_30_4_, useraccoun6_.gender as gender7_30_4_, useraccoun6_.is_blocked as is_block8_30_4_, useraccoun6_.lastname as lastname9_30_4_, useraccoun6_.password_hash as passwor10_30_4_, useraccoun6_.preferred_language as preferr11_30_4_, useraccoun6_.profile_pic_id as profile12_30_4_, media7_.id as id1_16_5_, media7_.created_at as created_2_16_5_, media7_.updated_at as updated_3_16_5_, media7_.media_type as media_ty4_16_5_ from posting posting0_ left outer join challenge challenge1_ on posting0_.challenge_id=challenge1_.id left outer join location location2_ on posting0_.location_id=location2_.id left outer join location_location_data locationda3_ on location2_.id=locationda3_.location_id left outer join team team4_ on location2_.team_id=team4_.id left outer join user_role participan5_ on location2_.uploader_id=participan5_.id left outer join user_account useraccoun6_ on posting0_.user_id=useraccoun6_.id left outer join media media7_ on useraccoun6_.profile_pic_id=media7_.id where posting0_.location_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select hashtags0_.posting_id as posting_1_22_0_, hashtags0_.value as value2_22_0_ from posting_hashtags hashtags0_ where hashtags0_.posting_id=?
select media0_.posting_id as posting_1_24_0_, media0_.media_id as media_id2_24_0_, media1_.id as id1_16_1_, media1_.created_at as created_2_16_1_, media1_.updated_at as updated_3_16_1_, media1_.media_type as media_ty4_16_1_ from posting_media media0_ inner join media media1_ on media0_.media_id=media1_.id where media0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select comments0_.posting_id as posting_1_21_0_, comments0_.comments_id as comments2_21_0_, comment1_.id as id1_2_1_, comment1_.created_at as created_2_2_1_, comment1_.updated_at as updated_3_2_1_, comment1_.date as date4_2_1_, comment1_.text as text5_2_1_, comment1_.user_id as user_id6_2_1_, useraccoun2_.id as id1_30_2_, useraccoun2_.created_at as created_2_30_2_, useraccoun2_.updated_at as updated_3_30_2_, useraccoun2_.activation_token as activati4_30_2_, useraccoun2_.email as email5_30_2_, useraccoun2_.firstname as firstnam6_30_2_, useraccoun2_.gender as gender7_30_2_, useraccoun2_.is_blocked as is_block8_30_2_, useraccoun2_.lastname as lastname9_30_2_, useraccoun2_.password_hash as passwor10_30_2_, useraccoun2_.preferred_language as preferr11_30_2_, useraccoun2_.profile_pic_id as profile12_30_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_ from posting_comments comments0_ inner join comment comment1_ on comments0_.comments_id=comment1_.id left outer join user_account useraccoun2_ on comment1_.user_id=useraccoun2_.id left outer join media media3_ on useraccoun2_.profile_pic_id=media3_.id where comments0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select likes0_.posting_id as posting_1_23_0_, likes0_.like_id as like_id2_23_0_, like1_.id as id1_25_1_, like1_.created_at as created_2_25_1_, like1_.updated_at as updated_3_25_1_, like1_.date as date4_25_1_, like1_.user_id as user_id5_25_1_, useraccoun2_.id as id1_30_2_, useraccoun2_.created_at as created_2_30_2_, useraccoun2_.updated_at as updated_3_30_2_, useraccoun2_.activation_token as activati4_30_2_, useraccoun2_.email as email5_30_2_, useraccoun2_.firstname as firstnam6_30_2_, useraccoun2_.gender as gender7_30_2_, useraccoun2_.is_blocked as is_block8_30_2_, useraccoun2_.lastname as lastname9_30_2_, useraccoun2_.password_hash as passwor10_30_2_, useraccoun2_.preferred_language as preferr11_30_2_, useraccoun2_.profile_pic_id as profile12_30_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_ from posting_likes likes0_ inner join postinglike like1_ on likes0_.like_id=like1_.id left outer join user_account useraccoun2_ on like1_.user_id=useraccoun2_.id left outer join media media3_ on useraccoun2_.profile_pic_id=media3_.id where likes0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?

-- 22 select
-- 39 joins
-- 480 as
