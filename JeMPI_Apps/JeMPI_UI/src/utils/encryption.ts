import CryptoJS from 'crypto-js'
import getSecretFromVault from '../services/vaultClient'

const secrets = ''

getSecretFromVault()
  .then(data => {
    console.log(`data - ${data}`)
  })
  .catch(err => {
    console.log(err)
  })

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
