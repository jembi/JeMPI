Mock Keycloak Server
---------------------

**Please note: The mock server requires docker to run**

This mock server mock keycloak, running its in memeory database (`KEYCLOAK_DATABASE_VENDOR=dev-mem`)
By default it adds the user `user1`, with the password `user1`, but you can add additional user by passing a 
user config file (example of this is below).
By default it uses the config file `./keycloakUsers.json` found in this directory

**Usage: **

`npx ts-node ./MockKeyCloak.ts [pathToConfig]`

And example of this file is below 

```json 
[
    {
        "username": "user1",
        "password": "user1"
    },
    {
        "username": "user2",
        "password": "user3"
    }
]
```

