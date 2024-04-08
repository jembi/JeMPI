#!/bin/bash

set -e
set -u

pushd JeMPI_UI
  ./build-image-init.sh || exit 1
popd

pushd JeMPI_UI
  ./push.sh
popd
