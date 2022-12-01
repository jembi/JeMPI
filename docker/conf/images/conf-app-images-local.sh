#!/bin/bash

FILE_PATH=$(
  cd "$(dirname "${BASH_SOURCE[0]}")" || exit
  pwd -P
)

source "$FILE_PATH"/conf-app-images-original.sh

# ------- image names -------

export ASYNC_RECEIVER_IMAGE=jembi/jempi-async-receiver:0.1.1
export SYNC_RECEIVER_IMAGE=jembi/jempi-sync-receiver:0.1.1
export PREPROCESSOR_IMAGE=jembi/jempi-pre-processor:0.1.1
export CONTROLLER_IMAGE=jembi/jempi-controller:0.1.1
export EM_IMAGE=jembi/jempi-em-calculator:0.1.1
export LINKER_IMAGE=jembi/jempi-linker:0.1.1
export API_IMAGE=jembi/jempi-api:0.1.1

# ------- JAR names -------

export ASYNC_RECEIVER_JAR=AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar
export SYNC_RECEIVER_JAR=SyncReceiver-1.0-SNAPSHOT-spring-boot.jar
export PREPROCESSOR_JAR=PreProcessor-1.0-SNAPSHOT-spring-boot.jar
export CONTROLLER_JAR=Controller-1.0-SNAPSHOT-spring-boot.jar
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar
