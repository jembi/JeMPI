#! /bin/bash

set -e
set -u

# Creating conf.env file
pushd ./docker/conf/env || exit
./create-env-linux-1.sh
popd || exit

# Clean the swarm
docker swarm leave --force
docker ps -a
echo
docker network prune -f
docker system prune --volumes -f
docker network ls
echo

# Init the swarm
docker swarm init --advertise-addr 127.0.0.1

# Run bash scripts
pushd ./docker/ || exit
./a-images-1-pull-from-hub.sh
popd || exit

pushd ./JeMPI_Apps/JeMPI_LibMPI
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_AsyncReceiver
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_SyncReceiver
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_PreProcessor
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_Controller
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_EM
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_Linker
  ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_API
  ./build.sh || exit 1
popd

# Update compose docker-stack files
pushd ./docker/conf/stack || exit
sed -i "s/\${REGISTRY_NODE_IP}\///" docker-stack-0.yml 
sed -i "s/\${REGISTRY_NODE_IP}\///" docker-stack-1.yml

cat docker-stack-0.yml
popd || exit

pushd ./docker/ || exit 
./z-stack-2-reboot.sh
popd || exit
