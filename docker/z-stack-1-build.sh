#!/bin/bash

set -e
set -u

echo
echo Build Apps
pwd
# pushd ../JeMPI_Apps
#   ./build-all.sh
# popd
./helper/scripts/c-registry-3-build-push-app-images.sh
