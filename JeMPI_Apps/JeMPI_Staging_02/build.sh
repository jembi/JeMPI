#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${STAGING_02_JAR}
APP_IMAGE=${STAGING_02_IMAGE}
APP=staging-02
 
source ../build-app.sh
