#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${PREPROCESSOR_JAR}
APP_IMAGE=${PREPROCESSOR_IMAGE}
APP=preprocessor
 
source ../build-app.sh
