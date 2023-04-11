#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  docker service scale ${STACK_NAME}_kafka-01=${SCALE_KAFKA_01}
  docker service scale ${STACK_NAME}_kafka-02=${SCALE_KAFKA_02}
  docker service scale ${STACK_NAME}_kafka-03=${SCALE_KAFKA_03}
  docker service scale ${STACK_NAME}_zero-01=${SCALE_ZERO_01}
  docker service scale ${STACK_NAME}_alpha-01=${SCALE_ALPHA_01}
  docker service scale ${STACK_NAME}_alpha-02=${SCALE_ALPHA_02}
  docker service scale ${STACK_NAME}_alpha-03=${SCALE_ALPHA_03}
  docker service scale ${STACK_NAME}_ratel=${SCALE_RATEL}
  docker service scale ${STACK_NAME}_postgresql=${SCALE_POSTGRESQL}

  pushd helper/topics
    source ./topics-create.sh
    source ./topics-list.sh
  popd
  pushd helper/postgres
    source ./create-schema.sh
  popd
popd
