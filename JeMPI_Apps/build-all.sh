#!/bin/bash

set -e
set -u

pushd JeMPI_LibMPI
  ./build.sh || exit 1
popd
pushd JeMPI_Test_02
  mvn clean
  mvn package
popd
pushd JeMPI_Stats
  mvn clean
  mvn package
popd
pushd JeMPI_EM_Ref
  mvn clean
  mvn package
popd
pushd JeMPI_Test_01
  ./build.sh || exit 1
popd
pushd JeMPI_Staging_01
  ./build.sh || exit 1
popd
pushd JeMPI_Input_02
  ./build.sh || exit 1
popd
pushd JeMPI_Staging_02
  ./build.sh || exit 1
popd
pushd JeMPI_InputDISI
  ./build.sh || exit 1
popd 
pushd JeMPI_StagingDISI
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
pushd JeMPI_Journal
  ./build.sh || exit 1
popd
pushd JeMPI_Notifications
  ./build.sh || exit 1
popd
pushd JeMPI_Test_01
  ./push.sh
popd
pushd JeMPI_Staging_01
  ./push.sh
popd
pushd JeMPI_Input_02
  ./push.sh
popd
pushd JeMPI_Staging_02
  ./push.sh
popd
pushd JeMPI_InputDISI
  ./push.sh
popd
pushd JeMPI_StagingDISI
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
pushd JeMPI_Journal
  ./push.sh
popd
pushd JeMPI_Notifications
  ./push.sh
popd

