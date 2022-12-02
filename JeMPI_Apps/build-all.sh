#!/bin/bash

mvn_clean() {
  pushd JeMPI_FHIRsyncSender || return
  mvn clean
  mvn package
  popd || return

  pushd JeMPI_Stats || return
  mvn clean
  mvn package
  popd || return

  pushd JeMPI_EM_Ref || return
  mvn clean
  mvn package
  popd || return
}

build() {
  pushd JeMPI_LibMPI || return
  ./build.sh || return
  popd || return

  mvn_clean || return

  pushd JeMPI_AsyncReceiver || return
  ./build.sh || return
  popd || return

  pushd JeMPI_SyncReceiver || return
  ./build.sh || return
  popd || return

  pushd JeMPI_PreProcessor || return
  ./build.sh || return
  popd || return

  pushd JeMPI_Controller || return
  ./build.sh || return
  popd || return

  pushd JeMPI_EM || return
  ./build.sh || return
  popd || return

  pushd JeMPI_Linker || return
  ./build.sh || return
  popd || return

  pushd JeMPI_API || return
  ./build.sh || return
  popd || return
}

push_to_local_registry() {
  pushd JeMPI_AsyncReceiver || return
  ./push.sh || return
  popd || return

  pushd JeMPI_SyncReceiver || return
  ./push.sh || return
  popd || return

  pushd JeMPI_PreProcessor || return
  ./push.sh || return
  popd || return

  pushd JeMPI_Controller || return
  ./push.sh || return
  popd || return

  pushd JeMPI_EM || return
  ./push.sh || return
  popd || return

  pushd JeMPI_Linker || return
  ./push.sh || return
  popd || return

  pushd JeMPI_API || return
  ./push.sh || return
  popd || return
}

main() {
  declare -r YELLOW='\033[1;33m'
  declare -r COLOUR_OFF='\033[0m'
  if [[ -z $1 ]] || [[ $1 != 'true' ]] && [[ $1 != 'false' ]]; then
    echo -e "${YELLOW}[WARN] missing/invalid option 'with_local_registry', continuing with default (false), to build with local registry, do './build-all.sh true'${COLOUR_OFF}"
  fi

  declare -r RED='\033[1;31m'
  declare -r local_dir=$(pwd)
  if ! build; then
    echo -e "${RED}[ERROR] failure in $local_dir/build-all.sh/build()${COLOUR_OFF}"
    exit 1
  fi

  declare -r with_local_registry=${1:-'false'}
  if [[ $with_local_registry == 'true' ]]; then
    if ! push_to_local_registry; then
      echo -e "${RED}[ERROR] failure in $local_dir/build-all.sh/push_to_local_registry()${COLOUR_OFF}"
      exit 1
    fi
  fi

  declare -r GREEN='\033[1;32m'
  echo -e "${GREEN}[INFO] images successfully built${COLOUR_OFF}"
}

main "$@"
