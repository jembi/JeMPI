#!/bin/bash

set -e
set -u

java --enable-preview -DLOG4J2_LEVEL="DEBUG" -DKAFKA_BOOTSTRAP_SERVERS="localhost:29092" -DKAFKA_APPLICATION_ID="app-id-api" -DPOSTGRESQL_SERVER="localhost:5432" -DPOSTGRESQL_DATABASE="notifications" -DPOSTGRESQL_USER="postgres" -DPOSTGRESQL_PASSWORD="postgres" -DHTTP_SERVER_PORT=50001 -DDGRAPH_HOSTS="localhost" -DDGRAPH_PORTS="9080" -jar ../JeMPI_Apps/JeMPI_API/target/API-1.0-SNAPSHOT-spring-boot.jar