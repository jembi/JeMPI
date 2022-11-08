#!/bin/bash

set -e
set -u

source ../../docker/0-conf.env
source ../../docker/conf/images/conf-app-images.sh

APP_IMAGE=$STAGING_DISI_IMAGE

docker tag ${APP_IMAGE} ${REGISTRY_NODE_IP}/${APP_IMAGE}
docker push ${REGISTRY_NODE_IP}/${APP_IMAGE}
docker rmi ${REGISTRY_NODE_IP}/${APP_IMAGE} 
