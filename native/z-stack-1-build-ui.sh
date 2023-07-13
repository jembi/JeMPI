#!/bin/bash

set -e
set -u

#echo
#echo Down stacks
#./helper/scripts/d-stack-08-rm.sh

echo
echo "Build Apps"
pwd
pushd ../JeMPI_Apps
  source ./build-all-ui.sh
popd
#./helper/scripts/c-registry-3-build-push-app-images.sh

