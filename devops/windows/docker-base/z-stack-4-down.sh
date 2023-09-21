#!/bin/bash

#set -e
set -u

source ./0-conf.env
docker stack rm ${STACK_NAME}
echo
