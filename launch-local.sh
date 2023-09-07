#! /bin/bash

set -e
set -u

export USE_LOCAL_REGISTRY=false

# Creating conf.env file
while true; do
    read -p "Do you want to (re)create the environment? " yn
    case $yn in
        [Yy]* )
          pushd ./docker/conf/env || exit
              source ./create-env-linux-low-1.sh
          popd || exit
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

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

# Copy Config for API
cp -L -f ./JeMPI_Apps/JeMPI_Configuration/config-api.json ./JeMPI_Apps/JeMPI_API/src/main/resources/config-api.json
cp -L -f ./JeMPI_Apps/JeMPI_Configuration/config-api.json ./JeMPI_Apps/JeMPI_API_KC/src/main/resources/config-api.json

# Maven package
pushd ./JeMPI_Apps || exit
    mvn clean package
popd || exit

# Run bash scripts
while true; do
    read -p "Do you want to pull the latest images from docker hub? " yn
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

pushd ./JeMPI_Apps/JeMPI_UI
  source ./build-image.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_AsyncReceiver
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_ETL
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_Controller
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_EM
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_Linker
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_API
  source ./build.sh || exit 1
popd
pushd ./JeMPI_Apps/JeMPI_API_KC
  source ./build.sh || exit 1
popd
pushd ./docker/ || exit
  source ./z-stack-2-reboot.sh
popd || exit

# Configure keycloak
echo "Waiting to configure keycloak..."
sleep 30
pushd docker/conf/keycloak/ || exit
  ./1-import.sh || exit 1
popd || exit
echo "Done configuring keycloak"
