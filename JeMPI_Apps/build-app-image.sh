#!/bin/bash

pwd
cp ./target/$JAR_FILE ./docker/.
  
pushd ./docker
  export JAR_FILE=$JAR_FILE
  [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
  docker system prune --volumes -f
  docker build --no-cache --build-arg JAVA_VERSION=${JAVA_VERSION_X} --tag $APP_IMAGE .
popd