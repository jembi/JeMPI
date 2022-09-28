#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  SERVICE=JeMPI_controller

  NAME=`docker ps -f name=$SERVICE --format "{{.Names}}"`
  if [ -n "$NAME" ]; then
      docker service scale $SERVICE=0
      docker wait $NAME
  fi

  docker service scale $SERVICE=1

popd