#!/bin/bash

#set -e
set -u

source ./0-conf.env

docker swarm leave --force
docker ps -a
echo
docker network prune -f
docker system prune --volumes -f
docker network ls
echo
