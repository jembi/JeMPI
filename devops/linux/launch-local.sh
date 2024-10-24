#! /bin/bash

set -e
set -u

CI="${CI:-false}"
if [ $# -eq 0 ]; then
    tag_to_use="ci-test-main" 
else
    tag_to_use=$1
fi

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
while true; do
    read -p "Do you want to reset docker swarm? " yn
    case $yn in
        [Yy]* ) 
          pushd ../../JeMPI_Apps || exit
              mvn clean package
          popd || exit
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

# Run bash scripts
while true; do
    read -p "Do you want to get the latest docker images? " yn
    case $yn in
        [Yy]* )
          pushd ./docker/ || exit
            CI=$CI TAG=$tag_to_use ./a-images-1-pull-from-hub.sh
          popd || exit
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

pushd ../../JeMPI_Apps/JeMPI_UI
  CI=$CI TAG=$tag_to_use ./build-image.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_AsyncReceiver
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_BackupRestoreAPI
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Bootstrapper
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_ETL
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Controller
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_EM
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_Linker
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_API
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ../../JeMPI_Apps/JeMPI_API_KC
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd ./docker/ || exit
  ./d-stack-3-reboot.sh
popd || exit
