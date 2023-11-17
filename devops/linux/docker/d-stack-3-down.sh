#!/bin/bash

#set -e
set -u

source ./conf.env
docker stack rm ${STACK_NAME}
echo
