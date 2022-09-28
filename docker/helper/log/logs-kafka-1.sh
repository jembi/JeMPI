#!/bin/bash

set -e
set -u

pushd ../../

source 0-conf.env
docker service logs --follow --raw ${STACK_NAME}_kafka-1
echo

popd
