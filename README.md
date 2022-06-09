
![BreakOut-Logo](https://static.break-out.org/breakout-logo.png "BreakOut")


![main workflow](https://github.com/BreakOutEvent/breakout-backend/actions/workflows/main.yml/badge.svg)

# breakout-backend

This is the backend application for BreakOut. 

# What is BreakOut?
With BreakOut we challenge you to travel as far away as possible in 36 hours without spending any money, starting from Munich. During this time each team collects money for the DAFI-program to give refugees the opportunity of higher education.
More information at http://www.break-out.org

# Local Setup

- Make sure to use Java JDK 8
- Make sure to have installed mysql client

- Create Mariadb with docker 
```
docker run --name mariadb -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mySecretPw -e MYSQL_DATABASE=breakout -d mariadb
```

 - Run Backend
```
SPRING_PROFILES_ACTIVE=localdev ./gradlew bootRun
```

- Add client credentials
```
mysql -h 127.0.0.1 -u root -pmySecretPw breakout -e "INSERT INTO breakout.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('breakout_app', 'BREAKOUT_BACKEND', '123456789', 'read,write', 'password,refresh_token', '', 'USER', null, null, '{}', ''); INSERT INTO breakout.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('client_app', 'BREAKOUT_BACKEND', '123456789', 'read,write', 'password,refresh_token', '', 'USER', null, null, '{}', '');"
```

- Register User in Frontend
- (Emails will not be sent in default configuration, content will be logged in console)
- Make User Admin
```
mysql -h 127.0.0.1 -u root -pmySecretPw breakout -e "INSERT INTO breakout.user_role (role_name, id, account_id) VALUES ('ADMIN', 1, 1); INSERT INTO breakout.user_account_user_roles (user_account_id, user_roles_id, user_roles_key) VALUES (1, 1, 'backend.model.user.Admin');"
```

- Logout then Login again in Frontend


# Test the backend
The newest dev build of this application is always deployed to http://breakout-development.herokuapp.com

In the future a stable master version will be deployed on Heroku as well.

# API
The API documentation is done automatically using [swagger](http://swagger.io) and [springfox](https://github.com/springfox/springfox) and is shown under ["/swagger-ui.html"](http://breakout-development.herokuapp.com) of the corresponding deployed version

# What about Apps
If you are interested in the other stuff we're building, feel free to visit:
* Android: https://github.com/BreakOutEvent/breakout-android
* iOS: https://github.com/BreakOutEvent/breakout-ios
* Web: https://github.com/BreakOutEvent/breakout-frontend
* More: https://github.com/BreakOutEvent/

# License
breakout-backend. The backend application for BreakOut

Copyright (C) 2015-2016 Florian Schmidt & Philipp Piwowarsky

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see http://www.gnu.org/licenses/
