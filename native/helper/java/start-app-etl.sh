#!/bin/bash

set -e
set -u

java --enable-preview -DLOG4J2_LEVEL="DEBUG" -DKAFKA_BOOTSTRAP_SERVERS="localhost:29092" -DKAFKA_APPLICATION_ID="app-id-etl" -jar ../JeMPI_Apps/JeMPI_ETL/target/ETL-1.0-SNAPSHOT-spring-boot.jar
