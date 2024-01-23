#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  docker service scale ${STACK_NAME}_postgresql=${SCALE_POSTGRESQL}
  docker service scale ${STACK_NAME}_kafka-01=${SCALE_KAFKA_01}
  if [ ! -z ${SCALE_KAFKA_02+x} ] ; then docker service scale ${STACK_NAME}_kafka-02=${SCALE_KAFKA_02}; fi
  if [ ! -z ${SCALE_KAFKA_03+x} ] ; then docker service scale ${STACK_NAME}_kafka-03=${SCALE_KAFKA_03}; fi
  docker service scale ${STACK_NAME}_zero-01=${SCALE_ZERO_01}
  if [ ! -z ${SCALE_ZERO_02+x} ] ; then docker service scale ${STACK_NAME}_zero-02=${SCALE_ZERO_02}; fi
  if [ ! -z ${SCALE_ZERO_03+x} ] ; then docker service scale ${STACK_NAME}_zero-03=${SCALE_ZERO_03}; fi
  docker service scale ${STACK_NAME}_alpha-01=${SCALE_ALPHA_01}
  if [ ! -z ${SCALE_ALPHA_02+x} ] ; then docker service scale ${STACK_NAME}_alpha-02=${SCALE_ALPHA_02}; fi
  if [ ! -z ${SCALE_ALPHA_03+x} ] ; then docker service scale ${STACK_NAME}_alpha-03=${SCALE_ALPHA_03}; fi
  docker service scale ${STACK_NAME}_ratel=${SCALE_RATEL}


  ./helper/bootstrapper/bootstrapper-docker.sh data resetAll

  docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}

popd
