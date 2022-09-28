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

JAR_FILE=$LINKER_JAR
APP_IMAGE=$LINKER_IMAGE
APP=linker

pushd ../JeMPI_Linker
  mvn clean package
  cp ./target/$JAR_FILE ../JeMPI_Build/images/$APP/.
  
  pushd ../JeMPI_Build/images/$APP    
    export JAR_FILE=$JAR_FILE
    envsubst < ../templates/Dockerfile-$APP > Dockerfile
    [ -z $(docker images -q ${APP_IMAGE}) ] || docker rmi ${APP_IMAGE}
    docker system prune --volumes -f
    echo $APP_IMAGE
    docker build --tag $APP_IMAGE .
  popd

popd

