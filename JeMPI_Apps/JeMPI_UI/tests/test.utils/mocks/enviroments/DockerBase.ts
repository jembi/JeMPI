import Docker from 'dockerode'

export abstract class DockerBase{

    protected docker:Docker
    protected serviceName:string

    constructor(){
        this.docker = new Docker()
        this.serviceName = this.GetServiceName()
    }

    abstract GetServiceName(): string;
    abstract GetConfig(): any;
    abstract HasLoadedFunc(): () => Promise<any>;

    GetEnvConfig(){
        return {
            isDev: process.env.NODE_ENV !== 'production',
            apiUrl: process.env.REACT_APP_JEMPI_BASE_API_HOST
              ? `${process.env.REACT_APP_JEMPI_BASE_API_HOST}:${process.env.REACT_APP_JEMPI_BASE_API_PORT}`
              : `${window.location.protocol}//${window.location.hostname}:${process.env.REACT_APP_JEMPI_BASE_API_PORT}`,
            shouldMockBackend: process.env.REACT_APP_MOCK_BACKEND === 'true',
            KeyCloakUrl: process.env.KC_FRONTEND_URL || 'http://localhost:9088',
            KeyCloakRealm: process.env.KC_REALM_NAME || 'platform-realm',
            KeyCloakClientId: process.env.KC_JEMPI_CLIENT_ID || 'jempi-oauth',
            useSso: process.env.REACT_APP_ENABLE_SSO === 'true',
            maxUploadCsvSize: +(
              process.env.REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES || 128
            ),
            showBrandLogo: process.env.REACT_APP_SHOW_BRAND_LOGO === 'true'
          } 
    }
    async Start(){
        await this.CreateService()
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
        await waitToLoad(this.HasLoadedFunc(), 'resolve')
          

    }
}