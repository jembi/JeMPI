#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${ASYNC_RECEIVER_JAR}
APP_IMAGE=${ASYNC_RECEIVER_IMAGE}
APP=async-receiver
 
source ../build-app-image.sh
