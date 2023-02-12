#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${ETL_JAR}
APP_IMAGE=${ETL_IMAGE}
APP=etl
 
source ../build-app.sh
