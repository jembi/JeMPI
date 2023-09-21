#!/bin/bash

set -e
set -u

pushd .    
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh

  rm -f ./0-docker-stack-0.yml
  rm -f ./0-docker-stack-1.yml
  envsubst < ./conf/stack/docker-stack-${SPEC_SETTINGS}-0.yml > ./0-docker-stack-0.yml
  envsubst < ./conf/stack/docker-stack-${SPEC_SETTINGS}-1.yml > ./0-docker-stack-1.yml

  docker stack deploy --prune --compose-file 0-docker-stack-0.yml ${STACK_NAME}
  echo
    
popd
