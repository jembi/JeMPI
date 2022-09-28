#!/bin/bash

set -e
set -u

pushd ../JeMPI_API
  mvn versions:display-plugin-updates
  mvn versions:display-dependency-updates
popd
