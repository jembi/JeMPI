#!/bin/bash

set -e
set -u

# Copy Config for API
cp -f ./JeMPI_Configuration/config-reference.json ./JeMPI_API/src/main/resources/config-reference.json

mvn clean package

pushd JeMPI_AsyncReceiver
  ./build.sh || exit 1
popd
pushd JeMPI_SyncReceiver
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
pushd JeMPI_UI
  ./build-image.sh || exit 1
popd

pushd JeMPI_AsyncReceiver
  ./push.sh
popd
pushd JeMPI_SyncReceiver
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
pushd JeMPI_UI
  ./push.sh
popd