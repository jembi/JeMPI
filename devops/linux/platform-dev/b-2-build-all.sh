#!/bin/bash

set -e
set -u


pushd conf/env
  source ./create-env.sh
popd

source ./conf.env

echo
echo "Building apps"
pwd
pushd ../../../JeMPI_Apps
  source ../devops/linux/platform-dev/z-build-all-java.sh "sd-wp2-dev"
  source ../devops/linux/platform-dev/z-build-all-ui.sh "sd-wp2-dev"
popd

