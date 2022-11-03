#!/bin/bash

set -e
set -u

source ./0-conf.env
source ./conf/images/conf-hub-images.sh

declare -a IMAGES=("$PORTAINER_AGENT_IMAGE"
  "$PORTAINER_IMAGE"
  "$KAFKA_IMAGE"
  "$DGRAPH_IMAGE"
  "$DGRAPH_LAMBDA_IMAGE"
  "$RATEL_IMAGE"
  "$CASSANDRA_IMAGE"
  "$PROMETHEUS_IMAGE"
  "$GRAFANA_IMAGE"
)

for IMAGE in ${IMAGES[@]}; do
  echo $IMAGE
  docker tag ${IMAGE} ${REGISTRY_NODE_IP}/${IMAGE}
  docker push ${REGISTRY_NODE_IP}/${IMAGE}
  docker rmi ${REGISTRY_NODE_IP}/${IMAGE}
done
