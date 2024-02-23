Mock JeMPI API Server
---------------------

This mock server mock the JEMPI API Server (found at JeMPI_Apps/JeMPI_API_KC)
It does this by allowing to pass server config file in which you specify the method you want mocked.
By default it uses the config file `./MockJeMPI_API.config.ts` found in this directory

**Usage: **

`npx ts-node ./MockJeMPI_API.ts [appPort] [pathToConfig]`

And example of this file is below 

```javascript 
// serverconfig.js (usage example: npx ts-node ./MockJeMPI_API.ts 3000 ./serverconfig.js)
const endpoints:IMockEndpointConfig = {
    './current-user': {
        GET:  (req:any) => { return {
            status: 403,
            data: {}
        }}
    },
    './config': {
        POST: (req:any) => { return {
            status: 200,
            data: {}
        }}
    }
}

export default endpoints
```

