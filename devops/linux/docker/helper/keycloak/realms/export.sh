#!/bin/bash

set -m
set -m

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../../..

  source ./conf.env

    docker exec $(docker ps -q -f name=${STACK_NAME}_keycloak-test-server) kc.sh export --file realm.json --realm jempi-dev
    docker exec $(docker ps -q -f name=${STACK_NAME}_keycloak-test-server) cat realm.json > ./conf/keycloak/export-jempi-dev-realm.json

popd