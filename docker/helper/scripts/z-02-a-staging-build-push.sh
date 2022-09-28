#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..
  
  pushd ../JeMPI_Apps/JeMPI_Build  
    ./staging-build.sh
    ./staging-push.sh
  popd

popd