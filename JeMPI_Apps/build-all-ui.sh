#!/bin/bash

set -e
set -u

pushd JeMPI_UI
./build-image.sh || exit 1
popd

if [ -z "$IMAGE_REGISTRY" ]; then
  # If using local registry stop here, no need to push
  return
fi

pushd JeMPI_UI
./push.sh
popd
