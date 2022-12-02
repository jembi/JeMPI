#!/bin/bash

JAVA_VERSION=17.0.5
JAVA_VERSION_X=${JAVA_VERSION}_8

# https://hub.docker.com/_/eclipse-temurin/tags
export JAVA_BASE_IMAGE=eclipse-temurin:${JAVA_VERSION_X}-jre

# ------- image names -------

export ASYNC_RECEIVER_IMAGE=jembi/jempi-async-receiver:latest
export SYNC_RECEIVER_IMAGE=jembi/jempi-sync-receiver:latest
export PREPROCESSOR_IMAGE=jembi/jempi-pre-processor:latest
export CONTROLLER_IMAGE=jembi/jempi-controller:latest
export EM_IMAGE=jembi/jempi-em-calculator:latest
export LINKER_IMAGE=jembi/jempi-linker:latest
export API_IMAGE=jembi/jempi-api:latest

# ------- JAR names -------

export ASYNC_RECEIVER_JAR=AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar
export SYNC_RECEIVER_JAR=SyncReceiver-1.0-SNAPSHOT-spring-boot.jar
export PREPROCESSOR_JAR=PreProcessor-1.0-SNAPSHOT-spring-boot.jar
export CONTROLLER_JAR=Controller-1.0-SNAPSHOT-spring-boot.jar
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar
