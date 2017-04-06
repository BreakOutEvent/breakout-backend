USE `${BREAKOUT}`;

-- A participant can be part of multiple teams (because he could have participated at previous events)
-- Therefore we need to support that by adding a new join table to show all teams a participant is / was part of
CREATE TABLE user_role_teams
(
  user_role_id BIGINT(20) NOT NULL,
  teams_id     BIGINT(20) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (user_role_id, teams_id),
  CONSTRAINT FK_rsido6q90b6x94n39bo0m9amh FOREIGN KEY (user_role_id) REFERENCES user_role (id),
  CONSTRAINT FK_70tpi5bqgk1j77daf9wp920j1 FOREIGN KEY (teams_id) REFERENCES team (id)
);

CREATE INDEX FK_70tpi5bqgk1j77daf9wp920j1
  ON user_role_teams (teams_id);

-- For all participants: Add the information about the current team to the table for all teams as well
INSERT INTO user_role_teams (SELECT
                               id,
                               current_team_id
                             FROM user_role
                             WHERE role_name = "PARTICIPANT"
                                   AND current_team_id IS NOT NULL);

