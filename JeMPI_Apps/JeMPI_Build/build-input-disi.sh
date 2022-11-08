#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh

JAR_FILE=$INPUT_DISI_JAR 
APP_IMAGE=$INPUT_DISI_IMAGE
APP=input-disi

pushd ../JeMPI_InputDISI
  source ../JeMPI_Build/build-app.sh
popd
