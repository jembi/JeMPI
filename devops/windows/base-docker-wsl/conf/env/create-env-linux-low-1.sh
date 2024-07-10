#!/usr/bin/bash

export USE_LOCAL_REGISTRY=${USE_LOCAL_REGISTRY:-"true"}
export PROJECT_DIR=$(builtin cd ../../; pwd)
export PROJECT_DATA_DIR=${PROJECT_DIR}/docker_data/data
export PROJECT_DATA_MONITOR_DIR=${PROJECT_DIR}/docker_data/data-monitor

export NODE1=$(hostname)
# export NODE1_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | head -1 | awk '{ print $2 }')
export NODE1_IP=$(ip addr show eth0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}')

export SCALE_NGINX=1
export SCALE_KEYCLOAK_TEST_SERVER=1
export SCALE_KAFKA_01=1
export SCALE_ZERO_01=1
export SCALE_ALPHA_01=1
export SCALE_RATEL=1
export SCALE_POSTGRESQL=1
export SCALE_LINKER=1

export POSTGRESQL_USERNAME="postgres"
export POSTGRESQL_PASSWORD="postgres"
export POSTGRESQL_DATABASE="notifications"

export KAFKA_SERVERS="kafka-01:9092"
export DGRAPH_HOSTS="alpha-01"
export DGRAPH_PORTS="9080"

# ram limit for linker
export POSTGRESQL_RAM_LIMIT="8G"
export KEYCLOAK_TEST_SERVER_RAM_LIMIT="8G"
export NGINX_RAM_LIMIT="8G"
export HAPROXY_RAM_LIMIT="8G"
export KAFKA_RAM_LIMIT="8G"
export DGRAPH_RAM_LIMIT="16G"

# DON'T CHANGE
export REGISTRY_NODE_IP=${NODE1}:5000/v2

if [ $USE_LOCAL_REGISTRY == 'true' ]; then
    export IMAGE_REGISTRY="$REGISTRY_NODE_IP/"
else
    export IMAGE_REGISTRY=""
fi

envsubst < conf-env-low-1-pc.template > ../../conf.env
