#!/bin/bash

set -e
set -u

#trap '' INT

pushd .
SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
cd ${SCRIPT_DIR}/../..

source ./0-conf.env

docker service scale ${STACK_NAME}_kafka-1=${SCALE_KAFKA_1}
docker service scale ${STACK_NAME}_kafka-2=${SCALE_KAFKA_2}
docker service scale ${STACK_NAME}_kafka-3=${SCALE_KAFKA_3}

docker service scale ${STACK_NAME}_zero=${SCALE_ZERO}
docker service scale ${STACK_NAME}_alpha1=${SCALE_ALPHA1}
docker service scale ${STACK_NAME}_alpha2=${SCALE_ALPHA2}
docker service scale ${STACK_NAME}_alpha3=${SCALE_ALPHA3}
docker service scale ${STACK_NAME}_ratel=${SCALE_RATEL}

docker service scale ${STACK_NAME}_prometheus=${SCALE_PROMETHEUS}
docker service scale ${STACK_NAME}_grafana=${SCALE_GRAFANA}

pushd helper/topics
./topics-create.sh
./topics-list.sh
popd

popd
