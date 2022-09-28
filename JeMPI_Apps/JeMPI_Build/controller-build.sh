#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh

if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo "no java"
    exit
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    if [[ "$version" != "$JAVA_VERSION" ]]; then
        echo java version $version is the wrong version
        exit
    fi
fi

 
pushd ../JeMPI_Controller
  mvn clean package
  cp ./target/Controller-1.0-SNAPSHOT-spring-boot.jar ../JeMPI_Build/images/controller/.
  
  pushd ../JeMPI_Build/images/controller
    export JAR_FILE=Controller-1.0-SNAPSHOT-spring-boot.jar
    envsubst < ../templates/Dockerfile-controller > Dockerfile
    [ -z $(docker images -q ${CONTROLLER_IMAGE}) ] || docker rmi ${CONTROLLER_IMAGE}
    docker system prune --volumes -f
    docker build --tag $CONTROLLER_IMAGE .
  popd

popd

