#!/bin/bash

# Variables
VAULT_ADDR="http://127.0.0.1:8200"  # Address of your Vault instance
VAULT_TOKEN="root"  # Vault token with write access
CERT_PATH="/etc/haproxy/certs"  # Directory to store certificates
CERT_FILE="$CERT_PATH/certificate.pem"  # File for the SSL certificate
KEY_FILE="$CERT_PATH/key.pem"  # File for the SSL key

# Check for sudo privileges
if [[ $EUID -ne 0 ]]; then
  echo "This script requires sudo privileges. Run it with 'sudo'."
  exit 1
fi

# Ensure the certificate directory exists
sudo mkdir -p "$CERT_PATH"

# Generate a self-signed certificate and key
echo "Generating self-signed certificate and key..."
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "$KEY_FILE" \
    -out "$CERT_FILE" \
    -subj "/CN=haproxy/O=YourOrganization/C=US"

# Check if Vault is installed
if ! command -v vault &> /dev/null; then
  echo "Vault is not installed. Please install it before running this script."
  exit 1
fi

# Store certificate and key in Vault
echo "Storing certificate and key in Vault..."
vault kv put -address="$VAULT_ADDR" -token="$VAULT_TOKEN" secret/haproxy/cert cert=@$CERT_FILE
vault kv put -address="$VAULT_ADDR" -token="$VAULT_TOKEN" secret/haproxy/key key=@$KEY_FILE

# Retrieve certificate from Vault
echo "Retrieving certificate from Vault..."
vault kv get -field=cert -address="$VAULT_ADDR" -token="$VAULT_TOKEN" secret/haproxy/cert > "$CERT_FILE"

echo "Retrieving key from Vault..."
vault kv get -field=key -address="$VAULT_ADDR" -token="$VAULT_TOKEN" secret/haproxy/key > "$KEY_FILE"

# Start HAProxy container (check if the service exists)
echo "Starting HAProxy container..."
if docker service ls | grep -q "haproxy"; then
  sudo docker service update --force haproxy
else
  echo "HAProxy service not found. Ensure it's correctly named and deployed."
  exit 1
fi

echo "Script execution completed."