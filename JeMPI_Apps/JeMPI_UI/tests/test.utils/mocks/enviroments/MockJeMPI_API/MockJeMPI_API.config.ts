import ROUTES from "../../../../../src/services/apiRoutes"
import { IMockEndpointConfig } from "./MockJeMPI_API"

export const SERVER_MEMORY = {}

const endpoints:IMockEndpointConfig = {
    [ROUTES.CURRENT_USER]: {
        GET:  (req:any) => { return {
            status: 403,
            data: {}
        }}
    },
    [ROUTES.VALIDATE_OAUTH]: {
        POST: (req:any) => { return {
            status: 200,
            data: {}
        }}
    }
}

export default endpoints