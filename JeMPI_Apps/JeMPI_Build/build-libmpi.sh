#!/bin/bash

set -e
set -u

source ../../docker/conf/images/conf-app-images.sh
source ./build-check-jdk.sh

pushd ../JeMPI_LibMPI
  mvn clean package
  mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=target/LibMPI-1.0-SNAPSHOT.jar
  # mvn org.apache.maven.plugins:maven-install-plugin:3.0.1:install-file -Dfile=target/LibMPI-1.0-SNAPSHOT.jar
popd

