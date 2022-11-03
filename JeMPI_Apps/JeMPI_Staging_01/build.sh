#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${STAGING_01_JAR}
APP_IMAGE=${STAGING_01_IMAGE}
APP=staging-01
 
source ../build-app.sh
