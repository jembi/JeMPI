#!/bin/bash

#mvn clean package
pwd
cp ./target/$JAR_FILE ./docker/.
  
pushd ./docker
  export JAR_FILE=$JAR_FILE
#  envsubst < ../templates/Dockerfile-$APP > Dockerfile
  [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
  docker system prune --volumes -f
  docker build --build-arg JAVA_VERSION=${JAVA_VERSION_X} --tag $APP_IMAGE .
popd
