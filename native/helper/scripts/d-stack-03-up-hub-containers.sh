#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}
  docker service scale ${STACK_NAME}_kafka-01=${SCALE_KAFKA_01}
  if [ ! -z ${SCALE_KAFKA_02+x} ] ; then docker service scale ${STACK_NAME}_kafka-02=${SCALE_KAFKA_02}; fi
  if [ ! -z ${SCALE_KAFKA_03+x} ] ; then docker service scale ${STACK_NAME}_kafka-03=${SCALE_KAFKA_03}; fi
  docker service scale ${STACK_NAME}_ratel=${SCALE_RATEL}


  pushd helper/topics
    source ./topics-create.sh
    source ./topics-list.sh
  popd
  pushd helper/postgres
    source ./create-schema.sh
  popd
popd
