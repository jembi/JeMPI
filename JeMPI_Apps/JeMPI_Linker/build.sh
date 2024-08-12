#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${LINKER_JAR}
APP_IMAGE=${LINKER_IMAGE}
APP=linker

if [ "$CI" = true ]; then
    APP_IMAGE=jembi/${LINKER_HUB_IMAGE}:${TAG}
fi
 
source ../build-app-image.sh
