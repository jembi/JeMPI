#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  POSTGRESQL_MPI_DB=mpi_db

# Creating databases
  EXISTING_MPI_DB=$(docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_MPI_DB}'")
  if [ -z "$EXISTING_MPI_DB" ]; then
      docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgres) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_DATABASE} -c "CREATE DATABASE ${POSTGRESQL_MPI_DB}"
  fi

  PGPASSWORD=${POSTGRESQL_PASSWORD} psql -h localhost -p 5432 -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_MPI_DB} -f ./conf/postgres/mpi-schema.sql

#docker exec -e PGPASSWORD=${POSTGRESQL_PASSWORD} $(docker ps -q -f name=${STACK_NAME}_postgresql) psql -U ${POSTGRESQL_USERNAME} -d ${POSTGRESQL_MPI_DB} -a -f /conf/mpi-schema.sql
popd
