#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  declare -a SERVICES=(
    em
    api-kc
    ratel
    kafka-03
    kafka-02
    kafka-01
    ui
  )

  for SERVICE in ${SERVICES[@]}; do
    NAME=`docker ps -f name=$SERVICE --format "{{.Names}}"`
    if [ -n "$NAME" ]; then
#     docker service scale $SERVICE=0
      docker wait $NAME
    fi
  done

popd

