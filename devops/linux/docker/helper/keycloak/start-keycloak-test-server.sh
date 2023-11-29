#!/bin/bash

set -e
set -u

pushd .
  echo "Starting keycloak (test) server" 

  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh

  # Creating db
  EXISTING_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${KC_TEST_DB}'")

  if [ -z "$EXISTING_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${KC_TEST_DB}"
  fi

  # Scaling services
  docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}

  sleep 2
  
  # Adding realms
  ./helper/keycloak/realms/import.sh || echo "Failed to import jempi-dev realm"

popd