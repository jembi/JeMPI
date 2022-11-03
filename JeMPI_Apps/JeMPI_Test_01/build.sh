#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${TEST_01_JAR}
APP_IMAGE=${TEST_01_IMAGE}
APP=test-01
 
source ../build-app.sh
