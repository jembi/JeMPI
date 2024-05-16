#!/bin/bash

set -e
set -u

if [ $# -eq 0 ]; then
    tag_to_use="ci-test-main" 
else
    tag_to_use=$1
fi

pushd JeMPI_Configuration
  ./create.sh reference/config-reference.json 
popd

cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API/src/main/resources/config-api.json
cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API_KC/src/main/resources/config-api.json

mvn clean package
pushd JeMPI_EM_Scala
  sbt clean assembly
popd

# Create a new builder instance named "container" using the docker-container driver
docker buildx create --name container --driver=docker-container

pushd JeMPI_AsyncReceiver
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_ETL
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Controller
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_EM_Scala
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Linker
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_API
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_API_KC
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Bootstrapper
  CI=true TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_UI
  CI=true TAG=$tag_to_use ./build-image.sh || exit 1
popd