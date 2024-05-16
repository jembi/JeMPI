#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${ETL_JAR}
APP_IMAGE=${ETL_IMAGE}
APP=etl

if [ "$CI" = true ]; then
    APP_IMAGE=jembi/${ETL_HUB_IMAGE}:${TAG}
fi
 
source ../build-app-image.sh
