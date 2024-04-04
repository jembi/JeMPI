#!/bin/bash

set -e
set -u

echo
echo "Down stacks"
source ../scripts/d-stack-rm.sh

echo
echo "Build Apps"
pwd
pushd ../../../../../JeMPI_Apps
  source ./build-all-java.sh
  source ./build-all-ui.sh
popd
sleep 2

echo
echo Up app containers
source ../scripts/d-stack-wait-removed.sh
sleep 2
echo Up app containers
source ../scripts/d-stack-deploy-0.sh
sleep 2
source ../scripts/d-stack-up-hub-containers.sh
sleep 2
source ../scripts/d-stack-up-keycloak-containers.sh
sleep 2
source ../scripts/d-stack-up-app-containers.sh
