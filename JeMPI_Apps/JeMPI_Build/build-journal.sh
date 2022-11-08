#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh

JAR_FILE=$JOURNAL_JAR 
APP_IMAGE=$JOURNAL_IMAGE
APP=journal

pushd ../JeMPI_Journal
  source ../JeMPI_Build/build-app.sh 
popd
