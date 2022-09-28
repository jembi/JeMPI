#!/bin/bash

set -e
set -u

source ./0-conf.env

docker swarm init --advertise-addr $NODE1_IP
echo

WORKER_TOKEN=$(docker swarm join-token worker --quiet)
MANAGER_TOKEN=$(docker swarm join-token manager --quiet)
  
echo "run this on the other nodes --worker"
echo "docker swarm join --token ${WORKER_TOKEN} ${NODE1_IP}:2377"
echo
echo "run this on the other nodes -- manager"
echo "docker swarm join --token ${MANAGER_TOKEN} ${NODE1_IP}:2377"


