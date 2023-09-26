#!/bin/bash

set -e
set -u

source ../../conf.env
docker service logs -f --raw ${STACK_NAME}_keycloak-test-server
echo
