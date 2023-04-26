export interface User {
  id: number
  email: string
  username: string
  familyName: string
  givenName: string
  provider: 'local' | 'keycloak'
}

export type OAuthParams = {
  code: string
  state: string
  session_state: string
}
