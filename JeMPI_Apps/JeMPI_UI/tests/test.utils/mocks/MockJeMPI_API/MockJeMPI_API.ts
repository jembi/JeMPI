import http from 'http';
import moxios from '../../../../src/services/mockBackend'
import path from 'path'
import fs from 'fs'

type methods = 'POST' | 'GET' | 'PUT' | string
export type IMockEndpointConfig = { [methodName:string] : {
  [K in methods]: {
    status?:number,
    headers?: any,
    func: (req:any) => any
  }
}}

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

const config:IMockEndpointConfig = require(fullPath).default

const server = http.createServer(async (req:any, res:any) => {
    try {
       console.log(req.url)
       let response:any =null
       if (req.url in config){
        if (req.method in config[req.url]){
          const responseToUse = config[req.url][req.method]
          response = {
            status: responseToUse.status || 200,
            headers: responseToUse.headers || {},
            data: responseToUse.func(req)
          }
        }
       }

       if (!response){
          response = await moxios({
            method: req.method,
            url: req.url,
            headers: req.headers,
            data: req.method === 'POST' || req.method === 'PUT' ? req.body : null,
          });
       }
        
        res.writeHead(response.status, response.headers);
        res.end(JSON.stringify(response.data));
      } catch (error:any) {
        res.writeHead(500, {'Content-Type': 'text/json'});
        res.end(JSON.stringify({ error: 'Internal Server Error'+error.message }));
      }
});

const PORT = 5000;

server.listen(PORT, () => {
  console.log(`Proxy server is running on port ${PORT}`);
});

