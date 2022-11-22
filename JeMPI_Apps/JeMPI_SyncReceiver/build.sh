#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${SYNC_RECEIVER_JAR}
APP_IMAGE=${SYNC_RECEIVER_IMAGE}
APP=sync-receiver
 
source ../build-app.sh
