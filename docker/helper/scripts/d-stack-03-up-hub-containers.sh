#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  docker service scale ${STACK_NAME}_jempi-kafka-01=${SCALE_KAFKA_01}
  docker service scale ${STACK_NAME}_jempi-kafka-02=${SCALE_KAFKA_02}
  docker service scale ${STACK_NAME}_jempi-kafka-03=${SCALE_KAFKA_03}
  docker service scale ${STACK_NAME}_jempi-zero-01=${SCALE_ZERO_01}
  docker service scale ${STACK_NAME}_jempi-alpha-01=${SCALE_ALPHA_01}
  docker service scale ${STACK_NAME}_jempi-alpha-02=${SCALE_ALPHA_02}
  docker service scale ${STACK_NAME}_jempi-alpha-03=${SCALE_ALPHA_03}
  docker service scale ${STACK_NAME}_jempi-ratel=${SCALE_RATEL}
  docker service scale ${STACK_NAME}_jempi-postgresql=${SCALE_POSTGRESQL}

  pushd helper/topics
    ./topics-create.sh
    ./topics-list.sh
  popd
  pushd helper/postgres
    ./create-schema.sh
  popd
popd