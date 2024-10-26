#!/bin/bash

set -e
set -u

CONTAINER=$(docker ps -aqf "name=jempi_jempi-async-receiver.1")
echo $CONTAINER
docker logs --follow ${CONTAINER}
