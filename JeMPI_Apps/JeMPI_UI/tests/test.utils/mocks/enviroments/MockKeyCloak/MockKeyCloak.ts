
import axios from 'axios'
import Docker from 'dockerode'
import path from 'path';
import fs from 'fs';
import { DockerBase } from '../DockerBase';

class MockKeyCloack extends DockerBase{

    keyClockUrl:string
    keyClockUrlPort:string
    keyClockRealm:string
    keyClockClient:string
    addtionalUser:any[]

    constructor(userConfig:any[]=[]){
        super()
        const config = this.GetEnvConfig();
        const parsedUrl = new URL(config.KeyCloakUrl);

        this.keyClockUrl = config.KeyCloakUrl
        this.keyClockUrlPort = parsedUrl.port
        this.keyClockRealm = config.KeyCloakRealm
        this.keyClockClient = config.KeyCloakClientId

        this.docker = new Docker()
        this.addtionalUser = userConfig    
    }

    GetConfig(){
        return {
            name: this.serviceName,
            Image: 'bitnami/keycloak:21.1.2',
            Env: ['KEYCLOAK_DATABASE_VENDOR=dev-mem'],
            ExposedPorts: {
                '8080/tcp': {},
                '8443/tcp': {}
            },
            HostConfig: {
                PortBindings: {
                    '8080/tcp': [{ HostPort: this.keyClockUrlPort }],
                    '8443/tcp': [{ HostPort: '8443' }]
                },
                AutoRemove: true 
            }
        }
    }

    GetUsers(){
        return  [{username: "admin", password: "admin"}, ...this.addtionalUser].map(v => {
            return {
                username: v.username,
                enabled: true,
                credentials: [
                  {
                    type: "password",
                    value: v.password,
                    temporary: false
                  }
                ]
              }
        })
    }

    async Start(){
        await this.CreateService()
        await this.AddRealms()
        console.log(`\n\nKeyClock Ready! Running on port ${this.keyClockUrlPort}\n\n`)
    }

    async AddRealms(){
        console.log("\nAdding Realms:\n\n")
        try {
            const accessTokenData:any = await axios.post(
              `${this.keyClockUrl}/realms/master/protocol/openid-connect/token`,
              {
                grant_type: "password",
                client_id: "admin-cli",
                username: "user",
                password: "bitnami",
              },
              {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
              }
            );

            const response = await axios.post(
                `${this.keyClockUrl}/admin/realms`,
                {
                    realm: this.keyClockRealm,
                    enabled: true,
                    clients: [
                      {
                        clientId: this.keyClockClient,
                        enabled: true,
                        publicClient: false,
                        directAccessGrantsEnabled: true,
                        redirectUris: ["*"],
                        webOrigins: ["*"],
                        defaultRoles: [
                          "user"
                        ]
                      }
                    ],
                    users: this.GetUsers()
                  },
                {
                  headers: {
                      "Content-Type": "application/json",
                        Authorization: `Bearer ${accessTokenData?.data?.access_token}`
                  },
                }
              );
        
            console.log(`Realm '${this.keyClockRealm}' created successfully.`);
            console.log(response.data);
          } catch (error:any) {
            console.error('Error creating realm:', error.response ? error.response.data : error.message);
          }
    }

    GetServiceName(): string {
        return "JeMPIMockKeyCloak"
    }

    HasLoadedFunc(): () => Promise<any> {
        return () => axios.get(this.keyClockUrl)
    }
    
}

let fullPath:string = ""
if(process.argv.length > 2 ){
    const potentialPath:string = process.argv[2]
    let potentialFullPath:string = path.isAbsolute(potentialPath) ? potentialPath : path.resolve(__dirname, potentialPath )

    if (!fs.existsSync(potentialFullPath)){
        console.error(`The file path ${potentialFullPath} does not exist`)
    }
    else{
        fullPath = potentialFullPath
    }
}
else{
    fullPath = path.resolve(__dirname, "./keycloakUsers.json")
}

new MockKeyCloack(require(fullPath)).Start()