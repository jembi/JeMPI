#!/bin/bash

set -e
set -u

pushd ../../

source ./conf.env
docker service logs --follow --raw ${STACK_NAME}_kafka-01
echo

popd
