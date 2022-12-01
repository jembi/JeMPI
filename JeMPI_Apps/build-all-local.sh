#!/bin/bash

FILE_PATH=$(
  cd "$(dirname "${BASH_SOURCE[0]}")" || exit
  pwd -P
)
readonly FILE_PATH

mvn_clean() {
  pushd JeMPI_FHIRsyncSender
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
}

build() {
  mv "$FILE_PATH"/../docker/conf/images/conf-app-images.sh "$FILE_PATH"/../docker/conf/images/conf-app-images-original.sh

  cp "$FILE_PATH"/../docker/conf/images/conf-app-images-local.sh "$FILE_PATH"/../docker/conf/images/conf-app-images.sh

  pushd JeMPI_LibMPI
    ./build.sh || return
  popd

  mvn_clean || return

  pushd JeMPI_AsyncReceiver
    ./build.sh || return
  popd

  pushd JeMPI_SyncReceiver
    ./build.sh || return
  popd

  pushd JeMPI_PreProcessor
    ./build.sh || return
  popd

  pushd JeMPI_Controller
    ./build.sh || return
  popd

  pushd JeMPI_EM
    ./build.sh || return
  popd

  pushd JeMPI_Linker
    ./build.sh || return
  popd

  pushd JeMPI_API
    ./build.sh || return
  popd
}

main() {
  if ! build; then
    RED='\033[1;31m'
    echo -e "${RED}[ERROR] encountered error during build"
    mv "$FILE_PATH"/../docker/conf/images/conf-app-images-original.sh "$FILE_PATH"/../docker/conf/images/conf-app-images.sh
    exit 1
  fi

  mv "$FILE_PATH"/../docker/conf/images/conf-app-images-original.sh "$FILE_PATH"/../docker/conf/images/conf-app-images.sh

  GREEN='\033[1;32m'
  echo -e "${GREEN}[INFO] images successfully built"
}

main "$@"
