#!/bin/bash

set -e
set -u

pushd JeMPI_EM_Scala
  sbt clean
  sbt assembly
popd

