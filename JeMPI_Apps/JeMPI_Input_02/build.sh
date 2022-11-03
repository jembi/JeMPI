#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${INPUT_02_JAR}
APP_IMAGE=${INPUT_02_IMAGE}
APP=input-02
 
source ../build-app.sh
