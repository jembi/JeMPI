#!/bin/bash

set -e
set -u

pwd
pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..
  pwd
  source ./0-conf.env
  pushd ../JeMPI_Apps/JeMPI_Build
    ./build-all.sh
  popd

popd

