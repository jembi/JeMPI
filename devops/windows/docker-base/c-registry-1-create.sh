#!/bin/bash

set -e
set -u

source ./conf.env
source ./conf/images/conf-hub-images.sh

sudo rm -r -f ${PWD}/data-registry
mkdir -p ${PWD}/data-registry

echo "PLACEMENT_REGISTRY: ${PLACEMENT_REGISTRY}"

docker service create \
  --name registry \
  --limit-memory=64M \
  --publish published=5000,target=5000,protocol=tcp,mode=host \
  --mount type=bind,source=${PWD}/data-registry,destination=/var/lib/registry,readonly=false \
  --constraint node.hostname==${PLACEMENT_REGISTRY} \
 $REGISTRY_IMAGE

