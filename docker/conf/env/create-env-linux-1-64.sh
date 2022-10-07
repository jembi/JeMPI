#!/bin/bash

export PROJECT_DIR=$(builtin cd ../../; pwd)
export PROJECT_DATA_DIR=${PROJECT_DIR}/docker_data/data
export PROJECT_DATA_APPS_DIR=${PROJECT_DIR}/docker_data/data-apps
export PROJECT_DATA_MONITOR_DIR=${PROJECT_DIR}/docker_data/data-monitor

export NODE1=$(hostname)
export NODE1_IP=$(hostname -i)

export SCALE_ZOOKEEPER=1
export SCALE_KAFKA_1=1
export SCALE_KAFKA_2=1
export SCALE_KAFKA_3=1
export SCALE_ZERO=1
export SCALE_ALPHA1=1
export SCALE_ALPHA2=1
export SCALE_ALPHA3=1
export SCALE_LAMBDA=1
export SCALE_RATEL=1
export SCALE_CASSANDRA_1=0
export SCALE_CASSANDRA_2=0
export SCALE_CASSANDRA_3=0

export ZOOKEEPER_LIMIT_MEMORY=512M    
export KAFKA_1_LIMIT_MEMORY=3G
export KAFKA_2_LIMIT_MEMORY=3G
export KAFKA_3_LIMIT_MEMORY=3G
export ZERO_LIMIT_MEMORY=2G
export ALPHA_1_LIMIT_MEMORY=8G
export ALPHA_2_LIMIT_MEMORY=8G
export ALPHA_3_LIMIT_MEMORY=8G
export LAMBDA_LIMIT_MEMORY=2G
export RATEL_LIMIT_MEMORY=32M
export CASSANDRA_1_LIMIT_MEMORY=8G
export CASSANDRA_2_LIMIT_MEMORY=8G
export CASSANDRA_3_LIMIT_MEMORY=8G

export TEST_01_LIMIT_MEMORY=3G
export STAGING_01_LIMIT_MEMORY=3G

export INPUT_02_LIMIT_MEMORY=3G
export STAGING_02_LIMIT_MEMORY=3G

export INPUT_DISI_LIMIT_MEMORY=3G
export STAGING_DISI_LIMIT_MEMORY=3G

export CONTROLLER_LIMIT_MEMORY=5G
export EM_LIMIT_MEMORY=10G
export LINKER_LIMIT_MEMORY=6G
export API_LIMIT_MEMORY=2G
export JOURNAL_LIMIT_MEMORY=3G
export NOTIFICATIONS_LIMIT_MEMORY=3G

export ZOOKEEPER_RESERVATION_MEMORY=128M
export KAFKA_1_RESERVATION_MEMORY=512M
export KAFKA_2_RESERVATION_MEMORY=512M
export KAFKA_3_RESERVATION_MEMORY=512M
export ZERO_RESERVATION_MEMORY=512M
export ALPHA_1_RESERVATION_MEMORY=512M
export ALPHA_2_RESERVATION_MEMORY=512M
export ALPHA_3_RESERVATION_MEMORY=512M
export LAMBDA_RESERVATION_MEMORY=512M
export RATEL_RESERVATION_MEMORY=32M
export CASSANDRA_1_RESERVATION_MEMORY=512M
export CASSANDRA_2_RESERVATION_MEMORY=512M
export CASSANDRA_3_RESERVATION_MEMORY=512M

export TEST_01_RESERVATION_MEMORY=512M
export STAGING_01_RESERVATION_MEMORY=512M

export INPUT_02_RESERVATION_MEMORY=512M
export STAGING_02_RESERVATION_MEMORY=512M

export INPUT_DISI_RESERVATION_MEMORY=512M
export STAGING_DISI_RESERVATION_MEMORY=512M

export CONTROLLER_RESERVATION_MEMORY=512M
export EM_RESERVATION_MEMORY=512M
export LINKER_RESERVATION_MEMORY=512M
export API_RESERVATION_MEMORY=512M
export JOURNAL_RESERVATION_MEMORY=512M
export NOTIFICATIONS_RESERVATION_MEMORY=512M

# DON'T CHANGE
export REGISTRY_NODE_IP=${NODE1}:5000/v2

envsubst < conf-env-1-pc.template > conf.env
