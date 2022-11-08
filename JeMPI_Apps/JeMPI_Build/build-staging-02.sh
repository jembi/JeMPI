#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh
 
JAR_FILE=$STAGING_02_JAR 
APP_IMAGE=$STAGING_02_IMAGE
APP=staging-02
 
pushd ../JeMPI_Staging_02
  source ../JeMPI_Build/build-app.sh   
popd