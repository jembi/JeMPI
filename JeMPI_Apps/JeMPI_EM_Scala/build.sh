#!/bin/bash

set -e
set -u

source "$PROJECT_DEVOPS_DIR"/conf/images/conf-app-images.sh

JAR_FILE=${EM_SCALA_JAR}
APP_IMAGE=${EM_SCALA_IMAGE}
APP=em_scala

if [ "$CI" = true ]; then
    APP_IMAGE=jembi/${EM_SCALA_HUB_IMAGE}:${TAG}
fi
 
source ../build-scala-app-image.sh
