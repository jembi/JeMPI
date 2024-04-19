import CryptoJS from 'crypto-js'
import { vault } from '../services/vaultClient'
const token = process.env.VAULT_DEV_TOKEN as string
const secrets = await vault.readKVSecret(token, 'jempi_api_secret')

export function encryptData(data: object): string {
  const ciphertext = CryptoJS.AES.encrypt(
    JSON.stringify(data),
    secrets
  ).toString()
  return ciphertext
}

export function decryptData(ciphertext: string): any {
  const bytes = CryptoJS.AES.decrypt(ciphertext, secrets)
  const decryptedData = JSON.parse(bytes.toString(CryptoJS.enc.Utf8))
  return decryptedData
}
