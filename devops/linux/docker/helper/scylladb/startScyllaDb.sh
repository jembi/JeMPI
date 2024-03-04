#!/bin/bash

set -e
set -u

pushd .
  rm -f ./0-scylladb-stack.yml

  envsubst < ./conf/stack/scylladb-stack.yml > ./0-scylladb-stack.yml
  docker stack deploy --prune --compose-file 0-docker-stack-scylladb.yml $STACK_NAME

popd
