#!/bin/bash

set -e
set -u

# Copy Config for API
cp -f ./JeMPI_Configuration/config-reference.json ./JeMPI_API/src/main/resources/config-reference.json

pushd JeMPI_UI
  ./build-image.sh || exit 1
popd

pushd JeMPI_UI
  ./push.sh
popd
