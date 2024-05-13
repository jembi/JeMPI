#!/bin/bash

set -e
set -u
# SERVICE_NAME = $1
pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  # docker service scale ${SERVICE_NAME}=1
   docker service scale jempi_api=1
   docker service scale jempi_async-receiver=1
  echo

popd  


