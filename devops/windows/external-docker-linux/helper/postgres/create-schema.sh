#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -a -f /conf/config.sql
popd