#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${API_JAR}
APP_IMAGE=${API_IMAGE}
APP=api

cp -f ../JeMPI_Configuration/config-reference.json ./src/main/resources/config-reference.json
source ../build-app.sh
