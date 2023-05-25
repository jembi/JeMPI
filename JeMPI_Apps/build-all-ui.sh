#!/bin/bash

set -e
set -u

pushd JeMPI_UI
  ./build-image.sh || exit 1
popd

pushd JeMPI_UI
  ./push.sh
popd
