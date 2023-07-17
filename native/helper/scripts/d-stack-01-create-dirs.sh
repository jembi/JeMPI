#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env
  source ./conf/images/conf-hub-images.sh
  source ./conf/images/conf-app-images.sh

  docker system prune --volumes

  sudo rm -f -r ${PROJECT_DATA_DIR}/*

  mkdir -p ${DATA_HAPROXY_DIR}
  cp conf/haproxy/*.* ${DATA_HAPROXY_DIR}

  mkdir -p ${DATA_KEYCLOAK_TEST_SERVER_DIR}
  cp conf/keycloak/*.* ${DATA_KEYCLOAK_TEST_SERVER_DIR}

  mkdir -p ${DATA_KAFKA_01_DIR}
  if [ ! -z ${DATA_KAFKA_02_DIR+x} ] ; then mkdir -p ${DATA_KAFKA_02_DIR}; fi
  if [ ! -z ${DATA_KAFKA_03_DIR+x} ] ; then mkdir -p ${DATA_KAFKA_03_DIR}; fi
  mkdir -p ${DATA_DGRAPH_ZERO_01_DIR}
  if [ ! -z ${DATA_DGRAPH_ZERO_02_DIR+x} ]; then mkdir -p ${DATA_DGRAPH_ZERO_02_DIR}; fi
  if [ ! -z ${DATA_DGRAPH_ZERO_03_DIR+x} ]; then mkdir -p ${DATA_DGRAPH_ZERO_03_DIR}; fi
  mkdir -p ${DATA_DGRAPH_ALPHA_01_DIR}
  if [ ! -z ${DATA_DGRAPH_ALPHA_02_DIR+x} ]; then mkdir -p ${DATA_DGRAPH_ALPHA_02_DIR}; fi
  if [ ! -z ${DATA_DGRAPH_ALPHA_03_DIR+x} ]; then mkdir -p ${DATA_DGRAPH_ALPHA_03_DIR}; fi

  mkdir -p ${DATA_POSTGRESQL_DIR}
  cp conf/postgres/*.* ${DATA_POSTGRESQL_DIR}/.
  
  mkdir -p ${DATA_DIR_EM}/conf
  
  mkdir -p ${DATA_DIR_API}/conf

  mkdir -p ${DATA_DIR_API_KC}/conf
  
  echo

popd
