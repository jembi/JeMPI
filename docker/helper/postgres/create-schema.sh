#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  docker exec $(docker ps -q -f name=jempi-postgres) \
  psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} \
   -a -f /conf/config.sql
popd