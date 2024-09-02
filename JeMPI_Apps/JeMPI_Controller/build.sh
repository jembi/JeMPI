#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${CONTROLLER_JAR}
APP_IMAGE=${CONTROLLER_IMAGE}
APP=controller

if [ "$CI" = true ]; then
    APP_IMAGE=jembi/${CONTROLLER_HUB_IMAGE}:${TAG}
fi
 
source ../build-app-image.sh
