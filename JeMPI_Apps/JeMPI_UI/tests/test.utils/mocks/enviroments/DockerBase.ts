import Docker from 'dockerode'
import { getTestEnvConfig } from './Utils'

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
        return getTestEnvConfig()
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