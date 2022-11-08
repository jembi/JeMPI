#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh
 
JAR_FILE=$NOTIFICATIONS_JAR 
APP_IMAGE=$NOTIFICATIONS_IMAGE
APP=notifications

pushd ../JeMPI_Notifications
  source ../JeMPI_Build/build-app.sh   
popd
