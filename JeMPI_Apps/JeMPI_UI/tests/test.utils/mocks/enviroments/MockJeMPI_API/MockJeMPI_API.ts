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

    constructor(extendingConfig:IMockEndpointConfig){
        this.server =this.CreateMockServer(extendingConfig)
        const parsedUrl = new URL(config.apiUrl);
        this.serverPort = parseInt(parsedUrl.port)
        this.serverBase = parsedUrl.pathname.replace(/^\/+|\/+$/g, '');
    }

    protected GetBaseHeaders(){
        const getCorsHeader = () =>{
            return {
                "access-control-allow-credentials": "true",
                "access-control-allow-headers": 'x-xsrf-token',
                "access-control-allow-methods": 'GET, POST, PUT, PATCH, POST, DELETE, OPTIONS',
                "access-control-allow-origin": 'http://localhost:8081'
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
            console.log(`Proxy server is running on port ${this.serverPort}`);
          });
    }
}

let fullPath:string = ""
if (process.argv.length > 2 ){
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
    fullPath = path.resolve(__dirname, "./MockJeMPI_API.config")
}

new MockJeMPIAPIServer(require(fullPath).default).StartServer()

// const config:IMockEndpointConfig = require(fullPath).default

// const server = http.createServer(async (req:any, res:any) => {
//     try {

//         if (req.method === "OPTIONS") {
//             res.writeHead(204, {
//                 'Access-Control-Allow-Credentials': true,
//                 'Access-Control-Allow-Origin': 'http://localhost:8081',
//                 'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, POST, DELETE, OPTIONS',
//                 'Access-Control-Max-Age': 2592000,
//                 'Access-Control-Allow-Headers': 'x-xsrf-token'
//             });
//             res.end();
//             return
//           }

//        console.log(req.url)
//        console.log(req)
//        let response:any =null
//        if (req.url in config){
//         if (req.method in config[req.url]){
//           const responseToUse = config[req.url][req.method]
//           response = {
//             status: responseToUse.status || 200,
//             headers: responseToUse.headers || {},
//             data: responseToUse.func(req)
//           }
//         }
//        }

//        if (!response){
//           response = await moxios({
//             method: req.method,
//             url: req.url,
//             headers: req.headers,
//             data: req.method === 'POST' || req.method === 'PUT' ? req.body : null,
//           });
//        }

       
//        res.writeHead(response.status, 
//                     {...response.headers,
//                         'Access-Control-Allow-Credentials': true,
//                         'Access-Control-Allow-Origin': 'http://localhost:8081',
//                         'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, POST, DELETE, OPTIONS',
//                         'Access-Control-Max-Age': 2592000,
//                         'Access-Control-Allow-Headers': 'x-xsrf-token'
//                     });

        
//         res.end(JSON.stringify(response.data));
//       } catch (error:any) {
//         res.writeHead(500, {'Content-Type': 'text/json',
//         'Access-Control-Allow-Credentials': true,
//         'Access-Control-Allow-Origin': 'http://localhost:8081',
//         'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, POST, DELETE, OPTIONS',
//         'Access-Control-Max-Age': 2592000,
//         'Access-Control-Allow-Headers': 'x-xsrf-token'});
//         res.end(JSON.stringify({ error: 'Internal Server Error'+error.message }));
//       }
// });

// const PORT = 50000;

// server.listen(PORT, () => {
//   console.log(`Proxy server is running on port ${PORT}`);
// });

