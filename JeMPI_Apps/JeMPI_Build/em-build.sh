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
 
pushd ../JeMPI_EM
  mvn clean package
  cp ./target/EM-1.0-SNAPSHOT-spring-boot.jar ../JeMPI_Build/images/em/.
  
  pushd ../JeMPI_Build/images/em
    export JAR_FILE=EM-1.0-SNAPSHOT-spring-boot.jar
    envsubst < ../templates/Dockerfile-controller > Dockerfile
    [ -z $(docker images -q ${EM_IMAGE}) ] || docker rmi ${EM_IMAGE}
    docker system prune --volumes -f
    echo $EM_IMAGE
    docker build --tag $EM_IMAGE .
  popd

popd

