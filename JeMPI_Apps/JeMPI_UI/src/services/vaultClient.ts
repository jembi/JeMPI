import axios from 'axios'

const getSecretFromVault = async () => {
  const VAULT_ENDPOINT = process.env.REACT_APP_VAULT_ENDPOINT
  const VAULT_TOKEN = process.env.REACT_APP_VAULT_DEV_TOKEN
  console.log({ VAULT_ENDPOINT })
  console.log({ VAULT_TOKEN })

  try {
    const response = await axios.get(
      `${VAULT_ENDPOINT}/v2/secret/data/my-secret`,
      {
        headers: {
          'X-Vault-Token': VAULT_TOKEN
        }
      }
    )
    return response.data.data
  } catch (error) {
    console.error('Error fetching secret from Vault:', error)
    return null
  }
}

export default getSecretFromVault
