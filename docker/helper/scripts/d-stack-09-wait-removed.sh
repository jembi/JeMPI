#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  declare -a SERVICES=(
    test-01
    staging-01
    input-02
    staging-02
    input-disi
    staging-disi
    jempi-controller
    jempi-em
    jempi-linker
    jempi-0api
    jempi-0ratel
    jempi-0alpha-03
    jempi-alpha-02
    jempi-alpha-01
    jempi-zero-01
    jempi-kafka-03
    jempi-kafka-02
    jempi-kafka-01
  )

  for SERVICE in ${SERVICES[@]}; do
    NAME=`docker ps -f name=$SERVICE --format "{{.Names}}"`
    if [ -n "$NAME" ]; then
#     docker service scale $SERVICE=0
      docker wait $NAME
    fi
  done

popd

