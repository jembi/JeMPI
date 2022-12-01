#!/bin/bash

set -e 
set -u

docker exec $(docker ps -q -f name=jempi-kafka-01) kafka-topics.sh --bootstrap-server jempi-kafka-01:9092 --list
