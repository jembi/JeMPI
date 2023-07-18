#!/bin/bash

set -e
set -u

java -DLOG4J2_LEVEL="TRACE" -DKAFKA_BOOTSTRAP_SERVERS="localhost:29092" -DKAFKA_CLIENT_ID_NOTIFICATIONS="client-id-lnk3" -DKAFKA_APPLICATION_ID_INTERACTIONS="app-id-lnk1" -DKAFKA_APPLICATION_ID_MU="app-id-lnk2" -DPOSTGRESQL_SERVER="localhost:5432" -DPOSTGRESQL_DATABASE="notifications" -DPOSTGRESQL_USER="postgres" -DPOSTGRESQL_PASSWORD="postgres" -DHTTP_SERVER_PORT=50021 -DLINKER_MATCH_THRESHOLD=0.90 -DLINKER_MATCH_THRESHOLD_MARGIN=0.1 -DDGRAPH_HOSTS="localhost" -DDGRAPH_PORTS="9080" -DUSE_DGRAPH=true -DAPI_SERVER="localhost:50001" -jar ../JeMPI_Apps/JeMPI_Linker/target/Linker-1.0-SNAPSHOT-spring-boot.jar