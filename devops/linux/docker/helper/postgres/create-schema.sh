#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env


# Creating databases
  EXISTING_NOTIFICATIONS_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_NOTIFICATIONS_DB}'")
  if [ -z "$EXISTING_NOTIFICATIONS_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_NOTIFICATIONS_DB}"
  fi

  EXISTING_AUDIT_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_AUDIT_DB}'")
  if [ -z "$EXISTING_NOTIFICATIONS_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_AUDIT_DB}"
  fi

  EXISTING_KC_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_KC_TEST_DB}'")
  if [ -z "$EXISTING_KC_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_KC_TEST_DB}"
  fi

  EXISTING_USERS_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_USERS_DB}'")
  if [ -z "$EXISTING_USERS_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_USERS_DB}"
  fi

  EXISTING_CONFIGURATION_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_CONFIGURATION_DB}'")
  if [ -z "$EXISTING_CONFIGURATION_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_CONFIGURATION_DB}"
  fi


  docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgresql) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_USERS_DB}         -a -f /conf/config-users.sql
  docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgresql) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_NOTIFICATIONS_DB} -a -f /conf/config-notifications.sql
  docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgresql) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_AUDIT_DB}         -a -f /conf/config-audit.sql
  docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgresql) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_CONFIGURATION_DB} -a -f /conf/config-configuration.sql
popd
