import CryptoJS from 'crypto-js'
import dotenv from 'dotenv'

dotenv.config()

const ENCRYPTION_KEY = process.env.ENCRYPTION_KEY as string

export function encryptData(data: object): string {
  const ciphertext = CryptoJS.AES.encrypt(
    JSON.stringify(data),
    ENCRYPTION_KEY
  ).toString()
  return ciphertext
}

export function decryptData(ciphertext: string): any {
  const bytes = CryptoJS.AES.decrypt(ciphertext, ENCRYPTION_KEY)
  const decryptedData = JSON.parse(bytes.toString(CryptoJS.enc.Utf8))
  return decryptedData
}
