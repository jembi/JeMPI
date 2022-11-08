#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh

JAR_FILE=$CONTROLLER_JAR 
APP_IMAGE=$CONTROLLER_IMAGE
APP=controller
 
pushd ../JeMPI_Controller
  source ../JeMPI_Build/build-app.sh
popd
