#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh
  source ./conf/images/conf-app-images.sh

  rm -f ./0-docker-stack-0.yml
  rm -f ./0-docker-stack-1.yml
  envsubst < ./conf/stack/docker-stack-${SPEC_SETTINGS}-0.yml > ./0-docker-stack-0.yml
  envsubst < ./conf/stack/docker-stack-${SPEC_SETTINGS}-1.yml > ./0-docker-stack-1.yml

  docker stack deploy --prune --compose-file 0-docker-stack-1.yml ${STACK_NAME}
  echo
  sleep 5
  docker stack services ${STACK_NAME}
  echo

popd
