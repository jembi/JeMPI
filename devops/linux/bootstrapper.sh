#!/bin/bash

pushd "$(dirname "$0")"
  # Creating conf.env file
  pushd ./docker/conf/env || exit
      source ./create-env-linux-high-1.sh
  popd || exit

  source ./source/conf.env

export POSTGRESQL_IP=127.0.0.1
export POSTGRESQL_PORT=5432
export POSTGRESQL_USER=postgres
export POSTGRESQL_PASSWORD=test
export POSTGRESQL_DATABASE=testdb

export KAFKA_BOOTSTRAP_SERVERS="localhost:9097"
export KAFKA_APPLICATION_ID=another_value

export DGRAPH_HOSTS=127.0.0.1
export DGRAPH_PORTS=9080
export DGRAPH_HTTP_PORTS=8081

export MONITOR_HTTP_PORT=7070

export LINKER_IP=another_value
export LINKER_HTTP_PORT=6000

export API_IP=127.0.0.1
export API_HTTP_PORT=5000
export API_KC_HTTP_PORT=5000
export LOG4J2_LEVEL=Debug

  java_args="${@:1}"

  pushd ../../JeMPI_Apps/JeMPI_Bootstrapper
    mvn compile exec:java  -Dexec.mainClass="org.jembi.jempi.bootstrapper.BootstrapperCLI" -Dexec.args="$java_args"
  popd
popd