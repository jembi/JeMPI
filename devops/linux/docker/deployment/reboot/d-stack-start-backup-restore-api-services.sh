#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd "${SCRIPT_DIR}/../.."

  # Check if conf.env exists in the current directory
  if [ ! -f "./conf.env" ]; then
    echo "conf.env does not exist in the current directory."
  else
    echo "conf.env exists and can be sourced."
  fi

  source ./conf.env

  docker service scale jempi_backup-restore-api=1
  echo

popd  


