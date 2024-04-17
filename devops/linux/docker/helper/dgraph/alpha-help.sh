#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  docker exec $(docker ps -q -f name=zero-01) dgraph alpha --help

popd
