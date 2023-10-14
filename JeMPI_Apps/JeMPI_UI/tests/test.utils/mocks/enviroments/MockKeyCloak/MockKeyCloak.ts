
import axios from 'axios'
import Docker from 'dockerode'
import path from 'path';
import fs from 'fs';
import { config } from '../../../../../src/config'

class MockKeyCloack {
    docker:Docker
    serviceName:string
    keyClockUrl:string
    keyClockUrlPort:string
    keyClockRealm:string
    keyClockClient:string
    addtionalUser:any[]

    constructor(userConfig:any[]=[]){
        const parsedUrl = new URL(config.KeyCloakUrl);

        this.keyClockUrl = config.KeyCloakUrl
        this.keyClockUrlPort = parsedUrl.port
        this.keyClockRealm = config.KeyCloakRealm
        this.keyClockClient = config.KeyCloakClientId

        this.docker = new Docker()
        this.serviceName = "JeMPIMockKeyCloak"
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
        return  [{username: "admin", password: "password"}, ...this.addtionalUser].map(v => {
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

    async CreateService(){
        const waitToLoad = (checkPromise:() => Promise<any>, waitFor: 'resolve' | 'reject') => {
            return new Promise( (resolve:any, reject:any) => {
                let time= 0
                const addTime = () =>{
                    if (time > 60000){
                        reject("Timeout starting keycloak")
                    }
                    time += 1000
                }
                
                const interval = setInterval(() => checkPromise().then(() => {
                    if (waitFor === 'resolve'){
                        clearInterval(interval)
                        resolve()
                    }else{
                        addTime()
                    }
                    
                }).catch(() => {
                    if (waitFor === 'reject'){
                        clearInterval(interval)
                        resolve()
                    }else{
                        addTime()
                    }
                }), 1000)
            })
        }

        const containers = await this.docker.listContainers()
        const targetContainerInfo = containers.find(container => container.Names.includes("/"+this.serviceName));
        if (targetContainerInfo){
            const targetContainer = await this.docker.getContainer(targetContainerInfo.Id)
            await targetContainer.stop()
            const stream = await targetContainer.attach({ stream: true, stdout: true, stderr: true })
            stream.pipe(process.stdout)
            await waitToLoad(() => this.docker.getContainer(targetContainerInfo.Id).inspect(), 'reject')
            

        }

        const container = await this.docker.createContainer(this.GetConfig())
        await container.start()
        const stream = await container.attach({ stream: true, stdout: true, stderr: true })
        stream.pipe(process.stdout)
        await waitToLoad(() => axios.get(this.keyClockUrl), 'resolve')
          

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