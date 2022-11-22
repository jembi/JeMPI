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

  mkdir -p ${DATA_KAFKA_01_DIR}
  mkdir -p ${DATA_KAFKA_02_DIR}
  mkdir -p ${DATA_KAFKA_03_DIR}
  mkdir -p ${DATA_DGRAPH_ZERO_01_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA_01_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA_02_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA_03_DIR}

  mkdir -p ${DATA_DIR_ASYNC_RECEIVER}/conf
  rm -rf   ${DATA_DIR_ASYNC_RECEIVER}/logs
  mkdir -p ${DATA_DIR_ASYNC_RECEIVER}/logs
  mkdir -p ${DATA_DIR_ASYNC_RECEIVER}/csv

  mkdir -p ${DATA_DIR_SYNC_RECEIVER}/conf
  rm -rf   ${DATA_DIR_SYNC_RECEIVER}/logs
  mkdir -p ${DATA_DIR_SYNC_RECEIVER}/logs

  mkdir -p ${DATA_DIR_PREPROCESSOR}/conf
  rm -rf   ${DATA_DIR_PREPROCESSOR}/logs
  mkdir -p ${DATA_DIR_PREPROCESSOR}/logs

  mkdir -p ${DATA_DIR_CONTROLLER}/conf
  rm -rf   ${DATA_DIR_CONTROLLER}/logs
  mkdir -p ${DATA_DIR_CONTROLLER}/logs

  mkdir -p ${DATA_DIR_EM}/conf
  rm -rf   ${DATA_DIR_EM}/logs
  mkdir -p ${DATA_DIR_EM}/logs

  mkdir -p ${DATA_DIR_LINKER}/conf
  rm -rf   ${DATA_DIR_LINKER}/logs
  mkdir -p ${DATA_DIR_LINKER}/logs

  mkdir -p ${DATA_DIR_API}/conf
  rm -rf   ${DATA_DIR_API}/logs
  mkdir -p ${DATA_DIR_API}/logs

# envsubst < ./conf/mysql/em/init.sql  > ${DATA_MYSQL_EM_INIT_DIR}/init.sql

  echo

popd
