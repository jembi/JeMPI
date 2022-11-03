#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${STAGING_DISI_JAR}
APP_IMAGE=${STAGING_DISI_IMAGE}
APP=staging-disi
 
source ../build-app.sh
