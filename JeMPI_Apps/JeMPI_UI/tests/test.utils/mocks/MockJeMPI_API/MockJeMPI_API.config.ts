import ROUTES from "../../../../src/services/apiRoutes"
import { IMockEndpointConfig } from "./MockJeMPI_API"

export const SERVER_MEMORY = {}

const endpoints:IMockEndpointConfig = {
    [ROUTES.CURRENT_USER]: {
        GET: {
            func: () => {return {}}
        }
    },
    [ROUTES.VALIDATE_OAUTH]: {
        POST:  {
            func: (req:any) => { console.log(req)}
        }
    }
}

export default endpoints