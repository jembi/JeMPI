#!/bin/bash

set -e
set -u

java -DLOG4J2_LEVEL="DEBUG" -DKAFKA_BOOTSTRAP_SERVERS="localhost:29092" -DKAFKA_CLIENT_ID="client-id-ctl" -DKAFKA_APPLICATION_ID="app-id-ctl" -DPOSTGRESQL_SERVER="localhost:5432" -DPOSTGRESQL_DATABASE="notifications" -DPOSTGRESQL_USER="postgres" -DPOSTGRESQL_PASSWORD="postgres" -DHTTP_SERVER_PORT=50010 -jar ../JeMPI_Apps/JeMPI_Controller/target/Controller-1.0-SNAPSHOT-spring-boot.jar