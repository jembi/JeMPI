import Vault from 'hashi-vault-js'
const VAULT_ENDPOINT = process.env.VAULT_ENDPOINT

export const vault = new Vault({
  https: true,
  baseUrl: VAULT_ENDPOINT,
  rootPath: 'secret',
  timeout: 5000,
  proxy: false
})
