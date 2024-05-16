#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${API_KC_JAR}
APP_IMAGE=${API_KC_IMAGE}
APP=api

if [ "$CI" = true ]; then
    APP_IMAGE=jembi/${API_KC_HUB_IMAGE}:${TAG}
fi

source ../build-app-image.sh
