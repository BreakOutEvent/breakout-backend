select posting0_.id as id1_20_, posting0_.created_at as created_2_20_, posting0_.updated_at as updated_3_20_, posting0_.challenge_id as challeng6_20_, posting0_.date as date4_20_, posting0_.location_id as location7_20_, posting0_.text as text5_20_, posting0_.user_id as user_id8_20_ from posting posting0_ where posting0_.id=?
select challenge0_.id as id1_1_0_, challenge0_.created_at as created_2_1_0_, challenge0_.updated_at as updated_3_1_0_, challenge0_.amount as amount4_1_0_, challenge0_.contract_id as contract7_1_0_, challenge0_.description as descript5_1_0_, challenge0_.invoice_id as invoice_8_1_0_, challenge0_.registered_sponsor_id as register9_1_0_, challenge0_.status as status6_1_0_, challenge0_.team_id as team_id10_1_0_, challenge0_.unregistered_sponsor_id as unregis11_1_0_, media1_.id as id1_16_1_, media1_.created_at as created_2_16_1_, media1_.updated_at as updated_3_16_1_, media1_.media_type as media_ty4_16_1_, sizes2_.media_id as media_i10_17_2_, sizes2_.id as id1_17_2_, sizes2_.id as id1_17_3_, sizes2_.created_at as created_2_17_3_, sizes2_.updated_at as updated_3_17_3_, sizes2_.height as height4_17_3_, sizes2_.length as length5_17_3_, sizes2_.media_id as media_i10_17_3_, sizes2_.media_type as media_ty6_17_3_, sizes2_.size as size7_17_3_, sizes2_.url as url8_17_3_, sizes2_.width as width9_17_3_, sponsoring3_.id as id2_13_4_, sponsoring3_.created_at as created_3_13_4_, sponsoring3_.updated_at as updated_4_13_4_, sponsoring3_.amount as amount5_13_4_, sponsoring3_.purpose_of_transfer as purpose_6_13_4_, sponsoring3_.purpose_of_transfer_code as purpose_7_13_4_, sponsoring3_.company as company8_13_4_, sponsoring3_.firstname as firstnam9_13_4_, sponsoring3_.lastname as lastnam10_13_4_, sponsoring3_.subject as subject11_13_4_, sponsoring3_.team_id as team_id12_13_4_, team4_.id as id1_27_5_, team4_.created_at as created_2_27_5_, team4_.updated_at as updated_3_27_5_, team4_.description as descript4_27_5_, team4_.event_id as event_id7_27_5_, team4_.has_started as has_star5_27_5_, team4_.name as name6_27_5_, team4_.profile_pic_id as profile_8_27_5_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team4_.id) as formula0_5_, sponsor5_.id as id2_32_6_, sponsor5_.created_at as created_3_32_6_, sponsor5_.updated_at as updated_4_32_6_, sponsor5_.account_id as account20_32_6_, sponsor5_.city as city5_32_6_, sponsor5_.country as country6_32_6_, sponsor5_.housenumber as housenum7_32_6_, sponsor5_.street as street8_32_6_, sponsor5_.zipcode as zipcode9_32_6_, sponsor5_.company as company17_32_6_, sponsor5_.is_hidden as is_hidd18_32_6_, sponsor5_.logo_id as logo_id22_32_6_, sponsor5_.value as value19_32_6_, useraccoun6_.id as id1_30_7_, useraccoun6_.created_at as created_2_30_7_, useraccoun6_.updated_at as updated_3_30_7_, useraccoun6_.activation_token as activati4_30_7_, useraccoun6_.email as email5_30_7_, useraccoun6_.firstname as firstnam6_30_7_, useraccoun6_.gender as gender7_30_7_, useraccoun6_.is_blocked as is_block8_30_7_, useraccoun6_.lastname as lastname9_30_7_, useraccoun6_.password_hash as passwor10_30_7_, useraccoun6_.preferred_language as preferr11_30_7_, useraccoun6_.profile_pic_id as profile12_30_7_, media7_.id as id1_16_8_, media7_.created_at as created_2_16_8_, media7_.updated_at as updated_3_16_8_, media7_.media_type as media_ty4_16_8_, team8_.id as id1_27_9_, team8_.created_at as created_2_27_9_, team8_.updated_at as updated_3_27_9_, team8_.description as descript4_27_9_, team8_.event_id as event_id7_27_9_, team8_.has_started as has_star5_27_9_, team8_.name as name6_27_9_, team8_.profile_pic_id as profile_8_27_9_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team8_.id) as formula0_9_, event9_.id as id1_7_10_, event9_.created_at as created_2_7_10_, event9_.updated_at as updated_3_7_10_, event9_.city as city4_7_10_, event9_.date as date5_7_10_, event9_.duration as duration6_7_10_, event9_.is_current as is_curre7_7_10_, event9_.latitude as latitude8_7_10_, event9_.longitude as longitud9_7_10_, event9_.title as title10_7_10_, media10_.id as id1_16_11_, media10_.created_at as created_2_16_11_, media10_.updated_at as updated_3_16_11_, media10_.media_type as media_ty4_16_11_, unregister11_.id as id1_29_12_, unregister11_.created_at as created_2_29_12_, unregister11_.updated_at as updated_3_29_12_, unregister11_.city as city4_29_12_, unregister11_.country as country5_29_12_, unregister11_.housenumber as housenum6_29_12_, unregister11_.street as street7_29_12_, unregister11_.zipcode as zipcode8_29_12_, unregister11_.company as company9_29_12_, unregister11_.firstname as firstna10_29_12_, unregister11_.gender as gender11_29_12_, unregister11_.is_hidden as is_hidd12_29_12_, unregister11_.lastname as lastnam13_29_12_, unregister11_.url as url14_29_12_, posting12_.id as id1_20_13_, posting12_.created_at as created_2_20_13_, posting12_.updated_at as updated_3_20_13_, posting12_.challenge_id as challeng6_20_13_, posting12_.date as date4_20_13_, posting12_.location_id as location7_20_13_, posting12_.text as text5_20_13_, posting12_.user_id as user_id8_20_13_, location13_.id as id1_14_14_, location13_.created_at as created_2_14_14_, location13_.updated_at as updated_3_14_14_, location13_.latitude as latitude4_14_14_, location13_.longitude as longitud5_14_14_, location13_.date as date6_14_14_, location13_.distance as distance7_14_14_, location13_.is_during_event as is_durin8_14_14_, location13_.team_id as team_id9_14_14_, location13_.uploader_id as uploade10_14_14_, useraccoun14_.id as id1_30_15_, useraccoun14_.created_at as created_2_30_15_, useraccoun14_.updated_at as updated_3_30_15_, useraccoun14_.activation_token as activati4_30_15_, useraccoun14_.email as email5_30_15_, useraccoun14_.firstname as firstnam6_30_15_, useraccoun14_.gender as gender7_30_15_, useraccoun14_.is_blocked as is_block8_30_15_, useraccoun14_.lastname as lastname9_30_15_, useraccoun14_.password_hash as passwor10_30_15_, useraccoun14_.preferred_language as preferr11_30_15_, useraccoun14_.profile_pic_id as profile12_30_15_ from challenge challenge0_ left outer join media media1_ on challenge0_.contract_id=media1_.id left outer join media_size sizes2_ on media1_.id=sizes2_.media_id left outer join invoice sponsoring3_ on challenge0_.invoice_id=sponsoring3_.id left outer join team team4_ on sponsoring3_.team_id=team4_.id left outer join user_role sponsor5_ on challenge0_.registered_sponsor_id=sponsor5_.id left outer join user_account useraccoun6_ on sponsor5_.account_id=useraccoun6_.id left outer join media media7_ on sponsor5_.logo_id=media7_.id left outer join team team8_ on challenge0_.team_id=team8_.id left outer join event event9_ on team8_.event_id=event9_.id left outer join media media10_ on team8_.profile_pic_id=media10_.id left outer join unregistered_sponsor unregister11_ on challenge0_.unregistered_sponsor_id=unregister11_.id left outer join posting posting12_ on challenge0_.id=posting12_.challenge_id left outer join location location13_ on posting12_.location_id=location13_.id left outer join user_account useraccoun14_ on posting12_.user_id=useraccoun14_.id where challenge0_.id=?
select posting0_.id as id1_20_11_, posting0_.created_at as created_2_20_11_, posting0_.updated_at as updated_3_20_11_, posting0_.challenge_id as challeng6_20_11_, posting0_.date as date4_20_11_, posting0_.location_id as location7_20_11_, posting0_.text as text5_20_11_, posting0_.user_id as user_id8_20_11_, challenge1_.id as id1_1_0_, challenge1_.created_at as created_2_1_0_, challenge1_.updated_at as updated_3_1_0_, challenge1_.amount as amount4_1_0_, challenge1_.contract_id as contract7_1_0_, challenge1_.description as descript5_1_0_, challenge1_.invoice_id as invoice_8_1_0_, challenge1_.registered_sponsor_id as register9_1_0_, challenge1_.status as status6_1_0_, challenge1_.team_id as team_id10_1_0_, challenge1_.unregistered_sponsor_id as unregis11_1_0_, media2_.id as id1_16_1_, media2_.created_at as created_2_16_1_, media2_.updated_at as updated_3_16_1_, media2_.media_type as media_ty4_16_1_, sponsoring3_.id as id2_13_2_, sponsoring3_.created_at as created_3_13_2_, sponsoring3_.updated_at as updated_4_13_2_, sponsoring3_.amount as amount5_13_2_, sponsoring3_.purpose_of_transfer as purpose_6_13_2_, sponsoring3_.purpose_of_transfer_code as purpose_7_13_2_, sponsoring3_.company as company8_13_2_, sponsoring3_.firstname as firstnam9_13_2_, sponsoring3_.lastname as lastnam10_13_2_, sponsoring3_.subject as subject11_13_2_, sponsoring3_.team_id as team_id12_13_2_, sponsor4_.id as id2_32_3_, sponsor4_.created_at as created_3_32_3_, sponsor4_.updated_at as updated_4_32_3_, sponsor4_.account_id as account20_32_3_, sponsor4_.city as city5_32_3_, sponsor4_.country as country6_32_3_, sponsor4_.housenumber as housenum7_32_3_, sponsor4_.street as street8_32_3_, sponsor4_.zipcode as zipcode9_32_3_, sponsor4_.company as company17_32_3_, sponsor4_.is_hidden as is_hidd18_32_3_, sponsor4_.logo_id as logo_id22_32_3_, sponsor4_.value as value19_32_3_, team5_.id as id1_27_4_, team5_.created_at as created_2_27_4_, team5_.updated_at as updated_3_27_4_, team5_.description as descript4_27_4_, team5_.event_id as event_id7_27_4_, team5_.has_started as has_star5_27_4_, team5_.name as name6_27_4_, team5_.profile_pic_id as profile_8_27_4_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team5_.id) as formula0_4_, unregister6_.id as id1_29_5_, unregister6_.created_at as created_2_29_5_, unregister6_.updated_at as updated_3_29_5_, unregister6_.city as city4_29_5_, unregister6_.country as country5_29_5_, unregister6_.housenumber as housenum6_29_5_, unregister6_.street as street7_29_5_, unregister6_.zipcode as zipcode8_29_5_, unregister6_.company as company9_29_5_, unregister6_.firstname as firstna10_29_5_, unregister6_.gender as gender11_29_5_, unregister6_.is_hidden as is_hidd12_29_5_, unregister6_.lastname as lastnam13_29_5_, unregister6_.url as url14_29_5_, location7_.id as id1_14_6_, location7_.created_at as created_2_14_6_, location7_.updated_at as updated_3_14_6_, location7_.latitude as latitude4_14_6_, location7_.longitude as longitud5_14_6_, location7_.date as date6_14_6_, location7_.distance as distance7_14_6_, location7_.is_during_event as is_durin8_14_6_, location7_.team_id as team_id9_14_6_, location7_.uploader_id as uploade10_14_6_, locationda8_.location_id as location1_15_13_, locationda8_.location_data_value as location2_15_13_, locationda8_.location_data_key as location3_13_, team9_.id as id1_27_7_, team9_.created_at as created_2_27_7_, team9_.updated_at as updated_3_27_7_, team9_.description as descript4_27_7_, team9_.event_id as event_id7_27_7_, team9_.has_started as has_star5_27_7_, team9_.name as name6_27_7_, team9_.profile_pic_id as profile_8_27_7_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team9_.id) as formula0_7_, participan10_.id as id2_32_8_, participan10_.created_at as created_3_32_8_, participan10_.updated_at as updated_4_32_8_, participan10_.account_id as account20_32_8_, participan10_.birthdate as birthda13_32_8_, participan10_.current_team_id as current21_32_8_, participan10_.emergencynumber as emergen14_32_8_, participan10_.hometown as hometow15_32_8_, participan10_.phonenumber as phonenu10_32_8_, participan10_.tshirtsize as tshirts16_32_8_, useraccoun11_.id as id1_30_9_, useraccoun11_.created_at as created_2_30_9_, useraccoun11_.updated_at as updated_3_30_9_, useraccoun11_.activation_token as activati4_30_9_, useraccoun11_.email as email5_30_9_, useraccoun11_.firstname as firstnam6_30_9_, useraccoun11_.gender as gender7_30_9_, useraccoun11_.is_blocked as is_block8_30_9_, useraccoun11_.lastname as lastname9_30_9_, useraccoun11_.password_hash as passwor10_30_9_, useraccoun11_.preferred_language as preferr11_30_9_, useraccoun11_.profile_pic_id as profile12_30_9_, media12_.id as id1_16_10_, media12_.created_at as created_2_16_10_, media12_.updated_at as updated_3_16_10_, media12_.media_type as media_ty4_16_10_ from posting posting0_ left outer join challenge challenge1_ on posting0_.challenge_id=challenge1_.id left outer join media media2_ on challenge1_.contract_id=media2_.id left outer join invoice sponsoring3_ on challenge1_.invoice_id=sponsoring3_.id left outer join user_role sponsor4_ on challenge1_.registered_sponsor_id=sponsor4_.id left outer join team team5_ on challenge1_.team_id=team5_.id left outer join unregistered_sponsor unregister6_ on challenge1_.unregistered_sponsor_id=unregister6_.id left outer join location location7_ on posting0_.location_id=location7_.id left outer join location_location_data locationda8_ on location7_.id=locationda8_.location_id left outer join team team9_ on location7_.team_id=team9_.id left outer join user_role participan10_ on location7_.uploader_id=participan10_.id left outer join user_account useraccoun11_ on posting0_.user_id=useraccoun11_.id left outer join media media12_ on useraccoun11_.profile_pic_id=media12_.id where posting0_.challenge_id=?
select teamentryf0_.id as id2_13_3_, teamentryf0_.created_at as created_3_13_3_, teamentryf0_.updated_at as updated_4_13_3_, teamentryf0_.amount as amount5_13_3_, teamentryf0_.purpose_of_transfer as purpose_6_13_3_, teamentryf0_.purpose_of_transfer_code as purpose_7_13_3_, teamentryf0_.team_id as team_id12_13_3_, team1_.id as id1_27_0_, team1_.created_at as created_2_27_0_, team1_.updated_at as updated_3_27_0_, team1_.description as descript4_27_0_, team1_.event_id as event_id7_27_0_, team1_.has_started as has_star5_27_0_, team1_.name as name6_27_0_, team1_.profile_pic_id as profile_8_27_0_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team1_.id) as formula0_0_, event2_.id as id1_7_1_, event2_.created_at as created_2_7_1_, event2_.updated_at as updated_3_7_1_, event2_.city as city4_7_1_, event2_.date as date5_7_1_, event2_.duration as duration6_7_1_, event2_.is_current as is_curre7_7_1_, event2_.latitude as latitude8_7_1_, event2_.longitude as longitud9_7_1_, event2_.title as title10_7_1_, media3_.id as id1_16_2_, media3_.created_at as created_2_16_2_, media3_.updated_at as updated_3_16_2_, media3_.media_type as media_ty4_16_2_ from invoice teamentryf0_ left outer join team team1_ on teamentryf0_.team_id=team1_.id left outer join event event2_ on team1_.event_id=event2_.id left outer join media media3_ on team1_.profile_pic_id=media3_.id where teamentryf0_.team_id=? and teamentryf0_.dtype='TeamEntryFeeInvoice'
select media0_.id as id1_16_0_, media0_.created_at as created_2_16_0_, media0_.updated_at as updated_3_16_0_, media0_.media_type as media_ty4_16_0_, sizes1_.media_id as media_i10_17_1_, sizes1_.id as id1_17_1_, sizes1_.id as id1_17_2_, sizes1_.created_at as created_2_17_2_, sizes1_.updated_at as updated_3_17_2_, sizes1_.height as height4_17_2_, sizes1_.length as length5_17_2_, sizes1_.media_id as media_i10_17_2_, sizes1_.media_type as media_ty6_17_2_, sizes1_.size as size7_17_2_, sizes1_.url as url8_17_2_, sizes1_.width as width9_17_2_ from media media0_ left outer join media_size sizes1_ on media0_.id=sizes1_.media_id where media0_.id=?
select posting0_.id as id1_20_11_, posting0_.created_at as created_2_20_11_, posting0_.updated_at as updated_3_20_11_, posting0_.challenge_id as challeng6_20_11_, posting0_.date as date4_20_11_, posting0_.location_id as location7_20_11_, posting0_.text as text5_20_11_, posting0_.user_id as user_id8_20_11_, challenge1_.id as id1_1_0_, challenge1_.created_at as created_2_1_0_, challenge1_.updated_at as updated_3_1_0_, challenge1_.amount as amount4_1_0_, challenge1_.contract_id as contract7_1_0_, challenge1_.description as descript5_1_0_, challenge1_.invoice_id as invoice_8_1_0_, challenge1_.registered_sponsor_id as register9_1_0_, challenge1_.status as status6_1_0_, challenge1_.team_id as team_id10_1_0_, challenge1_.unregistered_sponsor_id as unregis11_1_0_, media2_.id as id1_16_1_, media2_.created_at as created_2_16_1_, media2_.updated_at as updated_3_16_1_, media2_.media_type as media_ty4_16_1_, sponsoring3_.id as id2_13_2_, sponsoring3_.created_at as created_3_13_2_, sponsoring3_.updated_at as updated_4_13_2_, sponsoring3_.amount as amount5_13_2_, sponsoring3_.purpose_of_transfer as purpose_6_13_2_, sponsoring3_.purpose_of_transfer_code as purpose_7_13_2_, sponsoring3_.company as company8_13_2_, sponsoring3_.firstname as firstnam9_13_2_, sponsoring3_.lastname as lastnam10_13_2_, sponsoring3_.subject as subject11_13_2_, sponsoring3_.team_id as team_id12_13_2_, sponsor4_.id as id2_32_3_, sponsor4_.created_at as created_3_32_3_, sponsor4_.updated_at as updated_4_32_3_, sponsor4_.account_id as account20_32_3_, sponsor4_.city as city5_32_3_, sponsor4_.country as country6_32_3_, sponsor4_.housenumber as housenum7_32_3_, sponsor4_.street as street8_32_3_, sponsor4_.zipcode as zipcode9_32_3_, sponsor4_.company as company17_32_3_, sponsor4_.is_hidden as is_hidd18_32_3_, sponsor4_.logo_id as logo_id22_32_3_, sponsor4_.value as value19_32_3_, team5_.id as id1_27_4_, team5_.created_at as created_2_27_4_, team5_.updated_at as updated_3_27_4_, team5_.description as descript4_27_4_, team5_.event_id as event_id7_27_4_, team5_.has_started as has_star5_27_4_, team5_.name as name6_27_4_, team5_.profile_pic_id as profile_8_27_4_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team5_.id) as formula0_4_, unregister6_.id as id1_29_5_, unregister6_.created_at as created_2_29_5_, unregister6_.updated_at as updated_3_29_5_, unregister6_.city as city4_29_5_, unregister6_.country as country5_29_5_, unregister6_.housenumber as housenum6_29_5_, unregister6_.street as street7_29_5_, unregister6_.zipcode as zipcode8_29_5_, unregister6_.company as company9_29_5_, unregister6_.firstname as firstna10_29_5_, unregister6_.gender as gender11_29_5_, unregister6_.is_hidden as is_hidd12_29_5_, unregister6_.lastname as lastnam13_29_5_, unregister6_.url as url14_29_5_, location7_.id as id1_14_6_, location7_.created_at as created_2_14_6_, location7_.updated_at as updated_3_14_6_, location7_.latitude as latitude4_14_6_, location7_.longitude as longitud5_14_6_, location7_.date as date6_14_6_, location7_.distance as distance7_14_6_, location7_.is_during_event as is_durin8_14_6_, location7_.team_id as team_id9_14_6_, location7_.uploader_id as uploade10_14_6_, locationda8_.location_id as location1_15_13_, locationda8_.location_data_value as location2_15_13_, locationda8_.location_data_key as location3_13_, team9_.id as id1_27_7_, team9_.created_at as created_2_27_7_, team9_.updated_at as updated_3_27_7_, team9_.description as descript4_27_7_, team9_.event_id as event_id7_27_7_, team9_.has_started as has_star5_27_7_, team9_.name as name6_27_7_, team9_.profile_pic_id as profile_8_27_7_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team9_.id) as formula0_7_, participan10_.id as id2_32_8_, participan10_.created_at as created_3_32_8_, participan10_.updated_at as updated_4_32_8_, participan10_.account_id as account20_32_8_, participan10_.birthdate as birthda13_32_8_, participan10_.current_team_id as current21_32_8_, participan10_.emergencynumber as emergen14_32_8_, participan10_.hometown as hometow15_32_8_, participan10_.phonenumber as phonenu10_32_8_, participan10_.tshirtsize as tshirts16_32_8_, useraccoun11_.id as id1_30_9_, useraccoun11_.created_at as created_2_30_9_, useraccoun11_.updated_at as updated_3_30_9_, useraccoun11_.activation_token as activati4_30_9_, useraccoun11_.email as email5_30_9_, useraccoun11_.firstname as firstnam6_30_9_, useraccoun11_.gender as gender7_30_9_, useraccoun11_.is_blocked as is_block8_30_9_, useraccoun11_.lastname as lastname9_30_9_, useraccoun11_.password_hash as passwor10_30_9_, useraccoun11_.preferred_language as preferr11_30_9_, useraccoun11_.profile_pic_id as profile12_30_9_, media12_.id as id1_16_10_, media12_.created_at as created_2_16_10_, media12_.updated_at as updated_3_16_10_, media12_.media_type as media_ty4_16_10_ from posting posting0_ left outer join challenge challenge1_ on posting0_.challenge_id=challenge1_.id left outer join media media2_ on challenge1_.contract_id=media2_.id left outer join invoice sponsoring3_ on challenge1_.invoice_id=sponsoring3_.id left outer join user_role sponsor4_ on challenge1_.registered_sponsor_id=sponsor4_.id left outer join team team5_ on challenge1_.team_id=team5_.id left outer join unregistered_sponsor unregister6_ on challenge1_.unregistered_sponsor_id=unregister6_.id left outer join location location7_ on posting0_.location_id=location7_.id left outer join location_location_data locationda8_ on location7_.id=locationda8_.location_id left outer join team team9_ on location7_.team_id=team9_.id left outer join user_role participan10_ on location7_.uploader_id=participan10_.id left outer join user_account useraccoun11_ on posting0_.user_id=useraccoun11_.id left outer join media media12_ on useraccoun11_.profile_pic_id=media12_.id where posting0_.location_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select hashtags0_.posting_id as posting_1_22_0_, hashtags0_.value as value2_22_0_ from posting_hashtags hashtags0_ where hashtags0_.posting_id=?
select media0_.posting_id as posting_1_24_0_, media0_.media_id as media_id2_24_0_, media1_.id as id1_16_1_, media1_.created_at as created_2_16_1_, media1_.updated_at as updated_3_16_1_, media1_.media_type as media_ty4_16_1_ from posting_media media0_ inner join media media1_ on media0_.media_id=media1_.id where media0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select comments0_.posting_id as posting_1_21_0_, comments0_.comments_id as comments2_21_0_, comment1_.id as id1_2_1_, comment1_.created_at as created_2_2_1_, comment1_.updated_at as updated_3_2_1_, comment1_.date as date4_2_1_, comment1_.text as text5_2_1_, comment1_.user_id as user_id6_2_1_, useraccoun2_.id as id1_30_2_, useraccoun2_.created_at as created_2_30_2_, useraccoun2_.updated_at as updated_3_30_2_, useraccoun2_.activation_token as activati4_30_2_, useraccoun2_.email as email5_30_2_, useraccoun2_.firstname as firstnam6_30_2_, useraccoun2_.gender as gender7_30_2_, useraccoun2_.is_blocked as is_block8_30_2_, useraccoun2_.lastname as lastname9_30_2_, useraccoun2_.password_hash as passwor10_30_2_, useraccoun2_.preferred_language as preferr11_30_2_, useraccoun2_.profile_pic_id as profile12_30_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_ from posting_comments comments0_ inner join comment comment1_ on comments0_.comments_id=comment1_.id left outer join user_account useraccoun2_ on comment1_.user_id=useraccoun2_.id left outer join media media3_ on useraccoun2_.profile_pic_id=media3_.id where comments0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select userroles0_.user_account_id as user_acc1_31_0_, userroles0_.user_roles_id as user_rol2_31_0_, userroles0_.user_roles_key as user_rol3_0_, userrole1_.id as id2_32_1_, userrole1_.created_at as created_3_32_1_, userrole1_.updated_at as updated_4_32_1_, userrole1_.account_id as account20_32_1_, userrole1_.city as city5_32_1_, userrole1_.country as country6_32_1_, userrole1_.housenumber as housenum7_32_1_, userrole1_.street as street8_32_1_, userrole1_.zipcode as zipcode9_32_1_, userrole1_.phonenumber as phonenu10_32_1_, userrole1_.title as title11_32_1_, userrole1_.emp_tshirtsize as emp_tsh12_32_1_, userrole1_.birthdate as birthda13_32_1_, userrole1_.current_team_id as current21_32_1_, userrole1_.emergencynumber as emergen14_32_1_, userrole1_.hometown as hometow15_32_1_, userrole1_.tshirtsize as tshirts16_32_1_, userrole1_.company as company17_32_1_, userrole1_.is_hidden as is_hidd18_32_1_, userrole1_.logo_id as logo_id22_32_1_, userrole1_.value as value19_32_1_, userrole1_.role_name as role_nam1_32_1_, useraccoun2_.id as id1_30_2_, useraccoun2_.created_at as created_2_30_2_, useraccoun2_.updated_at as updated_3_30_2_, useraccoun2_.activation_token as activati4_30_2_, useraccoun2_.email as email5_30_2_, useraccoun2_.firstname as firstnam6_30_2_, useraccoun2_.gender as gender7_30_2_, useraccoun2_.is_blocked as is_block8_30_2_, useraccoun2_.lastname as lastname9_30_2_, useraccoun2_.password_hash as passwor10_30_2_, useraccoun2_.preferred_language as preferr11_30_2_, useraccoun2_.profile_pic_id as profile12_30_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_, team4_.id as id1_27_4_, team4_.created_at as created_2_27_4_, team4_.updated_at as updated_3_27_4_, team4_.description as descript4_27_4_, team4_.event_id as event_id7_27_4_, team4_.has_started as has_star5_27_4_, team4_.name as name6_27_4_, team4_.profile_pic_id as profile_8_27_4_, (select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = team4_.id) as formula0_4_, event5_.id as id1_7_5_, event5_.created_at as created_2_7_5_, event5_.updated_at as updated_3_7_5_, event5_.city as city4_7_5_, event5_.date as date5_7_5_, event5_.duration as duration6_7_5_, event5_.is_current as is_curre7_7_5_, event5_.latitude as latitude8_7_5_, event5_.longitude as longitud9_7_5_, event5_.title as title10_7_5_, media6_.id as id1_16_6_, media6_.created_at as created_2_16_6_, media6_.updated_at as updated_3_16_6_, media6_.media_type as media_ty4_16_6_, teamentryf7_.id as id2_13_7_, teamentryf7_.created_at as created_3_13_7_, teamentryf7_.updated_at as updated_4_13_7_, teamentryf7_.amount as amount5_13_7_, teamentryf7_.purpose_of_transfer as purpose_6_13_7_, teamentryf7_.purpose_of_transfer_code as purpose_7_13_7_, teamentryf7_.team_id as team_id12_13_7_, media8_.id as id1_16_8_, media8_.created_at as created_2_16_8_, media8_.updated_at as updated_3_16_8_, media8_.media_type as media_ty4_16_8_ from user_account_user_roles userroles0_ inner join user_role userrole1_ on userroles0_.user_roles_id=userrole1_.id left outer join user_account useraccoun2_ on userrole1_.account_id=useraccoun2_.id left outer join media media3_ on useraccoun2_.profile_pic_id=media3_.id left outer join team team4_ on userrole1_.current_team_id=team4_.id left outer join event event5_ on team4_.event_id=event5_.id left outer join media media6_ on team4_.profile_pic_id=media6_.id left outer join invoice teamentryf7_ on team4_.id=teamentryf7_.team_id and teamentryf7_.dtype='TeamEntryFeeInvoice' left outer join media media8_ on userrole1_.logo_id=media8_.id where userroles0_.user_account_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?
select likes0_.posting_id as posting_1_23_0_, likes0_.like_id as like_id2_23_0_, like1_.id as id1_25_1_, like1_.created_at as created_2_25_1_, like1_.updated_at as updated_3_25_1_, like1_.date as date4_25_1_, like1_.user_id as user_id5_25_1_, useraccoun2_.id as id1_30_2_, useraccoun2_.created_at as created_2_30_2_, useraccoun2_.updated_at as updated_3_30_2_, useraccoun2_.activation_token as activati4_30_2_, useraccoun2_.email as email5_30_2_, useraccoun2_.firstname as firstnam6_30_2_, useraccoun2_.gender as gender7_30_2_, useraccoun2_.is_blocked as is_block8_30_2_, useraccoun2_.lastname as lastname9_30_2_, useraccoun2_.password_hash as passwor10_30_2_, useraccoun2_.preferred_language as preferr11_30_2_, useraccoun2_.profile_pic_id as profile12_30_2_, media3_.id as id1_16_3_, media3_.created_at as created_2_16_3_, media3_.updated_at as updated_3_16_3_, media3_.media_type as media_ty4_16_3_ from posting_likes likes0_ inner join postinglike like1_ on likes0_.like_id=like1_.id left outer join user_account useraccoun2_ on like1_.user_id=useraccoun2_.id left outer join media media3_ on useraccoun2_.profile_pic_id=media3_.id where likes0_.posting_id=?
select sizes0_.media_id as media_i10_17_0_, sizes0_.id as id1_17_0_, sizes0_.id as id1_17_1_, sizes0_.created_at as created_2_17_1_, sizes0_.updated_at as updated_3_17_1_, sizes0_.height as height4_17_1_, sizes0_.length as length5_17_1_, sizes0_.media_id as media_i10_17_1_, sizes0_.media_type as media_ty6_17_1_, sizes0_.size as size7_17_1_, sizes0_.url as url8_17_1_, sizes0_.width as width9_17_1_ from media_size sizes0_ where sizes0_.media_id=?

-- 25 select
-- 703 as
-- 65 joins
