#!/bin/bash

pushd "$(dirname "$0")"
  # Creating conf.env file
  pushd ./docker/conf/env || exit
      source ./create-env-linux-high-1.sh
  popd || exit

  source ./source/conf.env

  java_args="${@:1}"

  pushd ../../JeMPI_Apps/JeMPI_Bootstrapper
    mvn compile exec:java  -Dexec.mainClass="org.jembi.jempi.bootstrapper.BootstrapperCLI" -Dexec.args="$java_args"
  popd
popd