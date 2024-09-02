#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf.env
source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh

rm -f ./.env

envsubst < $PROJECT_DEVOPS_DIR/conf/ui/.env > ./.env

if [ "$CI" = "true" ]; then
    docker buildx build --tag "jembi/$UI_HUB_IMAGE:$TAG" --push --target "$NODE_ENV-stage" --platform "linux/amd64,linux/arm64" --builder=container .
else
    docker build --tag "$UI_IMAGE" --target "$NODE_ENV-stage" .
fi
