#!/bin/bash

set -e
set -u

source $PROJECT_DEVOPS_DIR/conf.env
source $PROJECT_DEVOPS_DIR/conf/images/conf-app-images.sh

rm -f ./.env

envsubst < $PROJECT_DEVOPS_DIR/conf/ui/.env > ./.env

docker build --tag $UI_IMAGE --target $NODE_ENV-stage .
