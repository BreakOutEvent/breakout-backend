alter table posting add COLUMN `team_id` bigint(20) DEFAULT NULL;
alter table posting add KEY `FK8cbm596j5qvj2eve2j0j0kr9d` (`team_id`);
alter table posting add CONSTRAINT `FK8cbm596j5qvj2eve2j0j0kr9d` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`);

update posting up

inner join (
select p.id as posting_id, t.id as team_id from team t

join team_members tm
on tm.teams_id = t.id

join user_role r
on r.id = tm.members_id

join user_account a
on r.account_id = a.id

join posting p
on p.user_id = a.id

where t.event_id = 1 or t.event_id = 2
and r.role_name = 'PARTICIPANT'
and p.created_at < '2017-01-01') as ij

on ij.posting_id = up.id

set up.team_id = ij.team_id;
