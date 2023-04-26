#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh

[ -z $(docker images -q ${UI_IMAGE}) ] || docker rmi ${UI_IMAGE}
docker system prune --volumes -f
docker build --tag $UI_IMAGE .