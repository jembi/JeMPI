import Keycloak from 'keycloak-js'
import { config } from '../config'

const keycloak = new Keycloak({
  url: config.KeyCloakUrl,
  realm: config.KeyCloakRealm,
  clientId: config.KeyCloakClientId
})

export default keycloak
