#!/bin/bash

pwd
cp ./target/scala-2.13/$JAR_FILE ./docker/.
  
pushd ./docker
  export JAR_FILE=$JAR_FILE
  [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
  docker system prune --volumes -f
  docker build --build-arg JAVA_VERSION=${JAVA_VERSION_X} --tag $APP_IMAGE .
popd
