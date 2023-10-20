#!/bin/bash

set -e
set -u

pushd .    
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh

  rm -f ./docker-stack.yml
  envsubst < ./conf/stack/docker-stack-${SPEC_SETTINGS}.yml > ./docker-stack.yml

  docker stack deploy --prune --compose-file docker-stack.yml ${STACK_NAME}
  echo
    
popd
