#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env
  source ./helper/topics/topics-config.sh

  for TOPIC in ${TOPICS[@]}; do
    echo $TOPIC
    docker exec $(docker ps -q -f name=kafka-01) kafka-topics.sh --bootstrap-server kafka-01:9092 --describe --topic $TOPIC
  done  

popd