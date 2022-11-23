#!/bin/bash

set -e
set -u

pushd JeMPI_LibMPI
  ./build.sh || exit 1
popd

pushd JeMPI_Stats
  mvn clean
  mvn package
popd
pushd JeMPI_EM_Ref
  mvn clean
  mvn package
popd

pushd JeMPI_AsyncReceiver
  ./build.sh || exit 1
popd
pushd JeMPI_SyncReceiver
  ./build.sh || exit 1
popd
pushd JeMPI_PreProcessor
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
pushd JeMPI_SyncReceiver
  ./push.sh
popd
pushd JeMPI_PreProcessor
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

