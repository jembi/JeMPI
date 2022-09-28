#!/bin/bash

set -e
set -u

source ./0-conf.env
source ./conf/images/conf-hub-images.sh
source ./conf/images/conf-app-images.sh

docker pull $JAVA_BASE_IMAGE
docker pull $REGISTRY_IMAGE
docker pull $PORTAINER_AGENT_IMAGE
docker pull $PORTAINER_IMAGE
docker pull $KAFKA_IMAGE
docker pull $DGRAPH_IMAGE
docker pull $DGRAPH_LAMBDA_IMAGE
docker pull $RATEL_IMAGE
docker pull $CASSANDRA_IMAGE
