#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..
  
  source ./0-conf.env
  pushd ../JeMPI_Apps/JeMPI_Build
    ./build-all.sh
  popd

popd

