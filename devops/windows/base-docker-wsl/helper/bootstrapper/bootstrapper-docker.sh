#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  java_args="${@:1}"
  source ./conf.env
  docker exec $(docker ps -q -f name=bootstrapper) /bootstrapper.sh $java_args

popd