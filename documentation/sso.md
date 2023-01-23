---
description: Enable SSO using Keycloak
---

# Single Sign-On using Keycloak
We use KeyCloak for identity management. This provide us with a OpenID Connect (an extension to OAuth 2.0) compliant identity service that we can use to authenticate users. Much like what Google and Github provide to login to other apps. Keycloak will provide:
- The login user experience, including signing in page
- 2FA
- Password reset features, account management
- The ability to manage user permissions centrally, across applications
- Our applications will just consume the resulting ID token that is produced to authenticate users and to check the roles that they are assigned.

We currently support the Auth Code Flow : 
1. User access the JeMPI UI and clicks on "Sign-In with Keycloak".
2. User is redirected to Keycloak where he needs to submit his credentials.
3. User gets redirected back to the JeMPI UI along with the auth code parameters.
4. Auth code parameters are sent to the "POST /authenticate" JeMPI API endpoint.
5. JeMPI API sends the auth code to Keycloak along with the Client ID and Client Secret.
6. JeMPI gets token and verifies it, then parse the user infos (email, username, ...)
7. User is added to the Postgres Database if it's the first time he signs in.
8. JeMPI API creates a session and sends back the user object along with the session cookie.
9. User s redirected to the homepage.

## Local development

### Run Keycloak from platform
1. Checkout branch in platform : https://github.com/jembi/platform/pull/212
2. Clean the docker environment if needed : `docker service rm $(docker service ls -q) && docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q) && docker volume prune -f && docker config rm $(docker config ls -q)` (IMPORTANT, this will remove all docker images/containers/volumes/...) 
3. Run `./build-image.sh`
4. Run `mkdir /tmp/logs` if needed
5. Run `./get-cli.sh` to download the platform cli
6. Start Keycloak `./platform-linux init identity-access-manager-keycloak --only --dev --env-file=.env.local`
7. Access : http://localhost:9088/    (admin / dev_password_only)

### Run JeMPI Backend 
1. Checkout branch : https://github.com/jembi/JeMPI/pull/23
2. Install Build Utilities : https://app.gitbook.com/o/lTiMw1wKTVQEjepxV4ou/s/QSuKUyYfw2QNaNiZIQ1s/
3. Run `launch-local.sh` ("N" for swarm reset)
4. Connect the JeMPI API container to the platform network : `docker network connect instant_default JeMPI_jempi-api.1.<<CONTAINER_ID>>`

### Run JeMPI UI
1. Checkout branch : https://github.com/jembi/jempi-web/pull/30
2. Install npm modules if needed : `npm install`
3. Disable mocked API : `REACT_APP_MOCK_BACKEND=false`
4. Set proper env var for API URL : `REACT_APP_JEMPI_BASE_URL=http://localhost:50000/JeMPI`
4. Set proper env var for KeyCloak : `KC_FRONTEND_URL=http://localhost:9088`
5. Run `npm run start`
6. Access : http://localhost:3000/login

### Restart server after changes
1. Scale down : `docker service scale JeMPI_jempi-api=0`
2. Re-build : `cd ./JeMPI_Apps/JeMPI_API/ && ./build.sh`
3. Scale up : `docker service scale JeMPI_jempi-api=1`
4. Connect the JeMPI API container to the platform network : `docker network connect instant_default JeMPI_jempi-api.1.<<CONTAINER_ID>>`