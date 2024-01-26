#!/bin/bash

# Function to generate SSH key pair
generate_ssh_keys() {
    echo "Generating SSH keys..."
    ssh-keygen -t rsa -b 2048 -f ~/.ssh/id_rsa -q -N ""
}

# Function to copy public key to target machines
copy_public_key() {
    for machine in "${TARGET_MACHINES[@]}"; do
        echo "Copying public key to $machine..."
        ssh-copy-id $SSH_USERNAME@$machine
    done
}

# Function to SSH into the specified machine
ssh_to_machine() {
    echo "Enter the number corresponding to the machine you want to SSH into:"
    select machine in "${TARGET_MACHINES[@]}"; do
        if [[ -n "$machine" ]]; then
            echo "SSHing into $machine..."
            ssh -i ~/.ssh/id_rsa $SSH_USERNAME@$machine
            break
        else
            echo "Invalid selection. Please choose a valid machine."
        fi
    done
}

# Replace these values with your actual SSH key path, username, and target machine IPs
SSH_USERNAME="user"
TARGET_MACHINES=("target_machine_ip1" "target_machine_ip2" "target_machine_ip3")

# Generate SSH keys
generate_ssh_keys

# Copy public key to target machines
copy_public_key

# SSH into the specified machine
ssh_to_machine
