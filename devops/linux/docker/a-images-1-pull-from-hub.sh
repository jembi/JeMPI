#!/bin/bash

set -e
set -u

source ./conf.env
source ./conf/images/conf-hub-images.sh
source ./conf/images/conf-app-images.sh

docker pull $JAVA_BASE_IMAGE
docker pull $REGISTRY_IMAGE
docker pull $KEYCLOAK_IMAGE
docker pull $HAPROXY_IMAGE
docker pull $KAFKA_IMAGE
docker pull $DGRAPH_IMAGE
docker pull $RATEL_IMAGE
docker pull $POSTGRESQL_IMAGE
docker pull $JAVASCRIPT_RUNTIME
docker pull $HASHICORP_VAULT_IMAGE
