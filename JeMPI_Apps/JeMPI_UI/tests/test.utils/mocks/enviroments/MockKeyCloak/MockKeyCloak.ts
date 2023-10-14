
import axios from 'axios'
import Docker from 'dockerode'

class MockKeyCloack {
    docker:Docker
    serviceName:string

    constructor(){
        this.docker = new Docker()
        this.serviceName = "JeMPIMockKeyCloak"
    
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
                    '8080/tcp': [{ HostPort: '8080' }],
                    '8443/tcp': [{ HostPort: '8443' }]
                },
                AutoRemove: true 
            }
        }
    }

    async Start(){
        await this.CreateService()
        await this.AddRealms()
    }

    async AddRealms(){
        console.log("\nAdding Realms:\n\n")
        try {
            const accessTokenData:any = await axios.post(
              `http://localhost:8080/realms/master/protocol/openid-connect/token`,
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
                `http://localhost:8080/admin/realms`,
                {
                    realm: "realmName",
                    enabled: true,
                },
                {
                  headers: {
                      "Content-Type": "application/json",
                        Authorization: `Bearer ${accessTokenData?.data?.access_token}`
                  },
                }
              );
        
            console.log(`Realm "" created successfully.`);
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
        await waitToLoad(() => axios.get('http://localhost:8080'), 'resolve')
          

    }
}

new MockKeyCloack().Start()