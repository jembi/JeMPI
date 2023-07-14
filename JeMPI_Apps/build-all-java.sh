#!/bin/bash

set -e
set -u

cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API/src/main/resources/config-api.json
cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API_KC/src/main/resources/config-api.json

mvn clean package

pushd JeMPI_AsyncReceiver
  ./build.sh || exit 1
popd
pushd JeMPI_ETL
  ./build.sh || exit 1
popd
pushd JeMPI_Controller
  ./build.sh || exit 1
popd
pushd JeMPI_EM
  ./build.sh || exit 1
popd
pushd JeMPI_Linker
  ./build.sh || exit 1
popd
pushd JeMPI_API
  ./build.sh || exit 1
popd
pushd JeMPI_API_KC
  ./build.sh || exit 1
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
pushd JeMPI_EM
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

