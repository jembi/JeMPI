import ROUTES from "../../../../../src/services/apiRoutes"
import { IMockEndpointConfig } from "./MockJeMPI_API"

export const SERVER_MEMORY:any = {
    loggedInUser: null
}

const endpoints:IMockEndpointConfig = {
    [ROUTES.CURRENT_USER]: {
        GET:  (req:any) => { 
           
            if (SERVER_MEMORY.loggedInUser){
                return {
                    status: 200,
                    data: SERVER_MEMORY.loggedInUser
                }
            }
            return {
                status: 403,
                data: {}
            }
        }
    },
    [ROUTES.LOGOUT]: {
        GET:  (req:any) => { 
            SERVER_MEMORY.loggedInUser = null
            return {
                status: 200,
                data: {}
            }
        }
    },
    [ROUTES.VALIDATE_OAUTH]: {
        POST: (req:any) => { 
            const reqBody = JSON.parse(req.body)

            if ("session_state" in reqBody && "code" in reqBody){
                SERVER_MEMORY["loggedInUser"] = {
                    id: 1,
                    email: "testUser@jempi.com",
                    username: "testUser",
                    familyName: "User",
                    givenName: "Test",
                    provider:  'keycloak'
                }
                return {
                    status: 200,
                    data: SERVER_MEMORY["loggedInUser"]
                }
            }
            else{
                return {
                    status: 403,
                    data: {}
                }
            }
            
        }
    }
}

export default endpoints