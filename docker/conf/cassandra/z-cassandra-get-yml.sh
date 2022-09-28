#!/bin/bash

set -e 
set =u

source ../../0-conf.env
source ../images/conf-hub-images.sh

docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) ls /opt/bitnami/cassandra/conf -la
#docker cp  $(docker ps -q -f name=JeMPI_cassandra-1):/opt/bitnami/cassandra/conf/cassandra.yaml  cassandra-1.yaml
#docker cp  $(docker ps -q -f name=JeMPI_cassandra-2):/opt/bitnami/cassandra/conf/cassandra.yaml  cassandra-2.yaml
#docker cp  $(docker ps -q -f name=JeMPI_cassandra-3):/opt/bitnami/cassandra/conf/cassandra.yaml  cassandra-3.yaml

docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) free
docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) free -m
docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) free -g
docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) free -m | awk '/:/ {print $2;exit}'

docker exec -ti $(docker ps -q -f name=JeMPI_cassandra-1) egrep -c 'processor([[:space:]]+):.*' /proc/cpuinfo

docker cp  $(docker ps -q -f name=JeMPI_cassandra-1):/opt/bitnami/cassandra/conf/cassandra-env.sh  cassandra-env-1.sh
docker cp  $(docker ps -q -f name=JeMPI_cassandra-2):/opt/bitnami/cassandra/conf/cassandra-env.sh  cassandra-env-2.sh
docker cp  $(docker ps -q -f name=JeMPI_cassandra-3):/opt/bitnami/cassandra/conf/cassandra-env.sh  cassandra-env-3.sh

