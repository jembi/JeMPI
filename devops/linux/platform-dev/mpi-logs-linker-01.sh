#!/bin/bash

set -e
set -u

CONTAINER=$(docker ps -aqf "name=jempi_jempi-linker.1")
echo $CONTAINER
docker logs --follow ${CONTAINER}
