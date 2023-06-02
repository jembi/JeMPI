#!/bin/bash

set -e
set -u

source ../../docker/0-conf.env
source ../../docker/conf/images/conf-app-images.sh

docker tag ${API_KC_IMAGE} ${REGISTRY_NODE_IP}/${API_KC_IMAGE}
docker push ${REGISTRY_NODE_IP}/${API_KC_IMAGE}
docker rmi ${REGISTRY_NODE_IP}/${API_KC_IMAGE}
 
