#!/bin/bash

set -e
set -u

pushd ../JeMPI_Test_02
  mvn versions:display-plugin-updates
  mvn versions:display-dependency-updates
popd
