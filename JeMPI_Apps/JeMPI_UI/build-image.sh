#!/bin/bash

set -e
set -u

source ../../docker/0-conf.env
source ../../docker/conf/images/conf-app-images.sh

rm -f ./.env

envsubst < ../../docker/conf/ui/.env > ./.env

[ -z $(docker images -q ${UI_IMAGE}) ] || docker rmi ${UI_IMAGE}
docker system prune --volumes -f
# Injects env vars in .env as build args for use when the UI is built
docker build --tag "$UI_IMAGE" $(cat .env | sed '/^ *$/d' | sed 's@^@--build-arg @g' | paste -s -d " ") --target production-stage .
