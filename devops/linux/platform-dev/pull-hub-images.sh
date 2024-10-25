#!/bin/bash

set -e
set -u

source ./conf.env
#source ./conf/images/conf-hub-images.sh
source ./conf/images/conf-app-images.sh

docker pull $JAVA_BASE_IMAGE
docker pull node:erbium-alpine
docker pull openhie/package-base:2.3.2
docker pull jembi/platform:latest 
docker pull jembi/openhim-core:v8.5.0
docker pull jembi/openhim-console:v1.18.2
docker pull mongo:4.2
docker pull jembi/await-helper:1.0.1
docker pull bitnami/kafka:3.4.0
docker pull obsidiandynamics/kafdrop:3.27.0
docker pull quay.io/cloudhut/kminion:master
docker pull grafana/grafana-oss:9.2.3
docker pull grafana/loki:2.6.1
docker pull grafana/promtail:2.6.1
docker pull prom/prometheus:v2.38.0
docker pull quay.io/minio/minio:RELEASE.2022-10-24T18-35-07Z
docker pull bitnami/postgresql-repmgr:14
docker pull bitnami/pgpool:4.4.3
docker pull jembi/openhim-mediator-mapping:v3.3.0
docker pull keycloak/keycloak:20.0
docker pull bitnami/postgresql-repmgr:15.2.0
docker pull dgraph/dgraph:v23.1.1
docker pull dgraph/ratel:v21.03.2



