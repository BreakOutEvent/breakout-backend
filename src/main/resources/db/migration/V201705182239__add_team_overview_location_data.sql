CREATE TABLE team_overview_location_data
(
  team_overview_id         BIGINT       NOT NULL,
  last_location_data_value VARCHAR(255) NULL,
  last_location_data_key   VARCHAR(255) NOT NULL,
  PRIMARY KEY (team_overview_id, last_location_data_key),
  CONSTRAINT FKefrdkgpqo3whbvl8ta605mg0g
  FOREIGN KEY (team_overview_id) REFERENCES team_overview (id)
);

alter table team_overview add column location_timestamp datetime null;
