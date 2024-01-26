#!/bin/bash

# Replace these values with your actual IP addresses
MANAGER_NODE_IP="<MANAGER_NODE_IP>"
WORKER_NODE_IPS=("<WORKER_NODE_1_IP>" "<WORKER_NODE_2_IP>" "<WORKER_NODE_3_IP>")

# Function to initialize Docker Swarm on the Manager Node
initialize_swarm() {
    docker swarm init --advertise-addr $MANAGER_NODE_IP
}

# Function to join Worker Nodes to the Swarm
join_workers_to_swarm() {
    # Get the join token
    JOIN_TOKEN=$(docker swarm join-token worker -q)

    # Join each worker node to the swarm
    for WORKER_IP in "${WORKER_NODE_IPS[@]}"; do
        docker swarm join --token $JOIN_TOKEN $MANAGER_NODE_IP:2377 --advertise-addr $WORKER_IP
    done
}

# Function to check Swarm status
check_swarm_status() {
    docker node ls
}

# Main execution
echo "Initializing Docker Swarm on the Manager Node..."
initialize_swarm

echo "Joining Worker Nodes to the Swarm..."
join_workers_to_swarm

echo "Checking Swarm Status..."
check_swarm_status
