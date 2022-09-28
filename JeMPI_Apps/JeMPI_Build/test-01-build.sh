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

 
pushd ../JeMPI_Test_01
  mvn clean package
  cp ./target/Test_01-1.0-SNAPSHOT-spring-boot.jar ../JeMPI_Build/images/test-01/.
  
  pushd ../JeMPI_Build/images/test-01
    export JAR_FILE=Test_01-1.0-SNAPSHOT-spring-boot.jar
    envsubst < ../templates/Dockerfile-test-01 > Dockerfile
    [ -z $(docker images -q ${TEST_01_IMAGE}) ] || docker rmi ${TEST_01_IMAGE}
    docker system prune --volumes -f
    docker build --tag $TEST_01_IMAGE .
  popd

popd

