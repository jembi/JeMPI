#!/bin/bash

set -e
set -u

echo
echo Down stacks
./helper/scripts/d-stack-08-rm.sh

echo
echo Build Apps
pwd
pushd ../JeMPI_Apps
  ./build-all.sh
popd
#./helper/scripts/c-registry-3-build-push-app-images.sh
sleep 2

echo
echo Up app containers
#./helper/scripts/d-stack-09-wait-removed.sh
./helper/scripts/d-stack-01-create-dirs.sh
sleep 2
./helper/scripts/d-stack-02-deploy-0.sh
sleep 2
./helper/scripts/d-stack-03-up-hub-containers.sh
sleep 2
./helper/scripts/d-stack-04-up-app-containers.sh

