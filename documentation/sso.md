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

### Setup Local JeMPI

1. Clone the JeMPI git repository
```bash
git clone https://github.com/jembi/JeMPI.git && cd JeMPI/
```

2. Update local config to use Keycloak
```bash
export REACT_APP_JEMPI_BASE_API_PORT=50001
export REACT_APP_ENABLE_SSO="true"
```

3. Execute the local-deployment script
```bash
cd devops/linux/docker/deployment
./local-deployment.sh
```

![Deployment Script Options](.gitbook/assets/13)

4. Select Option 1: Deploy JeMPI (For Fresh Start)
6. Access : http://localhost:3000/login

![JeMPI Web Keycloak Sign in](.gitbook/assets/16)

7. Sign in with Keycloak user credentials

![JeMPI Web Keycloak Sign in](.gitbook/assets/17)
