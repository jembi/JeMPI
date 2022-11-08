#!/bin/bash

mvn clean package
cp ./target/$JAR_FILE ../JeMPI_Build/images/$APP/.
  
pushd ../JeMPI_Build/images/$APP
  export JAR_FILE=$JAR_FILE
  envsubst < ../templates/Dockerfile-$APP > Dockerfile
  [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
  docker system prune --volumes -f
  docker build --tag $APP_IMAGE .
popd
