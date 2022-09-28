#!/bin/bash

set -e 
set -u

docker exec $(docker ps -q -f name=kafka-1) kafka-topics.sh --help 
