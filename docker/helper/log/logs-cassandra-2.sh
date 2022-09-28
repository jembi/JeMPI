#!/bin/bash

set -e
set -u

source ../../0-conf.env
docker service logs -f --raw ${STACK_NAME}_cassandra-2
echo
