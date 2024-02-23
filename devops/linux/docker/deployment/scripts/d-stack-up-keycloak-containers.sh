#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  if [ "$REACT_APP_ENABLE_SSO" = "true" ]; then
    echo Up Keycloak Server
    docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}
  fi

popd
