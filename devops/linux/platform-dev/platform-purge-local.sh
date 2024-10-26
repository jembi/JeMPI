#!/bin/bash
set -e
set -m

pushd ../platform
  ./purge-local.sh
popd
