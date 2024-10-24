#! /bin/bash

set -e
set -u

export USE_LOCAL_REGISTRY=false

# Creating conf.env file
pushd ./docker/conf/env || exit
    source ./create-env-linux-low-1.sh
popd || exit

while true; do
    read -p "Do you want to reset docker swarm? " yn
    case $yn in
        [Yy]* ) 
          if [ "$(docker info | grep Swarm | sed 's/Swarm: //g' | cut -c 2-)" == "active" ]; then  
            # Clean the swarm
            docker swarm leave --force
            docker ps -a
            echo
            docker network prune -f
            docker system prune --volumes -f
            docker network ls
            echo
          fi
          # Init the swarm
          pushd ./docker || exit
            source ./b-swarm-1-init-node1.sh
          popd || exit
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

# Maven package
pushd ../../JeMPI_Apps || exit
    mvn clean package
popd || exit

# Run bash scripts
while true; do
    read -p "Do you want to get the latest docker images? " yn
    case $yn in
        [Yy]* )
          pushd ./docker/ || exit
            source ./a-images-1-pull-from-hub.sh
          popd || exit
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

pushd ../../JeMPI_Apps/JeMPI_UI
  source ./build-image.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_AsyncReceiver
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_BackupRestoreAPI
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Bootstrapper
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_ETL
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Controller
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_EM
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Linker
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_API
  source ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_API_KC
  source ./build.sh || exit 1
popd
pushd ./docker/ || exit
  source ./d-stack-3-reboot.sh
popd || exit
