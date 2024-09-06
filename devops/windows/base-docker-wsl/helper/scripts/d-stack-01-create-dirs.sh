#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh
  
  docker system prune --volumes

  sudo rm -f -r ${PROJECT_DATA_DIR}/*

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
  
  mkdir -p ${DATA_POSTGRESQL_DB_DIR}
  sudo chown -R 1001:1001 ${DATA_POSTGRESQL_DB_DIR}
  sudo chmod -R 770 ${DATA_POSTGRESQL_DB_DIR}
  
  mkdir -p ${POSTGRES_BACKUP_DIRECTORY}
  mkdir -p ${DGRAPH_BACKUP_DIRECTORY}
  mkdir -p ${SYSTEM_CONFIG_DIR}

  
  echo

popd
