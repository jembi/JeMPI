#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf.env
source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh

APP_IMAGE=${LINKER_IMAGE}

docker tag ${APP_IMAGE} ${REGISTRY_NODE_IP}/${APP_IMAGE}
docker push ${REGISTRY_NODE_IP}/${APP_IMAGE}
docker rmi ${REGISTRY_NODE_IP}/${APP_IMAGE}
 