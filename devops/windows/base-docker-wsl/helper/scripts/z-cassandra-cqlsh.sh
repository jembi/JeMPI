#!/bin/bash

set -e 
set =u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source conf/images/conf-hub-images.sh

  docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) cqlsh --username cassandra --password cassandra cassandra-1

popd