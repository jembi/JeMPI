#!/bin/bash

mvn clean package
cp ./target/$JAR_FILE ./docker/.
  
pushd ./docker
  export JAR_FILE=$JAR_FILE
#  envsubst < ../templates/Dockerfile-$APP > Dockerfile
  [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
  docker system prune --volumes -f
  docker build --tag $APP_IMAGE .
popd
