#!/bin/bash

set -e
set -u

STACK_NAME=jempi

declare -a APPS=(
  bootstrapper  
  async-receiver
  etl
  controller
  em-scala
  linker
  api
  api-kc
  web
)

docker stack services jempi

for APP in ${APPS[@]}; do
  SERVICE=${STACK_NAME}_jempi-${APP}
  ID=$(docker stack services  jempi | grep ${SERVICE}\   | awk '{print $1}')
  if [ -n "$ID" ]; then
     docker service scale $ID=0
  fi
done
