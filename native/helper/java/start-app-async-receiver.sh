#!/bin/bash

set -e
set -u

java -DLOG4J2_LEVEL="DEBUG" -DKAFKA_BOOTSTRAP_SERVERS="localhost:29092" -DKAFKA_CLIENT_ID="client-id-syncrx" -DCSV_DIR="./native_data/data-apps/async_receiver/csv/" -jar ../JeMPI_Apps/JeMPI_AsyncReceiver/target/AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar