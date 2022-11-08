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
  controller
  em
  linker
  api
  ratel1
  lambda
  alpha3
  alpha2
  alpha1
  zero
  kafka-3
  kafka-2
  kafka-1
  cassandra-3
  cassandra-2
  cassandra-1
  prometheus
  grafana
)

for SERVICE in ${SERVICES[@]}; do
  NAME=$(docker ps -f name=$SERVICE --format "{{.Names}}")
  if [ -n "$NAME" ]; then
    #     docker service scale $SERVICE=0
    docker wait $NAME
  fi
done

popd
