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

 
pushd ../JeMPI_Staging_01
  mvn clean package
  cp ./target/Staging_01-1.0-SNAPSHOT-spring-boot.jar ../JeMPI_Build/images/staging-01/.
  
  pushd ../JeMPI_Build/images/staging-01
    export JAR_FILE=Staging_01-1.0-SNAPSHOT-spring-boot.jar
    envsubst < ../templates/Dockerfile-staging-01 > Dockerfile
    [ -z $(docker images -q ${STAGING_01_IMAGE}) ] || docker rmi ${STAGING_01_IMAGE}
    docker system prune --volumes -f
    docker build --tag $STAGING_01_IMAGE .
  popd

popd

