#!/bin/bash

set -e
set -u

./build-libmpi.sh || exit 1
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
./build-test-01.sh || exit 1
./build-staging-01.sh || exit 1
./build-input-02.sh || exit 1
./build-staging-02.sh || exit 1
./build-input-disi.sh || exit 1
./build-staging-disi.sh || exit 1
./build-controller.sh || exit 1
./build-em.sh || exit 1
./build-linker.sh || exit 1
./build-api.sh || exit 1
./build-journal.sh || exit 1
./build-notifications.sh || exit 1

./push-test-01.sh
./push-staging-01.sh
./push-input-02.sh
./push-staging-02.sh
./push-input-disi.sh
./push-staging-disi.sh
./push-controller.sh
./push-em.sh
./push-linker.sh
./push-api.sh
./push-journal.sh
./push-notifications.sh

