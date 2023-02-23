#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  declare -a APPS=(
    jempi-sync-receiver
    jempi-async-receiver
    jempi-etl
    jempi-controller
    jempi-em
    jempi-linker
    jempi-api
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

