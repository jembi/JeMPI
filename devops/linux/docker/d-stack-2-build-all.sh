#!/bin/bash

set -e
set -u

source ./conf.env

echo
echo "Build java apps"
pwd
pushd ../../../JeMPI_Apps
  source ./build-all-java.sh
  source ./build-all-ui.sh
popd
