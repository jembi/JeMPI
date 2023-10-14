//@ts-nocheck
import http, { IncomingMessage, Server, ServerResponse } from 'http';
import { constants as httpsStatusCodes } from 'http2';
import path from 'path';
import fs from 'fs';
import moxios from '../../../../../src/services/mockBackend'
import { config } from '../../../../../src/config'

type methods = 'POST' | 'GET' | 'PUT' | string
export type IMockEndpointConfig = { [urlPath:string] : {
  [K in methods]: (req:any) =>  {
        status:number,
        headers?: any,
        data?:any,
        [key:string]:any
    }
}}

class MockJeMPIAPIServer {
    server:Server 
    serverPort:number
    serverBase:string
    appUrl:string

    constructor(extendingConfig:IMockEndpointConfig, appPort:number){
        this.server =this.CreateMockServer(extendingConfig)
        const parsedUrl = new URL(config.apiUrl);
        this.serverPort = parseInt(parsedUrl.port)
        this.appUrl = `http://localhost:${appPort}`
        this.serverBase = parsedUrl.pathname.replace(/^\/+|\/+$/g, '');
    }

    protected GetBaseHeaders(){
        const getCorsHeader = () =>{
            return {
                "access-control-allow-credentials": "true",
                "access-control-allow-headers": 'x-xsrf-token',
                "access-control-allow-methods": 'GET, POST, PUT, PATCH, POST, DELETE, OPTIONS',
                "access-control-allow-origin": this.appUrl
            }
        }

        const getMainHeaders = () => {
            return {
                'Content-Type': 'text/json'
            }
        }

        return {...getCorsHeader(), ...getMainHeaders()}
    }

    protected CreateMockServer(extendingConfig:IMockEndpointConfig){
        const getUpdatedUrl = (url?:string) => {
            return url ? url.replace(`/${this.serverBase}`, "") : ""
            
        }

        const getResponseFromEndpointConfig = async (req:IncomingMessage & {body:any} , url:string) => {
            console.log("-> Processing use endpoint config")
            let response:any =null
            if (url in extendingConfig){
             if (req.method && req.method in extendingConfig[url]){
               const responseToUse = await extendingConfig[url][req.method](req)
               response = {
                 status: responseToUse.status || 200,
                 headers: responseToUse.headers || {},
                 data: responseToUse.data || ""
               }
             }
            }

            return response
        }

        const getResponseFromMoxios = async (req:IncomingMessage & {body:any}, url:string) =>{
            console.log("-> Processing use moxios config")
            return await moxios({
                method: req.method,
                url,
                headers: req.headers,
                data: req.method === 'POST' || req.method === 'PUT' ? req.body : null,
              });
        }

        const sendResponse = (res:ServerResponse, response:any) => {
            res.writeHead(response.status, 
                {...response.headers,
                 ...this.GetBaseHeaders()
                });
            res.end(JSON.stringify(response.data));
        }

        return http.createServer(async (req:IncomingMessage | any , res:ServerResponse ) => {
            try{
                console.log(`Processing url '${req.url}'. Method '${req.method}'`)
                if (req.method === "OPTIONS"){
                    res.writeHead(httpsStatusCodes.HTTP_STATUS_NO_CONTENT, http.STATUS_CODES[httpsStatusCodes.HTTP_STATUS_NO_CONTENT], this.GetBaseHeaders())
                    res.end()
                    return
                }

                let body = ''
                req.on('data', (chunk:any) => {
                    body += chunk;
                });
            

                req.on('end', async () => {

                    req["body"] = body
                    const updatedUrl = getUpdatedUrl(req.url)
                    console.log(`-> Parsed url '${updatedUrl}'.`)
    
                    let response = await getResponseFromEndpointConfig(req, updatedUrl)
                    if (!response){
                        response = await getResponseFromMoxios(req, updatedUrl)
                    }
        
                    sendResponse(res, response)
                });
    
                
            }
            catch(err:any){
                res.writeHead(httpsStatusCodes.HTTP_STATUS_INTERNAL_SERVER_ERROR, http.STATUS_CODES[httpsStatusCodes.HTTP_STATUS_INTERNAL_SERVER_ERROR], this.GetBaseHeaders())
                res.end(JSON.stringify({ error: 'Internal Server Error', 
                                         message: err }));
            }
            
        })
    }

    StartServer(){
        this.server.listen(this.serverPort, () => {
            console.log(`Proxy server is running on port ${this.serverPort}. Accepting requesting from ${this.appUrl}`);
          });
    }
}


let fullPath:string = ""
let appPort:number

const loadConfigFile = () => {
    if(process.argv.length > 3 ){
        const potentialPath:string = process.argv[3]
        let potentialFullPath:string = path.isAbsolute(potentialPath) ? potentialPath : path.resolve(__dirname, potentialPath )
    
        if (!fs.existsSync(potentialFullPath)){
            console.error(`The file path ${potentialFullPath} does not exist`)
        }
        else{
            fullPath = potentialFullPath
        }
    }
    else{
        fullPath = path.resolve(__dirname, "./MockJeMPI_API.config")
    }
}
const startServer = () => {
    new MockJeMPIAPIServer(require(fullPath).default, appPort).StartServer()
}

if (process.argv.length > 2){
    appPort = parseInt(process.argv[2])
    loadConfigFile()
    startServer()
}
else{
    const readline = require('readline').createInterface({
        input: process.stdin,
        output: process.stdout
      });
      
    readline.question('What port is JeMPI app running on? ', port => {
        appPort = parseInt(port)
        readline.close();
        loadConfigFile()
        startServer()
    });
}