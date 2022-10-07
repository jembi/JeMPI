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

  mkdir -p ${DATA_KAFKA_1_DIR}
  mkdir -p ${DATA_KAFKA_2_DIR}
  mkdir -p ${DATA_KAFKA_3_DIR}
# mkdir -p ${DATA_MYSQL_EM_DATA_DIR}
# mkdir -p ${DATA_MYSQL_EM_INIT_DIR}
  mkdir -p ${DATA_DGRAPH_ZERO_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA1_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA2_DIR}
  mkdir -p ${DATA_DGRAPH_ALPHA3_DIR}
  mkdir -p ${DATA_DGRAPH_LAMBDA_DIR}
  mkdir -p ${DATA_CASSANDRA_1_DIR}/data
  mkdir -p ${DATA_CASSANDRA_2_DIR}/data
  mkdir -p ${DATA_CASSANDRA_3_DIR}/data
  cp    ./conf/cassandra/cassandra-1.yaml ${DATA_CASSANDRA_1_DIR}/cassandra.yaml
  cp    ./conf/cassandra/cassandra-2.yaml ${DATA_CASSANDRA_2_DIR}/cassandra.yaml
  cp    ./conf/cassandra/cassandra-3.yaml ${DATA_CASSANDRA_3_DIR}/cassandra.yaml


  mkdir -p ${DATA_DIR_JOURNAL}/conf
  rm -rf   ${DATA_DIR_JOURNAL}/logs
  mkdir -p ${DATA_DIR_JOURNAL}/logs

  mkdir -p ${DATA_DIR_NOTIFICATIONS}/conf
  rm -rf   ${DATA_DIR_NOTIFICATIONS}/logs
  mkdir -p ${DATA_DIR_NOTIFICATIONS}/logs

  mkdir -p ${DATA_DIR_TEST_01}/conf
  rm -rf   ${DATA_DIR_TEST_01}/logs
  mkdir -p ${DATA_DIR_TEST_01}/logs
  mkdir -p ${DATA_DIR_TEST_01}/csv

  mkdir -p ${DATA_DIR_STAGING_01}/conf
  rm -rf   ${DATA_DIR_STAGING_01}/logs
  mkdir -p ${DATA_DIR_STAGING_01}/logs

  mkdir -p ${DATA_DIR_INPUT_02}/conf
  rm -rf   ${DATA_DIR_INPUT_02}/logs
  mkdir -p ${DATA_DIR_INPUT_02}/logs
  mkdir -p ${DATA_DIR_INPUT_02}/csv

  mkdir -p ${DATA_DIR_STAGING_02}/conf
  rm -rf   ${DATA_DIR_STAGING_02}/logs
  mkdir -p ${DATA_DIR_STAGING_02}/logs

  mkdir -p ${DATA_DIR_INPUT_DISI}/conf
  rm -rf   ${DATA_DIR_INPUT_DISI}/logs
  mkdir -p ${DATA_DIR_INPUT_DISI}/logs
  mkdir -p ${DATA_DIR_INPUT_DISI}/csv

  mkdir -p ${DATA_DIR_STAGING_DISI}/conf
  rm -rf   ${DATA_DIR_STAGING_DISI}/logs
  mkdir -p ${DATA_DIR_STAGING_DISI}/logs

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