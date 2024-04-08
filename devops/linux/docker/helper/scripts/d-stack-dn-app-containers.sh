#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  declare -a APPS=(
    async-receiver
    etl
    controller
    em-scala
    linker
    api
    api-kc
    ui
  )

  for APP in ${APPS[@]}; do
    SERVICE=${STACK_NAME}_${APP}
    NAME=`docker ps -f name=$SERVICE --format "{{.Names}}"`
    echo $SERVICE
    if [ -n "$NAME" ]; then
      echo $SERVICE
      docker service scale $SERVICE=0
      docker wait $NAME
    fi
  done

popd

