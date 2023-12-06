#!/bin/bash

set -e
set -u

source ./conf.env
source ./conf/images/conf-hub-images.sh

declare -a IMAGES=("$HAPROXY_IMAGE"
                   "$KAFKA_IMAGE"
                   "$KEYCLOAK_IMAGE"
                   "$DGRAPH_IMAGE"
                   "$RATEL_IMAGE"
                   "$POSTGRESQL_IMAGE")

for IMAGE in ${IMAGES[@]}; do
  echo $IMAGE
  docker tag ${IMAGE} ${REGISTRY_NODE_IP}/${IMAGE}
  docker push ${REGISTRY_NODE_IP}/${IMAGE}
  docker rmi ${REGISTRY_NODE_IP}/${IMAGE}
done  
 
  
