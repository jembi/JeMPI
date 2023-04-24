#!/bin/bash

set -e
set -u

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
