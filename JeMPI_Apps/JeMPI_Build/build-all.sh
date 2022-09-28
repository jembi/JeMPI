#!/bin/bash

set -e
set -u

./build-libmpi.sh
pushd ../JeMPI_Test_02
  mvn clean
  mvn package
popd
pushd ../JeMPI_Stats
  mvn clean
  mvn package
popd
pushd ../JeMPI_EM_Ref
  mvn clean
  mvn package
popd
pushd ../JeMPI_Test_02
  mvn clean
  mvn package
popd
pushd ../JeMPI_Stats
  mvn clean
  mvn package
popd
./journal-build.sh
./notifications-build.sh
./test-01-build.sh
./staging-01-build.sh
./controller-build.sh
./em-build.sh
./linker-build.sh
./api-build.sh

./journal-push.sh
./notifications-push.sh
./test-01-push.sh
./staging-01-push.sh
./controller-push.sh
./em-push.sh
./linker-push.sh
./api-push.sh

