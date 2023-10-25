import { Config } from 'config'
import Keycloak from 'keycloak-js'

export default function getKeycloak(config: Config) {
  const keycloak = new Keycloak({
    url: config.KeyCloakUrl,
    realm: config.KeyCloakRealm,
    clientId: config.KeyCloakClientId
  })
  return keycloak
}
