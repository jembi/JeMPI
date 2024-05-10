#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh
source ../build-check-jdk.sh

JAR_FILE=${BACKUP_RESTORE_API_JAR}
APP_IMAGE=${BACKUP_RESTORE_API_IMAGE}
APP=api
source ../build-app-image.sh
