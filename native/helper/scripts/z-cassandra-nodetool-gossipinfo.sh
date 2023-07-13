#!/bin/bash

set -e 
set =u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source 0-conf.env
  source conf/images/conf-hub-images.sh

  docker exec $(docker ps -q -f name=JeMPI_cassandra-1) nodetool gossipinfo

popd