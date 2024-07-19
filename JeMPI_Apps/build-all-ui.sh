#!/bin/bash

set -e
set -u
if [ $# -eq 0 ]; then
    tag_to_use="ci-test-main" 
else
    tag_to_use=$1
fi

pushd JeMPI_UI
  CI=false TAG=$tag_to_use ./build-image.sh || exit 1
popd

pushd JeMPI_UI
  ./push.sh
popd
