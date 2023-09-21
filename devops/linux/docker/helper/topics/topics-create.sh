#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env
  source ./helper/topics/topics-config.sh

  RETENTION_PERIOD_MS=`echo "24*60*60*1000" | bc`
  SEGMENT_BYTES=`echo "1024*1024*4" | bc`

  if [[ "SPEC_SETTINGS" == "high" ]]; then  replication=REPLICATION[$TOPIC]; else replication=1; fi

  for TOPIC in ${TOPICS[@]}; do
    docker exec $(docker ps -q -f name=kafka-01) kafka-topics.sh \
    --bootstrap-server kafka-01:9092 \
    --create \
    --replication-factor ${replication} \
    --partitions ${PARTITIONS[$TOPIC]} \
    --config "retention.ms=${RETENTION_MS[$TOPIC]}" \
    --config "segment.bytes=${SEGMENT_BYTES[$TOPIC]}" \
    --topic $TOPIC 
  done

popd