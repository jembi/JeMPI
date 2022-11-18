#!/bin/bash

set -e
set -u

source ./0-conf.env
source ./conf/images/conf-hub-images.sh

mkdir -p ${PWD}/jenkins_docker_client

mkdir -p ${PWD}/jenkins_data
sudo chmod 777 ${PWD}/jenkins_data

sudo chmod 666 /var/run/docker.sock
sudo cp /usr/bin/docker ${PWD}/jenkins_docker_client/

echo $PWD

docker service create \
  --name jenkins \
  --publish published=8088,target=8080,protocol=tcp,mode=host \
  --mount type=bind,source=${PWD}/jenkins_data,destination=/bitnami/jenkins,readonly=false \
  --mount type=bind,source=${PWD}/jenkins_docker_client/docker,destination=/usr/bin/docker,readonly=true \
  --mount type=bind,source=/var/run/docker.sock,destination=/var/run/docker.sock \
  --mount type=bind,source=/bin/envsubst,destination=/bin/envsubst \
  --constraint node.hostname==${PLACEMENT_REGISTRY} \
${JENKINS_IMAGE}