#!/bin/bash

set -e
set -u

cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API/src/main/resources/config-api.json
cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API_KC/src/main/resources/config-api.json

CI="${CI:-false}"
if [ $# -eq 0 ]; then
    tag_to_use="ci-test-main" 
else
    tag_to_use=$1
fi
mvn clean package
pushd JeMPI_EM_Scala
  sbt clean assembly
popd

pushd JeMPI_AsyncReceiver
  CI=CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_ETL
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Controller
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_EM_Scala
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Linker
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_API
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_API_KC
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_Bootstrapper
  CI=$CI TAG=$tag_to_use ./build.sh || exit 1
popd
pushd JeMPI_AsyncReceiver
  ./push.sh
popd
pushd JeMPI_ETL
  ./push.sh
popd
pushd JeMPI_Controller
  ./push.sh
popd
pushd JeMPI_EM_Scala
  ./push.sh
popd
pushd JeMPI_Linker
  ./push.sh
popd
pushd JeMPI_API
  ./push.sh
popd
pushd JeMPI_API_KC
  ./push.sh
popd
pushd JeMPI_Bootstrapper
  ./push.sh
popd

