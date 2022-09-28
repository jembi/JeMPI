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

pushd ../JeMPI_LibMPI
  mvn clean package
  mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=target/LibMPI-1.0-SNAPSHOT.jar
popd

