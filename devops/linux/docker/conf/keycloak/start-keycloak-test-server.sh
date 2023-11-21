#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh

  # Deploying test server
  rm -f ./conf/keycloak/0-docker-stack-keycloak.yml
  envsubst < ./conf/keycloak/docker-stack-keycloak.yml > ./conf/keycloak/0-docker-stack-keycloak.yml
  docker stack deploy --compose-file ./conf/keycloak/0-docker-stack-keycloak.yml ${STACK_NAME}

  # Scaling services
  docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}

popd

 # Adding realms
./realms/import.sh