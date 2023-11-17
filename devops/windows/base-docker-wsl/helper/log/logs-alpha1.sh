#!/bin/bash

set -e
set -u

source ../../0-conf.env
docker service logs --follow --raw ${STACK_NAME}_alpha-01
echo
