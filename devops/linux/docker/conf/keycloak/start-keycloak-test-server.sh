#!/bin/bash

set -e
set -u

source ./test-keyclock.env

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./conf/images/conf-hub-images.sh

  # Creating db
  EXISTING_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${TEST_KEY_CLOCK_DB}'")

  if [ -z "$EXISTING_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${TEST_KEY_CLOCK_DB}"
  fi

  # Deploying test server
  rm -f ./conf/keycloak/0-docker-stack-keycloak.yml
  envsubst < ./conf/keycloak/docker-stack-keycloak.yml > ./conf/keycloak/0-docker-stack-keycloak.yml
  docker stack deploy --compose-file ./conf/keycloak/0-docker-stack-keycloak.yml ${STACK_NAME}

  # Scaling services
  docker service scale ${STACK_NAME}_keycloak-test-server=${SCALE_KEYCLOAK_TEST_SERVER}

popd

 # Adding realms
./realms/import.sh