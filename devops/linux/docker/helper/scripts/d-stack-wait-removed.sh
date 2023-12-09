#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  declare -a SERVICES=(
    async-receiver
    etl
    controller
    em-scala
    linker
    api
    api-kc
    ratel
    alpha-03
    alpha-02
    alpha-01
    zero-01
    kafka-03
    kafka-02
    kafka-01
    postgresql
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

