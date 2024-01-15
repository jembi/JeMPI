#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${BOOTSTRAPPER_JAR}
APP_IMAGE=${BOOTSTRAPPER_IMAGE}
APP=bootstrapper
 
source ../build-app-image.sh
