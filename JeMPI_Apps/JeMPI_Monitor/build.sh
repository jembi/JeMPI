#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${MONITOR_JAR}
APP_IMAGE=${MONITOR_IMAGE}
APP=monitor
 
source ../build-app-image.sh
