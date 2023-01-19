#! /bin/bash

set -e
set -u

export USE_LOCAL_REGISTRY=false

# Creating conf.env file
pushd ./docker/conf/env || exit
    ./create-env-linux-1.sh
popd || exit

read -p "Do you want to reset docker swarm? " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
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
fi

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

pushd ./docker/ || exit 
    ./z-stack-2-reboot.sh
popd || exit
